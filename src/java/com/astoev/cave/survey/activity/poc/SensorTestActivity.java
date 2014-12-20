/**
 * 
 */
package com.astoev.cave.survey.activity.poc;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.service.orientation.AzimuthChangedAdapter;
import com.astoev.cave.survey.service.orientation.MagneticOrientationProcessor;
import com.astoev.cave.survey.service.orientation.OrientationDeprecatedProcessor;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;
import com.astoev.cave.survey.service.orientation.RotationOrientationProcessor;
import com.astoev.cave.survey.service.orientation.SlopeChangedAdapter;
import com.astoev.cave.survey.util.ConfigUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Activity that tests all available azimuth sensors and processor implementations.
 * It also provides an option to choose the default sensor that will be used application wide.
 * 
 * @author Zhivko Mitrev
 */
public class SensorTestActivity extends MainMenuActivity {

    private Integer[] availableSensorsArray;

	private TextView orientationView;
	private TextView magneticView;
	private TextView rotationView;
	
	private TextView orientationSlopeView;
	private TextView magneticSlopeView;
	private TextView rotationSlopeView;
	
	private TextView orientationAccuracyView;
	private TextView magneticAccuracyView;
	private TextView rotationAccuracyView;
	
	private Button startButton;
	private Button stopButton;
	
	private OrientationDeprecatedProcessor orientationDeprecatedProcessor;
	private MagneticOrientationProcessor magneticOrientationProcessor;
	private RotationOrientationProcessor rotationOrientationProcessor;
	
	private OrientationDeprecatedProcessor orientationSlopeProcessor;
	private MagneticOrientationProcessor magneticSlopeProcessor;
	private RotationOrientationProcessor rotationSlopeProcessor;
	
	private DecimalFormat azimuthFormater;
	
