package com.astoev.cave.survey.service.bluetooth.device;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Leica disto devices.
 *
 * Created by astoev on 9/12/15.
 */
public class LeicaDistoBluetoothDevice extends AbstractBluetoothDevice {

    @Override
    public boolean isNameSupported(String aName) {
        // TODO
        return aName.toLowerCase().contains("disto");
    }

    @Override
    public String getDescription() {
        return "Leica DISTO";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {

    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {

    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws DataException {
        return null;
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        return false;
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        return false;
    }
}
