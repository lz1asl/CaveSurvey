package com.astoev.cave.survey.test.service.export;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.permission.PermissionRequester;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.home.SplashActivity;

import org.junit.Rule;
import org.junit.Test;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class SplashScreenTests {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new ActivityTestRule<>(SplashActivity.class);


    @Test
    @LargeTest
    public void testStoragePermissionGranted() {
        // grant needed permissions
        PermissionRequester permissionRequester = new PermissionRequester();
        permissionRequester.addPermissions(WRITE_EXTERNAL_STORAGE);
        permissionRequester.requestPermissions();

        // goes directly to the home screen
        onView(withId(R.id.homeProjects)).check(matches(isDisplayed()));
    }

}
