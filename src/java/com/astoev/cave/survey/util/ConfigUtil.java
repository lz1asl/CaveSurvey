package com.astoev.cave.survey.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by astoev on 12/27/13.
 */
public class ConfigUtil {

    private static final String SHARED_PREFS_KEY = "CaveSurvey";
    public static final String PROP_CURR_PROJECT = "curr_project_id";
    public static final String PROP_CURR_LEG = "curr_leg_id";

    private static Context mAppContext;

    public static void setContext(Context aContext) {
        mAppContext = aContext;
    }

    public static Context getContext() {
        return mAppContext;
    }

    public static Integer getIntProperty(String aName) {
        return getPrefs(Context.MODE_WORLD_READABLE).getInt(aName, 0);
    }

    public static boolean setIntProperty(String aName, Integer aValue) {
        SharedPreferences.Editor editor = getPrefs(Context.MODE_WORLD_WRITEABLE).edit();
        editor.putInt(aName, aValue);
        return editor.commit();
    }

    private static SharedPreferences getPrefs(int aMode) {
        return mAppContext.getSharedPreferences(SHARED_PREFS_KEY, aMode);
    }

}
