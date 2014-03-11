/**
 * 
 */
package com.astoev.cave.survey.service.orientation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.Surface;

/**
 * Azimuth processor that works with magnetic and accelerometer sensors
 * 
 * @author jmitrev
 */
public class MagneticOrientationProcessor extends OrientationProcessor {

    /** Compass sensor to use */
    private Sensor magneticSensor;
    
    /** Accelerometer sensor */
    private Sensor accelerometerSensor;

    /** Last read value from the sensor*/
    private float lastValue;
    
    private int magneticAccuracy;
    private int accelerometerAccuracy;
    
    private float[] R = new float[9];
//    private float[] I = new float[16];
//    private float[] R = new float[16];
//    private float[] I = new float[16];
    private float aData[] = new float[3];
    private float mData[] = new float[3];
    private float oData[] = new float[3];
    
    /**
     * Constructor for MagneticOrientationProcessor
     * 
     * @param contextArg  - context to use
     * @param listenerArg - listener to notify on value change
     */
    public MagneticOrientationProcessor(Context contextArg, OrientationChangedListener listenerArg){
    	super(contextArg, listenerArg);
    }

	/**
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor sensorArg, int accuracyArg) {
		switch (sensorArg.getType()) {
		case Sensor.TYPE_MAGNETIC_FIELD:
			magneticAccuracy = accuracyArg;
			break;
		case Sensor.TYPE_ACCELEROMETER:
			accelerometerAccuracy = accuracyArg;
			break;
		default:
			break;
		}
		if (listener != null){
			listener.onAccuracyChanged(getAccuracy());
		}
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
		
		int rotation = getRotation();
		
		boolean success = SensorManager.getRotationMatrix(R, null, aData, mData);
		if (success){
			R = (rotation != Surface.ROTATION_0) ? remapCoordinateSystem(R, rotation): R;
			if (R != null){
				SensorManager.getOrientation(R, oData);
				
				lastValue = oData[0] < 0 ? oData[0] * RAD2GRAD + 360 : oData[0] * RAD2GRAD;
				
				if (listener != null){
	                float[] converted = new float[3];
	                converted[0] = lastValue;
	                converted[1] = oData[1] * RAD2GRAD;
	                converted[2] = oData[2] * RAD2GRAD;
	                listener.onOrinationChanged(converted);
				}
			}
		}
	}
	
	/**
	 * Obtains a sensor and starts the listener
	 */
	@Override
	public void startListening(){

		if (canReadOrientation()){
            magneticSensor = getSensorMagnetic();
            accelerometerSensor = getSensorAccelerometer();
            
            sensorManager.registerListener(this, magneticSensor, SENSOR_DELAY);
            sensorManager.registerListener(this, accelerometerSensor, SENSOR_DELAY);
		}
	}
	
	/**
	 * Stops the sensor listener
	 */
	@Override
	public void stopListening(){
		if (sensorManager != null){
			if (magneticSensor != null){
				sensorManager.unregisterListener(this, magneticSensor);
			}
			if (accelerometerSensor != null){
				sensorManager.unregisterListener(this, accelerometerSensor);
			}
		}
	}
	
	/**
	 * Will be able to read only if both sensors are available
	 * 
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#canReadOrientation()
	 */
	@Override
	public boolean canReadOrientation(){
		return (getSensorMagnetic() != null && getSensorAccelerometer() != null);
	}
	
	/**
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#getSensor()
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
		if (magneticSensor == null){
			magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);	
		}
        return magneticSensor;
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

	/**
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#getAccuracy()
	 */
	@Override
	public int getAccuracy() {
		return magneticAccuracy * 10 + accelerometerAccuracy;
	}

	/**
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#getAccuracyAsString(int)
	 */
	@Override
	public String getAccuracyAsString(int accuracyArg) {
		int accuracy = Math.min(magneticAccuracy, accelerometerAccuracy);
		return super.getAccuracyAsString(accuracy);
	}

}
