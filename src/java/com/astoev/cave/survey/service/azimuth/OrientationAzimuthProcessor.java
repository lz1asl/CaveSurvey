/**
 * 
 */
package com.astoev.cave.survey.service.azimuth;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.Surface;

/**
 * Azimuth processor that works with orientation sensor. Note that Orientation sensor is deprecated since api9
 * 
 * @author jmitrev
 */
public class OrientationAzimuthProcessor extends AzimuthProcessor {

	/** Orientation sensor */
    private Sensor orientationSensor;
	
	/** Last read value from the sensor*/
    private float lastValue;
    
	
    /**
     * Constructor for OrientationAzimuthProcessor
     * 
     * @param contextArg  - context to use
     * @param listenerArg - listener to notify on value change
     */
    public OrientationAzimuthProcessor(Context contextArg, AzimuthChangedListener listenerArg){
    	super(contextArg, listenerArg);
    }
    
	/**
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] data = event.values;
		
		float currentValue = data[0] < 0 ? data[0] + 360 : data[0];
		
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
			break;
		case Surface.ROTATION_180:
			currentValue += 180;
			if (currentValue > 360){
				currentValue %= 360;
			}
			break;
		case Surface.ROTATION_270:
			currentValue += 270;
			if (currentValue > 360){
				currentValue %= 360;
			}
			break;
		default:
			break;
		}
		
		lastValue = currentValue;
		
		listener.onAzimuthChanged(lastValue);
	}

	/**
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthProcessor#startListening()
	 */
	@Override
	public void startListening() {
		if (canReadAzimuth()){
			orientationSensor = getSensor();
            
            sensorManager.registerListener(this, orientationSensor, SENSOR_DELAY);
		}
	}

	/**
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthProcessor#stopListening()
	 */
	@Override
	public void stopListening() {
		if (sensorManager != null && orientationSensor != null){
			sensorManager.unregisterListener(this, orientationSensor);
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
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthProcessor#getSensor()
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
