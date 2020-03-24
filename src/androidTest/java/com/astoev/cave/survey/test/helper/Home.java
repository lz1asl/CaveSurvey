package com.astoev.cave.survey.test.helper;

import androidx.test.runner.permission.PermissionRequester;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.astoev.cave.survey.R.id.surveysList;
import static com.astoev.cave.survey.test.helper.Common.checkVisible;

public class Home {

    public static void goHome() {
        // grant needed permissions
        PermissionRequester permissionRequester = new PermissionRequester();
        permissionRequester.addPermissions(WRITE_EXTERNAL_STORAGE);
        permissionRequester.requestPermissions();

        // goes directly to the surveys screen
        checkVisible(surveysList);
    }



}
