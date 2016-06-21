package com.astoev.cave.survey.service.imp;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.dto.ProjectConfig;
import com.astoev.cave.survey.manager.ProjectManager;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.util.StreamUtil;
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
                leg.ritht = getNotNullFloatCellValue(row, ExcelExport.CELL_RIGHT);

                legs.add(leg);
            }

            Log.i(Constants.LOG_TAG_SERVICE, legs.size() + " records found");

            TODO normalize legs by concatenating middle points


            // no errors reading, start creating as single operation
            return TransactionManager.callInTransaction(Workspace.getCurrentInstance().getDBHelper().getConnectionSource(), new Callable<Project>() {
                @Override
                public Project call() throws Exception {
                    final Project project = ProjectManager.instance().createProject(config);
                    Log.i(Constants.LOG_TAG_SERVICE, "Project created");



                    Log.i(Constants.LOG_TAG_SERVICE, " TODO create " + aName);
                    for (LegData leg : legs) {
                        Log.i(Constants.LOG_TAG_SERVICE, leg.fromGallery + leg.fromPoint + " -> " + leg.toGallery + leg.toPoint + ":" + leg.length);

                        // ensure gallery exists
                        Gallery fromGallery = DaoUtil.getGallery(project.getId(), leg.fromGallery);
                        if (fromGallery == null) {
                            fromGallery = DaoUtil.createGallery(project, leg.fromGallery);
                        }

                        if (!leg.vector) {
                            Gallery toGallery = DaoUtil.getGallery(project.getId(), leg.toGallery);
                            if (toGallery == null) {
                                toGallery = DaoUtil.createGallery(project, leg.toGallery);
                            }
                        }

                        // plain legs
                        if (!leg.vector && !leg.middlePoint) {
                            Point from = DaoUtil.getPoint(project.getId(), leg.fromPoint);


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
        Float up, down, left, ritht;
    }
}
