package com.astoev.cave.survey.test.service.export

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.astoev.cave.survey.activity.home.SplashActivity
import com.astoev.cave.survey.test.helper.Common.goBack
import com.astoev.cave.survey.test.helper.Data.dataScreen
import com.astoev.cave.survey.test.helper.Data.visualTopoExport
import com.astoev.cave.survey.test.helper.Survey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class VisualTopoTest {

    @get:Rule
    var activityRule: ActivityTestRule<SplashActivity>
            = ActivityTestRule(SplashActivity::class.java)

    @Test
    fun opensTopoExport() {

        // create survey
        val surveyName = Survey.createAndOpenSurvey()

        // export
        export();

    }

    private fun export() {
        // export
        dataScreen()
        visualTopoExport()
        goBack()
    }
}