package com.astoev.cave.survey.util;

import static com.astoev.cave.survey.Constants.LOG_TAG_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by astoev on 12/25/13.
 *
 * @author - astoev
 * @author - jmitrev
 */
public class FileStorageUtil {

    private static final String PNG_FILE_EXTENSION = ".png";
    public static final String NAME_DELIMITER = "_";
    private static final Character NAME_DELIMITER_CHAR = '_';

    public static final String JPG_FILE_EXTENSION = ".jpg";
    public static final String MIME_TYPE_JPG = "image/jpeg";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String POINT_PREFIX = "Point";
    public static final String MAP_PREFIX = "Map";

    private static final String FOLDER_DOCUMENTS = "Documents";
    public static final String FOLDER_CAVE_SURVEY = "CaveSurvey";
    private static final int MIN_REQUIRED_STORAGE = 50 * 1024;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");


    private static DocumentFile home;


    @SuppressLint("SimpleDateFormat")
    public static DocumentFile addProjectExport(Project aProject, InputStream aStream, String aMimeType, String anExtension, boolean unique) {

        DocumentFile projectHome = getProjectHome(aProject.getName());
        if (projectHome == null) {
            return null;
        }

        int index = 1;
        String exportName;

        if (unique) {
            // ensure unique name
            while (true) {
                exportName = getUniqueExportName(aProject.getName(), index) + anExtension;
                if (projectHome.findFile(exportName) != null ) {
                    index++;
                } else {
                    break;
                }
            }
        } else {
            // export file might get overriden
            exportName = getNormalizedProjectName(aProject.getName()) + anExtension;
            DocumentFile existingExport = projectHome.findFile(exportName);
            if (existingExport != null) {
                Log.i(LOG_TAG_SERVICE, "Overriding " + existingExport.getUri());
                existingExport.delete();
            }
        }

        DocumentFile exportFile = projectHome.createFile(aMimeType, exportName);
        boolean success = writeStreamToFile(aStream, exportFile);
        return success ? exportFile : null;
    }

    private static boolean writeStreamToFile(InputStream aStream, DocumentFile aExportFile) {
        OutputStream out = null;
        try {
            Log.i(LOG_TAG_SERVICE, "Store to " + aExportFile.getUri());
            out = ConfigUtil.getContext().getContentResolver().openOutputStream(aExportFile.getUri());
            StreamUtil.copy(aStream, out);
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to store export", e);
            return false;
        } finally {
            StreamUtil.closeQuietly(out);
            StreamUtil.closeQuietly(aStream);
        }
        return true;
    }

    public static String getUniqueExportName(String aProjectName, int aIndex) {
        return getNormalizedProjectName(aProjectName) + NAME_DELIMITER + DATE_FORMATTER.format(new Date()) + NAME_DELIMITER + aIndex;
    }

    public static DocumentFile addProjectFile(Activity contextArg, Project aProject, String filePrefixArg, String fileSuffixArg, String mimeType, byte[] byteArrayArg, boolean unique) throws Exception {

        DocumentFile pictureFile = createPictureFile(contextArg, getNormalizedProjectName(aProject.getName()), filePrefixArg, fileSuffixArg, mimeType, unique);

        writeStreamToFile(new ByteArrayInputStream(byteArrayArg), pictureFile);

        Log.i(LOG_TAG_SERVICE, "Just wrote: " + pictureFile.getUri());

        return pictureFile;
    }

    /**
     * Helper method that adds project's media content to public external storage
     *
     * @param contextArg    - context
     * @param aProject      - project owner
     * @param filePrefixArg - file prefix
     * @param byteArrayArg  - media content as a byte array
     * @return String for the file name created
     * @throws Exception
     */
    public static DocumentFile addProjectMedia(Activity contextArg, Project aProject, String filePrefixArg, String mimeType, byte[] byteArrayArg) throws Exception {
        DocumentFile pictureFile = addProjectFile(contextArg, aProject, filePrefixArg, PNG_FILE_EXTENSION, mimeType, byteArrayArg, true);

        // broadcast that picture was added to the project
        notifyPictureAddedToGallery(contextArg, pictureFile);

        return pictureFile;
    }

