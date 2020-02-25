/**
 * 
 */
package com.astoev.cave.survey.service.orientation;

import android.content.Context;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.util.ConfigUtil;

/**
 * Factory for OrientationProcessor implementations
 * 
 * @author Zhivko Mitrev
 */
public class OrientationProcessorFactory {

    public static final int SENSOR_TYPE_UNKNOWN = 0;
    public static final int SENSOR_TYPE_ROTATION = 1;
    public static final int SENSOR_TYPE_MAGNETIC = 2;
    public static final int SENSOR_TYPE_ORIENTATION = 3;
	
	/**
	 * Tries to instantiate processor for orientation. Will return instance by this priority list: rotation, 
	 * magnetic, orientation
	 * 
	 * @param contextArg  - context to work with
	 * @param listenerArg - listener to notify about changes
	 * @return OrientationProcessor
	 */
	public static OrientationProcessor getOrientationProcessor(Context contextArg, OrientationChangedListener listenerArg){

        // try first to load the configured sensor
        Integer preferredSensor = ConfigUtil.getIntProperty(ConfigUtil.PREF_SENSOR);
        if (preferredSensor != null){
            switch (preferredSensor){
                case SENSOR_TYPE_ROTATION : {
                    RotationOrientationProcessor rotationOrientationProcessor = new RotationOrientationProcessor(contextArg, listenerArg);
                    if (rotationOrientationProcessor.canReadOrientation()){
                        return rotationOrientationProcessor;
                    }
                    Log.e(Constants.LOG_TAG_SERVICE, "Configured rotation processor can't read orientation!");
                }break;
                case SENSOR_TYPE_MAGNETIC : {
                    MagneticOrientationProcessor magneticOrientationProcessor = new MagneticOrientationProcessor(contextArg, listenerArg);
                    if (magneticOrientationProcessor.canReadOrientation()){
                        return magneticOrientationProcessor;
                    }
                    Log.e(Constants.LOG_TAG_SERVICE, "Configured magnetic processor can't read orientation!");
                } break;
                case SENSOR_TYPE_ORIENTATION : {
                    OrientationDeprecatedProcessor orientationDeprecatedProcessor =  new OrientationDeprecatedProcessor(contextArg, listenerArg);
                    if (orientationDeprecatedProcessor.canReadOrientation()){
                        return orientationDeprecatedProcessor;
                    }
                    Log.e(Constants.LOG_TAG_SERVICE, "Configured orientation processor can't read orientation!");
                }break;

            }
        }

        // if there is no configured sensor try instantiating by expected accuracy
		RotationOrientationProcessor rotationOrientationProcessor = new RotationOrientationProcessor(contextArg, listenerArg);
		if (rotationOrientationProcessor.canReadOrientation()){
			return rotationOrientationProcessor;
		}
		
		MagneticOrientationProcessor magneticOrientationProcessor = new MagneticOrientationProcessor(contextArg, listenerArg);
		if (magneticOrientationProcessor.canReadOrientation()){
			return magneticOrientationProcessor;
		}
		
		return new OrientationDeprecatedProcessor(contextArg, listenerArg);
	}
	
	/**
	 * Helper method to check if there is a processor who can effectively read the orientation change
	 *
	 * @param contextArg - context
	 * @return true if there is at least single processor who can read the orientation, otherwise false
	 */
	public static boolean canReadOrientation(Context contextArg){
		return getOrientationProcessor(contextArg, null).canReadOrientation();
	}

    /**
     * Helper method that returns the type of the sensor that is set as default for the application
     *
     * @param contextArg - context
     * @return int value representing the sensor type
     */
    public static int getDefaultSensorType(Context contextArg){
        OrientationProcessor processor = getOrientationProcessor(contextArg, null);
        if (processor instanceof RotationOrientationProcessor){
            return SENSOR_TYPE_ROTATION;
        }
        if (processor instanceof MagneticOrientationProcessor){
            return SENSOR_TYPE_MAGNETIC;
        }
        if (processor instanceof OrientationDeprecatedProcessor){
            return SENSOR_TYPE_ORIENTATION;
        }
        return SENSOR_TYPE_UNKNOWN;
    }
	
}
