/**
 * 
 */
package com.astoev.cave.survey.service.orientation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.Surface;

/**
 * Processor that works with deprecated synthetic orientation sensor. Note that Orientation sensor is 
 * deprecated since api9
 * 
 * @author jmitrev
 */
public class OrientationDeprecatedProcessor extends OrientationProcessor {

	/** Orientation sensor */
    private Sensor orientationSensor;
	
	/** Last read value from the sensor*/
    private float lastValue;
    
	
    /**
     * Constructor for OrientationDeprecatedProcessor
     * 
     * @param contextArg  - context to use
     * @param listenerArg - listener to notify on value change
     */
    public OrientationDeprecatedProcessor(Context contextArg, OrientationChangedListener listenerArg){
    	super(contextArg, listenerArg);
    }
    
	/**
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] data = event.values.clone();
		
		float currentValue = data[0] < 0 ? data[0] + 360 : data[0];
		float pitch = data[1];
		float roll = data[2];
		
		// handle device rotation
		int rotation = getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			break;
		case Surface.ROTATION_90:
			currentValue = currentValue + 90;
			if (currentValue > 360){
				currentValue %= 360;
			}
			pitch = -data[2];
			roll = data[1];
			break;
		case Surface.ROTATION_180:
			currentValue += 180;
			if (currentValue > 360){
				currentValue %= 360;
			}
			pitch = -data[1];
			roll = -data[2];
			break;
		case Surface.ROTATION_270:
			currentValue += 270;
			if (currentValue > 360){
				currentValue %= 360;
			}
			pitch = data[2];
			roll = data[1];
			break;
		default:
			break;
		}

		lastValue = currentValue;
		
        float[] converted = new float[3];
        converted[0] = lastValue;
        converted[1] = pitch;
        converted[2] = roll;
        listener.onOrinationChanged(converted);

	}

	/**
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#startListening()
	 */
	@Override
	public void startListening() {
		if (canReadAzimuth()){
			orientationSensor = getSensor();
            
            sensorManager.registerListener(this, orientationSensor, SENSOR_DELAY);
		}
	}

	/**
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#stopListening()
	 */
	@Override
	public void stopListening() {
		if (sensorManager != null && orientationSensor != null){
			sensorManager.unregisterListener(this, orientationSensor);
		}
	}

	/**
	 * @see com.astoev.cave.survey.service.orientation.OrientationProcessor#getSensor()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Sensor getSensor() {
		if (orientationSensor == null){
			orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		}
		return orientationSensor;
	}

}
