package com.astoev.cave.survey.service.imp;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.GalleryUtil;
import com.astoev.cave.survey.util.LocationUtil;
import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.util.StreamUtil;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.misc.TransactionManager;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.astoev.cave.survey.model.Option.CODE_AZIMUTH_UNITS;
import static com.astoev.cave.survey.model.Option.CODE_DISTANCE_UNITS;
import static com.astoev.cave.survey.model.Option.CODE_SLOPE_UNITS;

/**
 * Created by astoev on 6/15/16.
 */
public class ExcelImport {

    public static Object importExcelFile(File aPath, final Project aProject) throws SQLException, IOException {

        Log.i(Constants.LOG_TAG_SERVICE, "Requested to import " + aPath + " as " + aProject.getName());

        // load
        final ProjectData data = loadProjectData(aPath);

        // no errors while reading, start creating as atomic operation
        return TransactionManager.callInTransaction(Workspace.getCurrentInstance().getDBHelper().getConnectionSource(), new Callable() {
            @Override
            public Object call() throws Exception {

                // project config
                updateProjectConfig(data);

                // project data
                Set<Leg> lastMiddleLegs = new HashSet<>();
                int rowCounter = 1;
                for (LegData leg : data.getLegs()) {
                    Log.i(Constants.LOG_TAG_SERVICE, "Processing record " + rowCounter ++);

                    // ensure gallery exists
                    Gallery fromGallery = getOrInitializeGallery(aProject, leg.getFromGallery());
                    Gallery toGallery = getOrInitializeGallery(aProject, leg.getToGallery());

                    if (leg.isMiddlePoint()) {

                        String fromRealName = PointUtil.getMiddleFromName(leg.getFromPoint());
                        String toRealName = PointUtil.getMiddleToName(leg.getToPoint());

                        if (fromRealName.equals(leg.getFromPoint())) {

                            lastMiddleLegs.clear();
                            // actual leg
                            Log.i(Constants.LOG_TAG_SERVICE, "Creating leg for middle " + leg.getFromGallery() + leg.getFromPoint()
                                    + " -> " + leg.getToGallery() + leg.getToPoint());


                            Point from = getOrInitializePoint(aProject, fromGallery, fromRealName);
                            Point to = getOrInitializePoint(aProject, toGallery, toRealName);

                            Leg fullLeg = DaoUtil.getLegByFromToPointId(toGallery, from, to);
                            if (fullLeg == null) {
                                Log.i(Constants.LOG_TAG_SERVICE, "Creating the full leg ");
                                fullLeg = new Leg(from, to, aProject, toGallery.getId());
                                fullLeg.setDistance(leg.getLength()); // leg will be updated later
                                fullLeg.setAzimuth(leg.getAzimuth());
                                fullLeg.setSlope(leg.getSlope());
                                fullLeg.setTop(leg.getUp());
                                fullLeg.setDown(leg.getDown());
                                fullLeg.setLeft(leg.getLeft());
                                fullLeg.setRight(leg.getRight());

                                Workspace.getCurrentInstance().getDBHelper().getLegDao().create(fullLeg);
                            }
                        } else {
                            // regular middle point
                            Log.i(Constants.LOG_TAG_SERVICE, "Middle " + leg.getFromGallery() + leg.getFromPoint()
                                    + " -> " + leg.getToGallery() + leg.getToPoint());

                            Point from = getOrInitializePoint(aProject, fromGallery, fromRealName);
                            Point to = getOrInitializePoint(aProject, toGallery, toRealName);

                            // create the leg, real length will be updated later
                            Leg middleLeg = new Leg(from, to, fromGallery.getProject(), toGallery.getId());
                            middleLeg.setMiddlePointDistance(PointUtil.getMiddleLength(leg.getFromPoint()));
                            middleLeg.setAzimuth(leg.getAzimuth());
                            middleLeg.setSlope(leg.getSlope());
                            middleLeg.setTop(leg.getUp());
                            middleLeg.setDown(leg.getDown());
                            middleLeg.setLeft(leg.getLeft());
                            middleLeg.setRight(leg.getRight());
                            Workspace.getCurrentInstance().getDBHelper().getLegDao().create(middleLeg);
                            lastMiddleLegs.add(middleLeg);

                            if (toRealName.equals(leg.getToPoint())) {

                                Log.i(Constants.LOG_TAG_SERVICE, "Last middle, adjusting lengths");

                                // apply actual leg distance
                                Leg fullLeg = DaoUtil.getLegByFromToPointId(toGallery, from, to);
                                Float actualLegDistance = leg.getLength() + PointUtil.getMiddleLength(leg.getFromPoint());
                                fullLeg.setDistance(actualLegDistance);
                                Workspace.getCurrentInstance().getDBHelper().getLegDao().update(fullLeg);

                                // all middle points have no proper distance
                                for (Leg l : lastMiddleLegs) {
                                    l.setDistance(actualLegDistance);
                                    Workspace.getCurrentInstance().getDBHelper().getLegDao().update(l);
                                }
                            }
                        }

                    } else if (leg.isVector()) {
                        Log.i(Constants.LOG_TAG_SERVICE, "Vector " + leg.getFromGallery() + leg.getFromPoint() + " -> " + leg.getNote());

                        // plain legs
                        Point from = getOrInitializePoint(aProject, fromGallery, leg.getFromPoint());

                        Vector vector = new Vector();
                        vector.setDistance(leg.getLength());
                        vector.setAzimuth(leg.getAzimuth());
                        vector.setSlope(leg.getSlope());
                        vector.setPoint(from);
                        vector.setGalleryId(fromGallery.getId());
                        DaoUtil.saveVector(vector);
                    } else {

                        Log.i(Constants.LOG_TAG_SERVICE, "Leg " +  leg.getFromGallery() + leg.getFromPoint() + " -> "
                                + leg.getToGallery() + leg.getToPoint() + " : " + leg.getLength());

                        // plain legs
                        Point from = getOrInitializePoint(aProject, fromGallery, leg.getFromPoint());
                        Point to = getOrInitializePoint(aProject, toGallery, leg.getToPoint());

                        Leg dbLeg = DaoUtil.getLegByFromToPointId(toGallery, from, to);
                        if (dbLeg == null) {
                            // not the first leg already created with the project
                            Log.i(Constants.LOG_TAG_SERVICE, "Creating leg");
                            dbLeg = new Leg(from, to, aProject, toGallery.getId());
                        }
                        dbLeg.setDistance(leg.getLength());
                        dbLeg.setAzimuth(leg.getAzimuth());
                        dbLeg.setSlope(leg.getSlope());
                        dbLeg.setTop(leg.getUp());
                        dbLeg.setDown(leg.getDown());
                        dbLeg.setLeft(leg.getLeft());
                        dbLeg.setRight(leg.getRight());

                        Workspace.getCurrentInstance().getDBHelper().getLegDao().createOrUpdate(dbLeg);

                        if (StringUtils.isNotEmpty(leg.getNote())) {
                            Note n = new Note(leg.getNote());
                            n.setPoint(from);
                            n.setGalleryId(fromGallery.getId());
                            Workspace.getCurrentInstance().getDBHelper().getNoteDao().create(n);
                        }

                        if (leg.getLat() != null && leg.getLon() != null) {
                            Log.i(Constants.LOG_TAG_SERVICE, "Import location");
                            Location location = new Location();
                            location.setLatitude(leg.getLat());
                            location.setLongitude(leg.getLon());
                            if (leg.getAlt() != null) {
                                location.setAltitude(leg.getAlt().intValue());
                            }
                            if (leg.getAccuracy() != null) {
                                location.setAccuracy(leg.getAccuracy().intValue());
                            }
                            DaoUtil.saveLocationToPoint(from, location);
                        }
                    }

                }
                UIUtilities.showNotification(R.string.import_success);
                return null;
            }


            private Point getOrInitializePoint(Project aProject, Gallery aGallery, String aName) throws SQLException {

                // point can be already created
                Long pointId = DaoUtil.getFromPointId(aProject.getId(), aGallery, aName);
                if (pointId == null) {
                    pointId = DaoUtil.getToPointId(aProject.getId(), aGallery, aName);
                    if (pointId == null) {
                        Log.i(Constants.LOG_TAG_SERVICE, "Creating to point for " + aGallery.getName() + aName);
                        Point point = new Point();
                        point.setName(aName);
                        Workspace.getCurrentInstance().getDBHelper().getPointDao().create(point);
                        return  point;
                    }
                }
                return DaoUtil.getPoint(pointId.intValue());
            }

            private Gallery getOrInitializeGallery(Project aProject, String aGalleryName) throws SQLException {
                Gallery gallery = null;
                if (StringUtils.isNotEmpty(aGalleryName)) {
                    gallery = DaoUtil.getGallery(aProject.getId(), aGalleryName);
                            Log.i(Constants.LOG_TAG_SERVICE, "Got gallery " + gallery);
                    if (gallery == null) {
                        Log.i(Constants.LOG_TAG_SERVICE, "Creating gallery " + aGalleryName);
                        gallery = GalleryUtil.createGallery(aProject, aGalleryName);
                    }
                }
                return gallery;
            }
        });
    }

