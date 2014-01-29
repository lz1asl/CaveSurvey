/**
 * 
 */
package com.astoev.cave.survey.activity.poc;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.service.orientation.AzimuthChangedAdapter;
import com.astoev.cave.survey.service.orientation.MagneticOrientationProcessor;
import com.astoev.cave.survey.service.orientation.OrientationDeprecatedProcessor;
import com.astoev.cave.survey.service.orientation.RotationOrientationProcessor;
import com.astoev.cave.survey.service.orientation.SlopeChangedAdapter;

/**
 * Activity that tests all available azimuth sensors and processor implementations
 * 
 * @author jmitrev
 */
public class AzimuthTestActivity extends MainMenuActivity {

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
        setContentView(R.layout.azimuthtest);
        
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
		return getString(R.string.azimuth_test_title);
	}

	/**
	 * Action method for start button. Starts all listeners
	 * 
	 * @param view
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
	 * @param view
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
}
