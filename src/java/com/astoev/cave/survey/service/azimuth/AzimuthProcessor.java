/**
 * 
 */
package com.astoev.cave.survey.service.azimuth;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.astoev.cave.survey.Constants;

/**
 * Abstract Azimuth processor defines the base interface for working with all azimuth processors and sensors.
 * 
 * @author jmitrev
 *
 */
public abstract class AzimuthProcessor implements SensorEventListener {
	
    protected final static float RAD2GRAD = (float)(180.0f/Math.PI);
    
    /** Default sensor delay */
    protected final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;
	
	/** Sensor manager*/
    protected SensorManager sensorManager;
    
    protected Context context;

	/** Listener to notify on value change*/
	protected AzimuthChangedListener listener;
	
    /**
     * Constructor for AzimuthProcessor
     * 
     * @param contextArg  - context to use
     * @param listenerArg - listener to notify on value change
     */
    public AzimuthProcessor(Context contextArg, AzimuthChangedListener listenerArg){
    	context = contextArg;
    	listener = listenerArg;
    	if (listenerArg == null){
    		Log.w(Constants.LOG_TAG_SERVICE, "AzimuthChangedListener is not specified");
    	}
    	
    	sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
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
	 * Returns the last available value
	 * 
	 * @return last read value
	 */
	public abstract float getLastValue();
	
	/**
	 * Helper method that shows if the this processor can be read. 
	 * 
	 * @return true if the underlying sensor is available, otherwise false 
	 */
	public boolean canReadAzimuth(){
		return (getSensor() != null);
	}
}
