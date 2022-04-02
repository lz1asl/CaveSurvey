package com.astoev.cave.survey.service.bluetooth;

import com.astoev.cave.survey.Constants.Measures;

import java.util.Map;

/**
 * To be implemented by UI elements receiving BT events.
 */
public interface BTResultAware {

    void onReceiveMeasures(Measures aMeasureTarget, float aMeasureValue);
    void onReceiveMetadata(Map<String, Object> aMetaData);
}
