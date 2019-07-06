package com.astoev.cave.survey.service.orientation;

import android.util.Log;

import com.astoev.cave.survey.util.ConfigUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.astoev.cave.survey.Constants.LOG_TAG_SERVICE;
import static com.astoev.cave.survey.model.Option.MAX_VALUE_AZIMUTH_DEGREES;
import static com.astoev.cave.survey.model.Option.VALUE_AZIMUTH_180_DEGREES;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_NOISE_REDUCTION;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS;

public class MeasurementsFilter {

    // up to 20% of the measurements removed as noise
    private static final float NOISE_COUNT_TRESHOLD = 0.2f;

    // considered noise if more than 60% away from the average
    private static final float DISTANCE_TRESHOLD = 0.6f;

    private static DecimalFormat formatter = new DecimalFormat("0.00");

    // raw values
    private List<Float> measurements = new ArrayList<>();

    // flags
    private int numMeasurements = 1;
    private boolean ready = false;
    private boolean averagingEnabled = false;
    private boolean averaging = false;

    // final values
    private float lastDeviation = 0;
    private float averaged;

    public void initializeFromConfig() {
        averagingEnabled = ConfigUtil.getBooleanProperty(PREF_SENSOR_NOISE_REDUCTION);
        numMeasurements = ConfigUtil.getIntProperty(PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS, 1);
    }

    public void addMeasurement(Float value) {

        if (ready) {
            Log.i(LOG_TAG_SERVICE, "Ignore measurement, ready");
            return;
        }

        if (averagingEnabled) {
            // collect measurements history
            if (measurements.size() > numMeasurements) {
                Log.i(LOG_TAG_SERVICE, measurements.size() +" samples collected");
                measurements.remove(0);
            }
            measurements.add(value);
            Log.i(LOG_TAG_SERVICE, "Adding " + value);


            List<Float> lastMeasurements = getLastMeasurements();
            averaged = getAverageAzimuthDegrees(lastMeasurements);
            float deviation = getDeviation(lastMeasurements, averaged);
            Log.i(LOG_TAG_SERVICE, "Deviation got " + deviation);

            // time to get the best value
            if (averaging) {

                if (deviation >= lastDeviation && lastMeasurements.size() >= numMeasurements) {
                    // enough measurements + new noise = finalize
                    Log.i(LOG_TAG_SERVICE, "Got enough measurements, noise increasing");
                    ready = true;

                    // remove last noise
                    if (deviation > lastDeviation) {
                        Log.i(LOG_TAG_SERVICE, "Remove last measurement " + value);
                        measurements.remove(measurements.size());
                    }

                    // final processing
                    lastMeasurements = getLastMeasurements();
                    averaged = getAverageAzimuthDegrees(lastMeasurements);
                    deviation = getDeviation(lastMeasurements, averaged);
                    Log.i(LOG_TAG_SERVICE, "Average " + averaged + " with deviation " + deviation);

                    // remove noise
                    lastMeasurements = removeNoise(lastMeasurements, averaged, deviation);

                    // average samples left
                    averaged = getAverageAzimuthDegrees(lastMeasurements);
                    lastDeviation = getDeviation(lastMeasurements, averaged);
                    Log.i(LOG_TAG_SERVICE, "With noise reduction " + averaged + " with deviation " + deviation);
                }
            }

            // prepare for next iteration
            lastDeviation = deviation;
        } else {
            // directly assign the value
            Log.i(LOG_TAG_SERVICE, "Direct value " + value);
            averaged = value;
            if (averaging) {
                Log.i(LOG_TAG_SERVICE, "Direct value ready " + value);
                ready = true;
            }
        }
    }

    private List<Float> removeNoise(List<Float> aLastMeasurements, float anAveraged, float aDeviation) {
        // remove up to 20 percent of the measurements
        int numMeasurementsToRemove = (int) (numMeasurements * NOISE_COUNT_TRESHOLD);

        // that are more than 60% away from the average
        float noiseDistance = (float) (Math.abs(aDeviation - anAveraged) * DISTANCE_TRESHOLD);

        for (int i=0; i< numMeasurementsToRemove; i++) {
            // find farthest value
            float value = findBiggestDistance(aLastMeasurements, anAveraged); // TODO

            // remove if distance above treshold
            float distance = getDeviation(anAveraged, value);
            if (distance > noiseDistance) {
                Log.i(LOG_TAG_SERVICE, "Removing noise " + value + " by distance " + distance);
                aLastMeasurements.remove(value);
            }
        }
        return aLastMeasurements;
    }

    private float findBiggestDistance(List<Float> aMeasurements, float anAverage) {
        float mostDistance = aMeasurements.get(0);
        for (int i=1; i<aMeasurements.size(); i++) {
            if (getDeviation(aMeasurements.get(i), anAverage) > mostDistance) {
                mostDistance = aMeasurements.get(i);
            }
        }
        return mostDistance;
    }

    public void startAveraging() {
        averaging = true;
    }

    public boolean isReady() {
        return ready;
    }

    public Float getValue() {
        return averaged;
    }

    public void setAveragingEnabled(boolean aAveragingEnabled) {
        averagingEnabled = aAveragingEnabled;
    }

    public void setNumMeasurements(int aNumMeasurements) {
        numMeasurements = aNumMeasurements;
    }

    public float getAverageAzimuthDegrees(float first, float second) {

        // sum
        float sum = first + second;

        // 360 degrees = 0
        if (sum == MAX_VALUE_AZIMUTH_DEGREES) {
            return 0;
        }

        // two opposite values
        if (Math.abs(first - second) > VALUE_AZIMUTH_180_DEGREES) {
            sum += MAX_VALUE_AZIMUTH_DEGREES;
        }

        // average
        float average = sum / 2;

        // normalize if needed
        if (average >= MAX_VALUE_AZIMUTH_DEGREES) {
            return average - MAX_VALUE_AZIMUTH_DEGREES;
        } else {
            return average;
        }
    }

    public Float getAverageAzimuthDegrees(List<Float> measurements) {

        if (measurements.size() == 1) {
            return measurements.get(0);
        }

        // left
        List<Float> averages = new ArrayList<>();
        for (int i=1; i<measurements.size(); i++) {
            float average = getAverageAzimuthDegrees(measurements.get(i-1), measurements.get(i));
            averages.add(average);
        }
        return getAverageAzimuthDegrees(averages);
    }

    public float getDeviation(List<Float> values, float anAverage) {
        if (values.size() <= 1) {
            return 0;
        }
        float maxDeviation = 0;
        for (Float reading : values) {
            float deviation = getDeviation(anAverage, reading);
            maxDeviation = Math.max(deviation, maxDeviation);
        }
        return maxDeviation;
    }

    private float getDeviation(float value1, float value2) {
        float average = getAverageAzimuthDegrees(value1, value2);
        return Math.min(Math.abs(average - value1), Math.abs(average - value2));
    }

    public String getAccuracyString() {
        if (averagingEnabled) {
            return " \u00B1" + formatter.format(lastDeviation) + "/" + formatter.format(numMeasurements);
        } else {
            return "";
        }
    }

    // last PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS measurements or less if not available yet
    private List<Float> getLastMeasurements() {
        int lastMeasurementsCount = Math.min(measurements.size(), numMeasurements);
        return measurements.subList(measurements.size() - lastMeasurementsCount, measurements.size());
    }
}
