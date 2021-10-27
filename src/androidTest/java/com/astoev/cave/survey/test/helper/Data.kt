package com.astoev.cave.survey.test.helper

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.astoev.cave.survey.R.id
import com.astoev.cave.survey.service.export.excel.ExcelExport
import com.astoev.cave.survey.util.FileStorageUtil

object Data {
    fun dataScreen() {
        Common.openContextMenu()
        Common.click("Data")
    }

    fun xlsExport() {
        Common.click(id.info_action_export_xls)
    }

    fun visualTopoExport() {
        Common.openContextMenu()
        Common.click("Visual Topo export")
    }

    fun opensTopo() {
        Common.click(id.info_action_openstopo)
    }

    fun getLastXlsExport(aSurveyName: String?): Uri {
        val excelExportFiles = FileStorageUtil.listProjectFiles(null, ExcelExport.EXCEL_FILE_EXTENSION)
        return excelExportFiles.stream()
                .filter { file: DocumentFile -> file.name.toString().startsWith(aSurveyName!!) }
                .min { f1: DocumentFile, f2: DocumentFile -> f2.name.toString().compareTo(f1.name.toString()) }
                .get().uri
    }

    fun getExportFile(aSurveyName: String, aFilename: String): DocumentFile {
        val excelExportFiles = FileStorageUtil.listProjectFiles(null, null)
        return excelExportFiles.stream()
            .filter { file: DocumentFile -> file.parentFile?.name.toString().equals(aSurveyName!!) }
            .filter { file: DocumentFile -> file.name.equals(aFilename)}
            .min { f1: DocumentFile, f2: DocumentFile -> f2.name.toString().compareTo(f1.name.toString()) }
            .get()
    }
}