	/** Flag to show if the listeners are started*/
	public boolean started = false;
	
	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensortest);
        
        orientationView = (TextView)findViewById(R.id.azimuth_orientation);
        magneticView = (TextView)findViewById(R.id.azimuth_magnetic);
        rotationView = (TextView)findViewById(R.id.azimuth_rotation);
        
        orientationSlopeView = (TextView)findViewById(R.id.slope_orientation);
        magneticSlopeView = (TextView)findViewById(R.id.slope_magnetic);
        rotationSlopeView = (TextView)findViewById(R.id.slope_rotation);
        
        orientationAccuracyView = (TextView)findViewById(R.id.azimuth_orientation_accuracy);
        magneticAccuracyView = (TextView)findViewById(R.id.azimuth_magnetic_accuracy);
        rotationAccuracyView = (TextView)findViewById(R.id.azimuth_rotation_accuracy);
        
        startButton = (Button)findViewById(R.id.azimuth_btn_start);
        stopButton = (Button)findViewById(R.id.azimuth_btn_stop);
        
        azimuthFormater = new DecimalFormat("#.#");
        
        orientationDeprecatedProcessor = new OrientationDeprecatedProcessor(this, new AzimuthChangedAdapter() {
			
			@Override
			public void onAzimuthChanged(float newValueArg) {
				orientationView.setText(azimuthFormater.format(newValueArg));
			}

			@Override
			public void onAccuracyChanged(int accuracyArg) {
				orientationAccuracyView.setText(String.valueOf(accuracyArg));
			}
		});
        magneticOrientationProcessor = new MagneticOrientationProcessor(this, new AzimuthChangedAdapter() {
			
			@Override
			public void onAzimuthChanged(float newValueArg) {
				magneticView.setText(azimuthFormater.format(newValueArg));
			}
			
			@Override
			public void onAccuracyChanged(int accuracyArg) {
				magneticAccuracyView.setText(String.valueOf(accuracyArg));
			}
		});
        rotationOrientationProcessor = new RotationOrientationProcessor(this, new AzimuthChangedAdapter() {
			
			@Override
			public void onAzimuthChanged(float newValueArg) {
				rotationView.setText(azimuthFormater.format(newValueArg));
			}
			
			@Override
			public void onAccuracyChanged(int accuracyArg) {
				rotationAccuracyView.setText(String.valueOf(accuracyArg));
			}
			
		});
        
        orientationSlopeProcessor = new OrientationDeprecatedProcessor(this, new SlopeChangedAdapter(){
            @Override
            public void onSlopeChanged(float newValueArg) {
                orientationSlopeView.setText(azimuthFormater.format(newValueArg));
            }
        });

        magneticSlopeProcessor = new MagneticOrientationProcessor(this, new SlopeChangedAdapter(){
            @Override
            public void onSlopeChanged(float newValueArg) {
                magneticSlopeView.setText(azimuthFormater.format(newValueArg));
            }
        });
        
        rotationSlopeProcessor = new RotationOrientationProcessor(this, new SlopeChangedAdapter(){
            @Override
            public void onSlopeChanged(float newValueArg) {
                rotationSlopeView.setText(azimuthFormater.format(newValueArg));
            }
        });

        //  determine orientation sensors that are available on the device
        ArrayList<Integer> sensorsList = new ArrayList<Integer>();
        if (rotationOrientationProcessor.canReadOrientation()){
            sensorsList.add(OrientationProcessorFactory.SENSOR_TYPE_ROTATION);
        }
        if (magneticOrientationProcessor.canReadOrientation()){
            sensorsList.add(OrientationProcessorFactory.SENSOR_TYPE_MAGNETIC);
        }
        if (orientationDeprecatedProcessor.canReadOrientation()){
            sensorsList.add(OrientationProcessorFactory.SENSOR_TYPE_ORIENTATION);
        }

        // fill the sensor spinner if there are any available sensors
        if (sensorsList.size() > 0) {
            availableSensorsArray = new Integer[sensorsList.size()];
            availableSensorsArray = sensorsList.toArray(availableSensorsArray);

            // convert to strings array
            String[] str = createTranslateArray(availableSensorsArray);

            Spinner spinner = (Spinner) findViewById(R.id.sensors_spinner);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, str);
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
                    Log.i(Constants.LOG_TAG_UI, "Selected position:" + position + " sensor:" + selectedSensor);

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
            // TODO: no available sensors !
        }

	}

    private String[] createTranslateArray(Integer[] availableSensorsArrayArg){
        String[] translateArray = new String[availableSensorsArrayArg.length];
        for (int i = 0; i < availableSensorsArrayArg.length; i++){
            int currentSensor = availableSensorsArrayArg[i];
            switch (currentSensor){
                case OrientationProcessorFactory.SENSOR_TYPE_ROTATION : translateArray[i] = getString(R.string.rotation_sensor); break;
                case OrientationProcessorFactory.SENSOR_TYPE_MAGNETIC : translateArray[i] = getString(R.string.magnetic_sensor); break;
                case OrientationProcessorFactory.SENSOR_TYPE_ORIENTATION : translateArray[i] = getString(R.string.orientation_sensor);break;
                default:
                    Log.e(Constants.LOG_TAG_UI, "Unknown sensor type: " + currentSensor);
                    translateArray[i] = getString(R.string.sensor_none);
            }
        }
        return translateArray;
    }

	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (started){
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
		} else {
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
	}

	/**
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		onStop(null);
	}
	
	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
	 */
	@Override
	protected String getScreenTitle() {
		return getString(R.string.sensor_test_title);
	}

	/**
	 * Action method for start button. Starts all listeners
	 * 
	 * @param view - view to use
	 */
	public void onStart(View view){
		if (!started){
			orientationDeprecatedProcessor.startListening();
			magneticOrientationProcessor.startListening();
			rotationOrientationProcessor.startListening();
			
			orientationSlopeProcessor.startListening();
			magneticSlopeProcessor.startListening();
			rotationSlopeProcessor.startListening();
			
			started = true;
			
			startButton.setEnabled(false);
			stopButton.setEnabled(true);

		}
	}
	
	/**
	 * Action method for stop button. Starts all listeners
	 * 
	 * @param view - view to use
	 */
	public void onStop(View view){
		if (started){
			orientationDeprecatedProcessor.stopListening();
			magneticOrientationProcessor.stopListening();
			rotationOrientationProcessor.stopListening();
			
            orientationSlopeProcessor.stopListening();
            magneticSlopeProcessor.stopListening();
            rotationSlopeProcessor.stopListening();			
			
			started = false;
			
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
	}
//
//    public static class MyAdapter extends ArrayAdapter<CharSequence>{
//
//        private boolean enabledMap[];
//
//        public MyAdapter(Context context, int textViewResId, CharSequence[] strings) {
//            super(context, textViewResId, strings);
//        }
//
//        public static MyAdapter createFromResource(
//                Context context, int textArrayResId, int textViewResId) {
//
//            Resources resources = context.getResources();
//            CharSequence[] strings   = resources.getTextArray(textArrayResId);
//
//            return new MyAdapter(context, textViewResId, strings);
//        }
//
//        public void setEnabledMap(boolean[] enabledMapArg){
//            enabledMap = enabledMapArg;
//        }
//
//        @Override
//        public boolean areAllItemsEnabled() {
//            return false;
//        }
//
//        @Override
//        public boolean isEnabled(int position) {
//            if (enabledMap == null || enabledMap.length <= position){
//                return super.isEnabled(position);
//            }
//            return enabledMap[position];
//        }
//    }
}
