package com.astoev.cave.survey.service.bluetooth.device.ble.bric;

/**
 *  https://www.bricsurvey.com/s/BRIC4-Bluetooth-Protocol-revF.pdf
 */
public class Bric4ErrorCode {

    private int mCode;
    private Float mData1;
    private Float mData2;
    private String mDescription;


    public Bric4ErrorCode(int aCode, Float aData1, Float aData2) {
        mCode = aCode;
        mData1 = aData1;
        mData2 = aData2;
        mDescription = buildDescription();
    }


    public int getCode() {
        return mCode;
    }

    public Float getData1() {
        return mData1;
    }

    public Float getData2() {
        return mData2;
    }

    public String getDescription() {
        return mDescription;
    }


    public static String findData1Description(int aCode) {
        switch (aCode) {
            case 1:
            case 2:
            case 3:
            case 4: return "Magnitude of vector";

            case 5:
            case 6: return "Delta";

            case 11: return "Timeout (seconds)";
            case 12: return "Rangefinder error code";
            case 13: return "Rangefinder message identifier";
            case 14: return "Inclination delta";
            case 15: return "Azimuth delta";

            default: return null;
        }
    }

    public static String findData2Description(Float aData2) {
        if (aData2 == null) {
            return null;
        }
        switch (aData2.intValue()) {
            case 1: return "Axis X";
            case 2: return "Axis Y";
            case 3: return "Axis Z";
            default: return null;
        }
    }

    public static String findCodeDescription(int aCode) {
        switch (aCode) {
            case 0 : return "No error detected";
            case 1 : return "Accelerometer 1 high magnitude. Nominal is 1.";
            case 2 : return "Accelerometer 2 high magnitude. Nominal is 1.";
            case 3 : return "Magnetometer 1 high magnitude. Nominal is 1.";
            case 4 : return "Magnetometer 2 high magnitude. Nominal is 1.";
            case 5 : return "Accelerometer disparity error. Significant difference in single axis measurement value between both accelerometers.";
            case 6 : return "Magnetometer disparity error. Significant difference in single axis measurement value between both magnetometers.";
            case 7 : return "Rangefinder calculation error. Target moved too fast.";
            case 8 : return "Rangefinder weak signal. Target not reflective enough or too far away.";
            case 9 : return "Rangefinder strong signal. Target too reflective.";
            case 10 : return "Rangefinder Pattern Error. Communication error due to lack of 0xAA message identifier.";
            case 11 : return "Rangefinder Response Timeout. Message not received in timeout period.";
            case 12 : return "Rangefinder unrecognized error.";
            case 13 : return "Rangefinder wrong message received. Communication error due to wrong message received.";
            case 14: return "Inclination angle error. Inclination calculated using combinations of all 4 sensors and results compared. Differences in the calculated inclination over a threshold triggers this error.";
            case 15: return "Azimuth angle error. Azimuth calculated using combinations of all 4 sensors and results compared. Differences in the calculated azimuth over a threshold triggers this error.";

            default: return null;
        }
    }

    private String buildDescription() {
        StringBuilder description = new StringBuilder();
        description.append(findCodeDescription(mCode));
        String data1Description = findData1Description(mCode);
        if (data1Description != null) {
            description.append("\n").append(data1Description).append(" : ").append(mData1);
        }
        String data2Description = findData2Description(mData2);
        if (data2Description != null) {
            description.append(", ").append(data2Description);
        }
        return description.toString();
    }

}
