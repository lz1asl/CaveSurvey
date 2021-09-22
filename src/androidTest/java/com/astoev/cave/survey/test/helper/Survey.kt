package com.astoev.cave.survey.test.helper

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.astoev.cave.survey.Constants
import com.astoev.cave.survey.R
import com.astoev.cave.survey.R.id
import com.astoev.cave.survey.activity.home.NewProjectActivity
import com.astoev.cave.survey.model.Option
import com.astoev.cave.survey.test.helper.Common.click
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import java.lang.Thread.sleep


object Survey {
    @JvmOverloads
    fun createSurvey(aName: String?, importFile: String? = null,
                     distanceUnits: String? = null, azimuthUnits: String? = null, slopeUnits: String? = null) {
        // open new survey screen
        Common.click(id.action_new_project)
        if (importFile != null) {
            Common.click(id.import_files)
            onData(allOf(Matchers.`is`(instanceOf(NewProjectActivity.ImportFile::class.java)),
                    object : BaseMatcher<NewProjectActivity.ImportFile>() {
                        override fun matches(item: Any?): Boolean {
                            return item.toString().startsWith(importFile)
                        }

                        override fun describeTo(description: Description?) {}
                    }))
                    .perform(ViewActions.click())

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


        // save & go back
        Common.click(id.new_action_create)
        if (importFile == null) {
            Espresso.onView(ViewMatchers.withId(id.point_main_view)).perform(ViewActions.pressBack())
        }
    }

    private fun selectSlopeUnits(aSlopeUnits: String) {
        Common.click(id.options_units_slope)
        when (aSlopeUnits) {
            Option.UNIT_DEGREES -> {
                Common.clickDialogSpinnerAtPosition(0)
                return
            }
            Option.UNIT_GRADS -> {
                Common.clickDialogSpinnerAtPosition(1)
                return
            }
            else -> throw RuntimeException()
        }
    }

    private fun selectAzimuthUnits(aAzimuthUnits: String) {
        Common.click(id.options_units_azimuth)
        when (aAzimuthUnits) {
            Option.UNIT_DEGREES -> {
                Common.clickDialogSpinnerAtPosition(0)
                return
            }
            Option.UNIT_GRADS -> {
                Common.clickDialogSpinnerAtPosition(1)
                return
            }
            else -> throw RuntimeException()
        }
    }

    private fun selectDistanceUnits(aDistanceUnits: String) {
        Common.click(id.options_units_distance)
        when (aDistanceUnits) {
            Option.UNIT_METERS -> {
                Common.clickDialogSpinnerAtPosition(0)
                return
            }
            Option.UNIT_FEET -> {
                Common.clickDialogSpinnerAtPosition(1)
                return
            }
            else -> throw RuntimeException()
        }
    }

    fun openSurvey(aName: String?) {
        Common.click(aName)
    }

    @JvmOverloads
    fun createAndOpenSurvey(importFile: String? = null, distanceUnits: String? = null, azimuthUnits: String? = null, slopeUnits: String? = null): String {
        initApp();
        Home.goHome()
        val surveyName = "" + System.currentTimeMillis()
        createSurvey(surveyName, importFile, distanceUnits, azimuthUnits, slopeUnits)
        openSurvey(surveyName)
        return surveyName
    }

    private fun initApp() {

        // language dialog
        try {
            onView(withText("English")).perform(ViewActions.click());
        } catch (e: Exception) {
            // language already selected
            Log.i(Constants.LOG_TAG_SERVICE, "Language already initialized");
        }

        // storage permissions
      /*  Intents.init()
        val openDocumentTreeResult = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        openDocumentTreeResult.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, openDocumentTreeResult)
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE)).respondWith(result)*/

        try {
            onView(withText(R.string.ok)).perform(ViewActions.click())
        } catch (e: Exception) {
            // storage already initialized
            Log.i(Constants.LOG_TAG_SERVICE, "Storage already initialized");
        }
    }

    @JvmOverloads
    fun addLeg(length: Float, azimuth: Float, slope: Float?, up: Float? = null, down: Float? = null, left: Float? = null, rigt: Float? = null) {
        // press new
        Common.click(id.main_action_add)

        // select the leg option
        Common.clickDialogSpinnerAtPosition(0)
        setLegData(length, azimuth, slope, up, down, left, rigt)
    }

    fun addLegMiddle(distance: Float, up: Float, down: Float, left: Float, right: Float) {
        // press new
        Common.click(id.main_action_add)

        // select leg
        Common.clickDialogSpinnerAtPosition(2)

        // middle at
        Common.type(id.middle_distance, distance)

        // save
        Common.click(id.middle_create)

        // populate
        setLegData(null, null, null, up, down, left, right)
    }

    fun selectFirstSurveyLeg() {
        Common.click(id.main_action_add)
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
        Common.click("Add Vector")

        Espresso.onView(ViewMatchers.withText("Add Vector"))
                .apply { // set data
                    Common.type(id.vector_distance, aDistance)
                    Common.type(id.vector_azimuth, anAzimuth)
                    Common.type(id.vector_slope, aSlope)

                    // save
                    Common.click(id.vector_add) }
    }

    fun addSketch() {
        Common.openContextMenu()

        Common.click("Draw");
        Common.click(R.id.saveDrawingBtn)
        sleep(10000)
    }

    fun addCoordinate(lat: Float, lon: Float, altitude: Int, precision: Int) {
        Common.openContextMenu()
        Common.click("GPS")
        Common.click("GPS Manual")
        Common.type(id.gps_manual_latitude, lat)
        Common.type(id.gps_manual_longitude, lon)
        Common.type(id.gps_manual_altitude, altitude)
        Common.type(id.gps_manual_accuracy, precision)
        Common.click(id.gps_action_save)
        saveLeg()
    }

    fun nextGallery() {
        // press new
        Common.click(id.main_action_add)

        // select gallery
        Common.clickDialogSpinnerAtPosition(1)
    }

    fun openLegWithText(text: String?) {
        Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.click())
    }

    fun saveLeg() {
        Common.click(id.point_action_save)
        Espresso.onView(ViewMatchers.withText("Current leg"))
        Espresso.onIdle()
    }
}