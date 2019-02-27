package com.astoev.cave.survey.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.dialog.ErrorReporterDialog;
import com.astoev.cave.survey.activity.dialog.LanguageDialog;
import com.astoev.cave.survey.activity.poc.SensorsActivity;
import com.astoev.cave.survey.fragment.InfoDialogFragment;
import com.astoev.cave.survey.service.reports.ErrorReporter;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.PermissionUtil;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;

/**
 * Created by astoev on 10/11/15.
 */
public class SettingsActivity extends MainMenuActivity {

    private static final String LANGUAGE_DIALOG = "LANGUAGE_DIALOG";
    private static final String ERROR_REPORTER_TOOLTIP_DIALOG = "ERROR_REPORTER_TOOLTIP_DIALOG";
    private static final String ERROR_REPORTER_MESSAGE_DIALOG = "ERROR_REPORTER_MESSAGE_DIALOG";
    private static final String AUTO_BACKUP_TOOLTIP_DIALOG = "AUTO_BACKUP_TOOLTIP_DIALOG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        prepareLanguage();
        prepareSensors();
        prepareAutoBackup();
        prepareErrorReporter();
    }

    private void prepareSensors() {
        TextView bt = (TextView) findViewById(R.id.settingsSensors);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.LOG_TAG_UI, "Azimuth settings");
                Intent intent = new Intent(SettingsActivity.this, SensorsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void prepareLanguage() {

        TextView language = (TextView) findViewById(R.id.settingsLanguage);
        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LanguageDialog languageDialog = new LanguageDialog();
                languageDialog.show(getSupportFragmentManager(), LANGUAGE_DIALOG);
            }
        });
    }

    private void prepareErrorReporter() {
        ToggleButton errorReporterToggle = (ToggleButton) findViewById(R.id.settingsDebugToggle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // access to system logs disabled since 7.1
            errorReporterToggle.setEnabled(false);
            return;
        }

        errorReporterToggle.setChecked(ErrorReporter.isDebugRunning());

        errorReporterToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                try {
                    if (!PermissionUtil.requestPermissions(new String[]{INTERNET, ACCESS_NETWORK_STATE}, SettingsActivity.this, 401)) {
                        return;
                    }
                } catch (Exception e) {
                    // requesting system permissions could be dangerous
                    Log.e(Constants.LOG_TAG_UI, "Failed to request log permission", e);
                    return;
                }

                if (isChecked) {

                    // start debug
                    try {
                        ErrorReporter.startDebugSession();

                        // show the info window
                        Bundle bundle = new Bundle();
                        String message = getString(R.string.error_reporter_info);
                        bundle.putString(InfoDialogFragment.MESSAGE, message);
                        InfoDialogFragment infoDialog = new InfoDialogFragment();
                        infoDialog.setArguments(bundle);
                        infoDialog.show(getSupportFragmentManager(), ERROR_REPORTER_TOOLTIP_DIALOG);
                    } catch (Exception e) {
                        Log.e(Constants.LOG_TAG_UI, "Error launching error reporter", e);
                        UIUtilities.showNotification(R.string.error);
                    }
                } else {
                    // stop session
                    String dumpFile = ErrorReporter.closeDebugSession();

                    // show message dialog that sends the report
                    ErrorReporterDialog aboutDialogFragment = new ErrorReporterDialog();
                    Bundle arguments = new Bundle();
                    arguments.putString("dumpFile", dumpFile);
                    aboutDialogFragment.setArguments(arguments);
                    aboutDialogFragment.show(getSupportFragmentManager(), ERROR_REPORTER_MESSAGE_DIALOG);
                }
            }
        });
    }

    private void prepareAutoBackup() {
        ToggleButton autoBackupToggle = (ToggleButton) findViewById(R.id.settingsBackupToggle);
        boolean enabled = ConfigUtil.getBooleanProperty(ConfigUtil.PREF_BACKUP);
        autoBackupToggle.setChecked(enabled);

        autoBackupToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(Constants.LOG_TAG_UI, "Auto backup on");
                    ConfigUtil.setBooleanProperty(ConfigUtil.PREF_BACKUP, true);
                } else {
                    Log.i(Constants.LOG_TAG_UI, "Auto backup off");
                    ConfigUtil.setBooleanProperty(ConfigUtil.PREF_BACKUP, false);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareErrorReporter();
    }

    public void onAutoBackupChooseInfo(View viewArg) {
        InfoDialogFragment infoDialog = new InfoDialogFragment();

        Bundle bundle = new Bundle();
        String message = getString(R.string.settings_auto_backup_info);
        bundle.putString(InfoDialogFragment.MESSAGE, message);
        infoDialog.setArguments(bundle);

        infoDialog.show(getSupportFragmentManager(), AUTO_BACKUP_TOOLTIP_DIALOG);
    }

    protected String getScreenTitle() {
        return getString(R.string.main_button_settings);
    }

    @Override
    protected boolean showBaseOptionsMenu() {
        return false;
    }

}
