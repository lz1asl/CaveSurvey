package com.astoev.cave.survey.test.util;

import com.astoev.cave.survey.service.orientation.MeasurementsFilter;

import junit.framework.TestCase;

import org.junit.Test;

public class MeasurementsFilterTest extends TestCase {

    @Test
    public void testAveragingDisabled() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(false);

        assertFalse(filter.isReady());

        float value1 = 1.23F;
        filter.addMeasurement(value1);
        assertTrue(filter.isReady());
        assertEquals(1, filter.getMeasurements().size());
        assertEquals(value1, filter.getMeasurements().get(0));
        assertEquals(value1, filter.getLastValue());
        assertEquals(value1, filter.getAverage());
        assertEquals(0, filter.getDeviation(), 0.0);

        float value2 = 2.34F;
        filter.addMeasurement(value2);
        assertTrue(filter.isReady());
        assertEquals(1, filter.getMeasurements().size());
        assertEquals(value2, filter.getMeasurements().get(0));
        assertEquals(value2, filter.getLastValue());
        assertEquals(value2, filter.getAverage());
        assertEquals(0, filter.getDeviation(), 0.0);
    }

    @Test
    public void testAveragingEnabled() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(2);

        filter.addMeasurement(1f);
        filter.addMeasurement(2f);
        assertTrue(filter.isReady());
        assertEquals(2, filter.getNumMeasurements());
        assertEquals(1.5, filter.getAverage(), 0.0);
        assertEquals(1, filter.getDeviation(), 0.0);
    }

    @Test
    public void testPlusMinus() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(2);

        filter.addMeasurement(-1f);
        filter.addMeasurement(1f);
        assertEquals(0, filter.getAverage(), 0);
    }

    @Test
    public void test360Degrees() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(2);

        filter.addMeasurement(355f);
        filter.addMeasurement(15f);
        assertEquals(5, filter.getAverage(), 0.0);
    }

    @Test
    public void testMultiple() {
        MeasurementsFilter filter = new MeasurementsFilter();
        filter.setAveragingEnabled(true);
        filter.setNumMeasurements(5);

        filter.addMeasurement(1f);
        filter.addMeasurement(2f);
        filter.addMeasurement(3f);
        filter.addMeasurement(4f);
        filter.addMeasurement(5f);
        assertEquals(3, filter.getAverage(), 0.0);
        assertEquals(4, filter.getDeviation(), 0.0);
    }

}
