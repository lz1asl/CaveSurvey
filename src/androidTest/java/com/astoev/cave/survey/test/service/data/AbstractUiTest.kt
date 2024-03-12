package com.astoev.cave.survey.test.service.data

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.astoev.cave.survey.activity.home.SplashActivity
import com.astoev.cave.survey.test.helper.Common
import com.astoev.cave.survey.test.helper.Survey
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
@LargeTest
abstract class AbstractUiTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(SplashActivity::class.java)

    @Before
    fun initApp() {
        Survey.initApp()
    }

    fun findAsset(path: String): InputStream {
        return Common.context.assets.open(path)
    }
}