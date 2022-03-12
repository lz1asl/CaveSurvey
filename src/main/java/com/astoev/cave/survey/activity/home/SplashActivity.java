package com.astoev.cave.survey.activity.home;

import static android.view.View.VISIBLE;
import static com.astoev.cave.survey.Constants.LOG_TAG_SERVICE;
import static com.astoev.cave.survey.activity.config.SettingsActivity.LANGUAGE_DIALOG;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.dialog.LanguageDialog;
import com.astoev.cave.survey.activity.dialog.StorageDialog;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.AndroidUtil;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.FileStorageUtil;

import java.sql.SQLException;

/** Displayed during initialization */
public class SplashActivity extends AppCompatActivity {

    private static final int PERM_REQ_CODE_STORAGE_PERMISSION = 201;
    private static final int REQ_CODE_STORAGE_PATH = 202;
    private static final String STORAGE_DIALOG = "STORAGE_DIALOG";

    private static StorageDialog storageWarningDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // render splash
        Log.i(Constants.LOG_TAG_UI, "Preparing ...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // current context
        ConfigUtil.setContext(this);
        Log.i(LOG_TAG_SERVICE, "Running CaveSurvey " + AndroidUtil.getAppVersion() + ", API " + Build.VERSION.SDK_INT);

        // user language
        String savedLanguage = ConfigUtil.getStringProperty(ConfigUtil.PREF_LOCALE);
        if (savedLanguage == null) {
            Log.i(LOG_TAG_SERVICE, "Ask user to select language");
            LanguageDialog languageDialog = new LanguageDialog(this);
            languageDialog.show(getSupportFragmentManager(), LANGUAGE_DIALOG);
            return;
        } else {
            Log.i(LOG_TAG_SERVICE, "Use locale " + savedLanguage);
        }

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
          /*  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                // FS initialization, permission needed for API < 28 and API29 with the requestLegacyExternalStorage flag
                if (!PermissionUtil.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this, PERM_REQ_CODE_STORAGE_PERMISSION)) {

                    // can't continue normally
                    displayError(R.string.splash_internal_storage_warning);
                    return;
                }

                storageHome = FileStorageUtil.searchLegacyHome();
            } else {*/
            {
                // API 30, first time, ask user to grant permission to new storage folder
                Log.i(Constants.LOG_TAG_UI, "API 30 request access to storage");

                storageWarningDialog = new StorageDialog(this);
                storageWarningDialog.show(getSupportFragmentManager(), STORAGE_DIALOG);
            }
        } else {
            Log.i(LOG_TAG_SERVICE, "Configured home: " + storageHome);

            // continue
            Log.i(Constants.LOG_TAG_UI, "Loading surveys");
            startActivity(new Intent(this, SurveysActivity.class));
            finish();
        }
    }

    /*public void ignoreStorageWarning(View aView) {
        Log.i(Constants.LOG_TAG_UI, "Ignoring ext storage unavailable");

        // TODO
      *//*  File home = FileStorageUtil.getStorageHome();
        if (home == null) {
            displayError(R.string.splash_error_storage);
            return;
        }

        // continue
        loadHomeScreen();*//*
    }*/

    private void loadHomeScreen() {
        Log.i(Constants.LOG_TAG_UI, "Loading surveys");
        startActivity(new Intent(this, SurveysActivity.class));
        finish();
    }

    private void displayError(int aMessage) {
        Log.e(LOG_TAG_SERVICE, getString(aMessage));
        UIUtilities.showNotification(aMessage);
        setStatus(aMessage);
    }

    public void displayFatalError(int aMessage) {
        displayError(aMessage);
        findViewById(R.id.splashRetry).setVisibility(VISIBLE);
    }

    private void setStatus(int aMessage) {
        TextView status = findViewById(R.id.splashStatus);
        status.setText(getString(R.string.splash_error, getText(aMessage)));
    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        Log.i(LOG_TAG_SERVICE, "User has selected");
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
                Log.i(LOG_TAG_SERVICE, "Ignore request " + requestCode);
        }
    }*/

    public void askHomeFolder(View aView) {

        Log.i(LOG_TAG_SERVICE, "Android permissions dialog");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQ_CODE_STORAGE_PATH);
        storageWarningDialog.onParentClose();
    }

    public void retry(View aView) {
        Log.i(LOG_TAG_SERVICE, "Reloading");
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    // storage home response
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent aData) {
        super.onActivityResult(requestCode, resultCode, aData);
        Log.i(LOG_TAG_SERVICE, "User has selected " + resultCode);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_CODE_STORAGE_PATH:
                    Uri userSelectedHome = aData.getData();
                    Log.i(LOG_TAG_SERVICE, "Got new home: " + userSelectedHome);
                    try {
                        FileStorageUtil.setNewHome(userSelectedHome);
                        loadHomeScreen();
                        return;
                    } catch (Exception e) {
                        // can't continue normally
                        Log.e(LOG_TAG_SERVICE, "Failed to set new home", e);
                        displayError(R.string.splash_internal_storage_warning);
                        return;
                    }
            }
        }
        displayFatalError(R.string.splash_error_storage);
    }
}
