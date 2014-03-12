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
	 * Tries to instantiate processor for orientation. Will return instance by this priority list: rotation, 
	 * magnetic, orientation
	 * 
	 * @param contextArg  - context to work with
	 * @param listenerArg - listener to notify about changes
	 * @return OrientationProcessor
	 */
	public static OrientationProcessor getOrientationProcessor(Context contextArg, OrientationChangedListener listenerArg){
		
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
	 * @param contextArg 
	 * @return true if there is at least single processor who can read the orientation, otherwise false
	 */
	public static boolean canReadOrientation(Context contextArg){
		return getOrientationProcessor(contextArg, null).canReadOrientation();
	}
	
}
