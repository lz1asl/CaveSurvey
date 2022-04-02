package com.astoev.cave.survey.service.bluetooth.device.ble.bric4;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.Constants.MeasureTypes;
import com.astoev.cave.survey.Constants.MeasureUnits;
import com.astoev.cave.survey.Constants.MetaData;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.ble.AbstractBluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.lecommands.AbstractBluetoothCommand;
import com.astoev.cave.survey.service.bluetooth.lecommands.WriteCharacteristicCommand;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kris's caving device.
 */

public class Bric4BluetoothLEDevice extends AbstractBluetoothLEDevice {

    private static final UUID MEASUREMENT_SERVICE_UUID = UUID.fromString("000058d0-0000-1000-8000-00805f9b34fb");
    public static final UUID MEASUREMENT_PRIMARY_CHARACTERISTIC_UUID = UUID.fromString("000058d1-0000-1000-8000-00805f9b34fb");
    public static final UUID MEASUREMENT_METADATA_CHARACTERISTIC_UUID = UUID.fromString("000058d2-0000-1000-8000-00805f9b34fb");
    public static final UUID MEASUREMENT_ERRORS_CHARACTERISTIC_UUID = UUID.fromString("000058d3-0000-1000-8000-00805f9b34fb");
    private static final UUID DEVICE_CONTROL_SERVICE_UUID = UUID.fromString("000058e0-0000-1000-8000-00805f9b34fb");
    private static final UUID DEVICE_COMMAND_CHARACTERISTIC_UUID = UUID.fromString("000058e1-0000-1000-8000-00805f9b34fb");
    private static final byte[] COMMAND_SHOT = "shot".getBytes();
    private static final byte[] COMMAND_SCAN = "scan".getBytes();


    @Override
    public List<UUID> getServices() {
        return Arrays.asList(MEASUREMENT_SERVICE_UUID);
    }

    @Override
    public List<UUID> getCharacteristics() {
        return Arrays.asList(MEASUREMENT_PRIMARY_CHARACTERISTIC_UUID,
                MEASUREMENT_METADATA_CHARACTERISTIC_UUID,
                MEASUREMENT_ERRORS_CHARACTERISTIC_UUID);
    }

    @Override
    public List<UUID> getDescriptors() {
        return Arrays.asList(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("00002901-0000-1000-8000-00805f9b34fb"));
    }

    @Override
    public UUID getService(MeasureTypes aMeasureType) {
        return MEASUREMENT_SERVICE_UUID;
    }

    @Override
    public UUID getCharacteristic(MeasureTypes aMeasureType) {
        return MEASUREMENT_PRIMARY_CHARACTERISTIC_UUID;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public List<Measure> characteristicToMeasures(BluetoothGattCharacteristic aCharacteristic, List<MeasureTypes> aMeasureTypes) throws DataException {

        byte[] rawMessage = aCharacteristic.getValue();
        Log.i(Constants.LOG_TAG_BT, "Got: " + new String(rawMessage));
        if (rawMessage.length != 20) {
            Log.e(Constants.LOG_TAG_BT, "Got incomplete message of lenfth " + rawMessage.length);
            return null;
        }

        List<Measure> measures = new ArrayList<>();
        Float distance = asFloat(rawMessage, 8);
        if (distance != null) {
            Measure distanceMeasure = new Measure(MeasureTypes.distance, MeasureUnits.meters, distance);
            measures.add(distanceMeasure);
            Log.d(Constants.LOG_TAG_BT, "Got distance " + distanceMeasure);
        }
        Float azimuth = asFloat(rawMessage, 12);
        if (azimuth != null) {
            Measure angleMeasure = new Measure(MeasureTypes.angle, MeasureUnits.degrees, azimuth);
            measures.add(angleMeasure);
            Log.d(Constants.LOG_TAG_BT, "Got angle " + angleMeasure);
        }
        Float slope = asFloat(rawMessage, 16);
        if (slope != null) {
            Measure slopeMeasure = new Measure(MeasureTypes.slope, MeasureUnits.degrees, slope);
            measures.add(slopeMeasure);
            Log.d(Constants.LOG_TAG_BT, "Got slope " + slopeMeasure);
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
    protected List<MeasureTypes> getSupportedMeasureTypes() {
        // all measures available on each shot
        return Arrays.asList(MeasureTypes.values());
    }

    @Override
    public boolean needCharacteristicIndication() {
        return true;
    }

    @Override
    public boolean needCharacteristicPull() {
        // combines with the command below
        return true;
    }

    @Override
    public AbstractBluetoothCommand getReadCharacteristicCommand(MeasureTypes aType) {
        // turn the laser on or take measurement if on
        return new WriteCharacteristicCommand(DEVICE_CONTROL_SERVICE_UUID, DEVICE_COMMAND_CHARACTERISTIC_UUID, COMMAND_SHOT);
    }

    @Override
    public AbstractBluetoothCommand getStartScanCommand() {
        // turn the laser on or take measurement if on
        return new WriteCharacteristicCommand(DEVICE_CONTROL_SERVICE_UUID, DEVICE_COMMAND_CHARACTERISTIC_UUID, COMMAND_SCAN);
    }

    @Override
    public AbstractBluetoothCommand getStopScanCommand() {
        // any other command will interrupt the scan
       return getReadCharacteristicCommand(null);
    }

    private Float asFloat(byte[] message, int start) {
        try {
            return ByteBuffer.wrap(message, start, 4).order(LITTLE_ENDIAN).getFloat();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_BT, "Failed to parse message", e);
            return null;
        }
    }

    private Integer asFourByteInt(byte[] message, int start) {
        try {
            return ByteBuffer.wrap(message, start, 4).order(LITTLE_ENDIAN).getInt();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_BT, "Failed to parse message", e);
            return null;
        }
    }

    private Byte asByte(byte[] message, int start) {
        try {
            return ByteBuffer.wrap(message, start, 1).order(LITTLE_ENDIAN).get();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_BT, "Failed to parse message", e);
            return null;
        }
    }

