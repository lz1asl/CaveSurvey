package com.astoev.cave.survey.util;

import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.astoev.cave.survey.Constants;

import java.io.File;

/**
 * A set of tools for file operations
 */
public class FileUtils {

    public static void deleteQuietly(DocumentFile aFile) {
        if (aFile != null) {
            try {
                aFile.delete();
            } catch (Exception e) {
                // noop
                Log.e(Constants.LOG_TAG_SERVICE, "Failed to delete " + aFile, e);
            }
        }
    }

    public static Uri getFileUri(File file) {
        return FileProvider.getUriForFile(ConfigUtil.getContext(),  "CaveSurvey.provider", file);
    }

}