    /**
     * Helper method that creates a prefix name for picture files based on Point objects
     *
     * @param pointArg       - parent point
     * @param galleryNameArg - name of the gallery
     * @return
     */
    public static String getFilePrefixForPicture(Point pointArg, String galleryNameArg) {
        return POINT_PREFIX + NAME_DELIMITER + galleryNameArg + pointArg.getName();
    }

    /**
     * Helper method to create a picture file. The file will be named <prefix>-<date_format><extension>. The
     * file is stored in public folder based on the project's name. ../CaveSurvay/<project_name>/<file>
     *
     * @param contextArg       - context to use
     * @param projectName      - project's name
     * @param filePrefix       - file prefix
     * @param fileExtensionArg - extension for the file.
     * @param unique
     * @return
     * @throws Exception
     */
    @SuppressLint("SimpleDateFormat")
    public static DocumentFile createPictureFile(Context contextArg, String projectName, String filePrefix, String fileExtensionArg, String mimeType, boolean unique)
            throws Exception {

        // Store in file system
        DocumentFile destinationDir = getProjectHome(projectName);

        Log.i(Constants.LOG_TAG_SERVICE, "Will write at: " + destinationDir.getUri());

        // build filename
        StringBuilder fileName = new StringBuilder();
        if (filePrefix != null) {
            fileName.append(filePrefix);
        }
        if (unique) {
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
            fileName.append(NAME_DELIMITER);
            fileName.append(df.format(date));
        }
        fileName.append(fileExtensionArg);

        return destinationDir.createFile(mimeType, fileName.toString());
    }

    public static DocumentFile getProjectHome(String projectName) {

        String normalizedName = getNormalizedProjectName(projectName);
        DocumentFile projectHome = getStorageHome(normalizedName);
        if (!projectHome.exists()) {
            projectHome = projectHome.createDirectory(projectName);
            Log.i(LOG_TAG_SERVICE, "Project folder created: " + projectName);
        }
        return projectHome;
    }

    public static DocumentFile getProjectHome(Integer projectId) throws SQLException {

        Project project = DaoUtil.getProject(projectId);
        return getProjectHome(project.getName());
    }

    public static String getNormalizedProjectName(String projectName) {
        return projectName.replace(' ', NAME_DELIMITER_CHAR)
                .replace(':', NAME_DELIMITER_CHAR);
            // and probably others to go
    }

    @SuppressWarnings("deprecation")
    public static DocumentFile getStorageHome() {

        if (home != null) {
            return home;
        }

        String storedHome = ConfigUtil.getStringProperty(ConfigUtil.PROP_STORAGE_PATH);
        if (storedHome != null) {
            Log.i(LOG_TAG_SERVICE, "Using predefined home " + storedHome);
            home = DocumentFile.fromTreeUri(ConfigUtil.getContext(), Uri.parse(storedHome));

            if (home == null || !home.isDirectory() || !home.exists()) {
                Log.i(LOG_TAG_SERVICE, "Home folder is missing");
                return null;
            }

            return home;
        }
        return null;
    }

    public static DocumentFile getStorageHome(String path) {
        String storedHome = ConfigUtil.getStringProperty(ConfigUtil.PROP_STORAGE_PATH);
        if (storedHome != null) {
            Log.i(LOG_TAG_SERVICE, "Using predefined home " + storedHome);
            DocumentFile home = DocumentFile.fromTreeUri(ConfigUtil.getContext(), Uri.parse(storedHome));
            DocumentFile projectHome = home.findFile(path);
            if (projectHome == null || !projectHome.exists()) {
                Log.i(LOG_TAG_SERVICE, "Creating project home " + path);
                projectHome = home.createDirectory(path);
            }
            return projectHome;
        }
        return null;
    }

    @NonNull
    public static String getFullRelativePath(DocumentFile projectFile) {
        String fullPath = projectFile.getUri().getPath();
        return fullPath.substring(fullPath.lastIndexOf(":") + 1);
    }

