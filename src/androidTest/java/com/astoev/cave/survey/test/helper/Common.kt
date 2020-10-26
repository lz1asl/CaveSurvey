package com.astoev.cave.survey.test.helper

import android.content.Context
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers

object Common {
    val targetContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
    val context: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    fun click(id: Int) {
        Espresso.onView(ViewMatchers.withId(id)).perform(ViewActions.click())
//        Espresso.onIdle()
    }

    fun click(text: String?) {
        Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.click())
//        Espresso.onIdle()
    }

    fun webViewClick(target: String?) {
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.LINK_TEXT, target)).perform(DriverAtoms.webClick())
    }

    fun scrollAndClick(text: String?) {
        Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onIdle()
    }

    fun clickDialogSpinnerAtPosition(position: Int) {
        Espresso.onData(CoreMatchers.anything()).atPosition(position).perform(ViewActions.click())
//        Espresso.onIdle()
    }

    fun type(id: Int, value: Number?) {
        if (value != null) {
            Espresso.onView(ViewMatchers.withId(id)).perform(ViewActions.typeText("" + value))
//            Espresso.onIdle()
        }
    }

    fun type(id: Int, value: String?) {
        if (value != null) {
//            Espresso.onIdle()
            Espresso.onView(ViewMatchers.withId(id)).perform(ViewActions.typeText(value))
//            Espresso.onIdle()
        }
    }

    fun checkVisible(id: Int) {
        Espresso.onView(ViewMatchers.withId(id)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    fun goBack() {
        Espresso.pressBack()
    }

    fun openContextMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(context)
    }
}