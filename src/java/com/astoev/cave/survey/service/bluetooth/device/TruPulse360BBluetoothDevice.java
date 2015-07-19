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
        // no need to trigger
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {
        Log.i(Constants.LOG_TAG_BT, "Configure LTI disabled");

/*
        Log.i(Constants.LOG_TAG_BT, "Configure device");

        Log.d(Constants.LOG_TAG_BT, "True distance");
        sendLogged("$MM,0\n", anOutput, false);
        Log.d(Constants.LOG_TAG_BT, "Response: " + IOUtils.toString(anInput));

        Log.d(Constants.LOG_TAG_BT, "Distance in meters");
        sendLogged("$DU,0\n", anOutput, false);
        Log.d(Constants.LOG_TAG_BT, "Response: " + IOUtils.toString(anInput));

        Log.d(Constants.LOG_TAG_BT, "Angle in degrees");
        sendLogged("$AU,0\n", anOutput, false);
        Log.d(Constants.LOG_TAG_BT, "Response: " + IOUtils.toString(anInput));

        Log.d(Constants.LOG_TAG_BT, "Start listening");
        sendLogged("$GO\n", anOutput, false);
        Log.d(Constants.LOG_TAG_BT, "Response: " + IOUtils.toString(anInput));

        Log.d(Constants.LOG_TAG_BT, "Config commands sent ");*/
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
