package com.astoev.cave.survey.test.service.data

import com.astoev.cave.survey.model.Option
import com.astoev.cave.survey.service.export.vtopo.VisualTopoExport
import com.astoev.cave.survey.test.helper.Common.goBack
import com.astoev.cave.survey.test.helper.Data.dataScreen
import com.astoev.cave.survey.test.helper.Data.visualTopoExport
import com.astoev.cave.survey.test.helper.Survey.addCoordinate
import com.astoev.cave.survey.test.helper.Survey.addLeg
import com.astoev.cave.survey.test.helper.Survey.addLegMiddle
import com.astoev.cave.survey.test.helper.Survey.addVector
import com.astoev.cave.survey.test.helper.Survey.createAndOpenSurvey
import com.astoev.cave.survey.test.helper.Survey.nextGallery
import com.astoev.cave.survey.test.helper.Survey.openLegWithText
import com.astoev.cave.survey.test.helper.Survey.openSurvey
import com.astoev.cave.survey.test.helper.Survey.saveLeg
import com.astoev.cave.survey.test.helper.Survey.setLegData
import com.astoev.cave.survey.util.AndroidUtil
import org.junit.Test
import java.util.*

class VisualTopoExportTest : AbstractExportTest() {

    @Test
    fun visualTopoExportTest() {

        // create survey
        val surveyName = createAndOpenSurvey()

        // first test legs
        setLegData(1f, 2f, null)
        openSurvey(surveyName)
        openLegWithText("A1")
        addCoordinate(42.811522f, 23.378906f, 123, 5)
        addLeg(1.2f, 2.2f, 1.3f)
        addLeg(2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)

        // compare
        exportAndCompare(surveyName, "initial")

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
        exportAndCompare(surveyName, "full")
    }

    @Test
    fun visualTopoExportNonDefaultUnitsTest() {

        // create survey
        val surveyName = createAndOpenSurvey(null, Option.UNIT_FEET, Option.UNIT_GRADS, Option.UNIT_GRADS)

        // first test legs
        setLegData(1f, 2f, null)
        openSurvey(surveyName)
        openLegWithText("A1")
        addCoordinate(42.811522f, 23.378906f, 123, 5)
        addLeg(1.2f, 2.2f, 1.3f)
        addLeg(2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)

        // compare
        exportAndCompare(surveyName, "initial_feet")
    }

    private fun exportAndCompare(surveyName: String, expected: String) {
        // export
        dataScreen()
        visualTopoExport()
        compare(surveyName, expected)
        goBack()
    }

    private fun compare(projectName: String, expected: String) {
        val expectedStream = findAsset("export/vtopo/$expected.tro")
        val params = mapOf(PARAM_PROJECT_NAME to projectName,
                PARAM_TODAY to VisualTopoExport.formatDate(Date()),
                PARAM_CAVESURVEY_VERSION to AndroidUtil.getAppVersion())
        compareContents(expectedStream, params, projectName, ".tro")
    }
}