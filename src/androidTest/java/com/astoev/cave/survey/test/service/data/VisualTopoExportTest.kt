package com.astoev.cave.survey.test.service.data

import com.astoev.cave.survey.test.helper.Common.goBack
import com.astoev.cave.survey.test.helper.Data.dataScreen
import com.astoev.cave.survey.test.helper.Data.visualTopoExport
import com.astoev.cave.survey.test.helper.Survey.createAndOpenSurvey
import org.junit.Assert.fail
import org.junit.Test

class VisualTopoExportTest() : AbstractExportTest() {

    @Test
    fun visualTopoExportTest() {

        // create survey
        val surveyName = createAndOpenSurvey()

        // TODO - add data

        // export
        exportAndCompare("empty");

        // TODO - full data set

        exportAndCompare("full");
    }

    private fun exportAndCompare(expected: String) {
        // export
        dataScreen()
        visualTopoExport()
        compare(expected)
        goBack()
    }

    private fun compare(expected: String) {
        // todo compare against the file
        fail("Not implemented")
    }
}