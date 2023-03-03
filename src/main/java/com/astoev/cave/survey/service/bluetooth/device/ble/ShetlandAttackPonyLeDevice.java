package com.astoev.cave.survey.service.bluetooth.device.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.util.DistoXProtocol;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * https://www.shetlandattackpony.co.uk/
 */

public class ShetlandAttackPonyLeDevice extends AbstractBluetoothLEDevice {

    private static final UUID MEASUREMENT_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID READ_CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID WRITE_SERVICE_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");


    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "SAP5") || deviceNameStartsWith(aName, "Shetland_");
    }

    @Override
    public String getDescription() {
        return "Shetland Attack Pony";
    }

    @Override
    protected List<Constants.MeasureTypes> getSupportedMeasureTypes() {
        // all measures available on each shot
        return Arrays.asList(Constants.MeasureTypes.values());
    }

    @Override
    public List<UUID> getServices() {
        return Arrays.asList(MEASUREMENT_SERVICE_UUID);
    }

    @Override
    public List<UUID> getCharacteristics() {
        return Arrays.asList(READ_CHARACTERISTIC_UUID);
    }

    @Override
    public List<UUID> getDescriptors() {
        return null;
    }

    @Override
    public UUID getService(Constants.MeasureTypes aMeasureType) {
        return MEASUREMENT_SERVICE_UUID;
    }

    @Override
    public UUID getCharacteristic(Constants.MeasureTypes aMeasureType) {
        return READ_CHARACTERISTIC_UUID;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public List<Measure> characteristicToMeasures(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) throws DataException {
        byte[] rawMessage = aCharacteristic.getValue();
        Log.i(Constants.LOG_TAG_BT, "Got: " + new String(rawMessage));

        Log.i(Constants.LOG_TAG_BT, "Data packet : " + DistoXProtocol.isDataPacket(rawMessage));
        if (DistoXProtocol.isDataPacket(rawMessage)) {
            Log.i(Constants.LOG_TAG_BT, DistoXProtocol.describeDataPacket(rawMessage));
            return DistoXProtocol.parseDataPacket(rawMessage);
        }
        return null;
    }
    @Override
    public boolean needCharacteristicIndication() {
        return true;
    }

}
