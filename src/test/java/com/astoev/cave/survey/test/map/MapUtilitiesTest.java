package com.astoev.cave.survey.test.map;

import com.astoev.cave.survey.activity.map.MapUtilities;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by astoev on 1/20/14.
 */
public class MapUtilitiesTest extends TestCase {

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

    public void testIsAzimuthInDegreesValid() {
        assertTrue(MapUtilities.isAzimuthInDegreesValid(0f));
        assertTrue(MapUtilities.isAzimuthInDegreesValid(20f));
        assertTrue(MapUtilities.isAzimuthInDegreesValid(20.1f));
        assertTrue(MapUtilities.isAzimuthInDegreesValid(355f));

        assertFalse(MapUtilities.isAzimuthInDegreesValid(-1f));
        assertFalse(MapUtilities.isAzimuthInDegreesValid(360f));
        assertFalse(MapUtilities.isAzimuthInDegreesValid(365f));
    }

    public void testIsAzimuthInGradsValid() {
        assertTrue(MapUtilities.isAzimuthInGradsValid(0f));
        assertTrue(MapUtilities.isAzimuthInGradsValid(20f));
        assertTrue(MapUtilities.isAzimuthInGradsValid(20.1f));
        assertTrue(MapUtilities.isAzimuthInGradsValid(375f));

        assertFalse(MapUtilities.isAzimuthInGradsValid(-1f));
        assertFalse(MapUtilities.isAzimuthInGradsValid(400f));
        assertFalse(MapUtilities.isAzimuthInGradsValid(420f));
    }

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

}
