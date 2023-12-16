package com.astoev.cave.survey.test.helper

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.astoev.cave.survey.R
import com.astoev.cave.survey.R.id
import com.astoev.cave.survey.activity.home.NewProjectActivity
import com.astoev.cave.survey.model.Option
import com.astoev.cave.survey.test.helper.Common.checkVisible
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`


object Survey {
    @JvmOverloads
    fun createSurvey(aName: String?, importFile: String? = null,
                     distanceUnits: String? = null, azimuthUnits: String? = null, slopeUnits: String? = null) {
        // open new survey screen
        Common.click(id.action_new_project)

        // enter params
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

        // import
        if (importFile != null) {
            Common.click(id.import_toggle)
            Common.click(id.import_files)
            onData(allOf(
                `is`(instanceOf(NewProjectActivity.ImportFile::class.java)),
                    object : BaseMatcher<NewProjectActivity.ImportFile>() {
                        override fun matches(item: Any?): Boolean {
                            return item.toString().startsWith(importFile)
                        }

                        override fun describeTo(description: Description?) {}
                    }))
                    .perform(ViewActions.click())

        }


        // save & go back
        Common.click(id.new_action_create)
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
        initApp()
        Home.goHome()
        val surveyName = "" + System.currentTimeMillis()
        createSurvey(surveyName, importFile, distanceUnits, azimuthUnits, slopeUnits)
        return surveyName
    }

    // first time start initialization
    fun initApp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation);

        // language dialog
        val languageDialog = device.findObject(UiSelector().text("Select language"))
        if (languageDialog.exists()) {
            onView(withText("English")).perform(click())
        }

        // storage permissions
        val storageDialog = device.findObject(UiSelector().text("New storage permissions"))
        if (storageDialog.exists()) {
            onView(withText(R.string.ok)).perform(click())

            val allowPermissions = device.findObject(UiSelector().text("USE THIS FOLDER"))
//            val allowPermissions = device.findObject(UiSelector().text("SELECT"));
            if (allowPermissions.exists()) {
                allowPermissions.click()
                device.findObject(UiSelector().text("ALLOW")).click()
            }
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

    fun addFirstSurveyLeg() {
        Common.click(id.main_action_add)
    }

    fun selectFirstSurveyLeg() {
        Common.click("A0")
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
        Common.click("Add Vectors")

        onView(withText(containsString("Vector")))
                .apply { // set data
                    Common.type(id.vector_distance, aDistance)
                    Common.type(id.vector_azimuth, anAzimuth)
                    Common.type(id.vector_slope, aSlope)

                    // save
                    Common.click(id.vector_add) }
    }

    fun addSketch() {
        Common.openContextMenu()
        Common.click("Draw")

        onView(withId(id.drawingSurface))
            .perform(ViewActions.longClick())
            .perform(ViewActions.swipeRight())
            .perform(ViewActions.click())

        Common.clickWithDescription("Save")
    }

    fun addPhoto() {

        // mock the camera
        Intents.init()

        val icon = BitmapFactory.decodeResource(
            InstrumentationRegistry.getInstrumentation().context.resources, R.drawable.logo
        )
        val resultData = Intent()
        resultData.putExtra("data", icon)
        val result = ActivityResult(Activity.RESULT_OK, resultData)
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)

        // request photo
        Common.openContextMenu()
        Common.click("Photo")
        checkVisible(id.point_photos_table)
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
        onView(withText(text)).perform(ViewActions.click())
    }

    fun saveLeg() {
        Common.click(id.point_action_save)
        onView(withText("Current leg"))
        Espresso.onIdle()
    }
}