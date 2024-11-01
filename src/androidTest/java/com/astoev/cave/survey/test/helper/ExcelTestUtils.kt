package com.astoev.cave.survey.test.helper

import com.astoev.cave.survey.model.Option
import com.astoev.cave.survey.service.imp.LegData
import com.astoev.cave.survey.service.imp.ProjectData
import org.junit.Assert
import org.junit.Assert.assertNull

object ExcelTestUtils {
    fun assertLegLocation(aLeg: LegData, aLat: Double?, aLon: Double?, anAlt: Double?, anAccuracy: Double?) {
        Assert.assertNotNull(aLeg)
        if (aLat == null) {
            assertNull(aLeg.alt)
        } else {
            Assert.assertEquals(aLat, aLeg.lat, 0.000002)
        }
        if (aLon == null) {
            assertNull(aLeg.lon)
        } else {
            Assert.assertEquals(aLon, aLeg.lon, 0.000002)
        }
        if (anAlt == null) {
            assertNull(aLeg.alt)
        } else {
            Assert.assertEquals(anAlt, aLeg.alt, 0.01)
        }
        if (anAccuracy == null) {
            assertNull(aLeg.accuracy)
        } else {
            Assert.assertEquals(anAccuracy, aLeg.accuracy, 0.1)
        }
    }

    @JvmOverloads
    fun assertLeg(aLeg: LegData, aDistance: Float?, anAzimuth: Float?, aSlope: Float?,
                  up: Float? = null, down: Float? = null, left: Float? = null, right: Float? = null) {
        Assert.assertNotNull(aLeg)
        Assert.assertEquals(aDistance, aLeg.length)
        Assert.assertEquals(anAzimuth, aLeg.azimuth)
        Assert.assertEquals(aSlope, aLeg.slope)
        Assert.assertEquals(up, aLeg.up)
        Assert.assertEquals(down, aLeg.down)
        Assert.assertEquals(left, aLeg.left)
        Assert.assertEquals(right, aLeg.right)
    }

    fun assertLeg(aLeg: LegData, aGaleryFrom: String?, aPointFrom: String?, aGalleryTo: String?,
                  aPointTo: String?, isMiddle: Boolean, isVector: Boolean) {
        Assert.assertEquals(aGaleryFrom, aLeg.fromGallery)
        Assert.assertEquals(aPointFrom, aLeg.fromPoint)
        Assert.assertEquals(aGalleryTo, aLeg.toGallery)
        Assert.assertEquals(aPointTo, aLeg.toPoint)
        Assert.assertEquals(isMiddle, aLeg.isMiddlePoint)
        Assert.assertEquals(isVector, aLeg.isVector)
    }

    fun assertDefaultUnits(aData: ProjectData) {
        assertConfigUnits(aData, Option.UNIT_METERS, Option.UNIT_DEGREES, Option.UNIT_DEGREES)
    }

    fun assertConfigUnits(aData: ProjectData, distanceUnit: String?, azimuthUni: String?, slopeUnit: String?) {
        assertConfig(aData, Option.CODE_DISTANCE_UNITS, distanceUnit)
        assertConfig(aData, Option.CODE_AZIMUTH_UNITS, azimuthUni)
        assertConfig(aData, Option.CODE_SLOPE_UNITS, slopeUnit)
    }

    fun assertConfig(aData: ProjectData, aProperty: String?, anExpectedValue: String?) {
        val actualValue = aData.options[aProperty]
        Assert.assertEquals(anExpectedValue, actualValue)
    }
}