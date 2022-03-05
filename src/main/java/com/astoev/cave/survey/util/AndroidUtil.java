package com.astoev.cave.survey.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.astoev.cave.survey.Constants;

public class AndroidUtil {

    public static String getAppVersion() {
        try {
            Context context = ConfigUtil.getContext();
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.LOG_TAG_UI, "Failed get version for about dialog", e);
            return "";
        }
    }

    public static boolean isAppPresent(String name) {
        try {
            ConfigUtil.getContext().getPackageManager().getPackageInfo(name, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
