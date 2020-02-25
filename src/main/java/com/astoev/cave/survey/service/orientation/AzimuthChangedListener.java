/**
 * 
 */
package com.astoev.cave.survey.service.orientation;

/**
 * Notifies a top level listener when an azimuth is changed
 * 
 * @author jmitrev
 *
 */
public interface AzimuthChangedListener {

	void onAzimuthChanged(float newValueArg);
	
}
