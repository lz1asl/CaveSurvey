package com.astoev.cave.survey.service.bluetooth.device;

import android.bluetooth.BluetoothGattCharacteristic;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.util.List;

/**
 * Bluetooth device using Bluetooth 4 LTE communication.
 */
public abstract class AbstractBluetoothLEDevice extends AbstractBluetoothDevice {

    public abstract String getService(Constants.MeasureTypes aMeasureType);

    public abstract String getMeasurementCharacteristics(Constants.MeasureTypes aMeasureType);

    public abstract Measure characteristicToMeasure(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes);

}
