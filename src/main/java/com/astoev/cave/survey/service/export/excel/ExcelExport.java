package com.astoev.cave.survey.service.export.excel;

import static com.astoev.cave.survey.model.Option.CODE_AZIMUTH_UNITS;
import static com.astoev.cave.survey.model.Option.CODE_DISTANCE_UNITS;
import static com.astoev.cave.survey.model.Option.CODE_SLOPE_UNITS;

import android.content.res.Resources;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.export.AbstractDataExport;
import com.astoev.cave.survey.service.export.ExportEntityType;
import com.astoev.cave.survey.util.LocationUtil;
import com.astoev.cave.survey.util.StringUtils;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.OutputStream;

/**
 * Exports the project's data as xsl file  
 * 
 * User: astoev
 * Date: 2/12/12
 * Time: 7:25 PM
 * 
 * @author astoev
 * @author jmitrev
 */
public class ExcelExport extends AbstractDataExport {

    public static final String MEASUREMENT_UNIT_DELIMITER = " - ";
    public static final String EXCEL_FILE_EXTENSION = ".xls";
    public static final String EXCEL_MIME_TYPE = "application/vnd.ms-excel";
    public static final int CELL_FROM = 0;
    public static final int CELL_TO = 1;
    public static final int CELL_LENGHT = 2;
    public static final int CELL_AZIMUTH = 3;
    public static final int CELL_SLOPE = 4;
    public static final int CELL_LEFT = 5;
    public static final int CELL_RIGHT = 6;
    public static final int CELL_UP = 7;
    public static final int CELL_DOWN = 8;
    public static final int CELL_NOTE = 9;
    public static final int CELL_LATITUDE = 10;
    public static final int CELL_LONGITUDE = 11;
    public static final int CELL_ALTTITUDE = 12;
    public static final int CELL_ACCURACY = 13;
    public static final int CELL_DRAWING = 14;
    public static final int CELL_PHOTO = 15;

    private Workbook wb;
    private Sheet sheet;
    private CreationHelper helper;

    private Row legRow;

    public ExcelExport(Resources aResources) {
        super(aResources);
    }

    @Override
    public String getExtension() {
        return EXCEL_FILE_EXTENSION;
    }

    @Override
    protected String getMimeType() {
        return EXCEL_MIME_TYPE;
    }

    @Override
    protected void prepare(Project aProject) {
        Log.i(Constants.LOG_TAG_SERVICE, "Excel export preparing");
        wb = new HSSFWorkbook();
        sheet = createHeader(aProject.getName(), wb);
        helper = wb.getCreationHelper();
    }

    @Override
    protected void prepareEntity(int rowCounter, ExportEntityType type) {
        legRow = sheet.createRow(rowCounter);
    }

    @Override
    protected void writeTo(Project aProject, OutputStream aStream) throws Exception {
        wb.write(aStream);
    }

    @Override
    protected void setValue(Entities entityType, String aLabel) {

        if (aLabel == null) {
            return;
        }

        switch (entityType) {
            case FROM:
                Cell from = legRow.createCell(CELL_FROM);
                from.setCellValue(aLabel);
                break;
            case TO:
                Cell to = legRow.createCell(CELL_TO);
                to.setCellValue(aLabel);
                break;
            case NOTE:
                Cell note = legRow.createCell(CELL_NOTE);
                note.setCellValue(aLabel);
                CellStyle cs = wb.createCellStyle();
                cs.setWrapText(true);
                note.setCellStyle(cs);
                break;
        }
    }

    @Override
    protected void setValue(Entities entityType, Float aValue) {

        if (aValue == null) {
            return;
        }

        switch (entityType) {
            case DISTANCE:
                Cell length = legRow.createCell(CELL_LENGHT);
                length.setCellValue(StringUtils.floatToLabel(aValue));
                break;
            case COMPASS:
                Cell compass = legRow.createCell(CELL_AZIMUTH);
                compass.setCellValue(StringUtils.floatToLabel(aValue));
                break;
            case INCLINATION :
                Cell clinometer = legRow.createCell(CELL_SLOPE);
                clinometer.setCellValue(StringUtils.floatToLabel(aValue));
                break;
            case LEFT :
                Cell left = legRow.createCell(CELL_LEFT);
                left.setCellValue(StringUtils.floatToLabel(aValue));
                break;
            case RIGHT:
                Cell right = legRow.createCell(CELL_RIGHT);
                right.setCellValue(StringUtils.floatToLabel(aValue));
                break;
            case UP:
                Cell up = legRow.createCell(CELL_UP);
                up.setCellValue(StringUtils.floatToLabel(aValue));
                break;
            case DOWN:
                Cell down = legRow.createCell(CELL_DOWN);
                down.setCellValue(StringUtils.floatToLabel(aValue));
                break;
        }
    }

