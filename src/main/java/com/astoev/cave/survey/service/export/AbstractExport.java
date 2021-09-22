package com.astoev.cave.survey.service.export;

import static com.astoev.cave.survey.service.export.ExportEntityType.LEG;
import static com.astoev.cave.survey.service.export.ExportEntityType.MIDDLE;
import static com.astoev.cave.survey.service.export.ExportEntityType.VECTOR;

import android.content.res.Resources;
import android.util.Log;
import android.util.SparseArray;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by astoev on 8/28/14.
 *
 * @author Alexander Stoev
 */
public abstract class AbstractExport {

    protected Resources mResources;
    protected enum Entities { FROM, TO, DISTANCE, COMPASS, INCLINATION, UP, DOWN, LEFT, RIGHT, NOTE}

    public AbstractExport(Resources aResources) {
        mResources = aResources;
    }

    // exported file info
    protected abstract String getExtension();

    protected String getMimeType() {
        // default implementation
        return "application/octet-stream";
    }


    // implementation details, methods called in the same order
    protected abstract void prepare(Project aProject);
    protected abstract void prepareEntity(int rowCounter, ExportEntityType type);
    protected void endEntity(int rowCounter) {};
    protected abstract void setValue(Entities entityType, String aLabel);
    protected abstract void setValue(Entities entityType, Float aValue);
    protected abstract void setPhoto(Photo aPhoto);
    protected abstract void setLocation(Location aLocation);
    protected abstract void setDrawing(Sketch aSketch);
    protected abstract InputStream getContent() throws IOException;

