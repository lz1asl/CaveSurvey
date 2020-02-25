package com.astoev.cave.survey.service.export;

import android.content.Context;
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

import org.json.JSONException;

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

    protected Context mContext;
    protected String mExtension;
    protected boolean mUseUniqueName;
    protected enum Entities { FROM, TO, DISTANCE, COMPASS, INCLINATION, UP, DOWN, LEFT, RIGHT, NOTE}

    public AbstractExport(Context aContext) {
        mContext = aContext;
    }

    // implementation details, methods called in the same order
    protected abstract void prepare(Project aProject) throws JSONException;
    protected abstract void prepareEntity(int rowCounter) throws JSONException;
    protected abstract void setValue(Entities entityType, String aLabel) throws JSONException;
    protected abstract void setValue(Entities entityType, Float aValue) throws JSONException;
    protected abstract void setPhoto(Photo aPhoto);
    protected abstract void setLocation(Location aLocation) throws JSONException;
    protected abstract void setDrawing(Sketch aSketch);
    protected abstract InputStream getContent() throws IOException;

    // public method for starting export
    public String runExport(Project aProject) throws Exception {

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


                rowCounter++;

                prepareEntity(rowCounter);

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

                // middles
                if (middles != null && middles.size() > 0) {

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
                        prepareEntity(rowCounter);

                        setValue(Entities.FROM, lastMiddleName == null ? fromPointName : lastMiddleName);
                        lastMiddleName = fromPointName + Constants.FROM_TO_POINT_DELIMITER + toPointName + Constants.MIDDLE_POINT_DELIMITER + StringUtils.floatToLabel(middle.getMiddlePointDistance());
                        setValue(Entities.TO, lastMiddleName);
                        setValue(Entities.DISTANCE, middle.getMiddlePointDistance() - prevLength);
                        exportLegCompass(l);
                        exportLegSlope(l);


                        if (index == 1) {
                            exportAroundMeasures(l);
                        } else {
                            exportAroundMeasures(prevMiddle);
                        }

                        prevLength = middle.getMiddlePointDistance();
                        prevMiddle = middle;
                    }

                    // last explicit leg
                    rowCounter++;
                    prepareEntity(rowCounter);

                    setValue(Entities.FROM, lastMiddleName);
                    setValue(Entities.TO, toPointName);
                    setValue(Entities.DISTANCE, l.getDistance() - prevLength);
                    exportLegCompass(l);
                    exportLegSlope(l);

                    exportAroundMeasures(middles.get(middles.size() - 1));
                }

                // vectors
                List<Vector> vectors = DaoUtil.getLegVectors(l);
                if (vectors != null) {
                    int vectorCounter = 1;
                    for (Vector v : vectors) {
                        rowCounter++;

                        prepareEntity(rowCounter);

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

                        vectorCounter++;
                    }
                }

                lastGalleryId = l.getGalleryId();
            }
            InputStream in = getContent();
            return FileStorageUtil.addProjectExport(aProject, in, getExtension(), isUseUniqueName());
        } catch (Exception t) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed with export", t);
            throw t;
        }
    }

    private void exportLegMeasures(Leg l) throws SQLException, JSONException {
        exportLegDistance(l);
        exportLegCompass(l);
        exportLegSlope(l);
    }

    private void exportNote(Leg l) throws SQLException, JSONException {
        Note n = DaoUtil.getActiveLegNote(l);
        if (n != null) {
            setValue(Entities.NOTE, n.getText());
        }
    }

    private void exportLegSlope(Leg l) throws SQLException, JSONException {
        if (l.getSlope() != null) {
            setValue(Entities.INCLINATION, l.getSlope());
        }
    }

    private void exportLegDistance(Leg l) throws SQLException, JSONException {
        if (l.getDistance() != null) {
            setValue(Entities.DISTANCE, l.getDistance());
        }
    }

    private void exportLegCompass(Leg l) throws SQLException, JSONException {
        if (l.getAzimuth() != null) {
            setValue(Entities.COMPASS, l.getAzimuth());
        }
    }

    private void exportAroundMeasures(Leg l) throws SQLException, JSONException {
        if (l.getLeft() != null) {
            setValue(Entities.LEFT, l.getLeft());
        }

        if (l.getRight() != null) {
            setValue(Entities.RIGHT, l.getRight());
        }

        if (l.getTop() != null) {
            setValue(Entities.UP, l.getTop());
        }

        if (l.getDown() != null) {
            setValue(Entities.DOWN, l.getDown());
        }
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

    private void exportLocation(Point fromPoint) throws SQLException, JSONException {
        Location location = DaoUtil.getLocationByPoint(fromPoint);
        if (location != null) {
            setLocation(location);
        }
    }

    public String getExtension() {
        return mExtension;
    }

    public void setExtension(String extension) {
        mExtension = extension;
    }

    public boolean isUseUniqueName() {
        return mUseUniqueName;
    }

    public void setUseUniqueName(boolean useUniqueName) {
        mUseUniqueName = useUniqueName;
    }
}