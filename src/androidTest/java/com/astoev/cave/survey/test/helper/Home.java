package com.astoev.cave.survey.test.helper;

import androidx.test.runner.permission.PermissionRequester;

import com.astoev.cave.survey.R;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class Home {

    public static void goHome() {
        // grant needed permissions
        PermissionRequester permissionRequester = new PermissionRequester();
        permissionRequester.addPermissions(WRITE_EXTERNAL_STORAGE);
        permissionRequester.requestPermissions();

        // goes directly to the home screen
        onView(withId(R.id.homeProjects)).check(matches(isDisplayed()));
    }

    public static void createSurvey() {
        // open new survey screen
        onView(withId(R.id.action_new_project))
                .perform(click());

        // enter name
        onView(withId(R.id.new_projectname))
                .perform(typeText("S" + System.currentTimeMillis()));

        // save
        onView(withId(R.id.new_action_create))
                .perform(click());

    }
}
