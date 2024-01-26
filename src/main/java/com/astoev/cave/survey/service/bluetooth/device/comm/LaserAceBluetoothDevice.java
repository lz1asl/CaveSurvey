package com.astoev.cave.survey.service.bluetooth.device.comm;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.Constants.MeasureTypes;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.protocol.AbstractDeviceProtocol;
import com.astoev.cave.survey.service.bluetooth.device.protocol.LaserAceProtocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by astoev on 2/21/14.
 */
public class LaserAceBluetoothDevice extends AbstractBluetoothRFCOMMDevice {


    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "LA SURVEY,");
    }

    public String getDescription() {
        return "Trimble LaserAce 100";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<MeasureTypes> aMeasures) {
        // should not be required
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) {
        // TODO
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<MeasureTypes> aMeasures) throws DataException {
        // ignore requested measures for now, we should have full set of measures
        return mProtocol.packetToMeasurements(aResponseBytes);
    }

    @Override
    protected List<Constants.MeasureTypes> getSupportedMeasureTypes() {
        // single device supported, name ignored
        // all current distance, angle and inclination are returned on each measure
        return Arrays.asList(Constants.MeasureTypes.values());
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        return mProtocol.isFullMessage(aBytesBuffer);
    }

    @Override
    public AbstractDeviceProtocol getProtocol() {
        return new LaserAceProtocol();
    }
}
