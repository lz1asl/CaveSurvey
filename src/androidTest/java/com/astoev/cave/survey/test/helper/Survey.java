package com.astoev.cave.survey.test.helper;

import androidx.test.espresso.action.ViewActions;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.astoev.cave.survey.R.id.action_new_project;
import static com.astoev.cave.survey.R.id.import_files;
import static com.astoev.cave.survey.R.id.main_action_add;
import static com.astoev.cave.survey.R.id.middle_create;
import static com.astoev.cave.survey.R.id.middle_distance;
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
import static com.astoev.cave.survey.R.id.vector_add;
import static com.astoev.cave.survey.R.id.vector_azimuth;
import static com.astoev.cave.survey.R.id.vector_distance;
import static com.astoev.cave.survey.R.id.vector_slope;
import static com.astoev.cave.survey.test.helper.Common.click;
import static com.astoev.cave.survey.test.helper.Common.clickDialogSpinnerAtPosition;
import static com.astoev.cave.survey.test.helper.Common.openContextMenu;
import static com.astoev.cave.survey.test.helper.Common.type;
import static com.astoev.cave.survey.test.helper.Data.getXlsExportFilesCount;
import static com.astoev.cave.survey.test.helper.Home.goHome;
import static org.hamcrest.Matchers.anything;

public class Survey {

    public static void createSurvey(String aName) {
        createSurvey(aName, false);
    }

    public static void createSurvey(String aName, boolean importFile) {
        // open new survey screen
        click(action_new_project);

        if (importFile) {
            selectLastImport();
        }

        // enter name
        type(new_projectname, aName);

        // save & go back
        click(new_action_create);

        if (!importFile) {
            onView(withId(point_main_view)).perform(pressBack());
        }
    }

    public static void selectLastImport() {
        click(import_files);
        onData(anything()).atPosition(getXlsExportFilesCount())
                .perform(scrollTo(), ViewActions.click());
        onIdle();
    }

    public static void openSurvey(String aName) {
        click(aName);
    }

    public static String createAndOpenSurvey() {
        return createAndOpenSurvey(false);
    }

    public static String createAndOpenSurvey(boolean importFile) {
        final String surveyName = "" + System.currentTimeMillis();
        goHome();
        createSurvey(surveyName, importFile);
        openSurvey(surveyName);
        return surveyName;
    }

    public static void addLeg(float length, float azimuth, Float slope) {
        addLeg(length, azimuth, slope, null, null, null, null);
    }

    public static void addLeg(float length, float azimuth, Float slope, Float up, Float down, Float left, Float rigt) {
        // press new
        click(main_action_add);

        // select the leg option
        clickDialogSpinnerAtPosition(0);

        setLegData(length, azimuth, slope, up, down, left, rigt);
    }

    public static void addLegMiddle(float distance, float up, float down, float left, float right) {
        // press new
        click(main_action_add);

        // select leg
        clickDialogSpinnerAtPosition(2);

        // middle at
        type(middle_distance, distance);

        // save
        click(middle_create);

        // populate
        setLegData(null, null, null, up, down, left, right);
    }

    public static void selectFirstSurveyLeg() {
        click(main_action_add);
    }

    public static void setLegData(Float length, Float azimuth, Float slope) {
        setLegData(length, azimuth, slope, null, null, null, null);
    }

    public static void setLegData(Float length, Float azimuth, Float slope, Float up, Float down, Float left, Float right) {
        // populate
        type(point_distance, length);
        type(point_azimuth, azimuth);
        type(point_slope, slope);

        type(point_up, up);
        type(point_down, down);
        type(point_left, left);
        type(point_right, right);

        // save
        saveLeg();
    }

    public static void addVector(float aDistance, float anAzimuth, float aSlope) {
        openContextMenu();

        // select leg
        click("Add vector");

        // set data
        type(vector_distance, aDistance);
        type(vector_azimuth, anAzimuth);
        type(vector_slope, aSlope);

        // save
        click(vector_add);
    }

    public static void nextGallery() {
        // press new
        click(main_action_add);

        // select gallery
        clickDialogSpinnerAtPosition(1);
    }

    public static void openLegWithText(String text) {
        onView(withText(text)).perform(ViewActions.click());
    }

    public static void saveLeg() {
        click(point_action_save);
    }

}
