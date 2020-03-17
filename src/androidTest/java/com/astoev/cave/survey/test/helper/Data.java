package com.astoev.cave.survey.test.helper;

import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu;
import static com.astoev.cave.survey.R.id.info_action_export;
import static com.astoev.cave.survey.R.string.main_button_data;
import static com.astoev.cave.survey.test.helper.Common.click;

public class Data {

    public static void dataScreen() {
        openContextualActionModeOverflowMenu();
        onIdle();
        click(main_button_data);
    }

    public static void xlsExport() {
        click(info_action_export);
    }
}
