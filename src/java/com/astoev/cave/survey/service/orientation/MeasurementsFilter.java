package com.astoev.cave.survey.service.orientation;

import com.astoev.cave.survey.util.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_NOISE_REDUCTION;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS;

public class MeasurementsFilter {

    private List<Float> measurements;
    boolean averagingEnabled;
    int numMeasurements;

    public MeasurementsFilter() {
        averagingEnabled = false;
        numMeasurements = 1;
        measurements = new ArrayList<>(numMeasurements);
    }

    public void initialize() {
        averagingEnabled = ConfigUtil.getBooleanProperty(PREF_SENSOR_NOISE_REDUCTION);
        numMeasurements = ConfigUtil.getIntProperty(PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS, 1);
        measurements = new ArrayList<>(numMeasurements);
    }

    public void addMeasurement(Float value) {
        measurements.add(value);
        // limit the size
        if (measurements.size() > numMeasurements) {
            measurements.remove(0);
        }
    }

    public boolean isReady() {
        return measurements.size() >= numMeasurements;
    }

    public Float getLastValue() {
        if (!measurements.isEmpty()) {
            return measurements.get(measurements.size() - 1);
        } else {
            return null;
        }
    }

    public List<Float> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Float> aMeasurements) {
        measurements = aMeasurements;
    }

    public boolean isAveragingEnabled() {
        return averagingEnabled;
    }

    public void setAveragingEnabled(boolean aAveragingEnabled) {
        averagingEnabled = aAveragingEnabled;
    }

    public int getNumMeasurements() {
        return numMeasurements;
    }

    public void setNumMeasurements(int aNumMeasurements) {
        numMeasurements = aNumMeasurements;
    }

    public Float getAverage() {
        if (measurements.isEmpty()) {
            return null;
        }
        int readingsCount = 0;
        float sum = 0;
        for (Float reading : measurements) {
            readingsCount++;
            sum += reading;
        }
        return sum / readingsCount;
    }

    public float getDeviation() {
        if (measurements.isEmpty()) {
            return 0;
        }
        float min = 0;
        float max = 0;
        boolean first = true;
        for (Float reading : measurements) {
            if (first) {
                min = max = reading;
                first = false;
            } else {
                min = Math.min(min, reading);
                max = Math.max(max, reading);
            }
        }
        return max - min;
    }

    public String getAccuracyString() {
        return "(" + getDeviation() + " out of " + getNumMeasurements() + ")";
    }
}
