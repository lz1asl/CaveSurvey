package com.astoev.cave.survey.activity.home;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.AndroidUtil;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.PermissionUtil;

import java.sql.SQLException;

/** Displayed while preparing */
public class SplashActivity extends AppCompatActivity {

    private static final int PERM_REQ_CODE_STORAGE_PERMISSION = 201;
    private static final int REQ_CODE_STORAGE_PATH = 202;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // render splash
        Log.i(Constants.LOG_TAG_UI, "Preparing ...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // current context
        ConfigUtil.setContext(this);

        Log.i(Constants.LOG_TAG_SERVICE, "Running CaveSurvey " + AndroidUtil.getAppVersion() + ", API " + Build.VERSION.SDK_INT);


        // db initialization
        try {
            Workspace.getCurrentInstance().getDBHelper().getProjectDao().queryForAll();
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_DB, "DB read failed", e);
            displayError(R.string.splash_error_database);
            return;
        }

        DocumentFile storageHome = FileStorageUtil.getStorageHome();
        if (storageHome == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                // FS initialization, permission needed for API < 28 and API29 with the requestLegacyExternalStorage flag
                if (!PermissionUtil.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this, PERM_REQ_CODE_STORAGE_PERMISSION)) {

                    // can't continue normally
                    displayError(R.string.splash_internal_storage_warning);
                    return;
                }

                storageHome = FileStorageUtil.searchLegacyHome();
            } else {
                // API 30, first time, ask user to grant permission to new storage folder


                Log.i(Constants.LOG_TAG_UI, "Request access to storage");
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Downloads.EXTERNAL_CONTENT_URI);//FileStorageUtil.getStorageHome().getUri());
                startActivityForResult(intent, REQ_CODE_STORAGE_PATH);
                return;
            }
        } // TODO handle legacy storage - or inform user to migrate


        if (storageHome == null) {
            displayError(R.string.splash_error_storage);
            return;
        } else {
            Log.i(Constants.LOG_TAG_SERVICE, "Configured home: " + storageHome);
        }

        // TODO - prepare language and configuration for later usage

        // continue
        Log.i(Constants.LOG_TAG_UI, "Loading surveys");
        startActivity(new Intent(this, SurveysActivity.class));
        finish();

    }

    public void ignoreStorageWarning(View aView) {
        Log.i(Constants.LOG_TAG_UI, "Ignoring ext storage unavailable");

        // TODO
      /*  File home = FileStorageUtil.getStorageHome();
        if (home == null) {
            displayError(R.string.splash_error_storage);
            return;
        }

        // continue
        loadHomeScreen();*/
    }

    private void loadHomeScreen() {
        Log.i(Constants.LOG_TAG_UI, "Loading surveys");
        startActivity(new Intent(this, SurveysActivity.class));
        finish();
    }


    private void displayError(int aMessage) {
        UIUtilities.showNotification(aMessage);
        TextView status = findViewById(R.id.splashStatus);
        status.setText(getString(R.string.splash_error, getText(aMessage)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        Log.i(Constants.LOG_TAG_SERVICE, "User has selected");
        switch (requestCode) {
            case PERM_REQ_CODE_STORAGE_PERMISSION:
                if (PermissionUtil.isGranted(permissions, grantResults)) {
                    loadHomeScreen();
                } else {
                    // can temporary ignore the warning
                    Button continueAnywayButton = findViewById(R.id.splash_ignore_storage_warning_button);
                    continueAnywayButton.setVisibility(View.VISIBLE);

                    // can't continue normally
                    displayError(R.string.splash_internal_storage_warning);
                    return;
                }
                return;

            default:
                Log.i(Constants.LOG_TAG_SERVICE, "Ignore request " + requestCode);
        }
    }

    // storage home response
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent aData) {
        Log.i(Constants.LOG_TAG_SERVICE, "User has selected " + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_CODE_STORAGE_PATH:
                    Uri userSelectedHome = aData.getData();
                    Log.i(Constants.LOG_TAG_SERVICE, "Got new home: " + userSelectedHome);
                    Log.i(Constants.LOG_TAG_SERVICE, "Got new home: " + userSelectedHome.getPath());
                    try {
                        FileStorageUtil.setNewHome(userSelectedHome);
                        Intent intent = new Intent(this, SplashActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        // can't continue normally
                        Log.e(Constants.LOG_TAG_SERVICE, "Failed to set new home", e);
                        displayError(R.string.splash_internal_storage_warning);
                        return;
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, aData);
    }
}
