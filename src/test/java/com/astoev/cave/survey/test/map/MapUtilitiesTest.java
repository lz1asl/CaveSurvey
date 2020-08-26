package com.astoev.cave.survey.test.map;

import com.astoev.cave.survey.activity.map.MapUtilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Created by astoev on 1/20/14.
 */
public class MapUtilitiesTest {

    @Test
    public void testGetNextGalleryColor() {
        int count = (int) (Math.random() * 5);
        int initial = MapUtilities.getNextGalleryColor(count);

        for (int i=0; i<3; i++) {
            // predictable for same input
            assertEquals(initial, MapUtilities.getNextGalleryColor(count));

            // different for new input
            assertNotSame(initial, MapUtilities.getNextGalleryColor(i));
        }
    }

    @Test
    public void testIsAzimuthInDegreesValid() {
        assertTrue(MapUtilities.isAzimuthInDegreesValid(0f));
        assertTrue(MapUtilities.isAzimuthInDegreesValid(20f));
        assertTrue(MapUtilities.isAzimuthInDegreesValid(20.1f));
        assertTrue(MapUtilities.isAzimuthInDegreesValid(355f));

        assertFalse(MapUtilities.isAzimuthInDegreesValid(-1f));
        assertFalse(MapUtilities.isAzimuthInDegreesValid(360f));
        assertFalse(MapUtilities.isAzimuthInDegreesValid(365f));
    }

    @Test
    public void testIsAzimuthInGradsValid() {
        assertTrue(MapUtilities.isAzimuthInGradsValid(0f));
        assertTrue(MapUtilities.isAzimuthInGradsValid(20f));
        assertTrue(MapUtilities.isAzimuthInGradsValid(20.1f));
        assertTrue(MapUtilities.isAzimuthInGradsValid(375f));

        assertFalse(MapUtilities.isAzimuthInGradsValid(-1f));
        assertFalse(MapUtilities.isAzimuthInGradsValid(400f));
        assertFalse(MapUtilities.isAzimuthInGradsValid(420f));
    }

    @Test
    public void testIsSlopeInDegreesValid() {
        assertTrue(MapUtilities.isSlopeInDegreesValid(0f));
        assertTrue(MapUtilities.isSlopeInDegreesValid(-2.5f));
        assertTrue(MapUtilities.isSlopeInDegreesValid(5.2f));
        assertTrue(MapUtilities.isSlopeInDegreesValid(90f));
        assertTrue(MapUtilities.isSlopeInDegreesValid(-90f));

        assertFalse(MapUtilities.isSlopeInDegreesValid(-91f));
        assertFalse(MapUtilities.isSlopeInDegreesValid(-100f));
        assertFalse(MapUtilities.isSlopeInDegreesValid(+91f));
        assertFalse(MapUtilities.isSlopeInDegreesValid(+100f));
    }

    @Test
    public void testIsSlopeInGradsValid() {
        assertTrue(MapUtilities.isSlopeInGradsValid(0f));
        assertTrue(MapUtilities.isSlopeInGradsValid(-2.5f));
        assertTrue(MapUtilities.isSlopeInGradsValid(5.2f));
        assertTrue(MapUtilities.isSlopeInGradsValid(100f));
        assertTrue(MapUtilities.isSlopeInGradsValid(-100f));

        assertFalse(MapUtilities.isSlopeInGradsValid(-101f));
        assertFalse(MapUtilities.isSlopeInGradsValid(-120f));
        assertFalse(MapUtilities.isSlopeInGradsValid(+101f));
        assertFalse(MapUtilities.isSlopeInGradsValid(+120f));
    }

    @Test
    public void testGetMiddleAngle() {
        assertEquals(20f, MapUtilities.getMiddleAngle(10f, 30f), 0);
        assertEquals(130f, MapUtilities.getMiddleAngle(50f, 210f), 0);
        assertEquals(330f, MapUtilities.getMiddleAngle(340f, 320f), 0);
        assertEquals(20f, MapUtilities.getMiddleAngle(340f, 60f), 0);
    }

    @Test
    public void testApplySlopeToDistance() {
        assertEquals(20f, MapUtilities.applySlopeToDistance(20f, null), 0);
        assertEquals(20f, MapUtilities.applySlopeToDistance(20f, 0f), 0);
        // TODO it seem bad values are produced below
//        assertEquals(10f, MapUtilities.applySlopeToDistance(20f, 45f));
//        assertEquals(10f, MapUtilities.applySlopeToDistance(20f, -45f));
//        assertEquals(0f, MapUtilities.applySlopeToDistance(20f, -90f));
//        assertEquals(0f, MapUtilities.applySlopeToDistance(20f, 90f));
    }

    @Test
    public void testAddDegrees() {
        assertEquals(110f, MapUtilities.add90Degrees(20f), 0);
        assertEquals(210f, MapUtilities.add90Degrees(120f), 0);
        assertEquals(310f, MapUtilities.add90Degrees(220f), 0);
        assertEquals(50f, MapUtilities.add90Degrees(320f), 0);
    }

    @Test
    public void testMinusDegrees() {
        assertEquals(10f, MapUtilities.minus90Degrees(100f), 0);
        assertEquals(220f, MapUtilities.minus90Degrees(310f), 0);
        assertEquals(330f, MapUtilities.minus90Degrees(60f), 0);
    }

    @Test
    public void testDegreeToGrads() {
        assertEquals(0f, MapUtilities.degreesToGrads(0f), 0);
        assertEquals(50f, MapUtilities.degreesToGrads(45f), 0.001);
        assertEquals(100f, MapUtilities.degreesToGrads(90f), 0.001);
        assertEquals(200f, MapUtilities.degreesToGrads(180f), 0.001);
        assertEquals(400f, MapUtilities.degreesToGrads(360f), 0.001);
    }

    @Test
    public void testFeetsConversion() {
        assertEquals(null, MapUtilities.getFeetsInMeters(null));
        assertEquals(0.3048f, MapUtilities.getFeetsInMeters(1f), 0.0001);
        assertEquals(3.6951f, MapUtilities.getFeetsInMeters(12.123f), 0.0001);

        assertEquals(null, MapUtilities.getMetersInFeet(null));
        assertEquals(1, MapUtilities.getMetersInFeet(0.3048f), 0.0001f);
        assertEquals(10, MapUtilities.getMetersInFeet(3.048f), 0.0001f);
    }

}
