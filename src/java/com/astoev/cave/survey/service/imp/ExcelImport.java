package com.astoev.cave.survey.service.imp;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.DaoUtil;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by astoev on 6/15/16.
 */
public class ExcelImport {

    public static Object importExcelFile(File aPath, final Project aProject) throws SQLException, IOException {

        Log.i(Constants.LOG_TAG_SERVICE, "Requested to import " + aPath + " as " + aProject.getName());

        // locate and open
        FileInputStream file = null;
        HSSFWorkbook workbook = null;
        try {
            file = new FileInputStream(aPath);
            workbook = new HSSFWorkbook(file);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // read header
            updateConfig(rowIterator.next(), aProject);

            // load all rows
            final List<LegData> legs = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                LegData leg = new LegData();
                Cell cellFrom = row.getCell(ExcelExport.CELL_FROM);
                if (cellFrom == null) {
                    Log.i(Constants.LOG_TAG_SERVICE, "End of file");
                    break;
                }
                String from = cellFrom.getStringCellValue();
                leg.fromGallery = PointUtil.getPointGalleryName(from);
                leg.fromPoint = PointUtil.getPointName(from);

                Cell cellTo = row.getCell(ExcelExport.CELL_TO);
                String to = cellTo == null ? null : cellTo.getStringCellValue();
                leg.toGallery = PointUtil.getPointGalleryName(to);
                leg.toPoint = PointUtil.getPointName(to);
                leg.vector = PointUtil.isVector(to);
                leg.middlePoint = PointUtil.isMiddlePoint(from, to);

                leg.length = getNotNullFloatCellValue(row, ExcelExport.CELL_LENGHT);
                leg.azimuth = getNotNullFloatCellValue(row, ExcelExport.CELL_AZIMUTH);
                leg.slope = getNotNullFloatCellValue(row, ExcelExport.CELL_SLOPE);

                leg.up = getNotNullFloatCellValue(row, ExcelExport.CELL_UP);
                leg.down = getNotNullFloatCellValue(row, ExcelExport.CELL_DOWN);
                leg.left = getNotNullFloatCellValue(row, ExcelExport.CELL_LEFT);
                leg.right = getNotNullFloatCellValue(row, ExcelExport.CELL_RIGHT);

                Cell noteCell = row.getCell(ExcelExport.CELL_NOTE);
                if (noteCell != null) {
                    leg.note = noteCell.getStringCellValue();
                }

                Cell latCell = row.getCell(ExcelExport.CELL_LATITUDE);
                if (latCell != null) {
                    leg.lat = LocationUtil.descriptionToValue(latCell.getStringCellValue());
                }
                Cell lonCell = row.getCell(ExcelExport.CELL_LONGITUDE);
                if (lonCell != null) {
                    leg.lon = LocationUtil.descriptionToValue(lonCell.getStringCellValue());
                }
                Cell altCell = row.getCell(ExcelExport.CELL_ALTTITUDE);
                if (altCell != null) {
                    leg.alt = (float) altCell.getNumericCellValue();
                }
                Cell accuracyCell = row.getCell(ExcelExport.CELL_ACCURACY);
                if (accuracyCell != null) {
                    leg.accuracy = (float) accuracyCell.getNumericCellValue();
                }
                legs.add(leg);
            }

            Log.i(Constants.LOG_TAG_SERVICE, legs.size() + " records found");

            // no errors while reading, start creating as atomic operation
            return TransactionManager.callInTransaction(Workspace.getCurrentInstance().getDBHelper().getConnectionSource(), new Callable() {
                @Override
                public Object call() throws Exception {

                    Set<Leg> lastMiddleLegs = new HashSet<>();
                    int rowCounter = 1;
                    for (LegData leg : legs) {
                        Log.i(Constants.LOG_TAG_SERVICE, "Processing record " + rowCounter ++);

                        // ensure gallery exists
                        Gallery fromGallery = getOrInitializeGallery(aProject, leg.fromGallery);
                        Gallery toGallery = getOrInitializeGallery(aProject, leg.toGallery);

                        if (leg.middlePoint) {

                            String fromRealName = PointUtil.getMiddleFromName(leg.fromPoint);
                            String toRealName = PointUtil.getMiddleToName(leg.toPoint);

                            if (fromRealName.equals(leg.fromPoint)) {

                                lastMiddleLegs.clear();
                                // actual leg
                                Log.i(Constants.LOG_TAG_SERVICE, "Creating leg for middle " + leg.fromGallery + leg.fromPoint + " -> " + leg.toGallery + leg.toPoint);


                                Point from = getOrInitializePoint(aProject, fromGallery, fromRealName);
                                Point to = getOrInitializePoint(aProject, toGallery, toRealName);

                                Leg fullLeg = DaoUtil.getLegByFromToPointId(toGallery, from, to);
                                if (fullLeg == null) {
                                    Log.i(Constants.LOG_TAG_SERVICE, "Creating the full leg ");
                                    fullLeg = new Leg(from, to, aProject, toGallery.getId());
                                    fullLeg.setDistance(leg.length); // leg will be updated later
                                    fullLeg.setAzimuth(leg.azimuth);
                                    fullLeg.setSlope(leg.slope);
                                    fullLeg.setTop(leg.up);
                                    fullLeg.setDown(leg.down);
                                    fullLeg.setLeft(leg.left);
                                    fullLeg.setRight(leg.right);

                                    Workspace.getCurrentInstance().getDBHelper().getLegDao().create(fullLeg);
                                }
                            } else {
                                // regular middle point
                                Log.i(Constants.LOG_TAG_SERVICE, "Middle " + leg.fromGallery + leg.fromPoint + " -> " + leg.toGallery + leg.toPoint);

                                Point from = getOrInitializePoint(aProject, fromGallery, fromRealName);
                                Point to = getOrInitializePoint(aProject, toGallery, toRealName);

                                // create the leg, real length will be updated later
                                Leg middleLeg = new Leg(from, to, fromGallery.getProject(), toGallery.getId());
                                middleLeg.setMiddlePointDistance(PointUtil.getMiddleLength(leg.fromPoint));
                                middleLeg.setAzimuth(leg.azimuth);
                                middleLeg.setSlope(leg.slope);
                                middleLeg.setTop(leg.up);
                                middleLeg.setDown(leg.down);
                                middleLeg.setLeft(leg.left);
                                middleLeg.setRight(leg.right);
                                Workspace.getCurrentInstance().getDBHelper().getLegDao().create(middleLeg);
                                lastMiddleLegs.add(middleLeg);

                                if (toRealName.equals(leg.toPoint)) {

                                    Log.i(Constants.LOG_TAG_SERVICE, "Last middle, adjusting lengths");

                                    // apply actual leg distance
                                    Leg fullLeg = DaoUtil.getLegByFromToPointId(toGallery, from, to);
                                    Float actualLegDistance = leg.length + PointUtil.getMiddleLength(leg.fromPoint);
                                    fullLeg.setDistance(actualLegDistance);
                                    Workspace.getCurrentInstance().getDBHelper().getLegDao().update(fullLeg);

                                    // all middle points have no proper distance
                                    for (Leg l : lastMiddleLegs) {
                                        l.setDistance(actualLegDistance);
                                        Workspace.getCurrentInstance().getDBHelper().getLegDao().update(l);
                                    }
                                }
                            }

                        } else if (leg.vector) {
                            Log.i(Constants.LOG_TAG_SERVICE, "Vector " + leg.fromGallery + leg.fromPoint + " -> " + leg.note);

                            // plain legs
                            Point from = getOrInitializePoint(aProject, fromGallery, leg.fromPoint);

                            Vector vector = new Vector();
                            vector.setDistance(leg.length);
                            vector.setAzimuth(leg.azimuth);
                            vector.setSlope(leg.slope);
                            vector.setPoint(from);
                            vector.setGalleryId(fromGallery.getId());
                            DaoUtil.saveVector(vector);
                        } else {

                            Log.i(Constants.LOG_TAG_SERVICE, "Leg " +  leg.fromGallery + leg.fromPoint + " -> " + leg.toGallery + leg.toPoint + " : " + leg.length);

                            // plain legs
                            Point from = getOrInitializePoint(aProject, fromGallery, leg.fromPoint);
                            Point to = getOrInitializePoint(aProject, toGallery, leg.toPoint);

                            Leg dbLeg = DaoUtil.getLegByFromToPointId(toGallery, from, to);
                            if (dbLeg == null) {
                                // not the first leg already created with the project
                                Log.i(Constants.LOG_TAG_SERVICE, "Creating leg");
                                dbLeg = new Leg(from, to, aProject, toGallery.getId());
                            }
                            dbLeg.setDistance(leg.length);
                            dbLeg.setAzimuth(leg.azimuth);
                            dbLeg.setSlope(leg.slope);
                            dbLeg.setTop(leg.up);
                            dbLeg.setDown(leg.down);
                            dbLeg.setLeft(leg.left);
                            dbLeg.setRight(leg.right);

                            Workspace.getCurrentInstance().getDBHelper().getLegDao().createOrUpdate(dbLeg);

                            if (StringUtils.isNotEmpty(leg.note)) {
                                Note n = new Note(leg.note);
                                n.setPoint(from);
                                n.setGalleryId(fromGallery.getId());
                                Workspace.getCurrentInstance().getDBHelper().getNoteDao().create(n);
                            }

                            if (leg.lat != null && leg.lon != null) {
                                Log.i(Constants.LOG_TAG_SERVICE, "Import location");
                                Location location = new Location();
                                location.setLatitude(leg.lat);
                                location.setLongitude(leg.lon);
                                if (leg.alt != null) {
                                    location.setAltitude(leg.alt.intValue());
                                }
                                if (leg.accuracy != null) {
                                    location.setAccuracy(leg.accuracy.intValue());
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
                            gallery = DaoUtil.createGallery(aProject, aGalleryName);
                        }
                    }
                    return gallery;
                }
            });

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed with import", e);
            String errorMessage = ConfigUtil.getContext().getString(R.string.error) + " - "
                    + e.getClass().getSimpleName() + " : " + e.getMessage();
            UIUtilities.showNotification(errorMessage);
        } finally {
            StreamUtil.closeQuietly(file);
            if (workbook != null) {
                workbook.close();
            }
        }
        return null;
    }

    private static void updateConfig(Row aRow, Project aProject) throws SQLException {

        // units from the file
        Options.createOption(Option.CODE_DISTANCE_UNITS, getUnitFromHeaderCell(aRow, ExcelExport.CELL_LENGHT));
        Options.createOption(Option.CODE_AZIMUTH_UNITS, getUnitFromHeaderCell(aRow, ExcelExport.CELL_AZIMUTH));
        Options.createOption(Option.CODE_SLOPE_UNITS, getUnitFromHeaderCell(aRow, ExcelExport.CELL_SLOPE));
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

    static class LegData {
        String fromGallery, fromPoint;
        String toGallery, toPoint;

        boolean vector, middlePoint;

        Float length, azimuth, slope;
        Float up, down, left, right;

        Float lat, lon, alt, accuracy;

        String note;
    }
}
