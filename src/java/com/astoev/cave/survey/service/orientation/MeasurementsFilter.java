package com.astoev.cave.survey.service.orientation;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.util.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

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
        if (values.isEmpty()) {
            return null;
        }
        int readingsCount = 0;
        float sum = 0;
        for (Float reading : values) {
            readingsCount++;
            sum += reading;
        }
        float average = sum / readingsCount;
        Log.i(Constants.LOG_TAG_SERVICE, "Averaged to: " + average);
        return average;
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
