package com.astoev.cave.survey.test.ui;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.astoev.cave.survey.activity.home.SplashActivity;
import com.astoev.cave.survey.test.helper.Home;

import org.junit.Rule;
import org.junit.Test;

public class HomeScreenTest {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new ActivityTestRule<>(SplashActivity.class);


    @Test
    @LargeTest
    public void testCreateSurvey() {
        Home.goHome();
        Home.createSurvey();
    }
}
