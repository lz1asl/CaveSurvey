package com.astoev.cave.survey.util;

import org.apache.poi.ss.usermodel.Cell;

/**
 * @author jivko
 *
 */
public class LocationUtil {

    private static final String DEGREE = "\u00B0";

    /** Template for formatting latitude and longitude */
    private static final String LAT_LON_TEMPLATE = "%f" + DEGREE + "%s";

    private static final String N = "N";
    private static final String S = "S";
    private static final String E = "E";
    private static final String W = "W";
    
    public static String formatLatitude(double latitudeArg){
        if (latitudeArg > 0){
            return String.format(LAT_LON_TEMPLATE, latitudeArg, N);
            
        } else {
            return String.format(LAT_LON_TEMPLATE, -latitudeArg, S);
        }
    }
    
    public static String formatLongitude(double longitudeArg){
        if (longitudeArg > 0){
            return String.format(LAT_LON_TEMPLATE, longitudeArg, E);
        } else {
            return String.format(LAT_LON_TEMPLATE, -longitudeArg, W);
        }
    }

    public static Float descriptionToValue(Cell aCell) {

        try {
            String aCoordinateDescription = aCell.getStringCellValue();
            // present
            if (StringUtils.isNotEmpty(aCoordinateDescription) && aCoordinateDescription.contains(DEGREE)) {
                // without suffix
                String numberPart = aCoordinateDescription.substring(0, aCoordinateDescription.indexOf(DEGREE));
                // proper number in default format
                numberPart = numberPart.replace(",", ".");

                float coordinate = Float.parseFloat(numberPart);
                if (aCoordinateDescription.endsWith(S) || aCoordinateDescription.endsWith(W)) {
                    coordinate = -coordinate;
                }
                return coordinate;
            }
            return null;
        } catch (IllegalStateException e) {
            return (float) aCell.getNumericCellValue();
        }
    }
}
