package com.astoev.cave.survey.service.reports;

import java.io.File;

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

        // TODO rotate old file

        // TODO start thread

    }

    public String stopCollection() {
        // TODO interrupt collectoion and thread

        // return the location of the log file
        return logFile.getAbsolutePath();
    }

    /**
     *
     private void runDumpLogThread() {
     new Thread() {
    @Override
    public void run() {
    OutputStream out = null;
    try {

    Process process = Runtime.getRuntime().exec("logcat -d");
    BufferedReader bufferedReader = new BufferedReader(
    new InputStreamReader(process.getInputStream()));
    File logFile = new File(FileStorageUtil.getStorageHome(), "CaveSurvey.log");
    out = new FileOutputStream(logFile);

    String line;
    while ((line = bufferedReader.readLine()) != null) {
    IOUtils.write(line, out);
    Thread.sleep(100);
    }

    } catch (Exception e) {
    Log.e(Constants.LOG_TAG_SERVICE, "Failed to copy output", e);
    } finally {
    IOUtils.closeQuietly(out);
    }
    }
    }.start();
     }
     */
}
