package com.astoev.cave.survey.test.helper

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.astoev.cave.survey.R.id
import com.astoev.cave.survey.model.Option
import com.astoev.cave.survey.test.helper.Common.click
import com.astoev.cave.survey.test.helper.Common.clickDialogSpinnerAtPosition
import com.astoev.cave.survey.test.helper.Gallery.createClassicGallery
import com.astoev.cave.survey.test.helper.Gallery.createGallery
import org.hamcrest.Matchers
import java.lang.Thread.sleep

object Survey {
    @JvmOverloads
    fun createSurvey(aName: String?, importFile: Boolean = false,
                     distanceUnits: String? = null, azimuthUnits: String? = null, slopeUnits: String? = null) {
        // open new survey screen
        click(id.action_new_project)
        if (importFile) {
            selectLastImport()
        }

        // enter name
        Common.type(id.new_projectname, aName)
        if (distanceUnits != null) {
            selectDistanceUnits(distanceUnits)
        }
        if (azimuthUnits != null) {
            selectAzimuthUnits(azimuthUnits)
        }
        if (slopeUnits != null) {
            selectSlopeUnits(slopeUnits)
        }

        // save
        click(id.new_action_create)
    }

    private fun selectSlopeUnits(aSlopeUnits: String) {
        click(id.options_units_slope)
        when (aSlopeUnits) {
            Option.UNIT_DEGREES -> {
                clickDialogSpinnerAtPosition(0)
                return
            }
            Option.UNIT_GRADS -> {
                clickDialogSpinnerAtPosition(1)
                return
            }
            else -> throw RuntimeException()
        }
    }

    private fun selectAzimuthUnits(aAzimuthUnits: String) {
        click(id.options_units_azimuth)
        when (aAzimuthUnits) {
            Option.UNIT_DEGREES -> {
                clickDialogSpinnerAtPosition(0)
                return
            }
            Option.UNIT_GRADS -> {
                clickDialogSpinnerAtPosition(1)
                return
            }
            else -> throw RuntimeException()
        }
    }

    private fun selectDistanceUnits(aDistanceUnits: String) {
        click(id.options_units_distance)
        when (aDistanceUnits) {
            Option.UNIT_METERS -> {
                clickDialogSpinnerAtPosition(0)
                return
            }
            Option.UNIT_FEET -> {
                clickDialogSpinnerAtPosition(1)
                return
            }
            else -> throw RuntimeException()
        }
    }

    fun selectLastImport() {
        click(id.import_files)
        Espresso.onData(Matchers.anything()).atPosition(Data.xlsExportFilesCount)
                .perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onIdle()
    }

    fun openSurvey(aName: String?) {
        click(aName)
    }

    @JvmOverloads
    fun createAndOpenSurvey(importFile: Boolean = false, distanceUnits: String? = null, azimuthUnits: String? = null, slopeUnits: String? = null): String {
        val surveyName = "" + System.currentTimeMillis()
        Home.goHome()
        createSurvey(surveyName, importFile, distanceUnits, azimuthUnits, slopeUnits)
        createClassicGallery()
        Common.goBack()
        openSurvey(surveyName)
        return surveyName
    }

    @JvmOverloads
    fun createAndOpenGeolocationSurvey(importFile: Boolean = false, distanceUnits: String? = null, azimuthUnits: String? = null, slopeUnits: String? = null): String {
        val surveyName = "" + System.currentTimeMillis()
        Home.goHome()
        createSurvey(surveyName, importFile, distanceUnits, azimuthUnits, slopeUnits)
        createGallery()
        return surveyName
    }

    @JvmOverloads
    fun addLeg(length: Float, azimuth: Float, slope: Float?, up: Float? = null, down: Float? = null, left: Float? = null, rigt: Float? = null) {
        // press new
        click(id.main_action_add)

        // select the leg option
        clickDialogSpinnerAtPosition(0)
        setLegData(length, azimuth, slope, up, down, left, rigt)
    }

    fun addLegMiddle(distance: Float, up: Float, down: Float, left: Float, right: Float) {
        // press new
        click(id.main_action_add)

        // select leg
        clickDialogSpinnerAtPosition(2)

        // middle at
        Common.type(id.middle_distance, distance)

        // save
        click(id.middle_create)

        // populate
        setLegData(null, null, null, up, down, left, right)
    }

    fun selectFirstSurveyLeg() {
        click(id.main_action_add)
    }

    fun setLegData(length: Float?, azimuth: Float?, slope: Float?) {
        setLegData(length, azimuth, slope, null, null, null, null)
    }

    fun setLegData(length: Float?, azimuth: Float?, slope: Float?, up: Float?, down: Float?, left: Float?, right: Float?) {
        // populate
        Common.type(id.point_distance, length)
        Common.type(id.point_azimuth, azimuth)
        Common.type(id.point_slope, slope)
        Common.type(id.point_up, up)
        Common.type(id.point_down, down)
        Common.type(id.point_left, left)
        Common.type(id.point_right, right)

        // save
        saveLeg()
    }

    fun addVector(aDistance: Float, anAzimuth: Float, aSlope: Float) {
        Common.openContextMenu()

        // select leg
        click("Add Vector")

        sleep(1000);

        // set data
        Common.type(id.vector_distance, aDistance)
        Common.type(id.vector_azimuth, anAzimuth)
        Common.type(id.vector_slope, aSlope)

        // save
        click(id.vector_add)
    }

    fun addCoordinate(lat: Float, lon: Float, altitude: Int, precision: Int) {
        Common.openContextMenu()
        click("GPS")
        click("GPS Manual")
        Common.type(id.gps_manual_latitude, lat)
        Common.type(id.gps_manual_longitude, lon)
        Common.type(id.gps_manual_altitude, altitude)
        Common.type(id.gps_manual_accuracy, precision)
        click(id.gps_action_save)
        saveLeg()
    }

    fun nextGallery() {
        // press new
        click(id.main_action_add)

        // select gallery
        clickDialogSpinnerAtPosition(1)

        click(id.new_gallery_create);
    }

    fun openLegWithText(text: String?) {
        Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.click())
    }

    fun saveLeg() {
        click(id.point_action_save)
    }
}