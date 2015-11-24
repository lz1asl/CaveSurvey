package com.astoev.cave.survey.util;

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
}
