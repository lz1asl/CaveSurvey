package com.astoev.cave.survey.service.bluetooth.device.protocol;

public abstract class AbstractNMEADeviceProtocol extends AbstractPacketBasedDeviceProtocol {

    public static final String NEW_LINE = "\n";


    protected String getCheckSum(String aString) {
        int checksum = 0;
        String checkedString = aString.substring(1, aString.indexOf("*"));

        for (int i = 0; i < checkedString.length(); i++) {
            checksum = checksum ^ checkedString.charAt(i);
        }

        String hex = Integer.toHexString(checksum);
        if (hex.length() == 1)
            hex = "0" + hex;

        return hex.toUpperCase();
    }


    @Override
    public boolean isFullMessage(byte[] aBytesBuffer) {
        // new line delimited NMEA messages
        return new String(aBytesBuffer).endsWith(NEW_LINE);
    }
}
