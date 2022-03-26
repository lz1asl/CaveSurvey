package com.astoev.cave.survey.test.service.bluetooth

import android.app.Activity
import android.os.Bundle
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.astoev.cave.survey.Constants.*
import com.astoev.cave.survey.R
import com.astoev.cave.survey.activity.main.PointActivity
import com.astoev.cave.survey.test.helper.Common
import com.astoev.cave.survey.test.helper.Common.checkValue
import com.astoev.cave.survey.test.helper.Common.goBack
import com.astoev.cave.survey.test.helper.Common.toggleSwitch
import com.astoev.cave.survey.test.helper.Common.type
import com.astoev.cave.survey.test.helper.Common.verifySwitchState
import com.astoev.cave.survey.test.helper.Config.openMeasurements
import com.astoev.cave.survey.test.helper.Config.openSettings
import com.astoev.cave.survey.test.helper.ExcelTestUtils
import com.astoev.cave.survey.test.helper.Survey.createAndOpenSurvey
import com.astoev.cave.survey.test.helper.Survey.saveLeg
import com.astoev.cave.survey.test.helper.Survey.selectFirstSurveyLeg
import com.astoev.cave.survey.test.service.data.AbstractExportTest
import org.junit.Test
import java.io.IOException
import java.lang.Thread.sleep


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

        // fist leg
        selectFirstSurveyLeg()
        Common.type(R.id.point_azimuth, 123f)

        // simulate measurements received
        simulateMeasureFromBtDevice(Measures.up, 1.2f)
        simulateMeasureFromBtDevice(Measures.distance, 12.3f)

        // assert value adjusted - only distance
        checkValue(R.id.point_azimuth,"123.0")
        checkValue(R.id.point_distance,"12.432")
        checkValue(R.id.point_up,"1.323")
        saveLeg()

        // also in the export
        var legs = exportToXlsAndRead(surveyName, 1)
        ExcelTestUtils.assertLeg(legs[0], 12.432f, 123f, null, 1.323f, null, null, null)

        // reset the adjustment
        openSettings()
        openMeasurements()
        verifySwitchState(R.id.measurements_use_adjustment, true)
        checkValue(R.id.measurements_length_adjustment, adjustment.toString())
        toggleSwitch(R.id.measurements_use_adjustment)
        goBack()
        goBack()

        // fist leg
        selectFirstSurveyLeg()

        // simulate measurements received
        simulateMeasureFromBtDevice(Measures.up, 1.2f)
        simulateMeasureFromBtDevice(Measures.distance, 12.3f)

        // assert value not adjusted
        checkValue(R.id.point_azimuth,"123.0");
        checkValue(R.id.point_distance,"12.3");
        checkValue(R.id.point_up,"1.2")

        // also in the export
        legs = exportToXlsAndRead(surveyName, 1)
        ExcelTestUtils.assertLeg(legs[0], 12.3f, 123f, null, 1.2f, null, null, null)
    }

    private fun simulateMeasureFromBtDevice(field: Measures, value: Float) {

        runOnUiThread {
            // format
            val b = Bundle()
            b.putFloatArray(MEASURE_VALUE_KEY, floatArrayOf(value))
            b.putStringArray(MEASURE_TYPE_KEY, arrayOf(MeasureTypes.distance.name))
            b.putStringArray(MEASURE_UNIT_KEY, arrayOf(MeasureUnits.meters.name))
            b.putStringArray(MEASURE_TARGET_KEY, arrayOf(field.name))

            // consume
            PointActivity.mReceiver.send(Activity.RESULT_OK, b)
//            PointActivity.mReceiver.target.onReceiveMeasures(field, value)
            sleep(1000)
        }
    }

}