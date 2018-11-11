package com.astoev.cave.survey.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.UIUtilities;

public class PermissionUtil {



    public static final int PERMISSIONS_REQUEST_CODE = 101;


    public static boolean requestPermission(String aPermission, AppCompatActivity aBaseActivity) {
        return hasOrRequestPermission(new String[] { aPermission }, aBaseActivity);
    }

    public static boolean requestPermissions(String[] aPermissions, AppCompatActivity aBaseActivity) {
        return hasOrRequestPermission(aPermissions, aBaseActivity);
    }

    private static boolean hasOrRequestPermission(String[] aPermissions, AppCompatActivity anActivity) {
        Log.d(Constants.LOG_TAG_SERVICE, "Checking permissions");

        if (isStaticPermissionBuild()) {
            Log.d(Constants.LOG_TAG_SERVICE, "Permission should be explicitly granted on app installation");
            return true;
        } else {
            // check all of the permissions
            for (String permission : aPermissions) {
                Log.d(Constants.LOG_TAG_SERVICE, "Checking for " + permission);
                if (!hasPermission(permission, anActivity)) {
                    Log.e(Constants.LOG_TAG_SERVICE, "Permission not granted, requesting access");
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(anActivity, permission)) {

//                    } else {
                        UIUtilities.showNotification("Please authorize CaveSurvey for " + permission);
                        ActivityCompat.requestPermissions(anActivity, aPermissions, PERMISSIONS_REQUEST_CODE);
//                    }
                    return false;
                }
            }
        }

        Log.d(Constants.LOG_TAG_SERVICE, "All requested permissions granted");
        return true;
    }

    public static boolean hasExtStoragePermission(AppCompatActivity anActivity) {
        return isStaticPermissionBuild()
                || hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, anActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean hasPermission(String aPermission, AppCompatActivity anActivity) {
        return anActivity.checkSelfPermission(aPermission) == PackageManager.PERMISSION_GRANTED;
    }

    // return true for below Marschmallow API version
    private static boolean isStaticPermissionBuild() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }


}
