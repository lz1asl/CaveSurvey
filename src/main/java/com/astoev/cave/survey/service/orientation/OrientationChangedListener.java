package com.astoev.cave.survey.service.orientation;

/**
 * Orientation Listener defines the methods need by the specific adapter that will use the whole or part
 * of the orientation
 * 
 * @author jmitrev
 */
public interface OrientationChangedListener {

    void onOrinationChanged(float[] data);
    
    void onAccuracyChanged(int accuracyArg);
}
