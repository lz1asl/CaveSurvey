package com.astoev.cave.survey.test.service.data

import com.astoev.cave.survey.model.Option.UNIT_FEET
import com.astoev.cave.survey.model.Option.UNIT_GRADS
import com.astoev.cave.survey.service.imp.LegData
import com.astoev.cave.survey.test.helper.Common.checkNotVisible
import com.astoev.cave.survey.test.helper.Common.checkVisible
import com.astoev.cave.survey.test.helper.Common.goBack
import com.astoev.cave.survey.test.helper.Data
import com.astoev.cave.survey.test.helper.ExcelTestUtils.assertLeg
import com.astoev.cave.survey.test.helper.ExcelTestUtils.assertLegLocation
import com.astoev.cave.survey.test.helper.Survey
import com.astoev.cave.survey.test.helper.Survey.addCoordinate
import com.astoev.cave.survey.test.helper.Survey.addFirstSurveyLeg
import com.astoev.cave.survey.test.helper.Survey.addLeg
import com.astoev.cave.survey.test.helper.Survey.addLegMiddle
import com.astoev.cave.survey.test.helper.Survey.addVector
import com.astoev.cave.survey.test.helper.Survey.createAndOpenSurvey
import com.astoev.cave.survey.test.helper.Survey.nextGallery
import com.astoev.cave.survey.test.helper.Survey.openLegWithText
import com.astoev.cave.survey.test.helper.Survey.openSurvey
import com.astoev.cave.survey.test.helper.Survey.saveLeg
import com.astoev.cave.survey.test.helper.Survey.setLegData
import com.astoev.cave.survey.util.FileStorageUtil.getUniqueExportName
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test
import java.io.IOException


class ExcelExportTest : AbstractExportTest() {

    @Test
    @Throws(IOException::class)
    fun excelExportTest() {

        // create survey
        var surveyName = createAndOpenSurvey()

        // empty first leg
        goBack()
        openSurvey(surveyName)
        var legs = exportToXlsAndRead(surveyName, 1)
        assertLeg(legs[0], null, null, null)
        assertLeg(legs[0], "A", "0", "A", "1", false, false)

        // fist minimal and base leg
        addFirstSurveyLeg()
        setLegData(1f, 2f, null)
        openLegWithText("A1")
        addCoordinate(42.811522f, 23.378906f, 123, 5)
        addLeg(1.2f, 2.2f, 1.3f)
        legs = exportToXlsAndRead(surveyName, 2)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)

        // with side measurements
        addLeg(2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)
        legs = exportToXlsAndRead(surveyName, 3)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
        assertThirdWithSidesLeg(legs)

        // with middles
        addLeg(5.5f, 4.4f, 5.5f, 2.1f, 2.2f, 2.3f, 2.4f)
        addLegMiddle(3.1f, 3.1f, 3.2f, 3.3f, 3.4f)
        legs = exportToXlsAndRead(surveyName, 5)
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
        legs = exportToXlsAndRead(surveyName, 9)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
        assertThirdWithSidesLeg(legs)
        assertFifthLegWithMiddlePoint(legs)
        assertSixthLegWithVectors(legs)

        // another gallery
        nextGallery()
        setLegData(4.4f, 4.5f, 4.6f, 0.1f, 0.2f, 0.3f, 0.4f)

        legs = exportToXlsAndRead(surveyName, 10)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
        assertThirdWithSidesLeg(legs)
        assertFifthLegWithMiddlePoint(legs)
        assertSixthLegWithVectors(legs)
        assertSeventhLegNextGallery(legs)

        // now try to import it back and check the data
        goBack()
        val lastExportName = getUniqueExportName(surveyName, 6)
        surveyName = createAndOpenSurvey(lastExportName, null, null, null)
        goBack()
        openSurvey(surveyName)
        legs = exportToXlsAndRead(surveyName, 10)
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
        val surveyName = createAndOpenSurvey(null, UNIT_FEET, UNIT_GRADS, UNIT_GRADS)

        // empty first leg, units preserved
        goBack()
        openSurvey(surveyName)
        var legs = exportToXlsAndRead(surveyName, 1, UNIT_FEET, UNIT_GRADS, UNIT_GRADS)
        assertLeg(legs[0], null, null, null)
        assertLeg(legs[0], "A", "0", "A", "1", false, false)

