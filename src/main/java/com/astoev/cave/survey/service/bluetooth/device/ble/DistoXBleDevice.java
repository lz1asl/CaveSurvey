package com.astoev.cave.survey.service.bluetooth.device.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.lecommands.AbstractBluetoothCommand;
import com.astoev.cave.survey.service.bluetooth.lecommands.WriteCharacteristicCommand;
import com.astoev.cave.survey.service.bluetooth.util.DistoXBLEProtocol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Siwei Tian's DistoX replacement over Bluetooth LE.
 */

public class DistoXBleDevice extends AbstractBluetoothLEDevice {

    private static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID WRITE_CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID READ_SERVICE_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private static final byte[] COMMAND_LASER_ON = new byte[] {0x64, 0x61, 0x74, 0x61, 0x3a, 0x01, 0x36, 0x0d, 0x0a};


    @Override
    public List<UUID> getServices() {
        return Arrays.asList(SERVICE_UUID);
    }

    @Override
    public List<UUID> getCharacteristics() {
        return Arrays.asList(WRITE_CHARACTERISTIC_UUID, READ_SERVICE_UUID);
    }

    @Override
    public List<UUID> getDescriptors() {
        return Collections.emptyList();
    }

    @Override
    public UUID getService(Constants.MeasureTypes aMeasureType) {
        return SERVICE_UUID;
    }

    @Override
    public UUID getCharacteristic(Constants.MeasureTypes aMeasureType) {
        return WRITE_CHARACTERISTIC_UUID;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public List<Measure> characteristicToMeasures(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) {
        byte[] rawMessage = aCharacteristic.getValue();
        Log.i(Constants.LOG_TAG_BT, "Got: " + new String(rawMessage));

        if (DistoXBLEProtocol.isDataPacket(rawMessage)) {
            Log.i(Constants.LOG_TAG_BT, DistoXBLEProtocol.describeDataPacket(rawMessage));
            return DistoXBLEProtocol.parseBleDataPacket(rawMessage);
        }
        return null;
    }

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "DistoXBLE-");
    }

    @Override
    public String getDescription() {
        return "DistoX BLE";
    }

    @Override
    protected List<Constants.MeasureTypes> getSupportedMeasureTypes() {
        // all measures available on each shot
        return Arrays.asList(Constants.MeasureTypes.values());
    }

    @Override
    public boolean needCharacteristicNotification() {
        return true;
    }


    /*@Override
    public AbstractBluetoothCommand getReadCharacteristicCommand(Constants.MeasureTypes aType) {
        // turn the laser on or take measurement if on
        return new WriteCharacteristicCommand(WRITE_CHARACTERISTIC_UUID, WRITE_CHARACTERISTIC_UUID, COMMAND_LASER_ON);
    }
*/
    @Override
    public AbstractBluetoothCommand getAcknowledgeCommand(BluetoothGattCharacteristic aCharacteristic) {
        byte[] ackPacket = DistoXBLEProtocol.createAcknowledgementPacket(aCharacteristic.getValue());
        return new WriteCharacteristicCommand(SERVICE_UUID, WRITE_CHARACTERISTIC_UUID, ackPacket);
    }
}
