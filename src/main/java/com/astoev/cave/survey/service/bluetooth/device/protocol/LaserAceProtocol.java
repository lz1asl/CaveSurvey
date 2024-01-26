package com.astoev.cave.survey.service.bluetooth.device.protocol;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Created by astoev on 2/22/14.
 */
public class LaserAceProtocol extends AbstractNMEADeviceProtocol {

    @Override
    public List<Measure> packetToMeasurements(byte[] aMessage) throws DataException {

        if (aMessage == null || aMessage.length <= 0) {
            throw new DataException("Empty message");
        }

        try {
            String messageString = new String(aMessage).trim();
            Log.i(Constants.LOG_TAG_BT, "Got message " + messageString);

            List<Measure> measures = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(messageString, ",*");

            // header
            if (!"$PTNLA".equals(tokenizer.nextToken())) {
                throw new DataException("Bad header");
            }
            if (!"HV".equals(tokenizer.nextToken())) {
                throw new DataException("Bad vector");
            }

            // distance
            String distanceString = tokenizer.nextToken();
            boolean distancePresent = true;
            if ("M".equals(distanceString) || "F".equals(distanceString)) {
                // no distance, no need to skip distance units
                distancePresent = false;
            } else {
                if (!"M".equals(tokenizer.nextToken())) {
                    throw new DataException("Please measure in meters ");
                }

                float distance = Float.parseFloat(distanceString);
                if (distance < 0) {
                    throw new DataException("Negative distance");
                }

                // this is horizontal distance, so ignored now
            }

            // angle
            float angle = Float.parseFloat(tokenizer.nextToken());
            if (!MapUtilities.isAzimuthInGradsValid(angle)) {
                throw new DataException("Invalid azimuth");
            }

            if (!"D".equals(tokenizer.nextToken())) {
                throw new DataException("Please measure angle in degrees");
            }

            Measure angleMeasure = new Measure(Constants.MeasureTypes.angle, Constants.MeasureUnits.degrees, angle);
            measures.add(angleMeasure);
            Log.i(Constants.LOG_TAG_BT, "Got angle " + angleMeasure);


            // slope
            float slope = Float.parseFloat(tokenizer.nextToken());
            if (!MapUtilities.isSlopeInDegreesValid(slope)) {
                throw new DataException("Invalid slope");
            }

            if (!"D".equals(tokenizer.nextToken())) {
                throw new DataException("Please measure slope in degrees");
            }

            Measure slopeMeasure = new Measure(Constants.MeasureTypes.slope, Constants.MeasureUnits.degrees, slope);
            measures.add(slopeMeasure);
            Log.i(Constants.LOG_TAG_BT, "Got slope " + slopeMeasure);

            // skip vertical distance
            if (distancePresent) {
                distanceString = tokenizer.nextToken();
                if ("M".equals(distanceString) || "F".equals(distanceString)) {
                    // no distance, no need to skip distance units
                } else {
                    if (!"M".equals(tokenizer.nextToken())) {
                        throw new DataException("Please measure in meters");
                    }

                    float distance = Float.parseFloat(distanceString);
                    if (distance < 0) {
                        throw new DataException("Negative distance");
                    }

                    Measure distanceMeasure = new Measure(Constants.MeasureTypes.distance, Constants.MeasureUnits.meters, distance);
                    measures.add(distanceMeasure);
                    Log.i(Constants.LOG_TAG_BT, "Got distance " + distanceMeasure);
                }
            } else {
                // skip the 999's and measure type
                String border = "";
                int count = 0;
                while (!"M".equals(border) && count < 3) {
                    border = tokenizer.nextToken();
                    count++;
                }
            }

            // checksum
            String calculatedCheck = getCheckSum(messageString);
            if (!calculatedCheck.equals(tokenizer.nextToken())) {
                throw new DataException("Bad checksum");
            }

            if (tokenizer.hasMoreTokens()) {
                throw new DataException("Extra data found");
            }

            return measures;

        } catch (NoSuchElementException nse) {
            throw new DataException("Too short message");
        } catch (NumberFormatException nfe) {
            throw new DataException("Bad value " + nfe.getMessage());
        }
    }

}
