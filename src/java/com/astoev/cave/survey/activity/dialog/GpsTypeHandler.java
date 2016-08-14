package com.astoev.cave.survey.activity.dialog;

/**
 * Interface for handling selection of gps type
 *
 * @author Jivko Mitrev
 */
public interface GpsTypeHandler {

    /**
     * Callback called once the gps type is selected
     *
     * @param gpsTypeArg gps type
     */
    void gpsTypeSelected(GpsTypeDialog.GPSType gpsTypeArg);
}
