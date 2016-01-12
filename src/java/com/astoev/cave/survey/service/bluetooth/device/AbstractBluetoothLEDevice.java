package com.astoev.cave.survey.service.bluetooth.device;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;

/**
 * Bluetooth device using Bluetooth 4 LTE communication.
 */
public abstract class AbstractBluetoothLEDevice extends AbstractBluetoothDevice {


    // abstract methods to define the LE device

    public abstract List<UUID> getServices() ;
    public abstract List<UUID> getCharacteristics();
    public abstract List<UUID> getDescriptors();
    public abstract UUID getService(Constants.MeasureTypes aMeasureType);
    public abstract UUID getCharacteristic(Constants.MeasureTypes aMeasureType);
    public abstract Measure characteristicToMeasure(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) throws DataException;


    // helper methods to reuse between the LE devices

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected Float asFloat(BluetoothGattCharacteristic aCharacteristic, ByteOrder anOrder) {
        return asFloat(aCharacteristic.getValue(), anOrder);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected Float asFloat(BluetoothGattDescriptor aDescriptor, ByteOrder anOrder) {
        return asFloat(aDescriptor.getValue(), anOrder);
    }

    private Float asFloat(byte[] buff, ByteOrder anOrder) {
        ByteBuffer buffer = ByteBuffer.wrap(buff);
        if (anOrder != null) {
            buffer.order(anOrder);
        }
        return buffer.getFloat();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected Integer asInt(BluetoothGattCharacteristic aCharacteristic) {
        return aCharacteristic.getIntValue(18, 0).intValue();
    }

}
