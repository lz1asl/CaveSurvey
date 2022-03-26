package com.astoev.cave.survey.test.service.data

import android.util.Log
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.astoev.cave.survey.Constants
import com.astoev.cave.survey.activity.home.SplashActivity
import com.astoev.cave.survey.model.Option
import com.astoev.cave.survey.service.imp.ExcelImport
import com.astoev.cave.survey.service.imp.LegData
import com.astoev.cave.survey.test.helper.Common
import com.astoev.cave.survey.test.helper.Data
import com.astoev.cave.survey.test.helper.ExcelTestUtils
import com.astoev.cave.survey.util.ConfigUtil
import com.astoev.cave.survey.util.FileStorageUtil
import com.astoev.cave.survey.util.StreamUtil
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
@LargeTest
abstract class AbstractExportTest {

    var PARAM_PROJECT_NAME = "PROJECT_NAME"
    var PARAM_TODAY = "TODAY"
    var PARAM_CAVESURVEY_VERSION = "CAVESURVEY_VERSION"

    @Rule @JvmField
    var activityRule = ActivityScenarioRule(SplashActivity::class.java)

//    @Rule @JvmField
//    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun findAsset(path: String): InputStream {
        return Common.context.assets.open(path)
    }

    @Throws(IOException::class)
    fun exportToXlsAndRead(aSurveyName: String, aLegsCount: Int): List<LegData> {
        return exportToXlsAndRead(aSurveyName, aLegsCount,
            Option.UNIT_METERS,
            Option.UNIT_DEGREES,
            Option.UNIT_DEGREES
        )
    }

    @Throws(IOException::class)
    fun exportToXlsAndRead(aSurveyName: String, aLegsCount: Int,
                                   aDistanceUnits: String, anAzimuthUnits: String, aSlopeUnits: String): List<LegData> {
        // export
        Data.dataScreen()
        Data.xlsExport()
        Common.goBack()

        // loadTransitionBridgingViewAction
        val exportFile = Data.getLastXlsExport(aSurveyName)
        val data = ExcelImport.loadProjectData(exportFile)

        // default units
        ExcelTestUtils.assertConfigUnits(
            data,
            aDistanceUnits,
            anAzimuthUnits,
            aSlopeUnits
        ) // expected number of legs

        // expected number of legs
        val legs = data.legs
        assertEquals(aLegsCount.toLong(), legs.size.toLong())
        return legs
    }

    fun compareContents(expected: InputStream, differences: Map<String, String>, projectName: String, extension: String) {

        // expected
        var content = expected.bufferedReader().use(BufferedReader::readText)

        // apply differences
        differences.forEach { (k, v) ->
            content = content.replace("$" + k, v)
        }

        // actual
        val home = FileStorageUtil.getProjectHome(projectName)
        val files = FileStorageUtil.getFolderFiles(home, extension)
        Log.i(Constants.LOG_TAG_SERVICE, "" + files.size + " exported files")

        val exportFile = files[files.size - 1]
        val actual = StreamUtil.read(ConfigUtil.getContext().contentResolver.openInputStream(exportFile.uri))

        // must match
        assertEquals(content, String(actual))
    }
}

