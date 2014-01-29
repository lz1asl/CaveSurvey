/**
 * 
 */
package com.astoev.cave.survey.service.orientation;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;

/**
 * Abstract Azimuth processor defines the base interface for working with all azimuth processors and sensors.
 * 
 * @author jmitrev
 *
 */
public abstract class OrientationProcessor implements SensorEventListener {
	
    protected final static float RAD2GRAD = (float)(180.0f/Math.PI);
    
    /** Default sensor delay */
    protected final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;
	
	/** Sensor manager*/
    protected SensorManager sensorManager;
    
    protected Context context;

	/** Listener to notify on value change*/
	protected OrientationChangedListener listener;
	
	/** Sensor's accuracy */
	protected int accuracy;
	
	/** Rotation of the screen */
	private int rotation = Surface.ROTATION_0;
	
    /**
     * Constructor for OrientationProcessor
     * 
     * @param contextArg  - context to use
     * @param listenerArg - listener to notify on value change
     */
    public OrientationProcessor(Context contextArg, OrientationChangedListener listenerArg){
    	context = contextArg;
    	listener = listenerArg;
    	if (listenerArg == null){
    		Log.w(Constants.LOG_TAG_SERVICE, "AzimuthChangedListener is not specified");
    	}
    	
    	sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }
    
    /**
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracyArg) {
		accuracy = accuracyArg;
		if (listener != null){
			listener.onAccuracyChanged(accuracyArg);
		}
	}

	/**
     * Returns the current sensor
     * 
     * @return Sensor
     */
    public abstract Sensor getSensor();

    /**
     * Starts the sensor listeners
     */
	public abstract void startListening();
	
	/**
	 * Stops the sensor listeners
	 */
	public abstract void stopListening();
	
	/**
	 * @return the accuracy
	 */
	public int getAccuracy() {
		return accuracy;
	}
	
	public String getAccuracyAsString(int accuracyArg){
		switch (accuracyArg) {
		case SensorManager.SENSOR_STATUS_UNRELIABLE:
			return context.getString(R.string.SENSOR_STATUS_UNRELIABLE);
		case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
			return context.getString(R.string.SENSOR_STATUS_ACCURACY_LOW);
		case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
			return context.getString(R.string.SENSOR_STATUS_ACCURACY_MEDIUM);
		case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
			return context.getString(R.string.SENSOR_STATUS_ACCURACY_HIGH);
		default:
			return context.getString(R.string.SENSOR_STATUS_ACCURACY_UNKNOWN);
		}
	}

	/**
	 * Helper method that shows if the this processor can be read. 
	 * 
	 * @return true if the underlying sensor is available, otherwise false 
	 */
	public boolean canReadAzimuth(){
		return (getSensor() != null);
	}
	
	/**
	 * Helper method to get the rotation
	 * 
	 * @return rotation
	 */
	protected int getRotation(){
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO){
    		rotation = getSafeRotation();
    	}
    	return rotation;
	}
	
	/**
	 * Safely get rotation for api 8+
	 * 
	 * @return rotation
	 */
    @TargetApi(Build.VERSION_CODES.FROYO)
    private int getSafeRotation(){
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		
		if (windowManager != null){
			Display display = windowManager.getDefaultDisplay();
			return display.getRotation();
		}
		
		return Surface.ROTATION_0;
    }
    
    /**
     * Re maps the coordinate system according the screen orientation
     * 
     * @param inRArg      - in rotation matrix
     * @param rotationArg - rotation
     * @return result rotation matrix if the remaping is correct, otherwise null
     */
    protected float[] remapCoordinateSystem(float[] inRArg, int rotationArg){
    	float[] outR = new float[9];
    	boolean success = false;
    	switch (rotationArg) {
    	case Surface.ROTATION_0:
    		success = true;
    		outR = inRArg;
    		break;
		case Surface.ROTATION_90:
			success =  SensorManager.remapCoordinateSystem(inRArg, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
			break;
		case Surface.ROTATION_180:
			success = SensorManager.remapCoordinateSystem(inRArg, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, outR); 
			break;
		case Surface.ROTATION_270:
			success = SensorManager.remapCoordinateSystem(inRArg, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, outR);
			break;
		}
    	
    	if (success){
    		return outR;
    	} else {
    		return null;
    	}
    }
}
