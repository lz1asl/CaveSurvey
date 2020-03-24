package com.astoev.cave.survey.activity.dialog;

/**
 * Interface for handling selection of gps_auto type
 *
 * @author Jivko Mitrev
 */
public interface GpsTypeHandler {

    /**
     * Callback called once the gps_auto type is selected
     *
     * @param gpsTypeArg gps_auto type
     */
    void gpsTypeSelected(GpsTypeDialog.GPSType gpsTypeArg);
}
