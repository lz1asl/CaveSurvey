package com.astoev.cave.survey.util;

import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * A set of tools for file operations
 */
public class FileUtils {

    public static void deleteQuietly(File aFile) {
        if (aFile != null) {
            try {
                aFile.delete();
            } catch (Exception e) {
                // noop
            }
        }
    }

    public static Uri getFileUri(File file) {
        return FileProvider.getUriForFile(ConfigUtil.getContext(),  "CaveSurvey.provider", file);
    }

}
