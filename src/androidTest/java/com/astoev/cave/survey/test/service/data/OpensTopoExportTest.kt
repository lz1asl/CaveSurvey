package com.astoev.cave.survey.test.service.data

import com.astoev.cave.survey.model.Option.UNIT_FEET
import com.astoev.cave.survey.model.Option.UNIT_GRADS
import com.astoev.cave.survey.test.helper.Common.goBack
import com.astoev.cave.survey.test.helper.Data.dataScreen
import com.astoev.cave.survey.test.helper.Data.opensTopoExport
import com.astoev.cave.survey.test.helper.Survey.*
import org.junit.Test

class OpensTopoExportTest() : AbstractExportTest() {

    @Test
    fun opensTopoExportTest() {

        // create survey
        var surveyName = createAndOpenSurvey()

        // first test legs
        selectFirstSurveyLeg()
        setLegData(1f, 2f, null)
        openLegWithText("A1")
        addCoordinate(42.811522f, 23.378906f, 123, 5);
        addLeg(1.2f, 2.2f, 1.3f)
        addLeg(2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)

        // compare
        exportAndCompare(surveyName, "initial");

        // rest of the test data
        addLeg(5.5f, 4.4f, 5.5f, 2.1f, 2.2f, 2.3f, 2.4f)
        addLegMiddle(3.1f, 3.1f, 3.2f, 3.3f, 3.4f)
        addLeg(3.4f, 3.5f, 3.6f, 6.1f, 6.2f, 6.3f, 6.4f)
        openLegWithText("A5")
        addVector(1.1f, 1.2f, 1.3f)
        addVector(1.4f, 1.5f, 1.6f)
        addVector(1.7f, 1.8f, 1.9f)
        saveLeg()
        nextGallery()
        setLegData(4.4f, 4.5f, 4.6f, 0.1f, 0.2f, 0.3f, 0.4f)

        // compare
        exportAndCompare(surveyName, "full");
    }

    @Test
    fun opensTopoExportNonStandardUnitsTest() {

        // create survey with non default units
        var surveyName = createAndOpenSurvey(false, UNIT_FEET, UNIT_GRADS, UNIT_GRADS)

        // first test legs - data is the same
        selectFirstSurveyLeg()
        setLegData(1f, 2f, null)
        openLegWithText("A1")
        addCoordinate(42.811522f, 23.378906f, 123, 5);
        addLeg(1.2f, 2.2f, 1.3f)
        addLeg(2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)

        // compare
        exportAndCompare(surveyName, "initial_feet");
    }

    private fun exportAndCompare(surveyName: String, expected: String) {
        dataScreen()
        opensTopoExport()
        compare(surveyName, expected)
        goBack()
        goBack()
    }

    private fun compare(projectName: String, expected: String) {
        val expectedStream = findAsset("export/openstopo/$expected.json")
        val params = mapOf(PARAM_PROJECT_NAME to projectName)
        compareContents(expectedStream, params, projectName, ".json");
    }
}