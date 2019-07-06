package com.astoev.cave.survey.service.orientation;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.util.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

import static com.astoev.cave.survey.model.Option.MAX_VALUE_AZIMUTH_DEGREES;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_NOISE_REDUCTION;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS;

public class MeasurementsFilter {

    private List<Float> measurements = new ArrayList<>();
    private boolean averagingEnabled = false;
    private int numMeasurements = 1;
    private boolean ready = false;
    private boolean averaging = false;
    private float lastDeviation = 0;
    private float averaged;

    public void initializeFromConfig() {
        averagingEnabled = ConfigUtil.getBooleanProperty(PREF_SENSOR_NOISE_REDUCTION);
        numMeasurements = ConfigUtil.getIntProperty(PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS, 1);
    }

    public void addMeasurement(Float value) {
        if (averagingEnabled) {
            // collect measurements history
            measurements.add(value);

            // remove old values
            if (measurements.size() > numMeasurements + 1) {
                measurements.remove(0);
            }

            if (averaging) {
                // time to get the best value
                Log.i(Constants.LOG_TAG_SERVICE, "Averaging: " + value);
                List<Float> lastMeasurements = getLastMeasurements();
                float deviation = getDeviation(lastMeasurements);

                lastDeviation = deviation;
                averaged = getAverage(lastMeasurements);

                if (deviation >= lastDeviation && lastMeasurements.size() >= numMeasurements) {
                    // enough measurements + new noise = interrupt
                    ready = true;
                }
            }
        } else {
            // directly assign the value
            averaged = value;
            ready = true;
        }
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

    public Float getAverage(List<Float> values) {
        // TODO to degrees if needed with kMapUtilities.getAzimuthInDegrees(java.lang.Float, java.lang.String)

        return getAverageAzimuthDegrees(values);

    }

    public float getAverageAzimuthDegrees(float first, float second) {

        // sum
        float sum = first + second;

        // 360 degrees = 0
        if (sum == MAX_VALUE_AZIMUTH_DEGREES) {
            return 0;
        }

        // two opposite values
        int half = MAX_VALUE_AZIMUTH_DEGREES / 2;
        if (Math.abs(first - second) > MAX_VALUE_AZIMUTH_DEGREES / 2) {
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

    public float getDeviation(List<Float> values) {
        if (values.isEmpty()) {
            return 0;
        }
        float maxDeviation = 0;
        float average = getAverage(values);
        for (Float reading : values) {
            float deviation = Math.abs(average - reading);
            maxDeviation = Math.max(deviation, maxDeviation);
        }
        return maxDeviation;
    }

    public String getAccuracyString() {
        return "(" + lastDeviation + " out of " + numMeasurements + ")";
    }

    // last PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS measurements or less if not available yet
    private List<Float> getLastMeasurements() {
        int lastMeasurementsCount = Math.min(measurements.size(), numMeasurements);
        return measurements.subList(measurements.size() - lastMeasurementsCount, measurements.size());
    }
}
