package com.astoev.cave.survey.test.service.data

import com.astoev.cave.survey.model.Option.*
import com.astoev.cave.survey.service.imp.ExcelImport.loadProjectData
import com.astoev.cave.survey.service.imp.LegData
import com.astoev.cave.survey.test.helper.Common.goBack
import com.astoev.cave.survey.test.helper.Data.*
import com.astoev.cave.survey.test.helper.ExcelTestUtils.assertConfigUnits
import com.astoev.cave.survey.test.helper.ExcelTestUtils.assertLeg
import com.astoev.cave.survey.test.helper.Survey.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class ExcelExportTest() : AbstractExportTest() {

    @Test
    @Throws(IOException::class)
    fun excelExportTest() {

        // create survey
        var surveyName = createAndOpenSurvey()

        // empty first leg
        var legs = exportAndRead(surveyName, 1)
        assertLeg(legs[0], null, null, null)
        assertLeg(legs[0], "A", "0", "A", "1", false, false)

        // fist minimal and base leg
        selectFirstSurveyLeg()
        setLegData(1f, 2f, null)
        openLegWithText("A1")
        addCoordinate(42.811522f, 23.378906f, 123, 5);
        addLeg(1.2f, 2.2f, 1.3f)
        legs = exportAndRead(surveyName, 2)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)

        // with side measurements
        addLeg(2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)
        legs = exportAndRead(surveyName, 3)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
        assertThirdWithSidesLeg(legs)

        // with middles
        addLeg(5.5f, 4.4f, 5.5f, 2.1f, 2.2f, 2.3f, 2.4f)
        addLegMiddle(3.1f, 3.1f, 3.2f, 3.3f, 3.4f)
        legs = exportAndRead(surveyName, 5)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
        assertThirdWithSidesLeg(legs)
        assertFifthLegWithMiddlePoint(legs)

        // with vectors
        addLeg(3.4f, 3.5f, 3.6f, 6.1f, 6.2f, 6.3f, 6.4f)
        openLegWithText("A5")
        addVector(1.1f, 1.2f, 1.3f)
        addVector(1.4f, 1.5f, 1.6f)
        addVector(1.7f, 1.8f, 1.9f)
        saveLeg()
        legs = exportAndRead(surveyName, 9)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
        assertThirdWithSidesLeg(legs)
        assertFifthLegWithMiddlePoint(legs)
        assertSixthLegWithVectors(legs)

        // another gallery
        nextGallery()
        setLegData(4.4f, 4.5f, 4.6f, 0.1f, 0.2f, 0.3f, 0.4f)
        legs = exportAndRead(surveyName, 10)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
        assertThirdWithSidesLeg(legs)
        assertFifthLegWithMiddlePoint(legs)
        assertSixthLegWithVectors(legs)
        assertSeventhLegNextGallery(legs)

        // now try to import it back and check the data
        goBack()
        surveyName = createAndOpenSurvey(true, null, null, null)
        legs = exportAndRead(surveyName, 10)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
        assertThirdWithSidesLeg(legs)
        assertFifthLegWithMiddlePoint(legs)
        assertSixthLegWithVectors(legs)
        assertSeventhLegNextGallery(legs)
    }

    @Test
    @Throws(IOException::class)
    fun excelExportInNonDefaultUnitsTest() {

        // create survey
        var surveyName = createAndOpenSurvey(false, UNIT_FEET, UNIT_GRADS, UNIT_GRADS);

        // empty first leg, units preserved
        var legs = exportAndRead(surveyName, 1, UNIT_FEET, UNIT_GRADS, UNIT_GRADS)
        assertLeg(legs[0], null, null, null)
        assertLeg(legs[0], "A", "0", "A", "1", false, false)

        // measurements as they are
        selectFirstSurveyLeg()
        setLegData(1f, 2f, null)
        openLegWithText("A1")
        addCoordinate(42.811522f, 23.378906f, 123, 5);
        addLeg(1.2f, 2.2f, 1.3f)
        legs = exportAndRead(surveyName, 2, UNIT_FEET, UNIT_GRADS, UNIT_GRADS)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
    }

    @Throws(IOException::class)
    private fun exportAndRead(aSurveyName: String, aLegsCount: Int): List<LegData> {
        return exportAndRead(aSurveyName, aLegsCount, UNIT_METERS, UNIT_DEGREES, UNIT_DEGREES);
    }

    @Throws(IOException::class)
    private fun exportAndRead(aSurveyName: String, aLegsCount: Int,
                              aDistanceUnits: String, anAzimuthUnits: String, aSlopeUnits: String): List<LegData> {
        // export
        dataScreen()
        xlsExport()
        goBack()

        // loadTransitionBridgingViewAction
        val exportFile = getLastXlsExport(aSurveyName)
        val data = loadProjectData(exportFile)

        // default units
        assertConfigUnits(data, aDistanceUnits, anAzimuthUnits, aSlopeUnits) // expected number of legs

        // expected number of legs
        val legs = data.legs
        assertEquals(aLegsCount.toLong(), legs.size.toLong())
        return legs
    }

    private fun assertFirstLegNoSlope(legs: List<LegData>) {
        assertLeg(legs[0], 1f, 2f, null)
        assertLeg(legs[0], "A", "0", "A", "1", false, false)
        assertLegCoordinate(legs[0], 42.811522f, 23.378906f, 123, 5);
    }

    private fun assertLegCoordinate(legData: LegData, lat: Float, lon: Float, alt: Int, accuracy: Int) {
        assertEquals(lat, legData.lat)
        assertEquals(lon, legData.lon)
        assertEquals(alt, legData.alt.toInt())
        assertEquals(accuracy, legData.accuracy.toInt())
    }

    private fun assertSecondSimpleLeg(legs: List<LegData?>) {
        assertLeg(legs[1], 1.2f, 2.2f, 1.3f)
        assertLeg(legs[1], "A", "1", "A", "2", false, false)
    }

    private fun assertThirdWithSidesLeg(legs: List<LegData?>) {
        assertLeg(legs[2], 2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)
        assertLeg(legs[2], "A", "2", "A", "3", false, false)
    }

    private fun assertFifthLegWithMiddlePoint(legs: List<LegData?>) {
        assertLeg(legs[3], 3.1f, 4.4f, 5.5f, 2.1f, 2.2f, 2.3f, 2.4f)
        assertLeg(legs[3], "A", "3", "A", "3-4@3.1", true, false)
        assertLeg(legs[4], 2.4f, 4.4f, 5.5f, 3.1f, 3.2f, 3.3f, 3.4f)
        assertLeg(legs[4], "A", "3-4@3.1", "A", "4", true, false)
    }

    private fun assertSixthLegWithVectors(legs: List<LegData?>) {
        assertLeg(legs[5], 3.4f, 3.5f, 3.6f, 6.1f, 6.2f, 6.3f, 6.4f)
        assertLeg(legs[5], "A", "4", "A", "5", false, false)
        assertLeg(legs[6], 1.1f, 1.2f, 1.3f)
        assertLeg(legs[6], "A", "4", null, null, false, true)
        assertLeg(legs[7], 1.4f, 1.5f, 1.6f)
        assertLeg(legs[7], "A", "4", null, null, false, true)
        assertLeg(legs[8], 1.7f, 1.8f, 1.9f)
        assertLeg(legs[8], "A", "4", null, null, false, true)
    }

    private fun assertSeventhLegNextGallery(legs: List<LegData?>) {
        assertLeg(legs[9], 4.4f, 4.5f, 4.6f, 0.1f, 0.2f, 0.3f, 0.4f)
        assertLeg(legs[9], "A", "4", "B", "1", false, false)
    }
}