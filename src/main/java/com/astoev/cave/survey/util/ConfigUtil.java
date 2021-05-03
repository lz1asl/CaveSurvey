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
    public static final String PROP_STORAGE_PATH = "storage_path";
    public static final String PROP_CURR_LEG = "curr_leg_id";
    public static final String PROP_CURR_GALLERY = "curr_gallery_id";
    public static final String PROP_CURR_BT_DEVICE_ADDRESS = "curr_bt_device_addr";
    public static final String PROP_CURR_BT_DEVICE_NAME = "curr_bt_device_name";
    public static final String PREF_LOCALE = "language_pref";
    public static final String PREF_SENSOR = "sensor_pref";
    public static final String PREF_AUTO_BACKUP = "backup_pref";
    public static final String PREF_SENSOR_TIMEOUT = "sensor_timeout";
    public static final String PREF_DEVICE_HEADING_CAMERA = "device_heading";
    public static final String PREF_SENSOR_SIMULTANEOUSLY = "sensor_simulateneously";
    public static final String PREF_SENSOR_NOISE_REDUCTION = "sensor_noise_reduction";
    public static final String PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS = "sensor_noise_reduction_measurements";
    public static final String PREF_VECTORS_MODE = "vectors_mode";

    private static Context mAppContext;

    public static void setContext(Context aContext) {
        mAppContext = aContext;
    }

    public static Activity getContext() {
        return (Activity) mAppContext;
    }

    public static Integer getIntProperty(String aName) {
        return getIntProperty(aName, 0);
    }

    public static Integer getIntProperty(String aName, Integer aDefaultValue) {
        return getPrefs(Context.MODE_PRIVATE).getInt(aName, aDefaultValue);
    }

    public static boolean setIntProperty(String aName, Integer aValue) {
        SharedPreferences.Editor editor = getPrefs(Context.MODE_PRIVATE).edit();
        editor.putInt(aName, aValue);
        return editor.commit();
    }

    public static String getStringProperty(String aName) {
        return getPrefs(Context.MODE_PRIVATE).getString(aName, null);
    }

    public static boolean setStringProperty(String aName, String aValue) {
        SharedPreferences.Editor editor = getPrefs(Context.MODE_PRIVATE).edit();
        editor.putString(aName, aValue);
        return editor.commit();
    }

    public static Boolean getBooleanProperty(String aName) {
        return getPrefs(Context.MODE_PRIVATE).getBoolean(aName, false);
    }

    public static Boolean getBooleanProperty(String aName, Boolean defaultValue) {
        return getPrefs(Context.MODE_PRIVATE).getBoolean(aName, defaultValue);
    }

    public static boolean setBooleanProperty(String aName, boolean aValue) {
        SharedPreferences.Editor editor = getPrefs(Context.MODE_PRIVATE).edit();
        editor.putBoolean(aName, aValue);
        return editor.commit();
    }

    public static boolean removeProperty(String aName) {
        SharedPreferences.Editor editor = getPrefs(Context.MODE_PRIVATE).edit();
        editor.remove(aName);
        return editor.commit();
    }


    private static SharedPreferences getPrefs(int aMode) {
        return mAppContext.getSharedPreferences(SHARED_PREFS_KEY, aMode);
    }

}
