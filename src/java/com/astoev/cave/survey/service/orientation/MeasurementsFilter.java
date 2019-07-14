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
    private Float lastSD = null;
    private float averaged;

    public void initializeFromConfig() {
        averagingEnabled = ConfigUtil.getBooleanProperty(PREF_SENSOR_NOISE_REDUCTION);
        numMeasurements = ConfigUtil.getIntProperty(PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS, 1);
    }

    public void addMeasurement(Float value) {

        if (averagingEnabled) {

            if (ready) {
                Log.i(LOG_TAG_SERVICE, "Ignore measurement, ready");
                return;
            }

            // collect measurements history
            measurements.add(value);
            Log.i(LOG_TAG_SERVICE, "Adding " + value);


            List<Float> lastMeasurements = getLastMeasurements();
            averaged = getAverageAzimuthDegrees(lastMeasurements);
            float sd = getStandardDeviation(lastMeasurements);

            if (measurements.size() <= 2) {
                // not enough to average
                return;
            }

            Log.i(LOG_TAG_SERVICE, "Deviation got " + sd);

            // time to get the best value
            if (averaging) {

                Log.i(LOG_TAG_SERVICE, "Averaging ...");

                if (lastSD != null && sd >= lastSD && measurements.size() >= numMeasurements) {
                    // enough measurements + new noise = finalize
                    Log.i(LOG_TAG_SERVICE, "Got enough measurements, noise increasing");
                    ready = true;

                    // remove last noise
                    if (sd > lastSD && measurements.size() > numMeasurements) {
                        Log.i(LOG_TAG_SERVICE, "Remove last measurement " + value);
                        measurements.remove(measurements.size() -1 );
                    }

                    // final processing
                    lastMeasurements = getLastMeasurements();
                    averaged = getAverageAzimuthDegrees(lastMeasurements);
                    sd = getStandardDeviation(lastMeasurements);
                    Log.i(LOG_TAG_SERVICE, "Average " + averaged + " with deviation " + sd);

                    // remove noise
                    lastMeasurements = removeNoise(lastMeasurements, averaged, sd);

                    // average samples left
                    averaged = getAverageAzimuthDegrees(lastMeasurements);
                    lastSD = getStandardDeviation(lastMeasurements);
                    Log.i(LOG_TAG_SERVICE, "With noise reduction " + averaged + " with deviation " + lastSD);
                }
            }

            // prepare for next iteration
            lastSD = sd;

            if (measurements.size() > numMeasurements) {
                measurements.remove(0);
            }
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

    public List<Float> removeNoise(List<Float> aLastMeasurements, float anAveraged, float aDeviation) {
        List<Float> filtered = new ArrayList<>(aLastMeasurements);
        // remove up to 20 percent of the measurements
        int maxNumMeasurementsToRemove = (int) (numMeasurements * NOISE_COUNT_TRESHOLD);

        // that are more than 60% away from the average
        float minNoiseDistance = Math.abs(aDeviation * DISTANCE_TRESHOLD);

        for (int i=0; i< maxNumMeasurementsToRemove; i++) {
            // find farthest value
            float value = findMostDistantValue(filtered, anAveraged); // TODO

            // remove if distance above treshold
            float distance = getHalfDistance(anAveraged, value);
            if (distance > minNoiseDistance) {
                Log.i(LOG_TAG_SERVICE, "Removing noise " + value + " by distance " + distance);
                filtered.remove(value);
            } else {
                break;
            }
        }
        return filtered;
    }

    public float findMostDistantValue(List<Float> aMeasurements, float anAverage) {
        int biggestDistanceIndex = 0;
        float biggestDistance = -1;
        float currentDistance;
        for (int i=0; i<aMeasurements.size(); i++) {
            currentDistance = getHalfDistance(aMeasurements.get(i), anAverage);
            if (currentDistance > biggestDistance) {
                biggestDistance = currentDistance;
                biggestDistanceIndex = i;
            }
        }
        return aMeasurements.get(biggestDistanceIndex);
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

    public float getAverageAzimuthDegrees(List<Float> measurements) {

        float sum = 0, average;

        // do we need normalization?
        float max = findBiggestValue(measurements);
        float min = findSmallestValue(measurements);

        if (max - min < VALUE_AZIMUTH_180_DEGREES) {
            for(Float measurement : measurements) {
                sum += measurement;
            }

            average = sum / measurements.size();
            return average;
        } else {
            for (Float measurement : measurements) {
                if (measurement > VALUE_AZIMUTH_180_DEGREES) {
                    // make 180+ degrees negative
                    sum += (measurement - MAX_VALUE_AZIMUTH_DEGREES);
                } else {
                    sum += measurement;
                }
            }

            average = sum / measurements.size();
            return Math.abs(average);
        }
    }

    private float findBiggestValue(List<Float> measurements) {
        float max = 0;
        for (Float measurement : measurements) {
            if (measurement > max) {
                max = measurement;
            }
        }
        return max;
    }

    private float findSmallestValue(List<Float> measurements) {
        float min = MAX_VALUE_AZIMUTH_DEGREES;
        for (Float measurement : measurements) {
            if (measurement < min) {
                min = measurement;
            }
        }
        return min;
    }

    public float getStandardDeviation(List<Float> values) {
        float sum = 0f, standardDeviation = 0f;
        for(float value : values) {
            sum += value;
        }
        double mean = sum / values.size();
        for(double value: values) {
            standardDeviation += Math.pow(value - mean, 2);
        }
        return (float) Math.sqrt(standardDeviation / values.size());
    }

    public float getHalfDistance(float value1, float value2) {
        float average = getAverageAzimuthDegrees(value1, value2);
        return Math.min(Math.abs(average - value1), Math.abs(average - value2));
    }

    public String getAccuracyString() {
        if (averagingEnabled) {
            return " \u00B1" + formatter.format(lastSD) + "/" + numMeasurements;
        } else {
            return "";
        }
    }

    // last PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS measurements or less if not available yet
    private List<Float> getLastMeasurements() {
        int lastMeasurementsCount = Math.min(measurements.size(), numMeasurements);
        return measurements.subList(measurements.size() - lastMeasurementsCount, measurements.size());
    }

    public void resetMeasurements() {
        measurements.clear();
        averaged = 0;
        lastSD = null;
    }


    public List<Float> getMeasurements() {
        return measurements;
    }
}
