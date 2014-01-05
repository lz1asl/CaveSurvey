/**
 * 
 */
package com.astoev.cave.survey.service.azimuth;

import com.astoev.cave.survey.Constants;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Azimuth processor wraps the work with azimuth sensors
 * 
 * @author jmitrev
 */
public class AzimuthProcessor implements SensorEventListener {

	/** Listener to notify on value change*/
	private AzimuthChangedListener listener;
	
	/** Sensor manager*/
    private SensorManager sensorManager;
    
    /** Compass sensor to use */
    private Sensor compassSensor;

    private Context context;
    
    /** Last read value from the sensor*/
    private float lastValue;
    
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
    }

	/**
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	/**
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		lastValue = event.values[0];
		if (listener != null){
			listener.onAzimuthChanged(lastValue);
		}
	}
	
	/**
	 * Obtains a sensor and starts the listener
	 */
	public void startListening(){
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        sensorManager.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	/**
	 * Stops the sensor listener
	 */
	public void stopListening(){
		if (sensorManager != null){
			sensorManager.unregisterListener(this, compassSensor);
		}
	}

	/**
	 * Getter for the last successfully read vale from the sensor
	 * 
	 * @return the lastValue
	 */
	public float getLastValue() {
		return lastValue;
	}

}
