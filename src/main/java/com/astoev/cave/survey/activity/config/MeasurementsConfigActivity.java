package com.astoev.cave.survey.activity.config;

import static com.astoev.cave.survey.util.StringUtils.getFloat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.BaseActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.StringUtils;

public class MeasurementsConfigActivity extends BaseActivity {

    @Override
    protected String getScreenTitle() {
        return getString(R.string.measurements_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurements_config);

        boolean useAdjustment = ConfigUtil.getBooleanProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT);
        Switch adjustmentsSpinner = findViewById(R.id.measurements_use_adjustment);
        adjustmentsSpinner.setChecked(useAdjustment);
        View adjustmentView = findViewById(R.id.peasurements_adjustment_layout);
        if (useAdjustment) {
            adjustmentView.setVisibility(View.VISIBLE);
        } else {
            adjustmentView.setVisibility(View.GONE);
        }

        adjustmentsSpinner.setOnCheckedChangeListener((button, state) -> {
            Log.i(Constants.LOG_TAG_UI, "Toggle 'enable measurements' to " + state);
            ConfigUtil.setBooleanProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT, state);
            if (state) {
                // show config
                adjustmentView.setVisibility(View.VISIBLE);
            } else {
                // hide config and reset adjustment
                adjustmentView.setVisibility(View.GONE);
                ConfigUtil.setFloatProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT_VALUE, 0f);
            }
        });

        // show current value
        EditText adjustmentValue = findViewById(R.id.measurements_length_adjustment);
        StringUtils.setNotNull(adjustmentValue, ConfigUtil.getFloatProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT_VALUE));


        // use back-shots switch
     /*   Switch useBackShotsSpinner = findViewById(R.id.measurements_use_backshots);
        useBackShotsSpinner.setOnCheckedChangeListener((button, state) -> {
            View backshotExportView = findViewById(R.id.measurements_backshot_export_settings);
            if (state) {
                backshotExportView.setVisibility(View.VISIBLE);
            } else {
                backshotExportView.setVisibility(View.GONE);
            }
        });

        // export spinner
        Spinner exportSpinner = findViewById(R.id.measurements_export_types);
        ArrayAdapter adapterExportTypes = ArrayAdapter.createFromResource(exportSpinner.getContext(), R.array.measurements_export_types, android.R.layout.simple_spinner_item);
        adapterExportTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exportSpinner.setAdapter(adapterExportTypes);*/
    }

    @Override
    protected void onPause() {
        super.onPause();

        // persist values
        if (ConfigUtil.getBooleanProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT)) {
            EditText adjustmentValue = findViewById(R.id.measurements_length_adjustment);
            Float adjustment = getFloat(adjustmentValue.getText().toString());
            Log.i(Constants.LOG_TAG_UI, "Update bt adjustment to " + adjustment);
            ConfigUtil.setFloatProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT_VALUE, adjustment);

            UIUtilities.showNotification(this, R.string.measurements_adjustment_warning, adjustment);
        }

    }
}
