package com.astoev.cave.survey.test.service.data

import android.Manifest
import android.util.Log
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.astoev.cave.survey.Constants
import com.astoev.cave.survey.activity.home.SplashActivity
import com.astoev.cave.survey.test.helper.Common
import com.astoev.cave.survey.util.FileStorageUtil
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.rules.TestRule
import java.io.BufferedReader
import java.io.InputStream

//@RunWith(AndroidJUnit4::class)
@LargeTest
abstract class AbstractExportTest {

    var PARAM_PROJECT_NAME = "PROJECT_NAME"
    var PARAM_TODAY = "TODAY"
    var PARAM_CAVESURVEY_VERSION = "CAVESURVEY_VERSION"

    @get:Rule
    var activityRule: ActivityTestRule<SplashActivity> = ActivityTestRule(SplashActivity::class.java)

    @get:Rule
    var permissionRule: TestRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)


    fun findAsset(path: String): InputStream {
        return Common.getContext().assets.open(path)
    }

    fun compareContents(expected: InputStream, differences: Map<String, String>, projectName: String, extension: String) {

        // expected
        var content = expected.bufferedReader().use(BufferedReader::readText)

        // apply differences
        differences.forEach { k, v ->
            content = content.replace("$" + k, v)
        }

        // actual
        val home = FileStorageUtil.getProjectHome(projectName)
        val files = FileStorageUtil.getFolderFiles(home, extension)
        Log.i(Constants.LOG_TAG_SERVICE, "" + files.size + " exported files")

        val actual = files.get(files.size - 1).readText(Charsets.UTF_8)

        // must match
        assertEquals(content, actual);
    }
}

