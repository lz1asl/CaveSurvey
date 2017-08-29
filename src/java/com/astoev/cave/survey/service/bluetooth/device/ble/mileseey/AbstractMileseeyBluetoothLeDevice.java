package com.astoev.cave.survey.service.bluetooth.device.ble.mileseey;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.ble.AbstractBluetoothLEDevice;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by astoev on 8/26/17.
 */

public abstract class AbstractMileseeyBluetoothLeDevice extends AbstractBluetoothLEDevice {

    protected static final UUID SERVICE_UUID = UUID.fromString("0000FFB0-0000-1000-8000-00805f9b34fb");
    protected static final UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //    private static final UUID CHAR_CMD_UUID = UUID.fromString("0000FFB1-0000-1000-8000-00805f9b34fb");
    public static final UUID CHAR_DATA_UUID = UUID.fromString("0000FFB2-0000-1000-8000-00805f9b34fb");

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        // distance and inclination supported
        return Constants.MeasureTypes.distance.equals(aMeasureType);
    }

    @Override
    public List<UUID> getServices() {
        return Arrays.asList(SERVICE_UUID);
    }

    @Override
    public List<UUID> getCharacteristics() {
        return Arrays.asList(CHAR_DATA_UUID);
    }

    @Override
    public List<UUID> getDescriptors() {
        return Arrays.asList(DESCRIPTOR_UUID);
    }

    @Override
    public UUID getService(Constants.MeasureTypes aMeasureType) {
        switch (aMeasureType) {
            case distance:
                return SERVICE_UUID;

            default:
                return null;
        }
    }

    @Override
    public UUID getCharacteristic(Constants.MeasureTypes aMeasureType) {
        switch (aMeasureType) {
            case distance:
                return CHAR_DATA_UUID;

            default:
                return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public Measure characteristicToMeasure(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) throws DataException {


        if (CHAR_DATA_UUID.equals(aCharacteristic.getUuid())) {


            byte [] data = aCharacteristic.getValue();
            String dataString = new String(data).trim();
            Log.i(Constants.LOG_TAG_BT, "Got distance " + dataString);

            if (!dataString.endsWith("m")) {
                Log.i(Constants.LOG_TAG_BT, "Please measure in meters!");
                throw new DataException("Please use meters");
            }

            String valueString = dataString.substring(0, dataString.length() - 1);
            Float distance = Float.valueOf(valueString);

            Measure measure = new Measure();
            measure.setMeasureUnit(Constants.MeasureUnits.meters);
            measure.setMeasureType(Constants.MeasureTypes.distance);
            measure.setValue(distance);
            return measure;
        } else {
            Log.d(Constants.LOG_TAG_BT, "Unnone characteristic received " + aCharacteristic.getUuid());
        }

        return null;
    }

    @Override
    public boolean needCharacteristicNotification() {
        return true;
    }
}
