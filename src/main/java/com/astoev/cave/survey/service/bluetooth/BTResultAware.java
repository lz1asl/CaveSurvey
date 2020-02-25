package com.astoev.cave.survey.service.bluetooth;

import com.astoev.cave.survey.Constants;

/**
 * To be implemented by UI elements receiving BT events.
 */
public interface BTResultAware {

    public void onReceiveMeasures(Constants.Measures aMeasureTarget, float aMeasureValue);
}
