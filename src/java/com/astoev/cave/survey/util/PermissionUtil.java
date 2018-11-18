package com.astoev.cave.survey.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;

public class PermissionUtil {

    public static boolean requestPermission(String aPermission, AppCompatActivity anActivity, int aCode) {
        return hasOrRequestPermission(new String[] { aPermission }, anActivity, aCode);
    }

    public static boolean requestPermissions(String[] aPermissions, AppCompatActivity anActivity, int aCode) {
        return hasOrRequestPermission(aPermissions, anActivity, aCode);
    }

    private static boolean hasOrRequestPermission(String[] aPermissions, AppCompatActivity anActivity, int aCode) {
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
                    if (ActivityCompat.shouldShowRequestPermissionRationale(anActivity, permission)) {
                        UIUtilities.showNotification(anActivity.getString(R.string.permission_required, permission));
                    }
                    ActivityCompat.requestPermissions(anActivity, new String[] {permission}, aCode);
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

    public static boolean isGranted(String permissions[], int[] grantResults) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(Constants.LOG_TAG_SERVICE, "Got permission");
                return true;
            } else {
                Log.i(Constants.LOG_TAG_SERVICE, "Permission denied");
                return false;
            }
    }

}
