package com.astoev.cave.survey.activity.poc;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.fragment.InfoDialogFragment;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;
import com.astoev.cave.survey.util.ConfigUtil;

import java.util.ArrayList;

import static com.astoev.cave.survey.Constants.LOG_TAG_UI;
import static com.astoev.cave.survey.activity.dialog.BaseBuildInMeasureDialog.PROGRESS_DEFAULT_VALUE;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_DEVICE_HEADING_CAMERA;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_NOISE_REDUCTION;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_SIMULTANEOUSLY;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_TIMEOUT;

/**
 * Pprovides an option to choose the default sensor that will be used application wide.
 * 
 * @author Zhivko Mitrev
 */
public class SensorsActivity extends MainMenuActivity {

    /** Dialog name to enable choose sensors tooltip dialog */
    private static final String CHOOSE_SENSORS_TOOLTIP_DIALOG = "CHOOSE_SENSORS_TOOLTIP_DIALOG";
    private static final int SIMULTANEOUSLY_READING_POSITION = 1;
    private static final int NOISE_REDUCTION_DISABLED_POSITION = 0;
    private static final int CAMERA_HEADING_POSITION = 1;

    private Integer[] availableSensorsArray;

	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensors);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //  determine orientation sensors that are available on the device
        ArrayList<Integer> sensorsList = new ArrayList<>();

        Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationSensor != null) {
            sensorsList.add(OrientationProcessorFactory.SENSOR_TYPE_ROTATION);
        }
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticSensor != null) {
            sensorsList.add(OrientationProcessorFactory.SENSOR_TYPE_MAGNETIC);
        }
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (orientationSensor != null) {
            sensorsList.add(OrientationProcessorFactory.SENSOR_TYPE_ORIENTATION);
        }

        // fill the sensor spinner if there are any available sensors
        if (sensorsList.size() > 0) {
            availableSensorsArray = new Integer[sensorsList.size()];
            availableSensorsArray = sensorsList.toArray(availableSensorsArray);

            // convert to strings array
            String[] str = createTranslateArray(availableSensorsArray);

            Spinner spinner = findViewById(R.id.sensors_spinner);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            // read the default senor type and find the index in the spinner list
            int savedSensorSelection = OrientationProcessorFactory.getDefaultSensorType(this);
            Integer savedSensorUIIndex = null;
            for(int i = 0; i < availableSensorsArray.length; i++){
                if (availableSensorsArray[i] == savedSensorSelection){
                    savedSensorUIIndex = i;
                    break;
                }
            }

            //set selection index in UI
            if (savedSensorUIIndex != null){
                spinner.setSelection(savedSensorUIIndex);
            }

            // add item selection that will handle the user input and will save the settings
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int selectedSensor = availableSensorsArray[position];
                    Log.i(LOG_TAG_UI, "Selected position:" + position + " sensor:" + selectedSensor);

                    // save setting for preferred sensor
                    int valueFromSettings = ConfigUtil.getIntProperty(ConfigUtil.PREF_SENSOR);
                    if (valueFromSettings == 0 || valueFromSettings != selectedSensor) {
                        ConfigUtil.setIntProperty(ConfigUtil.PREF_SENSOR, selectedSensor);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else {
            // no available sensors !
            //disable spinner for choosing sensors
            LinearLayout sensorsLinearLayout = findViewById(R.id.sensors_spinner_layout);
            sensorsLinearLayout.setVisibility(View.GONE);

            // change the text for sensors choose header
            TextView sensorsChooseText = findViewById(R.id.sensor_choose_text);
            sensorsChooseText.setText(R.string.no_sensors);
        }

        // read timeout
        final Integer [] timeouts = new Integer[]{ 1, 2, 3, 4, 5 };
        Spinner timeoutSpinner = findViewById(R.id.sensors_timeout_spinner);
        ArrayAdapter<Integer> timeoutAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeouts);
        timeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeoutSpinner.setAdapter(timeoutAdapter);

        // current setting
        Integer userMaxProgressValue = ConfigUtil.getIntProperty(PREF_SENSOR_TIMEOUT, PROGRESS_DEFAULT_VALUE);
        for(int i = 0; i < timeouts.length; i++){
            if (userMaxProgressValue.equals(timeouts[i])){
                timeoutSpinner.setSelection(i);
                break;
            }
        }
        // updated setting
        timeoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> aAdapterView, View aView, int aPosition, long aId) {
                int newReadTimeout = timeouts[aPosition];
                Log.i(LOG_TAG_UI, "Selected new timeout:" + newReadTimeout);
                ConfigUtil.setIntProperty(PREF_SENSOR_TIMEOUT, newReadTimeout);
            }

            @Override
            public void onNothingSelected(AdapterView<?> aAdapterView) {
            }
        });

        // read device orientation
        Spinner deviceHeadingSpinner = findViewById(R.id.sensors_device_heading);
        final ArrayAdapter<String> deviceHeadingsAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        getResources().getStringArray(R.array.sensor_device_headings));
        deviceHeadingSpinner.setAdapter(deviceHeadingsAdapter);
        deviceHeadingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> aAdapterView, View aView, int aPosition, long aId) {
                Log.i(LOG_TAG_UI, "Selected heading mode: " + aPosition + " " + deviceHeadingsAdapter.getItem(aPosition));
                ConfigUtil.setBooleanProperty(PREF_DEVICE_HEADING_CAMERA, aPosition == CAMERA_HEADING_POSITION);
            }

            @Override
            public void onNothingSelected(AdapterView<?> aAdapterView) {

            }
        });
        if (ConfigUtil.getBooleanProperty(PREF_DEVICE_HEADING_CAMERA)) {
            deviceHeadingSpinner.setSelection(CAMERA_HEADING_POSITION);
        }

        // sensors reading
        Spinner readTypeSpinner = findViewById(R.id.sensors_simultaneously_spinner);
        final ArrayAdapter<String> readTypesAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        getResources().getStringArray(R.array.sensors_reading_types));
        readTypeSpinner.setAdapter(readTypesAdapter);
        readTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> aAdapterView, View aView, int aPosition, long aId) {
                Log.i(LOG_TAG_UI, "Selected read mode: " + aPosition + " " + readTypesAdapter.getItem(aPosition));
                ConfigUtil.setBooleanProperty(PREF_SENSOR_SIMULTANEOUSLY, aPosition == SIMULTANEOUSLY_READING_POSITION);
            }

            @Override
            public void onNothingSelected(AdapterView<?> aAdapterView) {

            }
        });
        if (ConfigUtil.getBooleanProperty(PREF_SENSOR_SIMULTANEOUSLY)) {
            readTypeSpinner.setSelection(SIMULTANEOUSLY_READING_POSITION);
        }

        // noise reduction num measurements
        final Spinner noiseReductionNumMeasurements = findViewById(R.id.sensors_num_measurements_spinner);
        final TextView noiseReductionNumMeasurementsLabel = findViewById(R.id.sensors_num_measurements_label);
        final ArrayAdapter<String> numMeasurementValues = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sensor_num_measurements));
        noiseReductionNumMeasurements.setAdapter(numMeasurementValues);
        noiseReductionNumMeasurements.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> aAdapterView, View aView, int aPosition, long aL) {
                Log.i(LOG_TAG_UI, "Noise reduction num measurements: " + aPosition + " " + numMeasurementValues.getItem(aPosition));
                ConfigUtil.setIntProperty(PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS, Integer.valueOf(numMeasurementValues.getItem(aPosition)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> aAdapterView) {

            }
        });
        Integer numMeasurements = ConfigUtil.getIntProperty(PREF_SENSOR_NOISE_REDUCTION_NUM_MEASUREMENTS, 3);
        for (int i=0; i<numMeasurementValues.getCount(); i++) {
            if (numMeasurements.equals(Integer.valueOf(numMeasurementValues.getItem(i)))) {
                noiseReductionNumMeasurements.setSelection(i);
                break;
            }
        }

        // noise reduction
        final Spinner noiseReductionSpinner = findViewById(R.id.sensors_noise_spinner);
        final ArrayAdapter<String> noiseReductionTypes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sensor_noise_reduction_types));
        noiseReductionSpinner.setAdapter(noiseReductionTypes);
        noiseReductionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> aAdapterView, View aView, int aPosition, long aL) {
                Log.i(LOG_TAG_UI, "Noise reduction: " + aPosition + " " + noiseReductionTypes.getItem(aPosition));
                boolean enabled = aPosition != NOISE_REDUCTION_DISABLED_POSITION;
                ConfigUtil.setBooleanProperty(PREF_SENSOR_NOISE_REDUCTION, enabled);

                if (enabled) {
                    noiseReductionSpinner.setSelection(NOISE_REDUCTION_DISABLED_POSITION + 1);
                    noiseReductionNumMeasurements.setVisibility(View.VISIBLE);
                    noiseReductionNumMeasurementsLabel.setVisibility(View.VISIBLE);
                } else {
                    noiseReductionSpinner.setSelection(NOISE_REDUCTION_DISABLED_POSITION);
                    noiseReductionNumMeasurements.setVisibility(View.INVISIBLE);
                    noiseReductionNumMeasurementsLabel.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> aAdapterView) {

            }
        });
        if (ConfigUtil.getBooleanProperty(PREF_SENSOR_NOISE_REDUCTION)) {
            noiseReductionSpinner.setSelection(NOISE_REDUCTION_DISABLED_POSITION + 1);
            noiseReductionNumMeasurements.setVisibility(View.VISIBLE);
            noiseReductionNumMeasurementsLabel.setVisibility(View.VISIBLE);
        } else {
            noiseReductionSpinner.setSelection(NOISE_REDUCTION_DISABLED_POSITION);
            noiseReductionNumMeasurements.setVisibility(View.INVISIBLE);
            noiseReductionNumMeasurementsLabel.setVisibility(View.INVISIBLE);
        }


    }// end of onCreate

    private String[] createTranslateArray(Integer[] availableSensorsArrayArg){
        String[] translateArray = new String[availableSensorsArrayArg.length];
        for (int i = 0; i < availableSensorsArrayArg.length; i++){
            int currentSensor = availableSensorsArrayArg[i];
            switch (currentSensor){
                case OrientationProcessorFactory.SENSOR_TYPE_ROTATION : translateArray[i] = getString(R.string.rotation_sensor); break;
                case OrientationProcessorFactory.SENSOR_TYPE_MAGNETIC : translateArray[i] = getString(R.string.magnetic_sensor); break;
                case OrientationProcessorFactory.SENSOR_TYPE_ORIENTATION : translateArray[i] = getString(R.string.orientation_sensor);break;
                default:
                    Log.e(LOG_TAG_UI, "Unknown sensor type: " + currentSensor);
                    translateArray[i] = getString(R.string.sensor_none);
            }
        }
        return translateArray;
    }

	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
	 */
	@Override
	protected String getScreenTitle() {
		return getString(R.string.sensors_title);
	}

    /**
     * Action method that handles the tooltip for choosing a sensor
     *
     * @param viewArg - view to use
     */
    public void onSensorsChooseInfo(View viewArg){
        InfoDialogFragment infoDialog = new InfoDialogFragment();

        Bundle bundle = new Bundle();
        String message = getString(R.string.sensor_choose_tooltip);
        bundle.putString(InfoDialogFragment.MESSAGE, message);
        infoDialog.setArguments(bundle);

        infoDialog.show(getSupportFragmentManager(), CHOOSE_SENSORS_TOOLTIP_DIALOG);
    }

    @Override
    protected boolean showBaseOptionsMenu() {
        return false;
    }

    public void openSensorsTest(View viewArg) {
        Log.i(LOG_TAG_UI, "Azimuth Test");
        Intent intent = new Intent(SensorsActivity.this, SensorTestActivity.class);
        startActivity(intent);
    }
}
