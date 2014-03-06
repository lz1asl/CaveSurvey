package com.astoev.cave.survey.service.bluetooth.device;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.util.NMEAUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by astoev on 2/21/14.
 */
public class LaserAceBluetoothDevice extends AbstractBluetoothDevice {


    @Override
    public boolean isNameSupported(String aName) {
        return aName != null && aName.startsWith("LA SURVEY,");
    }

    @Override
    protected String getSPPUUIDString() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Laser Ace";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
        // should not be required
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws IOException, DataException {
        // ignore requested measures for now, we should have full set of measures
        return NMEAUtil.decode(aResponseBytes);
    }
}