    // public method for starting export
    public String runExport(Project aProject, String suffix, boolean unique) throws Exception {

        try {
            prepare(aProject);

            // legs
            List<Leg> legs = DaoUtil.getCurrProjectLegs(false);

            int rowCounter = 0;
            Integer lastGalleryId = null;
            Integer prevGalleryId;
            SparseArray<String> galleryNames = new SparseArray<>();

            // iterate legs
            for (Leg l : legs) {

                Point fromPoint = l.getFromPoint();
                DaoUtil.refreshPoint(fromPoint);
                Point toPoint = l.getToPoint();
                DaoUtil.refreshPoint(toPoint);

                // handle different galleries
                if (Constants.STRING_NOT_FOUND.equals(galleryNames.get(l.getGalleryId(), Constants.STRING_NOT_FOUND))) {
                    Gallery gallery = DaoUtil.getGallery(l.getGalleryId());
                    galleryNames.put(l.getGalleryId(), gallery.getName());
                }
                if (lastGalleryId == null) {
                    lastGalleryId = l.getGalleryId();
                }
                if (l.getGalleryId().equals(lastGalleryId)) {
                    prevGalleryId = l.getGalleryId();
                } else {
                    prevGalleryId = DaoUtil.getLegByToPoint(l.getFromPoint()).getGalleryId();
                }

                List<Leg> middles = DaoUtil.getLegsMiddles(l);


                if (middles == null || middles.size() == 0) {

                    // simple leg
                    rowCounter++;

                    prepareEntity(rowCounter, LEG);

                    if (l.getGalleryId().equals(lastGalleryId)) {
                        setValue(Entities.FROM, galleryNames.get(l.getGalleryId()) + fromPoint.getName());
                    } else {
                        setValue(Entities.FROM, galleryNames.get(prevGalleryId) + fromPoint.getName());
                    }

                    setValue(Entities.TO, galleryNames.get(l.getGalleryId()) + toPoint.getName());

                    // distance, azimuth, inclination
                    exportLegMeasures(l);

                    //up/down/left/right
                    exportAroundMeasures(l);

                    // note
                    exportNote(l);

                    // location
                    exportLocation(fromPoint);

                    // sketch
                    exportSketches(l);

                    // picture
                    exportPhotos(l);

                    endEntity(rowCounter);
                } else {
                    // leg split by middles
                    int index = 0;
                    float prevLength = 0;
                    String fromPointName;
                    if (l.getGalleryId().equals(prevGalleryId)) {
                        fromPointName = galleryNames.get(l.getGalleryId()) + fromPoint.getName();
                    } else {
                        fromPointName = galleryNames.get(prevGalleryId) + fromPoint.getName();
                    }
                    String toPointName = galleryNames.get(l.getGalleryId()) + toPoint.getName();
                    String lastMiddleName = null;

                    // middles
                    Leg prevMiddle = null;
                    for (Leg middle : middles) {
                        rowCounter++;
                        index ++;
                        prepareEntity(rowCounter, MIDDLE);

                        setValue(Entities.FROM, lastMiddleName == null ? fromPointName : lastMiddleName);
                        lastMiddleName = fromPointName + Constants.FROM_TO_POINT_DELIMITER + toPointName + Constants.MIDDLE_POINT_DELIMITER + StringUtils.floatToLabel(middle.getMiddlePointDistance());
                        setValue(Entities.TO, lastMiddleName);
                        setValue(Entities.DISTANCE, middle.getMiddlePointDistance() - prevLength);
                        exportLegCompass(l);
                        exportLegSlope(l);


                        if (index == 1) {
                            exportAroundMeasures(l);

                            // other attributes of the main leg
                            exportNote(l);
                            exportLocation(fromPoint);
                            exportSketches(l);
                            exportPhotos(l);

                        } else {
                            exportAroundMeasures(prevMiddle);
                        }

                        endEntity(rowCounter);

                        prevLength = middle.getMiddlePointDistance();
                        prevMiddle = middle;
                    }

                    // last explicit leg
                    rowCounter++;
                    prepareEntity(rowCounter, MIDDLE);

                    setValue(Entities.FROM, lastMiddleName);
                    setValue(Entities.TO, toPointName);
                    setValue(Entities.DISTANCE, l.getDistance() - prevLength);
                    exportLegCompass(l);
                    exportLegSlope(l);

                    exportAroundMeasures(middles.get(middles.size() - 1));

                    endEntity(rowCounter);
                }

                // vectors
                List<Vector> vectors = DaoUtil.getLegVectors(l);
                if (vectors != null) {
                    int vectorCounter = 1;
                    for (Vector v : vectors) {
                        rowCounter++;

                        prepareEntity(rowCounter, VECTOR);

                        String fromPointName;
                        if (l.getGalleryId().equals(prevGalleryId)) {
                            fromPointName = galleryNames.get(l.getGalleryId()) + fromPoint.getName();
                        } else {
                            fromPointName = galleryNames.get(prevGalleryId) + fromPoint.getName();
                        }
                        setValue(Entities.FROM, fromPointName);
//                        setValue(Entities.TO, fromPointName + "-" + galleryNames.get(l.getGalleryId()) + toPoint.getName() + "-v" + vectorCounter);
                        setValue(Entities.DISTANCE, v.getDistance());
                        setValue(Entities.COMPASS, v.getAzimuth());
                        if (v.getSlope() != null) {
                            setValue(Entities.INCLINATION, v.getSlope());
                        }
                        setValue(Entities.NOTE, "v" + vectorCounter);

                        endEntity(rowCounter);

                        vectorCounter++;
                    }
                }

                lastGalleryId = l.getGalleryId();
            }

            String exportSuffix = suffix == null ? getExtension() : FileStorageUtil.NAME_DELIMITER + suffix + getExtension();
            InputStream in = getContent();
            return FileStorageUtil.addProjectExport(aProject, in, getMimeType(), exportSuffix, unique);
        } catch (Exception t) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed with export", t);
            throw t;
        }
    }

    private void exportLegMeasures(Leg l) throws SQLException {
        exportLegDistance(l);
        exportLegCompass(l);
        exportLegSlope(l);
    }

    private void exportNote(Leg l) throws SQLException {
        Note n = DaoUtil.getActiveLegNote(l);
        if (n != null) {
            setValue(Entities.NOTE, n.getText());
        }
    }

    private void exportLegSlope(Leg l) {
        setValue(Entities.INCLINATION, l.getSlope());
    }

    private void exportLegDistance(Leg l) {
        if (l.getDistance() != null) {
            setValue(Entities.DISTANCE, l.getDistance());
        }
    }

    private void exportLegCompass(Leg l) {
        if (l.getAzimuth() != null) {
            setValue(Entities.COMPASS, l.getAzimuth());
        }
    }

    private void exportAroundMeasures(Leg l) {
        setValue(Entities.LEFT, l.getLeft());
        setValue(Entities.RIGHT, l.getRight());
        setValue(Entities.UP, l.getTop());
        setValue(Entities.DOWN, l.getDown());
    }

    private void exportPhotos(Leg l) throws SQLException {
        Photo photo = DaoUtil.getPhotoByLeg(l);
        if (photo != null) {
            setPhoto(photo);
        }
    }

    private void exportSketches(Leg l) throws SQLException {
        Sketch sketch = DaoUtil.getScetchByLeg(l);
        if (sketch != null) {
            setDrawing(sketch);
        }
    }

    private void exportLocation(Point fromPoint) throws SQLException {
        Location location = DaoUtil.getLocationByPoint(fromPoint);
        if (location != null) {
            setLocation(location);
        }
    }

}