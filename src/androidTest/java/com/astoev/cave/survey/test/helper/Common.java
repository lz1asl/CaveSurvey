package com.astoev.cave.survey.test.helper;

import android.content.Context;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.web.webdriver.Locator;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.webClick;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.anything;

public class Common {

    public static Context getTargetContext() {
        return getInstrumentation().getTargetContext();
    }

    public static Context getContext() {
        return getInstrumentation().getContext();
    }

    public static void click(int id) {
        onView(withId(id)).perform(ViewActions.click());
        onIdle();
    }

    public static void click(String text) {
        onView(withText(text)).perform(ViewActions.click());
        onIdle();
    }

    public static void webViewClick(String target) {
        onWebView().withElement(findElement(Locator.LINK_TEXT, target)).perform(webClick());
    }

    public static void scrollAndClick(String text) {
        onView(withText(text)).perform(scrollTo(), ViewActions.click());
        onIdle();
    }

    public static void clickDialogSpinnerAtPosition(int position) {
        onData(anything()).atPosition(position).perform(ViewActions.click());
        onIdle();
    }

    public static void type(int id, Number value) {
        if (value != null) {
            onView(withId(id)).perform(typeText("" + value));
            onIdle();
        }
    }

    public static void type(int id, String value) {
        if (value != null) {
            onIdle();
            onView(withId(id)).perform(typeText(value));
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
