package com.astoev.cave.survey.service.bluetooth.device;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by astoev on 3/26/15.
 */
public class DistoXBluetoothDevice extends AbstractBluetoothDevice {


    @Override
    public boolean isNameSupported(String aName) {
        // TODO filtering logic
        return false;
    }

    @Override
    public String getDescription() {
        return "DistoX";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
        // not needed, measures sent automatically
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {
        // not supported by DistoX
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws IOException, DataException {
        // TODO implement packet parsing logic
        return null;
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        // all measures available on each shot
        return true;
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        // TODO define logic
        return false;
    }

    @Override
    public void ack(OutputStream aStream) {
        // TODO acknowledge packet received
    }
}
