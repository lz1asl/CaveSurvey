package com.astoev.cave.survey.test.ui;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.astoev.cave.survey.activity.home.SplashActivity;

import org.junit.Rule;
import org.junit.Test;

import static com.astoev.cave.survey.test.helper.Home.goHome;

public class SplashScreenTests {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new ActivityTestRule<>(SplashActivity.class);


    @Test
    @LargeTest
    public void testStoragePermissionGranted() {
        goHome();
    }

}
