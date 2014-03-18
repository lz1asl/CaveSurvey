package com.astoev.cave.survey.service.bluetooth.util;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Created by astoev on 2/22/14.
 */
public class NMEAUtil {

    public static List<Measure> decode(byte [] aMessage) throws DataException {

        if (aMessage == null || aMessage.length <=0) {
            throw new DataException("Empty message");
        }

        try {
            String messageString = new String(aMessage);
            Log.i(Constants.LOG_TAG_BT, "Got message " + messageString);

            List<Measure> measures = new ArrayList<Measure>();
            StringTokenizer tokenizer = new StringTokenizer(messageString, ",");

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

                Measure distanceMeasure = new Measure(Constants.MeasureTypes.distance, Constants.MeasureUnits.meters, distance);
                measures.add(distanceMeasure);
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

            // skip vertical distance
            if (distancePresent) {
                tokenizer.nextToken();
            }
            tokenizer.nextToken("*");

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

    private static String getCheckSum(String aString) {
        int checksum = 0;
        String checkedString = aString.substring(1, aString.indexOf("*"));

        for (int i = 0; i < checkedString.length(); i++) {
            checksum = checksum ^ checkedString.charAt(i);
        }

        String hex = Integer.toHexString(checksum);
        if (hex.length() == 1)
            hex = "0" + hex;

        return hex.toUpperCase();
    }
}
