/**
 * 
 */
package com.astoev.cave.survey.service.orientation;


/**
 * @author jmitrev
 *
 */
public class SlopeChangedAdapter implements OrientationChangedListener, SlopeChangedListener{

    @Override
    public void onOrinationChanged(float[] dataArg) {
        onSlopeChanged(-dataArg[1]);
    }

    @Override
    public void onAccuracyChanged(int accuracyArg) {
    }

    @Override
    public void onSlopeChanged(float newValueArg) {
    }

}