    @Override
    public boolean isMetadataCharacteristic(BluetoothGattCharacteristic aCharacteristic) {
        return MEASUREMENT_METADATA_CHARACTERISTIC_UUID.equals(aCharacteristic.getUuid())
                || MEASUREMENT_ERRORS_CHARACTERISTIC_UUID.equals(aCharacteristic.getUuid());
    }

    @Override
    public Map<String, Object> characteristicToMetadata(BluetoothGattCharacteristic aCharacteristic) throws DataException {

        byte[] rawMessage = aCharacteristic.getValue();

        if (MEASUREMENT_METADATA_CHARACTERISTIC_UUID.equals(aCharacteristic.getUuid())) {
            Log.i(Constants.LOG_TAG_BT, "Got meta: " + new String(rawMessage));

            Integer refIndex = asFourByteInt(rawMessage, 0);
            Log.i(Constants.LOG_TAG_BT, "Point " + refIndex);

            Float temperature = asFloat(rawMessage, 12);
            Log.i(Constants.LOG_TAG_BT, "Temperature " + temperature);

            return Map.of(MetaData.reference.name(), refIndex,MetaData.temperature.name(), temperature);
        } else if (MEASUREMENT_ERRORS_CHARACTERISTIC_UUID.equals(aCharacteristic.getUuid())) {
            Log.i(Constants.LOG_TAG_BT, "Got error: " + new String(rawMessage));

            Byte code = asByte(rawMessage, 0);
            Log.i(Constants.LOG_TAG_BT, "Code " + code);
            Float data1 = asFloat(rawMessage, 1);
            Float data2 = asFloat(rawMessage, 5);

            Bric4ErrorCode error1 = new Bric4ErrorCode(code, data1, data2);
            Log.i(Constants.LOG_TAG_BT, error1.getDescription());

            Byte code2 = asByte(rawMessage, 9);
            if (code2 != null && code2 != 0) {
                Log.i(Constants.LOG_TAG_BT, "Code2 " + code2);
                data1 = asFloat(rawMessage, 10);
                data2 = asFloat(rawMessage, 4);

                Bric4ErrorCode error2 = new Bric4ErrorCode(code2, data1, data2);
                Log.i(Constants.LOG_TAG_BT, error2.getDescription());
                // TODO not returned
            }

            return Map.of(MetaData.errorCode.name(), code, MetaData.errorDesc.name(), error1.getDescription());
        } else {
            Log.i(Constants.LOG_TAG_BT, "Unable to handle " + aCharacteristic.getUuid());
        }

        return null;
    }
}
