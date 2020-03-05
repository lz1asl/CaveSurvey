package com.astoev.cave.survey.test.helper;

import com.astoev.cave.survey.R;

import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class Data {

    public static void dataScreen() {
        openContextualActionModeOverflowMenu();
        onIdle();
        onView(withText(R.string.main_button_data)).perform(click());
    }

    public static void xlsExport() {
        onView(withId(R.id.info_action_export)).perform(click());
    }
}
