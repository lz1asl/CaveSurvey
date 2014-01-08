/**
 * 
 */
package com.astoev.cave.survey.service.azimuth;

/**
 * Notifies a top level listener when an azimuth is changed
 * 
 * @author jmitrev
 *
 */
public interface AzimuthChangedListener {

	void onAzimuthChanged(float newValueArg);
	
	void onAccuracyChanged(int accuracyArg);
	
}
