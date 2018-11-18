package com.astoev.cave.survey.activity.home;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.PermissionUtil;

import java.io.File;
import java.sql.SQLException;

/** Displayed while preparing */
public class SplashActivity extends AppCompatActivity {

    private static final int PERM_REQ_CODE_STORAGE = 201;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // render splash
        Log.i(Constants.LOG_TAG_UI, "Preparing ...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // current context
        ConfigUtil.setContext(this);

        // db initialization
        try {
            Workspace.getCurrentInstance().getDBHelper().getProjectDao().queryForAll();
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_DB, "DB read failed", e);
            displayError(R.string.splash_error_database);
            return;
        }

        // FS initialization, if available and allowed prepare to use external storage
        PermissionUtil.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this, PERM_REQ_CODE_STORAGE);
        File home = FileStorageUtil.getStorageHome();
        if (home == null) {
            displayError(R.string.splash_error_storage);
            return;
        }

        // TODO - prepare language and configuration for later usage

        // continue
        Log.i(Constants.LOG_TAG_UI, "Loading home");
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void displayError(int aMessage) {
        UIUtilities.showNotification(aMessage);
        TextView status = (TextView) findViewById(R.id.splashStatus);
        status.setText(getString(R.string.splash_error, getText(aMessage)));
    }

}
