/**
 * 
 */
package com.astoev.cave.survey.activity.poc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.service.azimuth.AzimuthChangedListener;
import com.astoev.cave.survey.service.azimuth.MagneticAzimuthProcessor;
import com.astoev.cave.survey.service.azimuth.OrientationAzimuthProcessor;
import com.astoev.cave.survey.service.azimuth.RotationAzimuthProcessor;

/**
 * Activity that tests all available azimuth sensors and processor implementations
 * 
 * @author jmitrev
 */
public class AzimuthTestActivity extends MainMenuActivity {

	private TextView orientationView;
	private TextView magneticView;
	private TextView rotationView;
	
	private Button startButton;
	private Button stopButton;
	
	private OrientationAzimuthProcessor orientationAzimuthProcessor;
	private MagneticAzimuthProcessor magneticAzimuthProcessor;
	private RotationAzimuthProcessor rotationAzimuthProcessor;
	
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
        
        startButton = (Button)findViewById(R.id.azimuth_btn_start);
        stopButton = (Button)findViewById(R.id.azimuth_btn_stop);
        
        orientationAzimuthProcessor = new OrientationAzimuthProcessor(this, new AzimuthChangedListener() {
			
			@Override
			public void onAzimuthChanged(float newValueArg) {
				orientationView.setText(String.valueOf(newValueArg));
			}
		});
        magneticAzimuthProcessor = new MagneticAzimuthProcessor(this, new AzimuthChangedListener() {
			
			@Override
			public void onAzimuthChanged(float newValueArg) {
				magneticView.setText(String.valueOf(newValueArg));
			}
		});
        rotationAzimuthProcessor = new RotationAzimuthProcessor(this, new AzimuthChangedListener() {
			
			@Override
			public void onAzimuthChanged(float newValueArg) {
				rotationView.setText(String.valueOf(newValueArg));
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
			orientationAzimuthProcessor.startListening();
			magneticAzimuthProcessor.startListening();
			rotationAzimuthProcessor.startListening();
			
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
			orientationAzimuthProcessor.stopListening();
			magneticAzimuthProcessor.stopListening();
			rotationAzimuthProcessor.stopListening();
			
			started = false;
			
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
	}
}
