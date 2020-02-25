package com.astoev.cave.survey.service.bluetooth.device.comm;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.Constants.MeasureTypes;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.astoev.cave.survey.Constants.MeasureTypes.distance;
import static com.astoev.cave.survey.Constants.MeasureTypes.slope;

/**
 * Created by astoev on 2/21/14.
 */
public class LeicaDistoD3aBtBluetoothDevice extends AbstractBluetoothRFCOMMDevice {


    private final String MESSAGE_REGEX = "(22\\.\\.01[\\+|\\-][0-9]{8} )?31\\.\\.00\\+[0-9]{8}";


    @Override
    public boolean isNameSupported(String aName) {
        return aName == null;
    }

    public String getDescription() {
        return "Leica Disto D3a BT";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<MeasureTypes> aMeasures) throws IOException {
        // should not be required
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {
        // nothing to configure
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<MeasureTypes> aMeasures) throws DataException {

        if (aResponseBytes == null || aResponseBytes.length < 15) {
            Log.i(Constants.LOG_TAG_BT, "Got empty message");
            throw new DataException("Bad data");
        }

        String message = new String(aResponseBytes).trim();

        if (!message.matches(MESSAGE_REGEX)) {
            Log.i(Constants.LOG_TAG_BT, "Got bad message: " + message);
            throw new DataException("Bad data");
        }

        List<Measure> measures = new ArrayList<Measure>();
        if (message.length() > 15) {
            // inclination + distance

            float slope = getSlopeInDegrees(message.substring(6, 16));
            Measure slopeMeasure = new Measure(Constants.MeasureTypes.slope, Constants.MeasureUnits.degrees, slope);
            measures.add(slopeMeasure);

            float distance = getDistanceInMeters(message.substring(24));
            Measure distanceMeasure = new Measure(Constants.MeasureTypes.distance, Constants.MeasureUnits.meters, distance);
            measures.add(distanceMeasure);

        } else {
            // distance only
            float distance = getDistanceInMeters(message.substring(7));
            Measure distanceMeasure = new Measure(Constants.MeasureTypes.distance, Constants.MeasureUnits.meters, distance);
            measures.add(distanceMeasure);
        }

        return measures;
    }

    @Override
    protected List<MeasureTypes> getSupportedMeasureTypes() {
        // either distance or distance + slope
        return Arrays.asList(distance, slope);
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        return aBytesBuffer != null && new String(aBytesBuffer).endsWith("\n");
    }

    private float getDistanceInMeters(String distanceMessage) {
        return Float.parseFloat(distanceMessage) / 1000;
    }

    private float getSlopeInDegrees(String slopeMessage) {
        return Float.parseFloat(slopeMessage) / 100;
    }

}
