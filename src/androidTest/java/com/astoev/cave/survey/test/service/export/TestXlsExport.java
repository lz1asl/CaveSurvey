package com.astoev.cave.survey.test.service.export;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.astoev.cave.survey.activity.home.SplashActivity;

import org.junit.Rule;
import org.junit.Test;

import static com.astoev.cave.survey.test.helper.Data.dataScreen;
import static com.astoev.cave.survey.test.helper.Data.xlsExport;
import static com.astoev.cave.survey.test.helper.Home.goHome;
import static com.astoev.cave.survey.test.helper.Survey.addLeg;
import static com.astoev.cave.survey.test.helper.Survey.createSurvey;
import static com.astoev.cave.survey.test.helper.Survey.openSurvey;

public class TestXlsExport {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new ActivityTestRule<>(SplashActivity.class);


    @Test
    @LargeTest
    public void testCreateSurvey() {

//        clearDatabase();

        String surveyName = "xls" + System.currentTimeMillis();
        goHome();

        createSurvey(surveyName);
        openSurvey(surveyName);

        addLeg(1, 2);
        addLeg(1.2f, 2.2f);

        dataScreen();
        xlsExport();
    }
}