    public static DocumentFile searchLegacyHome() {
        // try to find writable folder <= version 28
        File root;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            boolean legacyStorage = Environment.isExternalStorageLegacy();
            Log.i(LOG_TAG_SERVICE, "Legacy storage: " + legacyStorage);
            if (legacyStorage) {
                Log.i(LOG_TAG_SERVICE, "Legacy storage, migrating ... TODO ");
//                UIUtilities.showBusy();
                // TODO migrate
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30 way to work with Files is deprecated and internally translates to the MediaStore API, will need full rewrite for higher API
            root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        } else {
            // API <=28 or API29 with the requestLegacyExternalStorage flag
            if (isExternalStorageWritable()) { // external storage
                root = Environment.getExternalStorageDirectory();
            } else { // internal storage
                root = ConfigUtil.getContext().getFilesDir();
            }
        }

        File storageHome = new File(root, FOLDER_CAVE_SURVEY);

        Log.i(LOG_TAG_SERVICE, "Using as home: " + storageHome.getAbsolutePath());

        // create folder for CaveSurvey if missing
        if (!storageHome.exists()) {
            if (!storageHome.mkdirs()) {
                Log.e(Constants.LOG_TAG_UI, "Failed to create surveys folder: " + storageHome.getAbsolutePath());
                return null;
            }
            Log.i(LOG_TAG_SERVICE, "Home folder created: " + storageHome.getAbsolutePath());
        }

        if (storageHome == null || !storageHome.exists()) {
            Log.e(Constants.LOG_TAG_UI, "Storage unavailable: " + storageHome);
            return null;
        }

        setNewHome(Uri.fromFile(storageHome));

        return DocumentFile.fromFile(storageHome);
    }

    /**
     * Helper method that checks if the external storage is available for writing
     *
     * @return true if available for writing, otherwise false
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state)) && PermissionUtil.hasExtStoragePermission(ConfigUtil.getContext());
    }

    /**
     * Helper method to broadcast a message that a picture is added
     *
     * @param contextArg      - context to use
     * @param addedFile - uri to picture
     */
    public static void notifyPictureAddedToGallery(Context contextArg, DocumentFile addedFile) {
        if (addedFile == null) {
            return;
        }
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(addedFile.getUri());
        contextArg.sendBroadcast(mediaScanIntent);
    }

    public static List<DocumentFile> listProjectFiles(Project aProject, String anExtension) {
        if (aProject != null) {
            return getFolderFiles(getProjectHome(aProject.getName()), anExtension);
        } else {
            DocumentFile root = getStorageHome();
            if (root == null) {
                return null;
            }
            List<DocumentFile> files = new ArrayList<>();
            for (DocumentFile projectHome : root.listFiles()) {
                files.addAll(getFolderFiles(projectHome, anExtension));
            }
            return files;
        }
    }

    public static List<DocumentFile> getFolderFiles(DocumentFile aFolder, final String anExtension) {
        if (aFolder == null || !aFolder.isDirectory()) {
            return new ArrayList();
        } else {
            if (StringUtils.isNotEmpty(anExtension)) {
                DocumentFile[] files = aFolder.listFiles();
                List<DocumentFile> filesWithExtension = new ArrayList<>();
                if (files != null) {
                    for (DocumentFile file : files) {
                        if (file.getName().endsWith(anExtension)) {
                            filesWithExtension.add(file);
                        }
                    }
                }
                return filesWithExtension;
            } else {
                return Arrays.asList(aFolder.listFiles());
            }
        }
    }

    public static void setNewHome(Uri aUserSelectedHome) {

        DocumentFile newHome = DocumentFile.fromTreeUri(ConfigUtil.getContext(), aUserSelectedHome);
        if (!FOLDER_CAVE_SURVEY.equals(newHome.getName())) {
            DocumentFile caveSurveyFolder = newHome.findFile(FOLDER_CAVE_SURVEY);
            newHome = caveSurveyFolder == null ? newHome.createDirectory(FOLDER_CAVE_SURVEY) : caveSurveyFolder;
        }

        if (!newHome.canRead() || !newHome.canWrite()) {
            throw new RuntimeException("New home not writable");
        }

        // TODO no space left check
       /* StatFs stats = new StatFs(newHome.getUri().getPath());
        long availableBytes = stats.getAvailableBlocks() * (long) stats.getBlockSize();
        if (availableBytes < MIN_REQUIRED_STORAGE) {
            Log.e(Constants.LOG_TAG_UI, "No space left");
            UIUtilities.showNotification(R.string.error_no_space);
            throw new RuntimeException("No space left on device");
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ConfigUtil.getContext().getContentResolver().takePersistableUriPermission(aUserSelectedHome, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        ConfigUtil.setStringProperty(ConfigUtil.PROP_STORAGE_PATH, newHome.getUri().toString());
    }

}
