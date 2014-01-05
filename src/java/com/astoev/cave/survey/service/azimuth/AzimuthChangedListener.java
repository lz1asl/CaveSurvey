/**
 * 
 */
package com.astoev.cave.survey.service.azimuth;

/**
 * Notifies a top level listener to notify when an azimuth is changed
 * 
 * @author jmitrev
 *
 */
public interface AzimuthChangedListener {

	void onAzimuthChanged(float newValueArg);
}
