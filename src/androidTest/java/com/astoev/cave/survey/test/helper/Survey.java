package com.astoev.cave.survey.test.helper;

import com.astoev.cave.survey.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class Survey {

    public static void createSurvey(String aName) {
        // open new survey screen
        onView(withId(R.id.action_new_project)).perform(click());

        // enter name
        onView(withId(R.id.new_projectname)).perform(typeText(aName));

        // save & go back
        onView(withId(R.id.new_action_create)).perform(click());
        onView(withId(R.id.point_main_view)).perform(pressBack());
    }

    public static void openSurvey(String aName) {
        onView(withText(aName)).perform(click());
    }

    public static void addLeg(float length, float azimuth) {
        // press new
        onView(withId(R.id.main_action_add)).perform(click());

        // select leg
        onView(withText(R.string.main_add_leg)).perform(click());

        setLegData(length, azimuth);
    }

    public static void selectFirstSurveyLeg() {
        onView(withId(R.id.main_action_add)).perform(click());
    }

    public static void setLegData(float length, float azimuth) {
        // populate
        onView(withId(R.id.point_distance)).perform(typeText("" + length));
        onView(withId(R.id.point_azimuth)).perform(typeText("" + azimuth));

        // save
        onView(withId(R.id.point_action_save)).perform(click());
    }
}
