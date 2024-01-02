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
public class TruPulseProtocol extends AbstractNMEADeviceProtocol {


    @Override
    public List<Measure> packetToMeasurements(byte[] aMessage) throws DataException {

        if (aMessage == null || aMessage.length <= 0) {
            throw new DataException("Empty message");
        }

        try {
            String messageString = new String(aMessage).trim();
            Log.i(Constants.LOG_TAG_BT, "Got message " + messageString);

            List<Measure> measures = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(messageString, ",*", true);

            // ignore OKs
            if (messageString.startsWith("$OK")) {
                return measures;
            }

            // header
            if (!"$PLTIT".equals(tokenizer.nextToken())) {
                throw new DataException("Bad header");
            }
            tokenizer.nextToken();

            if (!"HV".equals(tokenizer.nextToken())) {
                throw new DataException("Bad vector");
            }
            tokenizer.nextToken();

            // distance
            boolean distancePresent = false;
            String units;
            String distanceString = tokenizer.nextToken();
            if (",".equals(distanceString)) {
                distancePresent = false;
            } else {
                tokenizer.nextToken();

                units = tokenizer.nextToken();

                if ("M".equals(units) || "F".equals(units) || "Y".equals(units)) {
                    if (!"M".equals(units)) {
                        throw new DataException("Please measure in meters ");
                    }


                    float distance = Float.parseFloat(distanceString);
                    if (distance < 0) {
                        throw new DataException("Negative distance");
                    }
                    distancePresent = true;
                    // this is horizontal distance, so ignored now
                }
            }
            tokenizer.nextToken();

            // angle
            float angle = Float.parseFloat(tokenizer.nextToken());
            if (!MapUtilities.isAzimuthInGradsValid(angle)) {
                throw new DataException("Invalid azimuth");
            }

            tokenizer.nextToken();
            if (!"D".equals(tokenizer.nextToken())) {
                throw new DataException("Please measure angle in degrees");
            }

            Measure angleMeasure = new Measure(Constants.MeasureTypes.angle, Constants.MeasureUnits.degrees, angle);
            measures.add(angleMeasure);
            Log.i(Constants.LOG_TAG_BT, "Got angle " + angleMeasure);
            tokenizer.nextToken();


            // slope
            float slope = Float.parseFloat(tokenizer.nextToken());
            if (!MapUtilities.isSlopeInDegreesValid(slope)) {
                throw new DataException("Invalid slope");
            }

            tokenizer.nextToken();
            if (!"D".equals(tokenizer.nextToken())) {
                throw new DataException("Please measure slope in degrees");
            }

            Measure slopeMeasure = new Measure(Constants.MeasureTypes.slope, Constants.MeasureUnits.degrees, slope);
            measures.add(slopeMeasure);
            Log.i(Constants.LOG_TAG_BT, "Got slope " + slopeMeasure);
            tokenizer.nextToken();

            // skip vertical distance
            if (distancePresent) {
                distanceString = tokenizer.nextToken();
                tokenizer.nextToken();
                units = tokenizer.nextToken();
                if ("M".equals(units) || "F".equals(units) || "Y".equals(units)) {
                    if (!"M".equals(units)) {
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
                // skip the ,,'s for missing distance
                tokenizer.nextToken();
            }

            // checksum
            tokenizer.nextToken();
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
