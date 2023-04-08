package com.astoev.cave.survey.test.service.bluetooth

import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.astoev.cave.survey.Constants.*
import com.astoev.cave.survey.R
import com.astoev.cave.survey.activity.main.PointActivity
import com.astoev.cave.survey.test.helper.Common.checkValue
import com.astoev.cave.survey.test.helper.Common.goBack
import com.astoev.cave.survey.test.helper.Common.toggleSwitch
import com.astoev.cave.survey.test.helper.Common.type
import com.astoev.cave.survey.test.helper.Common.verifySwitchState
import com.astoev.cave.survey.test.helper.Config.openMeasurements
import com.astoev.cave.survey.test.helper.Config.openSettings
import com.astoev.cave.survey.test.helper.ExcelTestUtils
import com.astoev.cave.survey.test.helper.Survey.createAndOpenSurvey
import com.astoev.cave.survey.test.helper.Survey.openSurvey
import com.astoev.cave.survey.test.helper.Survey.saveLeg
import com.astoev.cave.survey.test.helper.Survey.selectFirstSurveyLeg
import com.astoev.cave.survey.test.service.data.AbstractExportTest
import com.astoev.cave.survey.util.ConfigUtil
import org.junit.After
import org.junit.Test
import java.io.IOException


class BluethootAdjustedMeasureTest : AbstractExportTest() {



    @Test
    @Throws(IOException::class)
    fun testAdjustedBluetoothLength() {

        // configure adjustment
        openSettings()
        openMeasurements()
        verifySwitchState(R.id.measurements_use_adjustment, false)
        toggleSwitch(R.id.measurements_use_adjustment)
        var adjustment = 0.123f
        type(R.id.measurements_length_adjustment, adjustment)
        goBack()
        goBack()

        // create survey
        val surveyName = createAndOpenSurvey()

        // simulate measurements received for any types
        type(R.id.point_down, 1.23f)
        type(R.id.point_azimuth, 234f)

        // simulate measurements received, only distances corrected
        simulateMeasureFromBtDevice(Measures.slope, 2.3f)
        simulateMeasureFromBtDevice(Measures.up, 1.2f)
        simulateMeasureFromBtDevice(Measures.distance, 12.3f)

        // assert value adjusted - only distance
        checkValue(R.id.point_azimuth,"234.0")
        checkValue(R.id.point_slope,"2.3")
        checkValue(R.id.point_distance,"12.42")
        checkValue(R.id.point_up,"1.323")
        checkValue(R.id.point_down,"1.23")
        saveLeg()

        // also in the export
        openSurvey(surveyName)
        var legs = exportToXlsAndRead(surveyName, 1)
        ExcelTestUtils.assertLeg(legs[0], 12.42f, 234f, 2.3f, 1.323f, 1.23f, null, null)

        // reset the adjustment
        goBack()
        openSettings()
        openMeasurements()
        verifySwitchState(R.id.measurements_use_adjustment, true)
        checkValue(R.id.measurements_length_adjustment, adjustment.toString())
        toggleSwitch(R.id.measurements_use_adjustment)
        goBack()
        goBack()

        // fist leg
        openSurvey(surveyName)
        selectFirstSurveyLeg()

        // simulate measurements received, this time no adjustments
        type(R.id.point_down, 1.23f)
        type(R.id.point_azimuth, 234f)
        simulateMeasureFromBtDevice(Measures.slope, 2.3f)
        simulateMeasureFromBtDevice(Measures.up, 1.2f)
        simulateMeasureFromBtDevice(Measures.distance, 12.3f)

        // assert value not adjusted
        checkValue(R.id.point_azimuth,"234.0")
        checkValue(R.id.point_slope,"2.3")
        checkValue(R.id.point_distance,"12.3")
        checkValue(R.id.point_up,"1.2")
        checkValue(R.id.point_down,"1.23")

        // also in the export
        saveLeg()
        legs = exportToXlsAndRead(surveyName, 1)
        ExcelTestUtils.assertLeg(legs[0], 12.3f, 234f, 2.3f, 1.2f, 1.23f, null, null)
    }

    @After
    fun cleanUpMeasurementsConfig() {
        ConfigUtil.setContext(ApplicationProvider.getApplicationContext<Context>().applicationContext)
        ConfigUtil.removeProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT_VALUE)
        ConfigUtil.removeProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT)
    }

    private fun simulateMeasureFromBtDevice(field: Measures, value: Float) {

        runOnUiThread {
            // format
            val b = Bundle()
            b.putFloatArray(MEASURE_VALUE_KEY, floatArrayOf(value))
            b.putStringArray(MEASURE_TARGET_KEY, arrayOf(field.name))

            // consume
            PointActivity.mReceiver.resetMeasureExpectations()
            PointActivity.mReceiver.awaitMeasure(field)
            PointActivity.mReceiver.onReceiveResult(RESULT_OK, b)
        }
    }

}