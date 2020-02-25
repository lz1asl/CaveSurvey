package com.astoev.cave.survey.activity.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.astoev.cave.survey.R;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/23/12
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class OptionsActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);
/*
        // measure mode
        Spinner measureMode = (Spinner) findViewById(R.id.options_distance_units);
        ArrayAdapter adapterMeasureModes = ArrayAdapter.createFromResource(this, R.array.options_modes, android.R.layout.simple_spinner_item);
        adapterMeasureModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measureMode.setAdapter(adapterMeasureModes);
        if (Option.CODE_MEASURE_MODE.equals(Options.getOptionValue(Option.CODE_SENSOR_BLUETOOTH))) {
            // bluetoth
            // TODO disable this option if (!BluetoothService.isBluetoothSupported())

            measureMode.setSelection(2);
        } else if (Option.CODE_MEASURE_MODE.equals(Options.getOptionValue(Option.CODE_SENSOR_INTERNAL))) {
            // internal
            measureMode.setSelection(1);
        } else {
            // manual
            measureMode.setSelection(0);
        }

        // distance units
        Spinner distanceUnits = (Spinner) findViewById(R.id.options_distance_units);
        ArrayAdapter adapterDistance = ArrayAdapter.createFromResource(this, R.array.distance_units, android.R.layout.simple_spinner_item);
        adapterDistance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceUnits.setAdapter(adapterDistance);

        // azimut units
        Spinner azimuthUnits = (Spinner) findViewById(R.id.options_units_azimuth);
        ArrayAdapter adapterAzimuth = ArrayAdapter.createFromResource(this, R.array.azimuth_units, android.R.layout.simple_spinner_item);
        adapterAzimuth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        azimuthUnits.setAdapter(adapterAzimuth);
        // TODO - listener event is fired and value is reset to its initial
        String azimuthUnit = Options.getOptionValue(Option.CODE_AZIMUTH_UNITS);

        azimuthUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Option azimuthUnit = Options.getOption(Option.CODE_AZIMUTH_UNITS);
                if (0 == position) {
                    azimuthUnit.setValue(Option.UNIT_DEGREES);
                } else {
                    azimuthUnit.setValue(Option.UNIT_GRADS);
                }
                try {
                    Workspace.getCurrentInstance().getDBHelper().getOptionsDao().update(azimuthUnit);
                } catch (SQLException e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed to save option", e);
                    UIUtilities.showNotification(OptionsActivity.this, R.string.error);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        if (Option.UNIT_DEGREES.equals(azimuthUnit)) {
            azimuthUnits.setSelection(0);
        } else {
            azimuthUnits.setSelection(1);
        }


        // slope units
        Spinner slopeUnits = (Spinner) findViewById(R.id.options_units_slope);
        ArrayAdapter adapterSlope = ArrayAdapter.createFromResource(this, R.array.slope_units, android.R.layout.simple_spinner_item);
        adapterSlope.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        slopeUnits.setAdapter(adapterSlope);
        if (Option.UNIT_DEGREES.equals(Options.getOptionValue(Option.CODE_SLOPE_UNITS))) {
            azimuthUnits.setSelection(0);
        } else {
            azimuthUnits.setSelection(1);
        }


        // BT protocol
        Button btButton = (Button) findViewById(R.id.bt_setup_button);
        if (!BluetoothService.isBluetoothSupported()) {
            Log.w(Constants.LOG_TAG_UI, "Bluetooth not supported");
            btButton.setEnabled(false);
            btButton.setText(R.string.bt_not_supported);
        } else {
            btButton.setEnabled(true);
        }*/
    }

    public void pairBtDevice(View aView) {
        Intent intent = new Intent(OptionsActivity.this, BTActivity.class);
        startActivity(intent);
    }

}