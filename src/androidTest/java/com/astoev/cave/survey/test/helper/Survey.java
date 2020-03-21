package com.astoev.cave.survey.test.helper;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.astoev.cave.survey.R.id.action_new_project;
import static com.astoev.cave.survey.R.id.main_action_add;
import static com.astoev.cave.survey.R.id.new_action_create;
import static com.astoev.cave.survey.R.id.new_projectname;
import static com.astoev.cave.survey.R.id.point_action_save;
import static com.astoev.cave.survey.R.id.point_azimuth;
import static com.astoev.cave.survey.R.id.point_distance;
import static com.astoev.cave.survey.R.id.point_down;
import static com.astoev.cave.survey.R.id.point_left;
import static com.astoev.cave.survey.R.id.point_main_view;
import static com.astoev.cave.survey.R.id.point_right;
import static com.astoev.cave.survey.R.id.point_slope;
import static com.astoev.cave.survey.R.id.point_up;
import static com.astoev.cave.survey.R.string.main_add_leg;
import static com.astoev.cave.survey.test.helper.Common.click;
import static com.astoev.cave.survey.test.helper.Common.type;
import static com.astoev.cave.survey.test.helper.Home.goHome;

public class Survey {

    public static void createSurvey(String aName) {
        // open new survey screen
        click(action_new_project);

        // enter name
        type(new_projectname, aName);

        // save & go back
        click(new_action_create);
        onView(withId(point_main_view)).perform(pressBack());
    }

    public static void openSurvey(String aName) {
        click(aName);
    }

    public static String createAndOpenSurvey() {
        final String surveyName = "xls" + System.currentTimeMillis();
        goHome();
        createSurvey(surveyName);
        openSurvey(surveyName);
        return surveyName;
    }

    public static void addLeg(float length, float azimuth, Float slope) {
        // press new
        click(main_action_add);

        // select leg
        click(main_add_leg);

        setLegData(length, azimuth, slope);
    }

    public static void selectFirstSurveyLeg() {
        click(main_action_add);
    }

    public static void setLegData(float length, float azimuth, Float slope) {
        setLegData(length, azimuth, slope, null, null, null, null);
    }


    public static void setLegData(float length, float azimuth, Float slope, Float up, Float down, Float left, Float right) {
        // populate
        type(point_distance, length);
        type(point_azimuth, azimuth);
        type(point_slope, slope);

        type(point_up, up);
        type(point_down, down);
        type(point_left, left);
        type(point_right, right);

        // save
        click(point_action_save);
    }


}
