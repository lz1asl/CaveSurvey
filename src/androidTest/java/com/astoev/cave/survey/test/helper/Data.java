package com.astoev.cave.survey.test.helper;

import com.astoev.cave.survey.util.FileStorageUtil;

import java.io.File;
import java.util.List;

import static com.astoev.cave.survey.R.id.info_action_export_xls;
import static com.astoev.cave.survey.R.id.info_action_openstopo;
import static com.astoev.cave.survey.service.export.excel.ExcelExport.EXCEL_FILE_EXTENSION;
import static com.astoev.cave.survey.test.helper.Common.click;
import static com.astoev.cave.survey.test.helper.Common.openContextMenu;

public class Data {

    public static void dataScreen() {
        openContextMenu();
        click("Data");
    }

    public static void xlsExport() {
        click(info_action_export_xls);
    }

    public static void visualTopoExport() {
        openContextMenu();
        click("Visual Topo export");
    }

    public static void opensTopoExport() {
        click(info_action_openstopo);
    }

    public static File getLastXlsExport(String aSurveyName) {
        List<File> excelExportFiles = FileStorageUtil.listProjectFiles(null, EXCEL_FILE_EXTENSION);
        return excelExportFiles.stream()
                .filter(file -> file.getName().startsWith(aSurveyName)).min((f1, f2) -> -f1.getName().compareTo(f2.getName())).get();
    }

    public static int getXlsExportFilesCount() {
        return FileStorageUtil.listProjectFiles(null, EXCEL_FILE_EXTENSION).size();
    }


}
