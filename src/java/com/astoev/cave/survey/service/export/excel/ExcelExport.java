package com.astoev.cave.survey.service.export.excel;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.*;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.stmt.QueryBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/12/12
 * Time: 7:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExcelExport {

    public static final String EXCEL_FILE_EXTENSION = ".xls";
    public static final String NAME_DELIMITER = "_";

    public static final String EXPORT_FOLDER = "CaveSurvey";
    public static final String TIME_PATTERN = "yyyyMMdd_HH:mm:ss";

    private Workspace mWorkspace;
    private Context mContext;


    public ExcelExport(Workspace aWorkspace, Context aContext) {
        mWorkspace = aWorkspace;
        mContext = aContext;
    }

    public String runExport() throws Exception {

        // TODO check sd card state
        File saveFolder = new File(Environment.getExternalStorageDirectory() + File.separator + EXPORT_FOLDER);
        Log.i(Constants.LOG_TAG_SERVICE, "Export folder " + saveFolder.getAbsolutePath());
        if (!saveFolder.exists()) {
            boolean exportFolderCreated = saveFolder.mkdirs();
            if (!exportFolderCreated) {
                UIUtilities.showNotification(mContext, R.string.export_io_error);
                return null;
            }
            Log.i(Constants.LOG_TAG_SERVICE, "Export folder created");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        File exportFile = new File(saveFolder, mWorkspace.getActiveProject().getName() + NAME_DELIMITER + dateFormat.format(new Date()) + EXCEL_FILE_EXTENSION);
        Log.i(Constants.LOG_TAG_SERVICE, "Start export to " + exportFile.getAbsolutePath());

        Workbook wb = new HSSFWorkbook();


        FileOutputStream out = null;

//        ProgressDialog calculatingDialog = ProgressDialog.show(mContext, "",
//                mContext.getString(R.string.export_calculate), true);

        try {
//            calculatingDialog.show();

            Sheet sheet = wb.createSheet(mWorkspace.getActiveProject().getName());
            Row headerRow = sheet.createRow(0);
            // header cells
            Cell headerFrom = headerRow.createCell(0);
            headerFrom.setCellValue(mContext.getString(R.string.main_table_header_from));
            Cell headerTo = headerRow.createCell(1);
            headerTo.setCellValue(mContext.getString(R.string.main_table_header_to));
            Cell headerLength = headerRow.createCell(2);
            String distanceTitle = mContext.getString(R.string.main_table_header_distance) + " - " + Options.getOptionValue(Option.CODE_DISTANCE_UNITS);
            headerLength.setCellValue(distanceTitle);
            Cell headerCompass = headerRow.createCell(3);
            String azimuthTitle = mContext.getString(R.string.main_table_header_azimuth) + " - " + Options.getOptionValue(Option.CODE_AZIMUTH_UNITS);
            headerCompass.setCellValue(azimuthTitle);
            Cell headerClinometer = headerRow.createCell(4);
            String clinometerTitle = mContext.getString(R.string.main_table_header_slope) + " - " + Options.getOptionValue(Option.CODE_SLOPE_UNITS);
            headerClinometer.setCellValue(clinometerTitle);
            Cell headerLeft = headerRow.createCell(5);
            headerLeft.setCellValue(mContext.getString(R.string.main_table_header_left));
            Cell headerRight = headerRow.createCell(6);
            headerRight.setCellValue(mContext.getString(R.string.main_table_header_right));
            Cell headerUp = headerRow.createCell(7);
            headerUp.setCellValue(mContext.getString(R.string.main_table_header_up));
            Cell headerDown = headerRow.createCell(8);
            headerDown.setCellValue(mContext.getString(R.string.main_table_header_down));
            Cell headerNote = headerRow.createCell(9);
            headerNote.setCellValue(mContext.getString(R.string.main_table_header_note));
            Cell headerDrawing = headerRow.createCell(10);
            headerDrawing.setCellValue(mContext.getString(R.string.main_table_header_drawing));
            Cell headerGPS = headerRow.createCell(11);
            headerGPS.setCellValue(mContext.getString(R.string.main_table_header_gps));
            Cell headerPhoto = headerRow.createCell(12);
            headerPhoto.setCellValue(mContext.getString(R.string.main_table_header_photo));

            // Create the drawing patriarch.  This is the top level container for all shapes.
            Drawing drawingPatriarch = sheet.createDrawingPatriarch();
            CreationHelper helper = wb.getCreationHelper();

            // legs
            List<Leg> legs = mWorkspace.getCurrProjectLegs();

            int rowCounter = 0;
            for (Leg l : legs) {
                rowCounter++;

                Row legRow = sheet.createRow(rowCounter);

                Cell from = legRow.createCell(0);
                Point fromPoint = l.getFromPoint();
                mWorkspace.getDBHelper().getPointDao().refresh(fromPoint);
                from.setCellValue(fromPoint.getName());
                Cell to = legRow.createCell(1);
                Point toPoint = l.getToPoint();
                mWorkspace.getDBHelper().getPointDao().refresh(toPoint);
                to.setCellValue(toPoint.getName());
                Cell length = legRow.createCell(2);
                if (l.getDistance() != null) {
                    length.setCellValue(StringUtils.floatToLabel(l.getDistance()));
                }
                Cell compass = legRow.createCell(3);
                if (l.getAzimuth() != null) {
                    compass.setCellValue(StringUtils.floatToLabel(l.getAzimuth()));
                }
                Cell clinometer = legRow.createCell(4);
                if (l.getSlope() != null) {
                    clinometer.setCellValue(StringUtils.floatToLabel(l.getSlope()));
                }
                Cell left = legRow.createCell(5);
                if (l.getLeft() != null) {
                    left.setCellValue(StringUtils.floatToLabel(l.getLeft()));
                }
                Cell right = legRow.createCell(6);
                if (l.getRight() != null) {
                    right.setCellValue(StringUtils.floatToLabel(l.getRight()));
                }
                Cell up = legRow.createCell(7);
                if (l.getTop() != null) {
                    up.setCellValue(StringUtils.floatToLabel(l.getTop()));
                }
                Cell down = legRow.createCell(8);
                if (l.getDown() != null) {
                    down.setCellValue(StringUtils.floatToLabel(l.getDown()));
                }
                Cell note = legRow.createCell(9);

                Note n = Leg.getActiveLegNote(l, mWorkspace);
                if (n != null) {
                    note.setCellValue(n.getText());
                }

                QueryBuilder<Sketch, Integer> queryBuilderSketch = mWorkspace.getDBHelper().getSketchDao().queryBuilder();
                queryBuilderSketch.where().eq(Sketch.COLUMN_POINT_ID, fromPoint.getId());
                Sketch existingDrawing = (Sketch) mWorkspace.getDBHelper().getSketchDao().queryForFirst(queryBuilderSketch.prepare());
                if (null != existingDrawing) {
                    Log.i(Constants.LOG_TAG_SERVICE, "Exporting drawing");
                    //add a picture shape
                    ClientAnchor anchor = helper.createClientAnchor();

                    int pictureIdx = wb.addPicture(existingDrawing.getBitmap(), Workbook.PICTURE_TYPE_PNG);

                    //set top-left corner of the picture,
                    //subsequent call of Picture#resize() will operate relative to it
                    anchor.setCol1(9);
//                    anchor.setCol2(12);
//                    anchor.setRow1(rowCounter);
//                    anchor.setRow2(rowCounter + 3);
                    Picture pict = drawingPatriarch.createPicture(anchor, pictureIdx);

                    //auto-size picture relative to its top-left corner
//                    pict.resize();
                }

                // TODO export GPS

                QueryBuilder<Photo, Integer> queryBuilderPhoto = mWorkspace.getDBHelper().getPhotoDao().queryBuilder();
                queryBuilderPhoto.where().eq(Sketch.COLUMN_POINT_ID, fromPoint.getId());
                Photo existingPoint = (Photo) mWorkspace.getDBHelper().getPhotoDao().queryForFirst(queryBuilderPhoto.prepare());
                if (null != existingPoint) {
                    Log.i(Constants.LOG_TAG_SERVICE, "Exporting photo");
                    //add a picture shape
                    ClientAnchor anchor = helper.createClientAnchor();

                    int pictureIdx = wb.addPicture(existingPoint.getPictureBytes(), Workbook.PICTURE_TYPE_JPEG);

                    //set top-left corner of the picture,
                    //subsequent call of Picture#resize() will operate relative to it
                    anchor.setCol1(13);
                    anchor.setCol2(15);
                    anchor.setRow1(rowCounter);
                    anchor.setRow2(rowCounter + 3);
                    drawingPatriarch.createPicture(anchor, pictureIdx);

                }


            }

            out = new FileOutputStream(exportFile);
            wb.write(out);
            out.close();
            return exportFile.getAbsolutePath();
        } catch (Exception t) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed with export", t);
            throw t;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_SERVICE, "Failed to close stream", e);
                }

            }
//            calculatingDialog.dismiss();
        }

    }
}