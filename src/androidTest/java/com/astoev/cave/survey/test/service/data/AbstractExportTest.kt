package com.astoev.cave.survey.test.service.data

import android.Manifest
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.astoev.cave.survey.activity.home.SplashActivity
import org.junit.Rule
import org.junit.rules.TestRule

//@RunWith(AndroidJUnit4::class)
@LargeTest
open abstract class AbstractExportTest {

    @get:Rule
    var activityRule: ActivityTestRule<SplashActivity> = ActivityTestRule(SplashActivity::class.java)

    @get:Rule
    var permissionRule: TestRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

}

