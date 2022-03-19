package com.astoev.cave.survey.activity.config;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.dialog.LanguageDialog;
import com.astoev.cave.survey.activity.dialog.VectorsModeDialog;
import com.astoev.cave.survey.activity.poc.SensorsActivity;
import com.astoev.cave.survey.fragment.InfoDialogFragment;
import com.astoev.cave.survey.util.ConfigUtil;

/**
 * Created by astoev on 10/11/15.
 */
public class SettingsActivity extends MainMenuActivity {

     public static final String LANGUAGE_DIALOG = "LANGUAGE_DIALOG";
    private static final String AUTO_BACKUP_TOOLTIP_DIALOG = "AUTO_BACKUP_TOOLTIP_DIALOG";
    private static final String VECTORS_DIALOG = "VECTORS_DIALOG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        prepareLanguage();
//        prepareMeasurements();
        prepareSensors();
        prepareVectors();
        prepareAutoBackup();
    }

    private void prepareSensors() {
        TextView bt = findViewById(R.id.settingsSensors);
        bt.setOnClickListener(v -> {
            Log.i(Constants.LOG_TAG_UI, "Azimuth settings");
            Intent intent = new Intent(SettingsActivity.this, SensorsActivity.class);
            startActivity(intent);
        });
    }

    private void prepareLanguage() {

        TextView language = findViewById(R.id.settingsLanguage);
        language.setOnClickListener(v -> {
            LanguageDialog languageDialog = new LanguageDialog();
            languageDialog.show(getSupportFragmentManager(), LANGUAGE_DIALOG);
        });
    }

    private void prepareMeasurements() {

        TextView language = findViewById(R.id.settingsMeasurements);
        language.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MeasurementsConfigActivity.class);
            startActivity(intent);
        });
    }

    private void prepareVectors() {

        TextView vectors = findViewById(R.id.settingsVectors);
        vectors.setOnClickListener(v -> {
            VectorsModeDialog vectorsDialog = new VectorsModeDialog();
            vectorsDialog.show(getSupportFragmentManager(), VECTORS_DIALOG);
        });
    }


    private void prepareAutoBackup() {
        ToggleButton autoBackupToggle = findViewById(R.id.settingsBackupToggle);
        boolean enabled = ConfigUtil.getBooleanProperty(ConfigUtil.PREF_AUTO_BACKUP);
        autoBackupToggle.setChecked(enabled);

        autoBackupToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Log.i(Constants.LOG_TAG_UI, "Auto backup on");
                ConfigUtil.setBooleanProperty(ConfigUtil.PREF_AUTO_BACKUP, true);
            } else {
                Log.i(Constants.LOG_TAG_UI, "Auto backup off");
                ConfigUtil.setBooleanProperty(ConfigUtil.PREF_AUTO_BACKUP, false);
            }
        });
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
