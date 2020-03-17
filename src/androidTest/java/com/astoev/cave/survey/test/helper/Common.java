package com.astoev.cave.survey.test.helper;

import android.content.Context;

import androidx.test.espresso.action.ViewActions;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class Common {

    public static Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    public static void click(int id) {
        onView(withId(id)).perform(ViewActions.click());
    }

    public static void click(String text) {
        onView(withText(text)).perform(ViewActions.click());
    }

    public static void type(int id, Float value) {
        if (value != null) {
            onView(withId(id)).perform(typeText("" + value));
        }
    }

    public static void type(int id, String value) {
        onView(withId(id)).perform(typeText(value));
    }

    public static void checkVisible(int id) {
        onView(withId(id)).check(matches(isDisplayed()));
    }
}
