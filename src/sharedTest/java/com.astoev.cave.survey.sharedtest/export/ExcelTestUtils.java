package com.astoev.cave.survey.sharedtest.export;

import com.astoev.cave.survey.service.imp.LegData;
import com.astoev.cave.survey.service.imp.ProjectData;

import static com.astoev.cave.survey.model.Option.CODE_AZIMUTH_UNITS;
import static com.astoev.cave.survey.model.Option.CODE_DISTANCE_UNITS;
import static com.astoev.cave.survey.model.Option.CODE_SLOPE_UNITS;
import static com.astoev.cave.survey.model.Option.UNIT_DEGREES;
import static com.astoev.cave.survey.model.Option.UNIT_METERS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExcelTestUtils {


    public static void assertLegLocation(LegData aLeg, Float aLat, Float aLon, Float anAlt, Float anAccuracy) {
        assertNotNull(aLeg);
        assertEquals(aLat, aLeg.getLat());
        assertEquals(aLon, aLeg.getLon());
        assertEquals(anAlt, aLeg.getAlt());
        assertEquals(anAccuracy, aLeg.getAccuracy());
    }

    public static void assertLeg(LegData aLeg, Float aDistance, Float anAzimuth, Float aSlope) {
        assertLeg(aLeg, aDistance, anAzimuth, aSlope, null, null, null, null);
    }

    public static void assertLeg(LegData aLeg, Float aDistance, Float anAzimuth, Float aSlope,
                                 Float up, Float down, Float left, Float right) {
        assertNotNull(aLeg);
        assertEquals(aDistance, aLeg.getLength());
        assertEquals(anAzimuth, aLeg.getAzimuth());
        assertEquals(aSlope, aLeg.getSlope());
        assertEquals(up, aLeg.getUp());
        assertEquals(down, aLeg.getDown());
        assertEquals(left, aLeg.getLeft());
        assertEquals(right, aLeg.getRight());
    }

    public static void assertLeg(LegData aLeg, String aGaleryFrom, String aPointFrom, String aGalleryTo,
                                 String aPointTo, boolean isMiddle, boolean isVector) {
        assertEquals(aGaleryFrom, aLeg.getFromGallery());
        assertEquals(aPointFrom, aLeg.getFromPoint());
        assertEquals(aGalleryTo, aLeg.getToGallery());
        assertEquals(aPointTo, aLeg.getToPoint());
        assertEquals(isMiddle, aLeg.isMiddlePoint());
        assertEquals(isVector, aLeg.isVector());
    }

    public static void assertDefaultUnits(ProjectData aData) {
        assertConfigUnits(aData, UNIT_METERS, UNIT_DEGREES, UNIT_DEGREES);
    }

    public static void assertConfigUnits(ProjectData aData, String distanceUnit, String azimuthUni, String slopeUnit) {
        assertConfig(aData, CODE_DISTANCE_UNITS, distanceUnit);
        assertConfig(aData, CODE_AZIMUTH_UNITS, azimuthUni);
        assertConfig(aData, CODE_SLOPE_UNITS, slopeUnit);

    }

    public static void assertConfig(ProjectData aData, String aProperty, String anExpectedValue) {
        String actualValue = aData.getOptions().get(aProperty);
        assertEquals(anExpectedValue, actualValue);
    }
}