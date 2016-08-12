package com.astoev.cave.survey.service.imp;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.dto.ProjectConfig;
import com.astoev.cave.survey.manager.ProjectManager;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.util.StreamUtil;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.misc.TransactionManager;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by astoev on 6/15/16.
 */
public class ExcelImport {

    public static Project importExcelFile(File aPath, final String aName) throws SQLException, IOException {

        Log.i(Constants.LOG_TAG_SERVICE, "Requested to import " + aPath + " as " + aName);

        if (aPath == null || !aPath.exists()) {
            Log.i(Constants.LOG_TAG_SERVICE, "Missing");
            return null;
        }

        // locate and open
        FileInputStream file = null;
        HSSFWorkbook workbook = null;
        try {
            file = new FileInputStream(aPath);
            workbook = new HSSFWorkbook(file);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // read header
            final ProjectConfig config = prepareConfig(rowIterator.next(), aName);
            Log.i(Constants.LOG_TAG_SERVICE, "Parsed header, working in " + config.getDistanceUnits() + ", " + config.getAzimuthUnits() + " and " + config.getSlopeUnits());

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

                // TODO GPS location

                legs.add(leg);
            }

            Log.i(Constants.LOG_TAG_SERVICE, legs.size() + " records found");

//            TODO normalize legs by concatenating middle points
            Log.i(Constants.LOG_TAG_SERVICE, "TODO process middles ");

            // no errors while reading, start creating as atomic operation
            return TransactionManager.callInTransaction(Workspace.getCurrentInstance().getDBHelper().getConnectionSource(), new Callable<Project>() {
                @Override
                public Project call() throws Exception {
                    final Project project = ProjectManager.instance().createProject(config);
                    Log.i(Constants.LOG_TAG_SERVICE, "Project created");

                    int rowCounter = 1;
                    for (LegData leg : legs) {
                        Log.i(Constants.LOG_TAG_SERVICE, "Processing record " + rowCounter ++);

                        // ensure gallery exists
                        Gallery fromGallery = DaoUtil.getGallery(project.getId(), leg.fromGallery);
                        Log.i(Constants.LOG_TAG_SERVICE, "Got from gallery " + fromGallery);
                        if (fromGallery == null) {
                            Log.i(Constants.LOG_TAG_SERVICE, "Creating gallery " + leg.fromGallery);
                            fromGallery = DaoUtil.createGallery(project, leg.fromGallery);
                        }

                        // plain legs
                        Point from = DaoUtil.getPoint(project.getId(), fromGallery, leg.fromPoint);
                        Log.i(Constants.LOG_TAG_SERVICE, "Got from point " + from);
                        if (from == null) {
                            Log.i(Constants.LOG_TAG_SERVICE, "Initialize from point " + leg.fromPoint);
                            from = new Point();
                            from.setName(leg.fromPoint);
                            Workspace.getCurrentInstance().getDBHelper().getPointDao().create(from);
                        }

                        if (!leg.vector) {

                            Log.i(Constants.LOG_TAG_SERVICE, "Leg " +  leg.fromGallery + leg.fromPoint + " -> " + leg.toGallery + leg.toPoint + " : " + leg.length);

                            Gallery toGallery = DaoUtil.getGallery(project.getId(), leg.toGallery);
                            Log.i(Constants.LOG_TAG_SERVICE, "Got to gallery " + toGallery);
                            if (toGallery == null) {
                                Log.i(Constants.LOG_TAG_SERVICE, "Creating gallery " + leg.toGallery);
                                toGallery = DaoUtil.createGallery(project, leg.toGallery);
                            }

                            Point to = DaoUtil.getPoint(project.getId(), toGallery, leg.toPoint);
                            Log.i(Constants.LOG_TAG_SERVICE, "Got to point " + to);
                            if (to == null) {
                                Log.i(Constants.LOG_TAG_SERVICE, "Initialize to point " + leg.toPoint);
                                to = new Point();
                                to.setName(leg.toPoint);
                                Workspace.getCurrentInstance().getDBHelper().getPointDao().create(to);
                            }

                            Log.i(Constants.LOG_TAG_SERVICE, "Creating leg");
                            Leg legCreated = new Leg(from, to, project, toGallery.getId());
                            // update model
                            if (leg.middlePoint) {
                                legCreated.setMiddlePointDistance(leg.length);
                            } else {
                                legCreated.setDistance(leg.length);
                            }
                            legCreated.setAzimuth(leg.azimuth);
                            legCreated.setSlope(leg.slope);
                            legCreated.setTop(leg.up);
                            legCreated.setDown(leg.down);
                            legCreated.setLeft(leg.left);
                            legCreated.setRight(leg.right);

                            Workspace.getCurrentInstance().getDBHelper().getLegDao().create(legCreated);

                            if (StringUtils.isNotEmpty(leg.note)) {
                                Note n = new Note(leg.note);
                                n.setPoint(from);
                                n.setGalleryId(fromGallery.getId());
                                Workspace.getCurrentInstance().getDBHelper().getNoteDao().create(n);
                            }

                            // TODO GPS location
                        } else {
                            Log.i(Constants.LOG_TAG_SERVICE, "Vector " + leg.fromGallery + leg.fromPoint + " -> " + leg.note);
                            Vector vector = new Vector();
                            vector.setDistance(leg.length);
                            vector.setAzimuth(leg.azimuth);
                            vector.setSlope(leg.slope);
                            vector.setPoint(from);
                            vector.setGalleryId(fromGallery.getId());
                            DaoUtil.saveVector(vector);
                        }
                    }
                    return  project;
                }
            });

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed with import", e);
            UIUtilities.showNotification(R.string.error);
            return null;
        } finally {
            StreamUtil.closeQuietly(file);
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    private static ProjectConfig prepareConfig(Row aRow, String aName) {
        ProjectConfig config = new ProjectConfig();

        // override the name
        config.setName(aName);

        // no sensors by default
        config.setAzimuthSensor(Option.CODE_SENSOR_NONE);
        config.setDistanceSensor(Option.CODE_SENSOR_NONE);
        config.setSlopeSensor(Option.CODE_SENSOR_NONE);

        // units
        config.setDistanceUnits(getUnitFromHeaderCell(aRow, ExcelExport.CELL_LENGHT));
        config.setAzimuthUnits(getUnitFromHeaderCell(aRow, ExcelExport.CELL_AZIMUTH));
        config.setSlopeUnits(getUnitFromHeaderCell(aRow, ExcelExport.CELL_SLOPE));

        return  config;
    }

    private static String getUnitFromHeaderCell(Row aRow, int aCellIndex) {
        Cell header = aRow.getCell(aCellIndex);
        String headerValue = header.getStringCellValue();
        String units = headerValue.substring(
                headerValue.indexOf(ExcelExport.MEASUREMENT_UNIT_DELIMITER) + ExcelExport.MEASUREMENT_UNIT_DELIMITER.length());
        //  TODO no validation
        return  units;
    }

    private static Float getNotNullFloatCellValue(Row row, int cellIndex) {
        Cell cellLength = row.getCell(cellIndex);
        if (cellLength != null && cellLength.getStringCellValue() != null) {
            return Float.parseFloat(cellLength.getStringCellValue());
        }
        return  null;
    }

    static class LegData {
        String fromGallery, fromPoint;
        String toGallery, toPoint;

        boolean vector, middlePoint;

        Float length, azimuth, slope;
        Float up, down, left, right;

        String note;
    }
}
