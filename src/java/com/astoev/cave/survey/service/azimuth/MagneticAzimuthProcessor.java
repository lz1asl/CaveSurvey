/**
 * 
 */
package com.astoev.cave.survey.service.azimuth;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * Azimuth processor that works with magnetic and accelerometer sensors
 * 
 * @author jmitrev
 */
public class MagneticAzimuthProcessor extends AzimuthProcessor {

    /** Compass sensor to use */
    private Sensor compassSensor;
    
    /** Accelerometer sensor */
    private Sensor accelerometerSensor;

    /** Last read value from the sensor*/
    private float lastValue;
    
    private float[] R = new float[16];
    private float[] I = new float[16];
    private float aData[] = new float[3];
    private float mData[] = new float[3];
    private float oData[] = new float[3];
    
    /**
     * Constructor for MagneticAzimuthProcessor
     * 
     * @param contextArg  - context to use
     * @param listenerArg - listener to notify on value change
     */
    public MagneticAzimuthProcessor(Context contextArg, AzimuthChangedListener listenerArg){
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
		int sensorType = event.sensor.getType();
		
		switch (sensorType) {
		case Sensor.TYPE_MAGNETIC_FIELD:
		{
			System.arraycopy(event.values, 0, mData, 0, 3);
		}
		break;
		case Sensor.TYPE_ACCELEROMETER:
		{
			System.arraycopy(event.values, 0, aData, 0, 3);
		}
		break;			

		default:
			break;
		}
		boolean success = SensorManager.getRotationMatrix(R, I, aData, mData);
		if (success){
			SensorManager.getOrientation(R, oData);
			lastValue = oData[0] * RAD2GRAD;
			listener.onAzimuthChanged(lastValue);
		}
	}
	
	/**
	 * Obtains a sensor and starts the listener
	 */
	@Override
	public void startListening(){

		if (canReadAzimuth()){
            compassSensor = getSensorMagnetic();
            accelerometerSensor = getSensorAccelerometer();
            
            sensorManager.registerListener(this, compassSensor, SENSOR_DELAY);
            sensorManager.registerListener(this, accelerometerSensor, SENSOR_DELAY);
		}
	}
	
	/**
	 * Stops the sensor listener
	 */
	@Override
	public void stopListening(){
		if (sensorManager != null){
			if (compassSensor != null){
				sensorManager.unregisterListener(this, compassSensor);
			}
			if (accelerometerSensor != null){
				sensorManager.unregisterListener(this, accelerometerSensor);
			}
		}
	}

	/**
	 * Getter for the last successfully read vale from the sensor
	 * 
	 * @return the lastValue
	 */
	@Override
	public float getLastValue() {
		return lastValue;
	}
	
	/**
	 * Will be able to read only if both sensors are available
	 * 
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthProcessor#canReadAzimuth()
	 */
	@Override
	public boolean canReadAzimuth(){
		return (getSensorMagnetic() != null && getSensorAccelerometer() != null);
	}
	
	/**
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthProcessor#getSensor()
	 */
	@Override
	public Sensor getSensor() {
		// use specific getSnesorXXX methods;
		return null;
	}

	/**
	 * Helper method to obtain the magnetic sensor
	 * 
	 * @return magnetic sensor
	 */
	private Sensor getSensorMagnetic(){
		if (compassSensor == null){
			compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);	
		}
        return compassSensor;
	}
	
	/**
	 * Helper method to obtain the accelerometer sensor
	 * @return
	 */
	private Sensor getSensorAccelerometer(){
		if (accelerometerSensor == null){
			accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		return accelerometerSensor;	
	}

}
