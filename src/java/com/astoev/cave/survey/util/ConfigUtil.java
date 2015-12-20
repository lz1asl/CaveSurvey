package com.astoev.cave.survey.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by astoev on 12/27/13.
 *
 * @author astoev
 */
public class ConfigUtil {

    private static final String SHARED_PREFS_KEY = "CaveSurvey";
    public static final String PROP_CURR_PROJECT = "curr_project_id";
    public static final String PROP_CURR_LEG = "curr_leg_id";
    public static final String PROP_CURR_GALLERY = "curr_gallery_id";
    public static final String PROP_CURR_BT_DEVICE_ADDRESS = "curr_bt_device_addr";
    public static final String PROP_CURR_BT_DEVICE_NAME = "curr_bt_device_name";
    public static final String PREF_LOCALE = "language_pref";
    public static final String PREF_SENSOR = "sensor_pref";
    public static final String PREF_REVERSE_MEASUREMENTS = "reverse_measurements";
    public static final String PREF_REVERSE_MAX_DISTANCE_DIFF = "reverse_distance_diff";
    public static final String PREF_REVERSE_AZIMUTH_DIFF = "reverse_azimuth_diff";
    public static final String PREF_REVERSE_CLINO_DIFF = "reverse_clino_diff";

    private static Activity mAppContext;

    public static void setContext(Activity aContext) {
        mAppContext = aContext;
    }

    public static Activity getContext() {
        return mAppContext;
    }

    public static Integer getIntProperty(String aName) {
        return getPrefs().getInt(aName, 0);
    }

    public static boolean setIntProperty(String aName, int aValue) {
        return getEditor().putInt(aName, aValue).commit();
    }

    public static String getStringProperty(String aName) {
        return getPrefs().getString(aName, null);
    }

    public static boolean setStringProperty(String aName, String aValue) {
        return getEditor().putString(aName, aValue).commit();
    }

    public static boolean setFloatProperty(String aName, float aValue) {
        return getEditor().putFloat(aName, aValue).commit();
    }

    public static boolean setBooleanProperty(String aName, boolean aValue) {
        return getEditor().putBoolean(aName, aValue).commit();
    }

    public static Float getFloatProperty(String aName, float aDefaultValue) {
        return getPrefs().getFloat(aName, aDefaultValue);
    }

    public static Boolean getBooleanProperty(String aName, boolean aDefaultValue) {
        return getPrefs().getBoolean(aName, aDefaultValue);
    }

    public static boolean removeProperty(String aName) {
        return getEditor().remove(aName).commit();
    }


    private static SharedPreferences.Editor getEditor() {
        return getPrefs().edit();
    }

    private static SharedPreferences getPrefs() {
        return mAppContext.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
    }

    public static boolean isBackMeasurementsEnabled() {
        return getBooleanProperty(PREF_REVERSE_MEASUREMENTS, false);
    }


}
