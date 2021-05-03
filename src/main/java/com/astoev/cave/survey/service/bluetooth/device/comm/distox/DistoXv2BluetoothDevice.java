package com.astoev.cave.survey.service.bluetooth.device.comm.distox;

import com.astoev.cave.survey.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DistoXv2BluetoothDevice extends AbstractDistoXBluetoothDevice {

    @Override
    public boolean isNameSupported(String aName) {
        // DistoX-xxxx
        return hasLength(aName, 11) && deviceNameStartsWith(aName, "DistoX-");
    }

    @Override
    public String getDescription() {
        return "DistoX v2";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
        // TODO turn the laser on

        // TODO check the version first and ensure 2.3?

        /*Switch Laser On (1 byte):
        Byte 0: 00110110 (version 2.3 & higher)
        Switch Laser Off (1 byte):
        Byte 0: 00110111 (version 2.3 & higher)*/
    }
}