    @Override
    protected void setPhoto(Photo photo) {
        Hyperlink fileLink = helper.createHyperlink(HyperlinkType.FILE);

        Cell photoCell = legRow.createCell(CELL_PHOTO);
        String path = photo.getFSPath();
        String name = new File(path).getName();

        fileLink.setAddress(name);
        photoCell.setCellValue(name);
        photoCell.setHyperlink(fileLink);
    }

    @Override
    protected void setLocation(Location aLocation) {
        Cell latitude = legRow.createCell(CELL_LATITUDE);
        latitude.setCellValue(LocationUtil.formatLatitude(aLocation.getLatitude()));
        Cell longitude = legRow.createCell(CELL_LONGITUDE);
        longitude.setCellValue(LocationUtil.formatLongitude(aLocation.getLongitude()));
        Cell altitude = legRow.createCell(CELL_ALTTITUDE);
        altitude.setCellValue(aLocation.getAltitude());
        Cell accuracy = legRow.createCell(CELL_ACCURACY);
        accuracy.setCellValue(aLocation.getAccuracy());
    }

    @Override
    protected void setDrawing(Sketch aSketch) {
        Hyperlink fileLink = helper.createHyperlink(HyperlinkType.FILE);

        Cell sketchCell = legRow.createCell(CELL_DRAWING);
        String path = aSketch.getFSPath();
        String name = new File(path).getName();

        fileLink.setAddress(name);
        sketchCell.setCellValue(name);
        sketchCell.setHyperlink(fileLink);
    }

    private Sheet createHeader(String aProjectName, Workbook wb) {
        Sheet sheet;
        try {
            sheet = wb.createSheet(aProjectName);
        } catch (IllegalArgumentException iae) {
            Log.i(Constants.LOG_TAG_SERVICE, "Failed to create sheet with the project name, creating default: " + iae.getMessage());
            sheet = wb.createSheet();
        }
        Row headerRow = sheet.createRow(0);
        // header cells
        Cell headerFrom = headerRow.createCell(CELL_FROM);
        headerFrom.setCellValue(mResources.getString(R.string.main_table_header_from));
        Cell headerTo = headerRow.createCell(CELL_TO);
        headerTo.setCellValue(mResources.getString(R.string.main_table_header_to));
        Cell headerLength = headerRow.createCell(CELL_LENGHT);
        String distanceTitle = mResources.getString(R.string.distance) + MEASUREMENT_UNIT_DELIMITER + Options.getOptionValue(CODE_DISTANCE_UNITS);
        headerLength.setCellValue(distanceTitle);
        Cell headerCompass = headerRow.createCell(CELL_AZIMUTH);
        String azimuthTitle = mResources.getString(R.string.azimuth) + MEASUREMENT_UNIT_DELIMITER + Options.getOptionValue(CODE_AZIMUTH_UNITS);
        headerCompass.setCellValue(azimuthTitle);
        Cell headerClinometer = headerRow.createCell(CELL_SLOPE);
        String clinometerTitle = mResources.getString(R.string.slope) + MEASUREMENT_UNIT_DELIMITER + Options.getOptionValue(CODE_SLOPE_UNITS);
        headerClinometer.setCellValue(clinometerTitle);
        Cell headerLeft = headerRow.createCell(CELL_LEFT);
        headerLeft.setCellValue(mResources.getString(R.string.left));
        Cell headerRight = headerRow.createCell(CELL_RIGHT);
        headerRight.setCellValue(mResources.getString(R.string.right));
        Cell headerUp = headerRow.createCell(CELL_UP);
        headerUp.setCellValue(mResources.getString(R.string.up));
        Cell headerDown = headerRow.createCell(CELL_DOWN);
        headerDown.setCellValue(mResources.getString(R.string.down));
        Cell headerNote = headerRow.createCell(CELL_NOTE);
        headerNote.setCellValue(mResources.getString(R.string.main_table_header_note));

        Cell gpsLatitude = headerRow.createCell(CELL_LATITUDE);//gps_latitude
        gpsLatitude.setCellValue(mResources.getString(R.string.gps_latitude));
        Cell gpsLongitude = headerRow.createCell(CELL_LONGITUDE);
        gpsLongitude.setCellValue(mResources.getString(R.string.gps_longitude));
        Cell gpsAltitude = headerRow.createCell(CELL_ALTTITUDE);
        gpsAltitude.setCellValue(mResources.getString(R.string.gps_altitude));
        Cell gpsAccuracy = headerRow.createCell(CELL_ACCURACY);
        gpsAccuracy.setCellValue(mResources.getString(R.string.gps_accuracy));

        Cell headerDrawing = headerRow.createCell(CELL_DRAWING);
        headerDrawing.setCellValue(mResources.getString(R.string.main_table_header_drawing));
        Cell headerPhoto = headerRow.createCell(CELL_PHOTO);
        headerPhoto.setCellValue(mResources.getString(R.string.main_table_header_photo));
        return sheet;
    }

}