package com.astoev.cave.survey.test.helper;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

public class Common {

    public static Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
}
