/**
 * 
 */
package com.astoev.cave.survey.util;

/**
 * @author jivko
 *
 */
public class LocationUtil {

    /** Template for formatting latitude and longitude */
    private static  String latLonTemplate = "%f\u00B0%s";

    private static  String latLonNumberTemplate = "%f";
    
    private static final String N = "N";
    private static final String S = "S";
    private static final String E = "E";
    private static final String W = "W";
    
    public static String formatLatitude(double latitudeArg){
        if (latitudeArg > 0){
            return String.format(latLonTemplate, latitudeArg, N);
            
        } else {
            return String.format(latLonTemplate, -latitudeArg, S);
        }
    }
    
    public static String formatLongitude(double longitudeArg){
        if (longitudeArg > 0){
            return String.format(latLonTemplate, longitudeArg, E);
        } else {
            return String.format(latLonTemplate, -longitudeArg, W);
        }
    }
}
