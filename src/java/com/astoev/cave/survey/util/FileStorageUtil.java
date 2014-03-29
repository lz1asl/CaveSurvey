package com.astoev.cave.survey.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by astoev on 12/25/13.
 * 
 * @author - astoev
 * @author - jmitrev
 */
public class FileStorageUtil {

    private static final String EXCEL_FILE_EXTENSION = ".xls";
    private static final String PNG_FILE_EXTENSION = ".png";
    private static final String NAME_DELIMITER = "_";
    
    public static final String JPG_FILE_EXTENSION = ".jpg";
    public static final String POINT_PREFIX = "Point";
    public static final String MAP_PREFIX = "Map";

    private static final String CAVE_SURVEY_FOLDER = "CaveSurvey";
    private static final String TIME_PATTERN = "yyyyMMdd";
    private static final int MIN_REQUIRED_STORAGE = 50 * 1024;


    @SuppressLint("SimpleDateFormat")
	public static String addProjectExport(Project aProject, InputStream aStream) {

        File projectHome = getProjectHome(aProject.getName());
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
    
    /**
     * Helper method that obtains Picture's directory (api level 8) 
     * 
     * @param projectName - project's name used as an album
     * @return File created
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    private static File getDirectoryPicture(String projectName){
    	return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), projectName);
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
	public static String addProjectMedia(Context contextArg, Project aProject, String filePrefixArg, byte[] byteArrayArg) throws Exception {

    	File pictureFile = createPictureFile(contextArg, aProject.getName(), filePrefixArg, PNG_FILE_EXTENSION);
        
        OutputStream os = null;
        try {
			os = new FileOutputStream(pictureFile);
			os.write(byteArrayArg);
		} catch (Exception e) {
			Log.e(Constants.LOG_TAG_SERVICE, "Unable to write file: " + pictureFile.getAbsolutePath(), e);
			throw e;
		} finally {
			IOUtils.closeQuietly(os);
		}

        Log.i(Constants.LOG_TAG_SERVICE, "Just wrote: " + pictureFile.getAbsolutePath());
        
        // broadcast that picture was added to the project
        notifyPictureAddedToGalery(contextArg, pictureFile);
        return pictureFile.getAbsolutePath();
    }
    
	/**
	 * Helper method that checks if we use a public folder to store files. 
	 * 
	 * @return if api version 8+ return true, otherwise false
	 */
//    public static boolean isPublicFolder(){
//    	return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO);
//    }
    
    /**
     * Helper method that creates a prefix name for picture files based on Point objects
     * 
     * @param pointArg - parent point
     * @param galleryNameArg - name of the gallery
     * @return
     */
    public static String getFilePrefixForPicture(Point pointArg, String galleryNameArg){
    	return POINT_PREFIX + NAME_DELIMITER + galleryNameArg + pointArg.getName();
    }
    
    /**
     * Helper method to create a picture file. The file will be named <prefix>-<date_format><extension>. The 
     * file is stored in public folder based on the project's name. ../CaveSurvay/<project_name>/<file>
     * 
     * @param contextArg - context to use
     * @param projectName - project's name
     * @param filePrefix - file prefix
     * @param fileExtensionArg - extension for the file.
     * @return
     * @throws Exception
     */
    @SuppressLint("SimpleDateFormat")
    public static File createPictureFile(Context contextArg, String projectName, String filePrefix, String fileExtensionArg)
    	throws Exception {
    	
    	// Store in file system
    	File destinationDir = getProjectHome(projectName);
    	if (destinationDir == null){
    		Log.e(Constants.LOG_TAG_SERVICE, "Directory not created");
    		throw new Exception();
    	}
    	
        Log.i(Constants.LOG_TAG_SERVICE, "Will write at: " + destinationDir.getAbsolutePath());
        
        // build filename
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
        
        StringBuilder fileName = new StringBuilder(filePrefix);
        fileName.append(NAME_DELIMITER);
        fileName.append(df.format(date));
        fileName.append(fileExtensionArg);
        
        return new File(destinationDir, fileName.toString());
    }// end of createPictureFile

    public static File getProjectHome(String projectName) {
        File storageHome = getStorageHome();
        if (storageHome == null) {
            return null;
        }
        
        //TODO if there is a problem with spaces in project's name substitute spaces with "_"
        
        File projectHome = new File(storageHome, projectName);
        if (!projectHome.exists()) {
            if (!projectHome.mkdirs()) {
                Log.e(Constants.LOG_TAG_UI, "Failed to create folder " + projectHome.getAbsolutePath());
                return null;
            }
            Log.i(Constants.LOG_TAG_SERVICE, "Project home created");
        }
        return projectHome;
    }
    
    @SuppressWarnings("deprecation")
    private static File getStorageHome() {
        if (!isExternalStorageWritable()) {
            Log.e(Constants.LOG_TAG_UI, "Storage unavailable");
            return null;
        }
        File extdir = Environment.getExternalStorageDirectory();
        StatFs stats = new StatFs(extdir.getAbsolutePath());
        
		long availableBytes = stats.getAvailableBlocks() * (long)stats.getBlockSize();
        if (availableBytes < MIN_REQUIRED_STORAGE) {
            Log.e(Constants.LOG_TAG_UI, "No space left");
            return null;
        }

        File storageHome = new File(Environment.getExternalStorageDirectory() + File.separator + CAVE_SURVEY_FOLDER);
        if (!storageHome.exists()) {
            if (!storageHome.mkdirs()) {
                Log.e(Constants.LOG_TAG_UI, "Failed to create folder " + storageHome.getAbsolutePath());
                return null;
            }
            Log.i(Constants.LOG_TAG_SERVICE, "Export folder created");
        }
        return storageHome;
    }
    
    /**
     * Helper method that checks if the external storage is available for writing
     * 
     * @return  true if available for writing, otherwise false
     */
    public static boolean isExternalStorageWritable(){
    	String state = Environment.getExternalStorageState();
    	return (Environment.MEDIA_MOUNTED.equals(state));
    }
    
    /**
     * Helper method that invokes system's media scanner to add a picture to Media Provider's database
     * 
     * @param contextArg   - context to use to send a broadcast
     * @param addedFileArg - the newly created file to notify for
     */
    public static void notifyPictureAddedToGalery(Context contextArg, File addedFileArg){
    	if (addedFileArg == null){
    		return;
    	}
    	Uri contentUri = Uri.fromFile(addedFileArg);
    	notifyPictureAddedToGalery(contextArg, contentUri);
    }
    
    /**
     * Helper method to broadcast a message that a picture is added
     * 
     * @param contextArg	  - context to use
     * @param addedFileUriArg - uri to picture
     */
    public static void notifyPictureAddedToGalery(Context contextArg, Uri addedFileUriArg){
    	if (addedFileUriArg == null){
    		return;
    	}
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(addedFileUriArg);
        contextArg.sendBroadcast(mediaScanIntent);
    }
    
    /**
     * Helper method to check if file exists
     * 
     * @param fileNameArg - file name
     * @return true if the file exists, otherwise false
     */ 
    public static final boolean isFileExists(String fileNameArg){
    	if (fileNameArg == null){
    		return false;
    	}
    	
    	File file = new File(fileNameArg);
    	return file.exists();
    }
}
