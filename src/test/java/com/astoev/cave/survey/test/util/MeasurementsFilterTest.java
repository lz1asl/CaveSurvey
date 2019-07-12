package com.astoev.cave.survey.test.util;

import com.astoev.cave.survey.service.orientation.MeasurementsFilter;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MeasurementsFilterTest extends TestCase {

    @Test
    public void testAveragingDisabled() {
        // filter without averaging
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(false);
        assertFalse(filter.isReady());

        // value returned as is
        float value1 = 1.23F;
        filter.startAveraging();
        filter.addMeasurement(value1);
        assertTrue(filter.isReady());
        assertEquals(value1, filter.getValue());
        assertEquals( "", filter.getAccuracyString());

        // last value always used
        float value2 = 2.34F;
        filter.addMeasurement(value2);
        assertTrue(filter.isReady());
        assertEquals(value2, filter.getValue());
        assertEquals( "", filter.getAccuracyString());

        // averaging 1 out of 1
        filter.startAveraging();
        filter.addMeasurement(value1);
        assertTrue(filter.isReady());
        assertEquals(value1, filter.getValue());
        assertEquals( "", filter.getAccuracyString());
    }

    @Test
    public void testAveragingEnabled() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(2);
        filter.startAveraging();

        filter.addMeasurement(1f);
        filter.addMeasurement(2f);
        assertTrue(filter.isReady());
        assertEquals(1.5, filter.getValue(), 0.0);
        assertEquals( " ±0.50/2", filter.getAccuracyString());
    }

    @Test
    public void testPlusMinus() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(2);
        filter.startAveraging();

        filter.addMeasurement(-1f);
        filter.addMeasurement(1f);
        assertEquals(0, filter.getValue(), 0);
        assertEquals( " ±1.00/2", filter.getAccuracyString());
    }

    @Test
    public void test360Degrees() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(2);
        filter.startAveraging();

        filter.addMeasurement(355f);
        filter.addMeasurement(15f);
        assertEquals(5f, filter.getValue(), 0.0f);

        filter.resetMeasurements();
        filter.addMeasurement(45f);
        filter.addMeasurement(47f);
        assertEquals(46f, filter.getValue(), 0.0f);

        filter.resetMeasurements();
        filter.addMeasurement(89f);
        filter.addMeasurement(91f);
        assertEquals(90f, filter.getValue(), 0.0f);

        filter.resetMeasurements();
        filter.addMeasurement(145f);
        filter.addMeasurement(147f);
        assertEquals(146f, filter.getValue(), 0.0f);

        filter.resetMeasurements();
        filter.addMeasurement(229f);
        filter.addMeasurement(231f);
        assertEquals(230f, filter.getValue(), 0.0f);

        filter.resetMeasurements();
        filter.addMeasurement(355f);
        filter.addMeasurement(5f);
        assertEquals(0f, filter.getValue(), 0.0f);
    }

    @Test
    public void testMultiple() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(5);
        filter.startAveraging();

        filter.addMeasurement(1f);
        filter.addMeasurement(2f);
        filter.addMeasurement(3f);
        filter.addMeasurement(4f);
        filter.addMeasurement(5f);
        assertEquals(5, filter.getMeasurements().size());
        assertEquals(3f, filter.getValue(), 0.0f);
        assertEquals( " ±2.00/2", filter.getAccuracyString());
    }

    @Test
    public void testTwoAzimuthsAverage() {
        MeasurementsFilter filter = new MeasurementsFilter();
        assertEquals(2f, filter.getAverageAzimuthDegrees(2f, 2f), 0.00f);
        assertEquals(10f, filter.getAverageAzimuthDegrees(0f, 20f), 0.00f);
        assertEquals(1.5f, filter.getAverageAzimuthDegrees(1f, 2f), 0.00f);
        assertEquals(330f, filter.getAverageAzimuthDegrees(320f, 340f), 0.00f);
        assertEquals(110f, filter.getAverageAzimuthDegrees(120f, 100f), 0.00f);
        assertEquals(110f, filter.getAverageAzimuthDegrees(120f, 100f), 0.00f);
        assertEquals(0f, filter.getAverageAzimuthDegrees(355f, 5f), 0.00f);
        assertEquals(356f, filter.getAverageAzimuthDegrees(350f, 2f), 0.00f);
        assertEquals(1f, filter.getAverageAzimuthDegrees(355f, 7f), 0.00f);
    }

    @Test
    public void testMultipleAzimuthsAverage() {
        MeasurementsFilter filter = new MeasurementsFilter();
        assertEquals(2f, filter.getAverageAzimuthDegrees(Arrays.asList(2f)), 0.00f);
        assertEquals(2f, filter.getAverageAzimuthDegrees(Arrays.asList(2f, 2f)), 0.00f);
        assertEquals(2f, filter.getAverageAzimuthDegrees(Arrays.asList(2f, 2f, 2f)), 0.00f);
        assertEquals(2f, filter.getAverageAzimuthDegrees(Arrays.asList(2f, 2f, 2f, 2f)), 0.00f);
        assertEquals(2.5f, filter.getAverageAzimuthDegrees(Arrays.asList(1f, 2f, 3f, 4f)), 0.00f);
        assertEquals(0f, filter.getAverageAzimuthDegrees(Arrays.asList(350f, 0f, 10f)), 0.00f);
        assertEquals(0f, filter.getAverageAzimuthDegrees(Arrays.asList(350f, 355f, 0f, 5f, 10f)), 0.00f);
        assertEquals(130f, filter.getAverageAzimuthDegrees(Arrays.asList(120f, 130f, 140f)), 0.00f);
        assertEquals(2.66f, filter.getAverageAzimuthDegrees(Arrays.asList(2f, 2f, 4f)), 0.01f);
        assertEquals(3.33f, filter.getAverageAzimuthDegrees(Arrays.asList(2f, 2f, 6f)), 0.01f);
        assertEquals(2f, filter.getAverageAzimuthDegrees(Arrays.asList(1f, 1f, 4f)), 0.00f);
        assertEquals(1.7f, filter.getAverageAzimuthDegrees(Arrays.asList(1.5f, 2f, 2.1f, 1.2f, 1.7f)), 0.00f);
    }

    @Test
    public void testNoiseReduction() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setNumMeasurements(10);
        float avg = 1f;
        float dev = 1f;
        // fewer values unchanged
        assertThat(filter.removeNoise(Arrays.asList(1f), avg, dev), is(Arrays.asList(1f)));
        assertThat(filter.removeNoise(Arrays.asList(1f, 1f, 1f, 1f, 1f), avg, dev), is(Arrays.asList(1f, 1f, 1f, 1f, 1f)));
        assertThat(filter.removeNoise(Arrays.asList(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f), avg, dev),
                is(Arrays.asList(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)));
        assertThat(filter.removeNoise(Arrays.asList(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f), avg, dev),
                is(Arrays.asList(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)));
        // distant values removed
        assertThat(filter.removeNoise(Arrays.asList(4f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f), avg, dev),
                is(Arrays.asList(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)));
        // close values kept
        assertThat(filter.removeNoise(Arrays.asList(2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f), avg, dev),
                is(Arrays.asList(2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)));
        // only most extreme removed
        assertThat(filter.removeNoise(Arrays.asList(1f, 2f, 6f, 1f, 8f, 1f, 1f, 4f, 1f, 1f), avg, dev),
                is(Arrays.asList(1f, 2f, 1f, 1f, 1f, 1f, 4f, 1f, 1f)));
        assertThat(filter.removeNoise(Arrays.asList(4f, 1f, -5f, 1f, 3f, 1f, 1f, 1f, 1f, 1f), avg, dev),
                is(Arrays.asList(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)));
    }

    @Test
    public void testMaxDeviation() {
        MeasurementsFilter filter = new MeasurementsFilter();
        assertEquals(0, filter.getDeviation(Arrays.asList(1f), 1f), 0.00f);
        assertEquals(0, filter.getDeviation(Arrays.asList(1f, 1f, 1f), 1f), 0.00f);
        assertEquals(1, filter.getDeviation(Arrays.asList(1f, 2f, 3f), 2f), 0.00f);
        assertEquals(2, filter.getDeviation(Arrays.asList(1f, 2f, 3f, 4f, 5f), 3f), 0.00f);
        assertEquals(12, filter.getDeviation(Arrays.asList(1f, 2f, 3f, 4f, 15f), 3f), 0.00f);
        assertEquals(2, filter.getDeviation(Arrays.asList(1f, 2f, 3f, 4f, 5f), 3f), 0.00f);
    }


    @Test
    public void testGetHalfDistance() {
        MeasurementsFilter filter = new MeasurementsFilter();
        assertEquals(1, filter.getHalfDistance(0, 2), 0.0f);
        assertEquals(0.5, filter.getHalfDistance(1, 2), 0.0f);
        assertEquals(2, filter.getHalfDistance(350, 354), 0.0f);
        assertEquals(5, filter.getHalfDistance(355, 5), 0.0f);
    }

    @Test
    public void testContinueSameDirection() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(2);
        filter.startAveraging();

        filter.addMeasurement(1f);
        filter.addMeasurement(2f);
        filter.addMeasurement(1.5f);
        assertTrue(filter.isReady());
        assertEquals(1.5f, filter.getValue(), 0.0f);
        assertEquals( " ±0.50/2", filter.getAccuracyString());
        assertThat(filter.getMeasurements(), is(Arrays.asList(2f, 1.5f)));
    }


    @Test
    public void testStopOnNoise() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(2);
        filter.startAveraging();

        filter.addMeasurement(1f);
        filter.addMeasurement(2f);
        filter.addMeasurement(4f);
        assertTrue(filter.isReady());
        assertEquals(1.5f, filter.getValue(), 0.0f);
        assertEquals( " ±0.50/2", filter.getAccuracyString());
        assertThat(filter.getMeasurements(), is(Arrays.asList(1f, 2f)));
    }

}
