package com.astoev.cave.survey.test.helper;

import androidx.test.runner.permission.PermissionRequester;

import com.astoev.cave.survey.R;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
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

}