    public static ProjectData loadProjectData(File aPath) throws IOException {
        return loadProjectData(new FileInputStream(aPath));
    }

    public static ProjectData loadProjectData(InputStream stream) throws IOException {
        // locate and open
        HSSFWorkbook workbook = null;
        ProjectData data = new ProjectData();
        int rowNum = 1;
        try {
            workbook = new HSSFWorkbook(stream);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // read header
            readProjectConfig(rowIterator.next(), data);

            // load all rows
            final List<LegData> legs = data.getLegs();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;
                LegData leg = new LegData();
                Cell cellFrom = row.getCell(ExcelExport.CELL_FROM);
                if (cellFrom == null) {
                    Log.i(Constants.LOG_TAG_SERVICE, "End of file");
                    break;
                }
                String from = cellFrom.getStringCellValue();
                leg.setFromGallery(PointUtil.getPointGalleryName(from));
                leg.setFromPoint(PointUtil.getPointName(from));

                Cell cellTo = row.getCell(ExcelExport.CELL_TO);
                String to = cellTo == null ? null : cellTo.getStringCellValue();
                leg.setToGallery(PointUtil.getPointGalleryName(to));
                leg.setToPoint(PointUtil.getPointName(to));
                leg.setVector(PointUtil.isVector(to));
                leg.setMiddlePoint(PointUtil.isMiddlePoint(from, to));

                leg.setLength(getNotNullFloatCellValue(row, ExcelExport.CELL_LENGHT));
                leg.setAzimuth(getNotNullFloatCellValue(row, ExcelExport.CELL_AZIMUTH));
                leg.setSlope(getNotNullFloatCellValue(row, ExcelExport.CELL_SLOPE));

                leg.setUp(getNotNullFloatCellValue(row, ExcelExport.CELL_UP));
                leg.setDown(getNotNullFloatCellValue(row, ExcelExport.CELL_DOWN));
                leg.setLeft(getNotNullFloatCellValue(row, ExcelExport.CELL_LEFT));
                leg.setRight(getNotNullFloatCellValue(row, ExcelExport.CELL_RIGHT));

                Cell noteCell = row.getCell(ExcelExport.CELL_NOTE);
                if (noteCell != null) {
                    leg.setNote(noteCell.getStringCellValue());
                }

                Cell latCell = row.getCell(ExcelExport.CELL_LATITUDE);
                if (latCell != null) {
                    leg.setLat(LocationUtil.descriptionToValue(latCell.getStringCellValue()));
                }
                Cell lonCell = row.getCell(ExcelExport.CELL_LONGITUDE);
                if (lonCell != null) {
                    leg.setLon(LocationUtil.descriptionToValue(lonCell.getStringCellValue()));
                }
                Cell altCell = row.getCell(ExcelExport.CELL_ALTTITUDE);
                if (altCell != null) {
                    leg.setAlt((float) altCell.getNumericCellValue());
                }
                Cell accuracyCell = row.getCell(ExcelExport.CELL_ACCURACY);
                if (accuracyCell != null) {
                    leg.setAccuracy((float) accuracyCell.getNumericCellValue());
                }
                legs.add(leg);
            }

            Log.i(Constants.LOG_TAG_SERVICE, legs.size() + " records found");
            return data;
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Error during import", e);
            String errorMessage = ConfigUtil.getContext().getString(R.string.settings_import_error,
                    rowNum, e.getClass().getSimpleName(), e.getMessage());
            UIUtilities.showNotification(errorMessage);
        } finally {
            StreamUtil.closeQuietly(stream);
            if (workbook != null) {
                workbook.close();
            }
        }
        return null;
    }

    public static void readProjectConfig(Row aRow, ProjectData aProjectData) {

        // units from the file
        Map<String, String> options = aProjectData.getOptions();
        options.put(CODE_DISTANCE_UNITS, getUnitFromHeaderCell(aRow, ExcelExport.CELL_LENGHT));
        options.put(CODE_AZIMUTH_UNITS, getUnitFromHeaderCell(aRow, ExcelExport.CELL_AZIMUTH));
        options.put(CODE_SLOPE_UNITS, getUnitFromHeaderCell(aRow, ExcelExport.CELL_SLOPE));
    }

    public static void updateProjectConfig(ProjectData aProjectData) throws SQLException {

        Map<String, String> options = aProjectData.getOptions();
        Options.updateOption(CODE_DISTANCE_UNITS, options.get(CODE_DISTANCE_UNITS));
        Options.updateOption(CODE_AZIMUTH_UNITS, options.get(CODE_AZIMUTH_UNITS));
        Options.updateOption(CODE_SLOPE_UNITS, options.get(CODE_SLOPE_UNITS));
    }

    private static String getUnitFromHeaderCell(Row aRow, int aCellIndex) {
        Cell header = aRow.getCell(aCellIndex);
        String headerValue = header.getStringCellValue();
        String units = headerValue.substring(
                headerValue.indexOf(ExcelExport.MEASUREMENT_UNIT_DELIMITER) + ExcelExport.MEASUREMENT_UNIT_DELIMITER.length());
        //  TODO no validation
        return  units;
    }

    private static Float getNotNullFloatCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell != null) {
            DataFormatter formatter = new DataFormatter();
            String val = formatter.formatCellValue(cell);
            if (StringUtils.isNotEmpty(val)) {
                return Float.parseFloat(val);
            }
        }
        return  null;
    }

}
