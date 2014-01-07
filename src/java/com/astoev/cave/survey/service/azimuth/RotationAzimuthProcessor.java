/**
 * 
 */
package com.astoev.cave.survey.service.azimuth;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;

/**
 * Azimuth processor that works with rotation sensor. Note that rotation sensor is available after api 9
 * 
 * @author jmitrev
 */
public class RotationAzimuthProcessor extends AzimuthProcessor {
	
    protected float[] R = new float[16];
    protected float[] I = new float[16];
    protected float[] rData = new float[3];
    protected float[] oData = new float[3];

    /** Last read value from the sensor */
    private float lastValue;
    
    /** Rotation sensor */
    private Sensor rotationSensor;
	
    /**
     * Constructor for RotationAzimuthProcessor
     * 
     * @param contextArg  - context to use
     * @param listenerArg - listener to notify on value change
     */
    public RotationAzimuthProcessor(Context contextArg, AzimuthChangedListener listenerArg){
    	super(contextArg, listenerArg);
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
		System.arraycopy(event.values, 0, rData, 0, 3);
		
		if (isApiAvailable()){
			processSafeData();
			
//			lastValue = oData[0] * RAD2GRAD;
			lastValue = oData[0] < 0 ? oData[0] * RAD2GRAD + 360 : oData[0] * RAD2GRAD;
			
			if (listener != null){
				listener.onAzimuthChanged(lastValue);
			}
		}
	}
	
	/**
	 * Helper method to process safely the date for implementations newer then api 9
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void processSafeData(){
		SensorManager.getRotationMatrixFromVector(R, rData);
		SensorManager.getOrientation(R, oData);
	}

	/**
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthProcessor#startListening()
	 */
	@Override
	public void startListening() {
		if (canReadAzimuth()){
			rotationSensor = getSensor();
			if (rotationSensor != null){
				sensorManager.registerListener(this, rotationSensor, SENSOR_DELAY);
			}
		}
	}
	
	/**
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthProcessor#getSensor()
	 */
	@Override
	public Sensor getSensor() {
		if (!isApiAvailable()){
			return null;
		}
		return getSafeRotationSensor();
	}

	/**
	 * Helper method that instantiates safely the sensor for os newer the api 9
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private Sensor getSafeRotationSensor(){
		if (rotationSensor == null){
			rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		}
		return rotationSensor;
	}

	/**
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthProcessor#stopListening()
	 */
	@Override
	public void stopListening() {
		if (sensorManager != null && rotationSensor != null){
			sensorManager.unregisterListener(this, rotationSensor);
		}
	}

	/**
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthProcessor#getLastValue()
	 */
	@Override
	public float getLastValue() {
		return lastValue;
	}

	/**
	 * Helper method that checks if the sensor is available for the current api version
	 * 
	 * @return true if os version newer the api 9, otherwise false
	 */
	private boolean isApiAvailable(){
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD);
	}

}
