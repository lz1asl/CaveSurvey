package com.astoev.cave.survey.service.bluetooth.device;

import com.astoev.cave.survey.Constants.MeasureTypes;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.util.NMEAUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by astoev on 2/21/14.
 */
public class LaserAceBluetoothDevice extends AbstractBluetoothDevice {


    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "LA SURVEY,");
    }

    public String getDescription() {
        return "Trimble LaserAce 100";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<MeasureTypes> aMeasures) throws IOException {
        // should not be required
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {
        // TODO
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<MeasureTypes> aMeasures) throws DataException {
        // ignore requested measures for now, we should have full set of measures
        return NMEAUtil.decodeTrimbleLaserAce(aResponseBytes);
    }

    @Override
    public boolean isMeasureSupported(String aName, MeasureTypes aMeasureType) {
        // single device supported, name ignored
        // all current distance, angle and inclination are returned on each measure
        return true;
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        return NMEAUtil.isFullSizeMessage(aBytesBuffer);
    }

}
