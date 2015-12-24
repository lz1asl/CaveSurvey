package com.astoev.cave.survey.service.bluetooth.device;

import com.astoev.cave.survey.Constants;

/**
 * Bluetooth device using Bluetooth 4 LTE communication.
 */
public abstract class AbstractBluetoothLEDevice extends AbstractBluetoothDevice {

    public abstract String getService();

    public abstract String getMeasurementCharacteristics(Constants.MeasureTypes aMeasureType);
}
