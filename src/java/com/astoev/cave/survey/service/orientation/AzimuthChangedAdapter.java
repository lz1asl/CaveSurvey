/**
 * 
 */
package com.astoev.cave.survey.service.orientation;

/**
 * Adapter for listening for azimuth changes.
 * 
 * @author jmitrev
 */
public class AzimuthChangedAdapter implements AzimuthChangedListener, OrientationChangedListener {

    @Override
    public void onOrinationChanged(float[] dataArg) {
        onAzimuthChanged(dataArg[0]);
    }

    @Override
    public void onAzimuthChanged(float newValueArg) {
    }

    @Override
    public void onAccuracyChanged(int accuracyArg) {
    }

}
