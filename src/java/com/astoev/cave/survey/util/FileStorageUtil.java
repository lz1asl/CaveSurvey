package com.astoev.cave.survey.util;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.Project;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by astoev on 12/25/13.
 */
public class FileStorageUtil {

    private static final String EXCEL_FILE_EXTENSION = ".xls";
    private static final String NAME_DELIMITER = "_";

    private static final String CAVE_SURVEY_FOLDER = "CaveSurvey";
    private static final String TIME_PATTERN = "yyyyMMdd";
    private static final int MIN_REQUIRED_STORAGE = 50 * 1024;


    public static File getProjectHome(Project aProject) {
        File storageHome = getStorageHome();
        if (storageHome == null) {
            return null;
        }
        File projectHome = new File(storageHome, aProject.getName());
        if (!projectHome.exists()) {
            boolean projectHomeCreated = projectHome.mkdirs();
            if (!projectHomeCreated) {
                Log.e(Constants.LOG_TAG_UI, "Failed to create folder " + projectHome.getAbsolutePath());
                return null;
            }
            Log.i(Constants.LOG_TAG_SERVICE, "Project home created");
        }
        return projectHome;
    }

    public static String addProjectExport(Project aProject, InputStream aStream) {

        File projectHome = getProjectHome(aProject);
        if (projectHome == null) {
            return null;
        }

        FileOutputStream out = null;
        try {

            int index = 1;
            String exportName;
            File exportFile;
            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);

            // ensure unique name
            while (true) {
                exportName = aProject.getName() + NAME_DELIMITER + dateFormat.format(new Date()) + NAME_DELIMITER + index;
                exportFile = new File(projectHome, exportName + EXCEL_FILE_EXTENSION);
                if (exportFile.exists()) {
                    index++;
                } else {
                    break;
                }
            }

            Log.i(Constants.LOG_TAG_SERVICE, "Store to " + exportFile.getAbsolutePath());

            out = new FileOutputStream(exportFile);
            IOUtils.copy(aStream, out);
            return exportFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to store export", e);
            return null;
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(aStream);
        }
    }

    public static String addProjectMedia(Project aProject, InputStream aStream) {

        // store
        // TODO

        // add in Gallery
        // TODO

        return null;
    }

    private static File getStorageHome() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e(Constants.LOG_TAG_UI, "Storage unavailable");
            return null;
        }
        File extdir = Environment.getExternalStorageDirectory();
        StatFs stats = new StatFs(extdir.getAbsolutePath());
        int availableBytes = stats.getAvailableBlocks() * stats.getBlockSize();
        if (availableBytes < MIN_REQUIRED_STORAGE) {
            Log.e(Constants.LOG_TAG_UI, "No space left");
            return null;
        }

        File storageHome = new File(Environment.getExternalStorageDirectory() + File.separator + CAVE_SURVEY_FOLDER);
        if (!storageHome.exists()) {
            boolean exportFolderCreated = storageHome.mkdirs();
            if (!exportFolderCreated) {
                Log.e(Constants.LOG_TAG_UI, "Failed to create folder " + storageHome.getAbsolutePath());
                return null;
            }
            Log.i(Constants.LOG_TAG_SERVICE, "Export folder created");
        }
        return storageHome;
    }
}
