package com.astoev.cave.survey.service.bluetooth.device.comm;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Bosch PLR 30 C over comm.
 * Created by astoev on 12/24/15.
 */
public class BoschPLR30CBluetoothDevice extends AbstractBluetoothRFCOMMDevice {

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
        // not implemented
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {
        // not implemented
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws DataException {
        return null;
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        return false;
    }

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "TODO");
    }

    @Override
    public String getDescription() {
        return "Bosch PLR 30C";
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        // only distance
        return Constants.MeasureTypes.distance.equals(aMeasureType);
    }
}
