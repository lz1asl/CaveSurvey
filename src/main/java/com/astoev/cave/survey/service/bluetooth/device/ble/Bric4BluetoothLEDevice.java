package com.astoev.cave.survey.service.bluetooth.device.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Kris's caving device.
 */

public class Bric4BluetoothLEDevice extends AbstractBluetoothLEDevice {

    private static final UUID MEASUREMENT_SERVICE_UUID = UUID.fromString("000058d0-0000-1000-8000-00805f9b34fb");
    private static final UUID MEASUREMENT_PRIMARY_CHARACTERISTIC_UUID = UUID.fromString("000058d1-0000-1000-8000-00805f9b34fb");


    @Override
    public List<UUID> getServices() {
        return Arrays.asList(MEASUREMENT_SERVICE_UUID);
    }

    @Override
    public List<UUID> getCharacteristics() {
        return Arrays.asList(MEASUREMENT_PRIMARY_CHARACTERISTIC_UUID);
    }

    @Override
    public List<UUID> getDescriptors() {
        return Arrays.asList(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("00002901-0000-1000-8000-00805f9b34fb"));
    }

    @Override
    public UUID getService(Constants.MeasureTypes aMeasureType) {
        return MEASUREMENT_SERVICE_UUID;
    }

    @Override
    public UUID getCharacteristic(Constants.MeasureTypes aMeasureType) {
        return MEASUREMENT_PRIMARY_CHARACTERISTIC_UUID;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public List<Measure> characteristicToMeasures(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) throws DataException {

        byte[] rawMessage = aCharacteristic.getValue();
        Log.i(Constants.LOG_TAG_BT, "Got: " + new String(rawMessage));
        if (rawMessage.length != 20) {
            Log.e(Constants.LOG_TAG_BT, "Got incomplete message of lenfth " + rawMessage.length);
            return null;
        }

        List<Measure> measures = new ArrayList<>();
        Float azimuth = asFloat(rawMessage, 12, 15);
        if (azimuth != null) {
            Measure angleMeasure = new Measure(Constants.MeasureTypes.angle, Constants.MeasureUnits.degrees, azimuth);
            measures.add(angleMeasure);
            Log.i(Constants.LOG_TAG_BT, "Got angle " + angleMeasure);
        }

        Float slope = asFloat(rawMessage, 16, 19);
        if (slope != null) {
            Measure slopeMeasure = new Measure(Constants.MeasureTypes.slope, Constants.MeasureUnits.degrees, slope);
            measures.add(slopeMeasure);
            Log.i(Constants.LOG_TAG_BT, "Got slope " + slopeMeasure);
        }

        Float distance = asFloat(rawMessage, 8, 11);
        if (distance != null) {
            Measure distanceMeasure = new Measure(Constants.MeasureTypes.distance, Constants.MeasureUnits.meters, distance);
            measures.add(distanceMeasure);
            Log.i(Constants.LOG_TAG_BT, "Got distance " + distanceMeasure);
        }

        return measures;
    }

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "BRIC4_");
    }

    @Override
    public String getDescription() {
        return "BRIC4";
    }

    @Override
    protected List<Constants.MeasureTypes> getSupportedMeasureTypes() {
        // all measures available on each shot
        return Arrays.asList(Constants.MeasureTypes.values());
    }

    @Override
    public boolean needCharacteristicIndication() {
        return true;
    }

    private Float asFloat(byte[] message, int start, int end) {
        try {
            return ByteBuffer.wrap(message, start, end).order(LITTLE_ENDIAN).getFloat();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_BT, "Failed to parse message", e);
            return null;
        }
    }
}
