package com.astoev.cave.survey.service.bluetooth.device.protocol;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.util.ArrayList;
import java.util.List;

public class LeicaDistoD3aProtocol extends AbstractPacketBasedDeviceProtocol {

    private static final String MESSAGE_REGEX = "(22\\.\\.01[\\+|\\-][0-9]{8} )?31\\.\\.00\\+[0-9]{8}";


    @Override
    public List<Measure> packetToMeasurements(byte[] aDataPacket) throws DataException {
        if (aDataPacket == null || aDataPacket.length < 15) {
            Log.i(Constants.LOG_TAG_BT, "Got empty message");
            throw new DataException("Bad data");
        }

        String message = new String(aDataPacket).trim();

        if (!message.matches(MESSAGE_REGEX)) {
            Log.i(Constants.LOG_TAG_BT, "Got bad message: " + message);
            throw new DataException("Bad data");
        }

        List<Measure> measures = new ArrayList<>();
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


    private float getDistanceInMeters(String distanceMessage) {
        return Float.parseFloat(distanceMessage) / 1000;
    }

    private float getSlopeInDegrees(String slopeMessage) {
        return Float.parseFloat(slopeMessage) / 100;
    }

    @Override
    public boolean isFullMessage(byte[] dataPacket) {
        return dataPacket != null && new String(dataPacket).endsWith("\n");
    }
}
