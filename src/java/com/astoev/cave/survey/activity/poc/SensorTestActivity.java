/**
 * 
 */
package com.astoev.cave.survey.activity.poc;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.fragment.InfoDialogFragment;
import com.astoev.cave.survey.service.bluetooth.util.MeasurementsUtil;
import com.astoev.cave.survey.service.orientation.AzimuthChangedAdapter;
import com.astoev.cave.survey.service.orientation.MagneticOrientationProcessor;
import com.astoev.cave.survey.service.orientation.OrientationDeprecatedProcessor;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;
import com.astoev.cave.survey.service.orientation.RotationOrientationProcessor;
import com.astoev.cave.survey.service.orientation.SlopeChangedAdapter;

import java.text.DecimalFormat;

import static com.astoev.cave.survey.model.Option.CODE_SENSOR_INTERNAL;

/**
 * Activity that tests all available azimuth sensors and processor implementations.
 *
 * @author Zhivko Mitrev
 */
public class SensorTestActivity extends MainMenuActivity {

    /** Dialog name to enable choose sensors tooltip dialog */
    private static final String CHOOSE_SENSORS_TOOLTIP_DIALOG = "CHOOSE_SENSORS_TOOLTIP_DIALOG";
    private static final String TEST_SENSORS_TOOLTIP_DIALOG = "TEST_SENSORS_TOOLTIP_DIALOG";

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

                // Format the value from int to String (prevents loosing leading 0 for the first sensor)
                StringBuilder builder = new StringBuilder();
                if (accuracyArg < 10){
                    builder.append("0 ").append(accuracyArg);
                } else {
                    builder.append(accuracyArg / 10).append(" ")
                    .append(accuracyArg % 10);
                }
				magneticAccuracyView.setText(builder.toString());
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

        // test fields
		// handle double click for reading built-in azimuth
		EditText azimuth = (EditText) findViewById(R.id.sensortest_azimuth);
		MeasurementsUtil.bindAzimuthAwareField(azimuth, getSupportFragmentManager(), CODE_SENSOR_INTERNAL);

		// handle double click for reading built-in slope
		EditText slope = (EditText) findViewById(R.id.sensortest_slope);
		MeasurementsUtil.bindSlopeAwareField(slope, getSupportFragmentManager(), CODE_SENSOR_INTERNAL);

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

    /**
     * Action method that handles the tooltip for testing sensors
     *
     * @param viewArg - view to use
     */
    public void  onSensorsTestInfo(View viewArg){
        InfoDialogFragment infoDialog = new InfoDialogFragment();

        Bundle bundle = new Bundle();
        String message = getString(R.string.sensor_test_tooltip);
        bundle.putString(InfoDialogFragment.MESSAGE, message);
        infoDialog.setArguments(bundle);

        infoDialog.show(getSupportFragmentManager(), TEST_SENSORS_TOOLTIP_DIALOG);
    }

    @Override
    protected boolean showBaseOptionsMenu() {
        return false;
    }
}
