package com.astoev.cave.survey.service.reports;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.NetworkUnil;
import com.astoev.cave.survey.util.StreamUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to report application errors.
 *
 * Created by astoev on 10/22/15.
 */
public class ErrorReporter {

    // where to report the errors
    private static final String REPORTS_SERVER_URL = "https://cavesurveyreports.herokuapp.com/errors";

    private static LogCatDumpThread logsDumpThread;

    public static void startDebugSession() {
        // start dump thread, it will clean existing logs
        logsDumpThread = new LogCatDumpThread();
        logsDumpThread.start();
    }

    public static String closeDebugSession() {
        // stop dump thread
        String logFile = logsDumpThread.stopCollection();
        logsDumpThread = null;

        return logFile;
    }

    public static boolean isDebugRunning() {
        return logsDumpThread != null;
    }

    public static void reportToServer(String aMessage, String aLogFile) {

        // ensure can send the report
        if (!NetworkUnil.isNetworkAvailable()) {
            Log.e(Constants.LOG_TAG_SERVICE, "No network service available");
            UIUtilities.showNotification(R.string.network_unavailable);
            return;
        }

        Log.i(Constants.LOG_TAG_SERVICE, "Preparing report body");

        // collect data
        String content = null;
        try {
            content = prepareReportBody(aMessage, aLogFile);
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed to generate report", e);
            UIUtilities.showNotification(R.string.error);
        }

        Log.i(Constants.LOG_TAG_SERVICE, "Will report : " + content);

        // post
        try {
            NetworkUnil.postJSON(REPORTS_SERVER_URL, content);
            Log.i(Constants.LOG_TAG_SERVICE, "Report scheduled");
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed to talk to server", e);
            UIUtilities.showNotification(R.string.network_error);
        }

    }

    private static String prepareReportBody(String aMessage, String aLogFile) throws JSONException, PackageManager.NameNotFoundException, IOException {
        JSONObject report = new JSONObject();

        Activity context = ConfigUtil.getContext();

        // CaveSurvey app details
        JSONObject version = new JSONObject();
        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        version.put("versionName", info.versionName);
        version.put("versionCode", info.versionCode);
        version.put("firstInstall", info.firstInstallTime);
        version.put("lastUpdate", info.lastUpdateTime);
        //        version.put("installLocation", info.installLocation);
        report.put("cave_survey_app", version);

        // Android device details
        JSONObject device = new JSONObject();
        device.put("MANUFACTURER", Build.MANUFACTURER);
        device.put("MODEL", Build.MODEL);
        device.put("BRAND", Build.BRAND);
        device.put("DEVICE", Build.DEVICE);
        device.put("DISPLAY", Build.DISPLAY);
        device.put("VERSION.RELEASE", Build.VERSION.RELEASE);
        device.put("VERSION.SDK_INT", Build.VERSION.SDK_INT);
        report.put("android_device", device);

        // the user message
        report.put("message", aMessage);

        // The log file contents
        InputStream in = null;
        try {
            // load
            in = new FileInputStream(aLogFile);
            String errorContents = new String(StreamUtil.read(in));

            // need to adjust bad new lines
            errorContents = errorContents.replaceAll("D/", "\n");
            errorContents = errorContents.replaceAll("I/", "\n");
            errorContents = errorContents.replaceAll("W/", "\n");

            // it's good idea to zip the contents once the report get big
            report.put("error", errorContents);
        } finally {
            StreamUtil.closeQuietly(in);
        }

        return report.toString();
    }
}
