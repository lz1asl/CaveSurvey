package com.astoev.cave.survey.service.bluetooth.device;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.util.NMEAUtil;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * TruPulse 360B handler.
 * Created by astoev on 9/9/14.
 */
public class TruPulse360BBluetoothDevice extends AbstractBluetoothDevice {

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "TP360B");
    }

    @Override
    public String getDescription() {
        return "Laser Technology Inc TruPulse 360B";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
        // no need to trigger+
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {
        Log.i(Constants.LOG_TAG_BT, "Configure device");

        Log.d(Constants.LOG_TAG_BT, "True distance");
        IOUtils.write("$MM,0\n", anOutput);
        Log.d(Constants.LOG_TAG_BT, "Distance in meters");
        IOUtils.write("$DU,0\n", anOutput);
        Log.d(Constants.LOG_TAG_BT, "Angle in degrees");
        IOUtils.write("$AU,0\n", anOutput);

        Log.d(Constants.LOG_TAG_BT, "Start listening");
        IOUtils.write("$GO\n", anOutput);

        Log.d(Constants.LOG_TAG_BT, "Command sent ");
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws IOException, DataException {
        return NMEAUtil.decodeTruPulse(aResponseBytes);
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        // has all distance, clino and azimuth
        return true;
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        return NMEAUtil.isFullSizeMessage(aBytesBuffer);
    }
}
