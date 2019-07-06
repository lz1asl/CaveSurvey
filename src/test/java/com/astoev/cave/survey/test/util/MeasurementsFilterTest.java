package com.astoev.cave.survey.test.util;

import com.astoev.cave.survey.service.orientation.MeasurementsFilter;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Arrays;

public class MeasurementsFilterTest extends TestCase {

    @Test
    public void testAveragingDisabled() {
        // filter without averaging
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(false);
        assertFalse(filter.isReady());

        // value returned as is
        float value1 = 1.23F;
        filter.addMeasurement(value1);
        assertTrue(filter.isReady());
        assertEquals(value1, filter.getValue());
        assertEquals( "(0.0 out of 1)", filter.getAccuracyString());

        // last value always used
        float value2 = 2.34F;
        filter.addMeasurement(value2);
        assertTrue(filter.isReady());
        assertEquals(value2, filter.getValue());
        assertEquals( "(0.0 out of 1)", filter.getAccuracyString());

        // averaging 1 out of 1
        filter.startAveraging();
        filter.addMeasurement(value1);
        assertTrue(filter.isReady());
        assertEquals(value1, filter.getValue());
        assertEquals( "(0.0 out of 1)", filter.getAccuracyString());
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
        assertEquals( "(0.5 out of 2)", filter.getAccuracyString());
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
        assertEquals( "(1.0 out of 2)", filter.getAccuracyString());
    }

    @Test
    public void test360Degrees() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(2);
        filter.startAveraging();

        filter.addMeasurement(355f);
        filter.addMeasurement(15f);
        assertEquals(5, filter.getValue(), 0.0);

        filter.addMeasurement(45f);
        filter.addMeasurement(47f);
        assertEquals(46, filter.getValue(), 0.0);

        filter.addMeasurement(89f);
        filter.addMeasurement(91f);
        assertEquals(90, filter.getValue(), 0.0);

        filter.addMeasurement(145f);
        filter.addMeasurement(147f);
        assertEquals(146, filter.getValue(), 0.0);

        filter.addMeasurement(229f);
        filter.addMeasurement(231f);
        assertEquals(230, filter.getValue(), 0.0);

        filter.addMeasurement(355f);
        filter.addMeasurement(5f);
        assertEquals(0, filter.getValue(), 0.0);
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
        assertEquals(3, filter.getValue(), 0.0);
        assertEquals( "(2.0 out of 5)", filter.getAccuracyString());
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
        assertEquals(130f, filter.getAverageAzimuthDegrees(Arrays.asList(120f, 130f, 140f)), 0.00f);
        assertEquals(2.5f, filter.getAverageAzimuthDegrees(Arrays.asList(2f, 2f, 4f)), 0.00f);
        assertEquals(4f, filter.getAverageAzimuthDegrees(Arrays.asList(2f, 2f, 6f)), 0.00f);
        assertEquals(2f, filter.getAverageAzimuthDegrees(Arrays.asList(1f, 1f, 4f)), 0.00f);

    }

}
