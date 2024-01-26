package com.astoev.cave.survey.service.bluetooth.device.ble;

import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;
import android.os.ParcelUuid;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.lecommands.AbstractBluetoothCommand;

import org.apache.commons.collections4.CollectionUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bluetooth device using Bluetooth 4 LTE communication.
 */
public abstract class AbstractBluetoothLEDevice extends AbstractBluetoothDevice {


    // abstract methods to define the LE device
    public abstract List<UUID> getServices();
    public abstract List<UUID> getCharacteristics();
    public abstract List<UUID> getDescriptors();
    public abstract UUID getService(Constants.MeasureTypes aMeasureType);
    public abstract UUID getCharacteristic(Constants.MeasureTypes aMeasureType);
    public abstract List<Measure> characteristicToMeasures(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) throws DataException;

    // not sure why, some devices use notifications (e.g. Mileseey) and others indication (e.g. Leica)
    public boolean needCharacteristicNotification() {
        return false;
    }
    public boolean needCharacteristicIndication() {
        return false;
    }
    public boolean needCharacteristicPull() { return false; }

    public boolean useServiceMatch() {
        return false;
    }

    // devices that provide metadata
    public boolean isMetadataCharacteristic(BluetoothGattCharacteristic aCharacteristic) { return false;}
    public Map<String, Object> characteristicToMetadata(BluetoothGattCharacteristic aCharacteristic) throws DataException {return null;}


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
    protected Integer asInt(BluetoothGattCharacteristic aCharacteristic, int dataFormat) {
        return aCharacteristic.getIntValue(dataFormat, 0).intValue();
    }

    @Override
    public int getDeviceType() {
        return DEVICE_TYPE_LE;
    }

    public AbstractBluetoothCommand getReadCharacteristicCommand(Constants.MeasureTypes aType) {
        return null;
    }

    public AbstractBluetoothCommand getStartScanCommand() {
        return null;
    }

    public AbstractBluetoothCommand getStopScanCommand() { return null; };

    public void configure() {
        // called once after successful connection
        // does nothing by default
    }

    public boolean isServiceSupported(List<ParcelUuid> aLeServics) {
        if (useServiceMatch()) {
            if (CollectionUtils.isNotEmpty(aLeServics)) {
                for (ParcelUuid uuid : aLeServics) {
                    for (UUID service : getServices()) {
                        if (service.equals(uuid.getUuid())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public AbstractBluetoothCommand getAcknowledgeCommand(BluetoothGattCharacteristic aCharacteristic) {
        return null;
    }
}
