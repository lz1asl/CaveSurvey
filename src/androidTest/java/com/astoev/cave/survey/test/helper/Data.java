package com.astoev.cave.survey.test.helper;

import static com.astoev.cave.survey.R.id.info_action_export_xls;
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
}
