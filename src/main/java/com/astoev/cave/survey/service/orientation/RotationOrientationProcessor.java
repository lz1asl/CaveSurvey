package com.astoev.cave.survey.service.orientation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.Surface;

/**
 * Azimuth processor that works with rotation sensor. Note that rotation sensor is available after api 9
 * 
 * @author jmitrev
 */
public class RotationOrientationProcessor extends OrientationProcessor {
	
    protected float[] R = new float[9];
//    protected float[] I = new float[9];
    protected float[] rData = new float[3];
    protected float[] oData = new float[3];

    /** Last read value from the sensor */
    private float lastValue;
    
    /** Rotation sensor */
    private Sensor rotationSensor;
	
    /**
     * Constructor for RotationOrientationProcessor
     * 
     * @param contextArg  - context to use
     * @param listenerArg - listener to notify on value change
     */
    public RotationOrientationProcessor(Context contextArg, OrientationChangedListener listenerArg){
    	super(contextArg, listenerArg);
    }
    
	/**
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		System.arraycopy(event.values, 0, rData, 0, 3);
		
		processSafeData();

		int rotation = getRotation();
		R = (rotation != Surface.ROTATION_0) ? remapCoordinateSystem(R, rotation): R;

		if (R != null){
			SensorManager.getOrientation(R, oData);

			lastValue = oData[0] < 0 ? oData[0] * RAD2GRAD + 360 : oData[0] * RAD2GRAD;

			if (listener != null){
				float[] converted = new float[3];
				converted[0] = lastValue;
				converted[1] = oData[1] * RAD2GRAD;
				converted[2] = oData[2] * RAD2GRAD;
				listener.onOrientationChanged(converted);
			}
		}
	}
	
	/**
	 * Helper method to process safely the date for implementations newer then api 9
	 */
	private void processSafeData(){
		SensorManager.getRotationMatrixFromVector(R, rData);
	}

	/**
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#startListening()
	 */
	@Override
	public void startListening() {
		if (canReadOrientation()){
			rotationSensor = getSensor();
			if (rotationSensor != null){
				sensorManager.registerListener(this, rotationSensor, SENSOR_DELAY);
			}
		}
	}
	
	/**
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#getSensor()
	 */
	@Override
	public Sensor getSensor() {
		return getSafeRotationSensor();
	}

	/**
	 * Helper method that instantiates safely the sensor for os newer the api 9
	 * @return
	 */
	private Sensor getSafeRotationSensor(){
		if (rotationSensor == null){
			rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		}
		return rotationSensor;
	}

	/**
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#stopListening()
	 */
	@Override
	public void stopListening() {
		if (sensorManager != null && rotationSensor != null){
			sensorManager.unregisterListener(this, rotationSensor);
		}
	}

}
