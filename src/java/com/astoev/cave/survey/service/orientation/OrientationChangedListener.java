/**
 * 
 */
package com.astoev.cave.survey.service.orientation;

/**
 * 
 * @author jmitrev
 */
public interface OrientationChangedListener {

    void onOrinationChanged(float[] data);
    
    void onAccuracyChanged(int accuracyArg);
}
