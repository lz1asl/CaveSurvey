package com.astoev.cave.survey.service.bluetooth.device.comm.distox;

import com.astoev.cave.survey.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DistoXv1BluetoothDevice extends AbstractDistoXBluetoothDevice {

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameEquals(aName, "DistoX");
    }

    @Override
    public String getDescription() {
        return "DistoX";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
        // not needed, measures sent automatically
    }
}
