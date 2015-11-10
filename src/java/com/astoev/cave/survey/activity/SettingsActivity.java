package com.astoev.cave.survey.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.dialog.AboutDialog;
import com.astoev.cave.survey.activity.dialog.ErrorReporterDialog;
import com.astoev.cave.survey.activity.dialog.LanguageDialog;
import com.astoev.cave.survey.fragment.InfoDialogFragment;
import com.astoev.cave.survey.service.reports.ErrorReporter;

/**
 * Created by astoev on 10/11/15.
 */
public class SettingsActivity extends MainMenuActivity {

    private static final String LANGUAGE_DIALOG = "LANGUAGE_DIALOG";
    private static final String ABOUT_DIALOG = "ABOUT_DIALOG";
    private static final String ERROR_REPORTER_TOOLTIP_DIALOG = "ERROR_REPORTER_TOOLTIP_DIALOG";
    private static final String ERROR_REPORTER_MESSAGE_DIALOG = "ERROR_REPORTER_MESSAGE_DIALOG";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        prepareLanguage();
        prepareErrorReporter();
        prepareAbout();
    }

    private void prepareAbout() {

        TextView about = (TextView) findViewById(R.id.settingsAbout);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutDialog aboutDialogFragment = new AboutDialog();
                aboutDialogFragment.show(getSupportFragmentManager(), ABOUT_DIALOG);
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

    protected String getScreenTitle() {
        return getString(R.string.main_button_settings);
    }

}
