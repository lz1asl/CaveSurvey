package com.astoev.cave.survey.test.ui;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.astoev.cave.survey.activity.home.SplashActivity;

import org.junit.Rule;
import org.junit.Test;

import static com.astoev.cave.survey.test.helper.Home.goHome;
import static com.astoev.cave.survey.test.helper.Survey.createSurvey;

public class SurveysScreenTest {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new ActivityTestRule<>(SplashActivity.class);


    @Test
    @LargeTest
    public void testCreateSurvey() {
        goHome();
        createSurvey("New");
    }
}
