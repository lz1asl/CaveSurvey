package com.astoev.cave.survey.test.helper

import android.content.Context
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.not

object Common {
    val context: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    fun click(id: Int) {
        Espresso.onView(ViewMatchers.withId(id)).perform(ViewActions.click())
        Espresso.onIdle()
    }

    fun click(text: String?) {
        Espresso.onView(withText(text)).perform(ViewActions.click())
        Espresso.onIdle()
    }

    fun clickWithDescription(description: String?) {
        Espresso.onView(ViewMatchers.withContentDescription(description)).perform(ViewActions.click())
        Espresso.onIdle()
    }

    fun webViewClick(target: String?) {
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.LINK_TEXT, target)).perform(DriverAtoms.webClick())
    }

    fun scrollAndClick(text: String?) {
        Espresso.onView(withText(text)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onIdle()
    }

    fun clickDialogSpinnerAtPosition(position: Int) {
        Espresso.onData(CoreMatchers.anything()).atPosition(position).perform(ViewActions.click())
        Espresso.onIdle()
    }

    fun type(id: Int, value: Number?) {
        if (value != null) {
            Espresso.onView(ViewMatchers.withId(id))
                .perform(ViewActions.click())
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText("" + value))
                .perform(ViewActions.closeSoftKeyboard())
            checkValue(id, "" + value)
        }
    }

    fun type(id: Int, value: String?) {
        if (value != null) {
            Espresso.onView(ViewMatchers.withId(id))
                    .perform(
                        ViewActions.click(),
                        ViewActions.typeText(value),
                        ViewActions.closeSoftKeyboard())
            checkValue(id, value)
        }
    }

    fun checkValue(id: Int, value: String) {
        Espresso.onView(ViewMatchers.withId(id))
            .check(matches(withText(value)))
    }

    fun checkVisible(id: Int) {
        Espresso.onView(ViewMatchers.withId(id)).check(matches(ViewMatchers.isDisplayed()))
    }

    fun checkVisible(text: String) {
        Espresso.onView(withText(text)).check(matches(ViewMatchers.isDisplayed()))
    }

    fun checkNotVisible(text: String) {
        Espresso.onView(withText(text)).check(ViewAssertions.doesNotExist())
    }

    fun goBack() {
        Espresso.pressBack()
    }

    fun openContextMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(context)
    }

    fun toggleSwitch(id: Int) {
        click(id)
    }

    fun verifySwitchState(id: Int, state: Boolean) {
        if (state) {
            Espresso.onView(withId(id)).check(matches(isChecked()));
        } else {
            Espresso.onView(withId(id)).check(matches(not(isChecked())));
        }

    }
}