/**
 * 
 */
package com.astoev.cave.survey.service.orientation;

/**
 * @author jivko
 *
 */
public class AzimuthChangedAdapter implements AzimuthChangedListener,
        OrientationChangedListener {

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
