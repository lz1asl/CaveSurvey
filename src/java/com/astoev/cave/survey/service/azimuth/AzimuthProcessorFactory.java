/**
 * 
 */
package com.astoev.cave.survey.service.azimuth;

import android.content.Context;

/**
 * Factory for AzimuthProcessor implementations
 * 
 * @author jmitrev
 */
public class AzimuthProcessorFactory {

	
	/**
	 * Tries to instantiate processor for azimuth. Will return instance by this priority list: rotation, 
	 * magnetic, orientation
	 * 
	 * @param contextArg  - context to work with
	 * @param listenerArg - listener to notify about changes
	 * @return AzimuthProcessor
	 */
	public static AzimuthProcessor getAzimuthProcessor(Context contextArg, AzimuthChangedListener listenerArg){
		
		RotationAzimuthProcessor rotationAzimuthProcessor = new RotationAzimuthProcessor(contextArg, listenerArg);
		if (rotationAzimuthProcessor.canReadAzimuth()){
			return rotationAzimuthProcessor;
		}
		
		MagneticAzimuthProcessor magneticAzimuthProcessor = new MagneticAzimuthProcessor(contextArg, listenerArg);
		if (magneticAzimuthProcessor.canReadAzimuth()){
			return magneticAzimuthProcessor;
		}
		
		return new OrientationAzimuthProcessor(contextArg, listenerArg);
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