        // measurements as they are
        addFirstSurveyLeg()
        setLegData(1f, 2f, null)
        openLegWithText("A1")
        addCoordinate(42.811522f, 23.378906f, 123, 5)
        addLeg(1.2f, 2.2f, 1.3f)
        legs = exportToXlsAndRead(surveyName, 2, UNIT_FEET, UNIT_GRADS, UNIT_GRADS)
        assertFirstLegNoSlope(legs)
        assertSecondSimpleLeg(legs)
    }

    @Test
    @Throws(IOException::class)
    fun excelExportSketches() {
        // create survey
        val surveyName = createAndOpenSurvey()

        // fist minimal leg
        setLegData(1f, 2f, null)
        openSurvey(surveyName)
        openLegWithText("A1")

        // no sketches
        checkNotVisible("Sketches")
        checkNotVisible("Sketch 1")

        // create sketch
        Survey.addSketch()
        checkVisible("Sketches")
        checkVisible("Sketch 1")

        // create sketch
        Survey.addSketch()
        checkVisible("Sketches")
        checkVisible("Sketch 1")
        checkVisible("Sketch 2")

        // sketch data exported
        goBack()
        val legs = exportToXlsAndRead(surveyName, 1)
        assertNotNull(legs[0].sketch)
        val sketch = Data.getExportFile(surveyName, legs[0].sketch)
        assert(sketch.exists())
    }

    @Ignore("not ready")
    @Test
    @Throws(IOException::class)
    fun excelExportPhotos() {
        // create survey
        val surveyName = createAndOpenSurvey()

        // fist minimal leg
        setLegData(1f, 2f, null)
        openLegWithText("A1")

        // no sketches
        checkNotVisible("Photos")
        checkNotVisible("Photo 1")

        // create sketch
        Survey.addPhoto()
        checkVisible("Photos")
        checkVisible("Photo 1")

        // create sketch
        Survey.addPhoto()
        checkVisible("Photos")
        checkVisible("Photo 1")
        checkVisible("Photo 2")

        // sketch data exported
        goBack()
        val legs = exportToXlsAndRead(surveyName, 1)
        assertNotNull(legs[0].photo)
        val photo = Data.getExportFile(surveyName, legs[0].photo)
        assert(photo.exists())
    }

    private fun assertFirstLegNoSlope(legs: List<LegData>) {
        assertLeg(legs[0], 1f, 2f, null)
        assertLeg(legs[0], "A", "0", "A", "1", false, false)
        assertLegLocation(legs[0], 42.811522, 23.378906, 123.0, 5.0)
    }

    private fun assertSecondSimpleLeg(legs: List<LegData>) {
        assertLeg(legs[1], 1.2f, 2.2f, 1.3f)
        assertLeg(legs[1], "A", "1", "A", "2", false, false)
    }

    private fun assertThirdWithSidesLeg(legs: List<LegData>) {
        assertLeg(legs[2], 2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)
        assertLeg(legs[2], "A", "2", "A", "3", false, false)
    }

    private fun assertFifthLegWithMiddlePoint(legs: List<LegData>) {
        assertLeg(legs[3], 3.1f, 4.4f, 5.5f, 2.1f, 2.2f, 2.3f, 2.4f)
        assertLeg(legs[3], "A", "3", "A", "3-4@3.1", true, false)
        assertLeg(legs[4], 2.4f, 4.4f, 5.5f, 3.1f, 3.2f, 3.3f, 3.4f)
        assertLeg(legs[4], "A", "3-4@3.1", "A", "4", true, false)
    }

    private fun assertSixthLegWithVectors(legs: List<LegData>) {
        assertLeg(legs[5], 3.4f, 3.5f, 3.6f, 6.1f, 6.2f, 6.3f, 6.4f)
        assertLeg(legs[5], "A", "4", "A", "5", false, false)
        assertLeg(legs[6], 1.1f, 1.2f, 1.3f)
        assertLeg(legs[6], "A", "4", null, null, false, true)
        assertLeg(legs[7], 1.4f, 1.5f, 1.6f)
        assertLeg(legs[7], "A", "4", null, null, false, true)
        assertLeg(legs[8], 1.7f, 1.8f, 1.9f)
        assertLeg(legs[8], "A", "4", null, null, false, true)
    }

    private fun assertSeventhLegNextGallery(legs: List<LegData>) {
        assertLeg(legs[9], 4.4f, 4.5f, 4.6f, 0.1f, 0.2f, 0.3f, 0.4f)
        assertLeg(legs[9], "A", "4", "B", "1", false, false)
    }
}