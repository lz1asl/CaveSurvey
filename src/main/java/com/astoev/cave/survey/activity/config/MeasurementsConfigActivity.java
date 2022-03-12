package com.astoev.cave.survey.activity.config;

import android.os.Bundle;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.BaseActivity;

public class MeasurementsConfigActivity extends BaseActivity {

    @Override
    protected String getScreenTitle() {
        return getString(R.string.measurements_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurements_config);

        // use backshots switch
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
        Spinner exportSpinner = findViewById(R.id.measrements_export_types);
        ArrayAdapter adapterExportTypes = ArrayAdapter.createFromResource(exportSpinner.getContext(), R.array.measurements_export_types, android.R.layout.simple_spinner_item);
        adapterExportTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exportSpinner.setAdapter(adapterExportTypes);*/
    }
}
