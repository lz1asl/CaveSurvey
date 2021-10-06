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

    public static void deleteQuietly(DocumentFile projectFolder, String path) {
        if (path != null && projectFolder != null) {
            DocumentFile file = projectFolder.findFile(path);
            deleteQuietly(file);
        }
    }

    public static void deleteQuietly(DocumentFile aFile) {
        if (aFile != null) {
            boolean success = aFile.delete();
            if (!success) {
                Log.e(Constants.LOG_TAG_SERVICE, "Failed to delete " + aFile.getUri());
            }
        }
    }

    public static Uri getFileUri(File file) {
        return FileProvider.getUriForFile(ConfigUtil.getContext(),  "CaveSurvey.provider", file);
    }

}
