/**
 * 
 */
package com.astoev.cave.survey.service.orientation;

import android.content.Context;

/**
 * Factory for OrientationProcessor implementations
 * 
 * @author jmitrev
 */
public class OrientationProcessorFactory {

	
	/**
	 * Tries to instantiate processor for azimuth. Will return instance by this priority list: rotation, 
	 * magnetic, orientation
	 * 
	 * @param contextArg  - context to work with
	 * @param listenerArg - listener to notify about changes
	 * @return OrientationProcessor
	 */
	public static OrientationProcessor getAzimuthProcessor(Context contextArg, OrientationChangedListener listenerArg){
		
		RotationOrientationProcessor rotationOrientationProcessor = new RotationOrientationProcessor(contextArg, listenerArg);
		if (rotationOrientationProcessor.canReadAzimuth()){
			return rotationOrientationProcessor;
		}
		
		MagneticOrientationProcessor magneticOrientationProcessor = new MagneticOrientationProcessor(contextArg, listenerArg);
		if (magneticOrientationProcessor.canReadAzimuth()){
			return magneticOrientationProcessor;
		}
		
		return new OrientationDeprecatedProcessor(contextArg, listenerArg);
	}
	
	/**
	 * Helper method to check if there is a processor who can effectively read the azimuth
	 * 
	 * @param contextArg 
	 * @return true if there is at least single processor who can read the azimuth, otherwise false
	 */
	public static boolean canReadAzimuth(Context contextArg){
		return getAzimuthProcessor(contextArg, null).canReadAzimuth();
	}
	
}
