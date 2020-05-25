package com.astoev.cave.survey.test.service.data

import com.astoev.cave.survey.test.helper.Common.goBack
import com.astoev.cave.survey.test.helper.Data.dataScreen
import com.astoev.cave.survey.test.helper.Data.opensTopoExport
import com.astoev.cave.survey.test.helper.Survey.*
import org.junit.Assert.fail
import org.junit.Test

class OpensTopoExportTest() : AbstractExportTest() {

    @Test
    fun opensTopoExportTest() {

        // create survey
        var surveyName = createAndOpenSurvey()

        // first test legs
        selectFirstSurveyLeg()
        setLegData(1f, 2f, null)
        addLeg(1.2f, 2.2f, 1.3f)
        addLeg(2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)

        // export
        exportAndCompare("initial");

        // TODO - full data set

        exportAndCompare("full");
    }

    private fun exportAndCompare(expected: String) {
        // export
        dataScreen()
        opensTopoExport()
        compare(expected)
        goBack()
    }

    private fun compare(expected: String) {
        // todo load the resource file and compare contents
        fail("Not ready")
    }
}