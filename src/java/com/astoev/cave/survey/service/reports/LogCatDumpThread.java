package com.astoev.cave.survey.service.reports;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.util.FileStorageUtil;

import org.apache.commons.io.IOUtils;

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

        OutputStream out = null;
        BufferedReader in = null;

        try {
            // start logcat to monitor the logs
            process = Runtime.getRuntime().exec("logcat -d");

            // store logcat output to a file
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            File logFile = new File(FileStorageUtil.getStorageHome(), LOG_FILE_NAME);
            // TODO rotate old file, don't override
            out = new FileOutputStream(logFile);

            String line;
            while (running) {
                line = in.readLine();
                if (line != null) {
                    IOUtils.write(line, out);
                }
                Thread.sleep(100);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed to dump logs", e);
            UIUtilities.showNotification("Failed to dump logs");
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }


    }

    public String stopCollection() {

        // interrupt collectoion and thread
        running = false;
        process.destroy();

        // return the location of the log file
        return logFile.getAbsolutePath();
    }

}
