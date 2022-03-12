package com.astoev.cave.survey.test.service.data

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.astoev.cave.survey.service.export.csv.CsvExport
import com.astoev.cave.survey.test.helper.Common
import com.astoev.cave.survey.test.helper.Data
import com.astoev.cave.survey.test.helper.Survey
import com.astoev.cave.survey.util.ConfigUtil
import com.astoev.cave.survey.util.FileStorageUtil
import com.astoev.cave.survey.util.StreamUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class CsvExportTest : AbstractExportTest() {

    @Test
    @Throws(IOException::class)
    fun csvExportTest() {

        // create survey
        val surveyName = Survey.createAndOpenSurvey()

        // add some data
        Survey.selectFirstSurveyLeg()
        Survey.setLegData(1f, 2f, null)
        Survey.addLeg(1.2f, 2.2f, 1.3f)
        Survey.addLeg(2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f)
        Survey.addLeg(5.5f, 4.4f, 5.5f, 2.1f, 2.2f, 2.3f, 2.4f)
        Survey.addLegMiddle(3.1f, 3.1f, 3.2f, 3.3f, 3.4f)
        Survey.addLeg(3.4f, 3.5f, 3.6f, 6.1f, 6.2f, 6.3f, 6.4f)
        Survey.openLegWithText("A5")
        Survey.saveLeg()
        Survey.nextGallery()
        Survey.setLegData(4.4f, 4.5f, 4.6f, 0.1f, 0.2f, 0.3f, 0.4f)
        Data.dataScreen()
        Data.caveAR()
        Common.goBack()

        val export = getLastCsvExport(surveyName)
        val exportStream = ConfigUtil.getContext().contentResolver.openInputStream(export)
        assertEquals("""From,To,Length,Compass,Clino,Left,Right,Top,Bottom,I,Note
            ,,m,deg,deg,m,m,m,m,,
            A0,A1,1.0,2.0,,,,,
            A1,A2,1.2,2.2,1.3,,,,
            A2,A3,2.3,3.4,4.5,1.3,1.4,1.1,1.2
            A3,A3-A4@3.1,3.1,4.4,5.5,2.3,2.4,2.1,2.2
            A3-A4@3.1,A4,2.4,4.4,5.5,3.3,3.4,3.1,3.2
            A4,A5,3.4,3.5,3.6,6.3,6.4,6.1,6.2
            A4,B1,4.4,4.5,4.6,0.3,0.4,0.1,0.2
            """,
            String(StreamUtil.read(exportStream)))
    }

    private fun getLastCsvExport(aSurveyName: String?): Uri {
        val excelExportFiles = FileStorageUtil.listProjectFiles(null, CsvExport.CSV_FILE_EXTENSION)
        return excelExportFiles.stream()
            .filter { file: DocumentFile -> file.name.toString().startsWith(aSurveyName!!) }
            .min { f1: DocumentFile, f2: DocumentFile -> f2.name.toString().compareTo(f1.name.toString()) }
            .get().uri
    }
}