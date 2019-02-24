package com.astoev.cave.survey.test.map;

import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.model.Leg;

import junit.framework.TestCase;

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

    public void testGetMiddleAngle() {
        assertEquals(20f, MapUtilities.getMiddleAngle(10f, 30f));
        assertEquals(130f, MapUtilities.getMiddleAngle(50f, 210f));
        assertEquals(330f, MapUtilities.getMiddleAngle(340f, 320f));
        assertEquals(20f, MapUtilities.getMiddleAngle(340f, 60f));
    }

    public void testApplySlopeToDistance() {
        assertEquals(20f, MapUtilities.applySlopeToDistance(20f, null));
        assertEquals(20f, MapUtilities.applySlopeToDistance(20f, 0f));
        // TODO it seem bad values are produced below
//        assertEquals(10f, MapUtilities.applySlopeToDistance(20f, 45f));
//        assertEquals(10f, MapUtilities.applySlopeToDistance(20f, -45f));
//        assertEquals(0f, MapUtilities.applySlopeToDistance(20f, -90f));
//        assertEquals(0f, MapUtilities.applySlopeToDistance(20f, 90f));
    }

    public void testAddDegrees() {
        assertEquals(110f, MapUtilities.add90Degrees(20f));
        assertEquals(210f, MapUtilities.add90Degrees(120f));
        assertEquals(310f, MapUtilities.add90Degrees(220f));
        assertEquals(50f, MapUtilities.add90Degrees(320f));
    }

    public void testMinusDegrees() {
        assertEquals(10f, MapUtilities.minus90Degrees(100f));
        assertEquals(220f, MapUtilities.minus90Degrees(310f));
        assertEquals(330f, MapUtilities.minus90Degrees(60f));
    }

    public void testDegreeToGrads() {
        assertEquals(0f, MapUtilities.degreesToGrads(0f));
        assertEquals(50f, MapUtilities.degreesToGrads(45f), 0.001);
        assertEquals(100f, MapUtilities.degreesToGrads(90f), 0.001);
        assertEquals(200f, MapUtilities.degreesToGrads(180f), 0.001);
        assertEquals(400f, MapUtilities.degreesToGrads(360f), 0.001);
    }

    public void testCalculateTriangleAngle() {
        // equilateral
        assertEquals(60.0, MapUtilities.calculateTriangleAngle(1, 1, 1), 0.1);
        assertEquals(60.0, MapUtilities.calculateTriangleAngle(5, 5, 5), 0.1);
        assertEquals(60.0, MapUtilities.calculateTriangleAngle(3.6f, 3.6f, 3.6f), 1);

        // any + reversed direction
        assertEquals(75.522f, MapUtilities.calculateTriangleAngle(6, 7, 8), 0.001);
        assertEquals(75.522f, MapUtilities.calculateTriangleAngle(7, 6, 8), 0.001);
        assertEquals(46.567f, MapUtilities.calculateTriangleAngle(7, 8, 6), 0.001);
        assertEquals(46.567f, MapUtilities.calculateTriangleAngle(8, 7, 6), 0.001);

        assertEquals(28.388f, MapUtilities.calculateTriangleAngle(7.9f, 3.5f, 5.1f), 0.001);
        assertEquals(132.568f, MapUtilities.calculateTriangleAngle(3.5f, 5.1f, 7.9f), 0.001);
        assertEquals(19, MapUtilities.calculateTriangleAngle(7.9f, 5.1f, 3.5f), 0.1);

        // all 180 degrees
        assertEquals(180, MapUtilities.calculateTriangleAngle(6, 7, 8)
            + MapUtilities.calculateTriangleAngle(7, 8, 6)
            + MapUtilities.calculateTriangleAngle(8, 6, 7), 0.001);
    }

    public void testCalculateTriangleAzimuths() {
        Leg first = new Leg();
        first.setDistance(6f);
        first.setAzimuth(30f);

        Leg second = new Leg();
        second.setDistance(7f);

        Leg third = new Leg();
        third.setDistance(8f);

        // second angle
        float secondAzimuth = MapUtilities.calculateTriangleSecondLegAzimuth(first, second, third);
        assertEquals(first.getAzimuth() + 75.522, secondAzimuth, 0.001);

        // third angle
        second.setAzimuth(secondAzimuth);
        float thirdAzimuth = MapUtilities.calculateTriangleThirdLegAzimuth(first, second, third);
        assertEquals(secondAzimuth + 46.567, thirdAzimuth, 0.001);
    }

    public void testCalculateTriangleWithGrads() {
        fail("Not ready");
    }

    public void testCalculateTriangleWithInclination() {
        fail("Not ready");;
    }
}
