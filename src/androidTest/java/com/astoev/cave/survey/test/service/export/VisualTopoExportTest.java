package com.astoev.cave.survey.test.service.export;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.astoev.cave.survey.activity.home.SplashActivity;
import com.astoev.cave.survey.test.helper.Survey;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.astoev.cave.survey.test.helper.Data.dataScreen;
import static com.astoev.cave.survey.test.helper.Survey.addLeg;
import static com.astoev.cave.survey.test.helper.Survey.selectFirstSurveyLeg;
import static com.astoev.cave.survey.test.helper.Survey.setLegData;

public class VisualTopoExportTest {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new ActivityTestRule<>(SplashActivity.class);

    @Test
    @LargeTest
    public void testExport() throws IOException {

        // create survey
        final String surveyName = Survey.createAndOpenSurvey();

        // add data
        selectFirstSurveyLeg();
        setLegData(1f, 2f, null);
        addLeg(1.2f, 2.2f, 1.2f);

        // export
        dataScreen();

        // TODO
    }

}
