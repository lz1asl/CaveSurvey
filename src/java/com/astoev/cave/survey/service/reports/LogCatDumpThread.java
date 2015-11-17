package com.astoev.cave.survey.service.reports;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StreamUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Dumps android logs to a file, see http://stackoverflow.com/questions/6175002/write-android-logcat-data-to-a-file
 *
 * Created by astoev on 10/22/15.
 */
public class LogCatDumpThread extends Thread {

    private static final String LOG_FILE_NAME = "CaveSurvey.log";

    private File logFile;
    private Process process;
    private boolean running = false;

    @Override
    public void run() {

        Log.i(Constants.LOG_TAG_SERVICE, "Starting debug session");

        OutputStream out = null;
        BufferedReader in = null;

        try {
            // start logcat to monitor the logs
            process = Runtime.getRuntime().exec("logcat -d");
            running = true;

            // store logcat output to a file
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            logFile = new File(FileStorageUtil.getStorageHome(), LOG_FILE_NAME);
            // TODO rotate old file, don't override
            out = new FileOutputStream(logFile);

            String line;
            while (running) {
                line = in.readLine();
                if (line != null) {
                    out.write(line.getBytes());
                    out.flush();
                }
                Thread.sleep(100);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed to dump logs", e);
            UIUtilities.showNotification("Failed to dump logs");
        } finally {
            StreamUtil.closeQuietly(out);
            StreamUtil.closeQuietly(in);
        }

        Log.i(Constants.LOG_TAG_SERVICE, "End debug session");
    }

    public String stopCollection() {

        Log.i(Constants.LOG_TAG_SERVICE, "End debug session requested");

        // interrupt collectoion and thread
        running = false;
        process.destroy();

        // return the location of the log file
        return logFile.getAbsolutePath();
    }

}
