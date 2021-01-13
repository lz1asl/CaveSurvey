package com.astoev.cave.survey.test.helper

import com.astoev.cave.survey.R.id
import com.astoev.cave.survey.service.export.excel.ExcelExport
import com.astoev.cave.survey.util.FileStorageUtil
import java.io.File

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

    fun getLastXlsExport(aSurveyName: String?): File {
        val excelExportFiles = FileStorageUtil.listProjectFiles(null, ExcelExport.EXCEL_FILE_EXTENSION)
        return excelExportFiles.stream()
                .filter { file: File -> file.name.startsWith(aSurveyName!!) }.min { f1: File, f2: File -> -f1.name.compareTo(f2.name) }.get()
    }

}