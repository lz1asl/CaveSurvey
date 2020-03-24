package com.astoev.cave.survey.test.helper;

import android.content.Context;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;

public class Common {

    public static Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    public static void click(int id) {
        onView(withId(id)).perform(ViewActions.click());
        onIdle();
    }

    public static void click(String text) {
        onView(withText(text)).perform(ViewActions.click());
        onIdle();
    }

    public static void clickDialogSpinnerAtPosition(int position) {
        onData(anything()).atPosition(position).perform(ViewActions.click());
        onIdle();
    }

    public static void type(int id, Float value) {
        if (value != null) {
            onView(withId(id)).perform(typeText("" + value));
            onIdle();
        }
    }

    public static void type(int id, String value) {
        if (value != null) {
            onIdle();
            onView(withId(id)).noActivity().perform(typeText(value));
            onIdle();
        }
    }

    public static void checkVisible(int id) {
        onView(withId(id)).check(matches(isDisplayed()));
    }

    public static void goBack() {
        Espresso.pressBack();
    }

    public static void openContextMenu() {
        openActionBarOverflowOrOptionsMenu(getContext());
    }
}
