package com.astoev.cave.survey.activity;

import android.content.Intent;
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
import com.astoev.cave.survey.activity.poc.SensorTestActivity;
import com.astoev.cave.survey.fragment.InfoDialogFragment;
import com.astoev.cave.survey.service.reports.ErrorReporter;
import com.astoev.cave.survey.util.ConfigUtil;

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
        prepareBluetooth();
        prepareAutoBackup();
        prepareErrorReporter();
    }

    private void prepareBluetooth() {
        TextView bt = (TextView) findViewById(R.id.settingsBluetooth);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.LOG_TAG_UI, "Azimuth Test");
                Intent intent = new Intent(SettingsActivity.this, SensorTestActivity.class);
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
        errorReporterToggle.setChecked(ErrorReporter.isDebugRunning());

        errorReporterToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ErrorReporter.startDebugSession();
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

    public void onErrorReporterChooseInfo(View viewArg) {
        InfoDialogFragment infoDialog = new InfoDialogFragment();

        Bundle bundle = new Bundle();
        String message = getString(R.string.error_reporter_info);
        bundle.putString(InfoDialogFragment.MESSAGE, message);
        infoDialog.setArguments(bundle);

        infoDialog.show(getSupportFragmentManager(), ERROR_REPORTER_TOOLTIP_DIALOG);
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

}
