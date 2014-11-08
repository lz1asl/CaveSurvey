package com.astoev.cave.survey.service.bluetooth.device;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.util.NMEAUtil;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * TruPulse 360B handler.
 * Created by astoev on 9/9/14.
 */
public class TruPulse360BBluetoothDevice extends AbstractBluetoothDevice {
    @Override
    public boolean isNameSupported(String aName) {
        return aName != null && aName.startsWith("TP360B");
    }

    @Override
    protected String getSPPUUIDString() {
        return "00001101-0000-1000-8000-00805F9B34FB";
    }

    @Override
    public String getDescription() {
        return "Laser Technology, Inc TruPulse 360B";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
//        IOUtils.write("$MM,0\n", aStream);
//        IOUtils.write("$DU,0\n", aStream);
//        IOUtils.write("$AU,0\n", aStream);
        IOUtils.write("$PLTIT,RQ,HV\n", aStream);
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws IOException, DataException {
        return NMEAUtil.decodeTruPulse(aResponseBytes);
    }

    @Override
    public boolean isPassiveBTConnection() {
        return false;
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        // has all distance, clino and azimuth
        return true;
    }
}
