package com.astoev.cave.survey.service.bluetooth.device.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.astoev.cave.survey.Constants.LOG_TAG_BT;
import static com.astoev.cave.survey.Constants.MeasureTypes.distance;


/**
 * Stanley TLM 99s Bluetooth LE device.
 */
public class StanleyBluetoothLeDevice extends AbstractBluetoothLEDevice {

    public static final String MESSAGE_PREFIX = "10010002";
    private static final UUID SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_DISTANCE_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");

    @Override
    public List<UUID> getServices() {
        return Arrays.asList(SERVICE_UUID);
    }

    @Override
    public List<UUID> getCharacteristics() {
        return Arrays.asList(CHARACTERISTIC_DISTANCE_UUID);
    }

    @Override
    public List<UUID> getDescriptors() {
        return Collections.emptyList();
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
                return CHARACTERISTIC_DISTANCE_UUID;
            default:
                return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public List<Measure> characteristicToMeasures(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) throws DataException {

        if (CHARACTERISTIC_DISTANCE_UUID.equals(aCharacteristic.getUuid())) {


            byte [] data = aCharacteristic.getValue();
            String dataString = new String(data);
            Log.d(LOG_TAG_BT, "Got message: " + dataString);

            if (StringUtils.isEmpty(dataString)) {
                Log.e(LOG_TAG_BT, "Empty distance");
                return null;
            }

            if (dataString.startsWith("[")) {
                dataString = dataString.substring(1);
            }
            if (dataString.endsWith("]")) {
                dataString = dataString.substring(0, dataString.length() - 1);
            }

            if (!dataString.startsWith(MESSAGE_PREFIX)) {
                Log.e(LOG_TAG_BT, "Invalid header");
                return null;
            }

            if (dataString.length() != MESSAGE_PREFIX.length() + 6) {
                Log.e(LOG_TAG_BT, "Bad length");
                return null;
            }

            // prefix, little endian in hex, and probably checksum
            String value = dataString.substring(MESSAGE_PREFIX.length(), MESSAGE_PREFIX.length() + 4);
            String orderFixed = value.substring(2, 4) + value.substring(0, 2);
            float distance = ((float) Integer.parseInt(orderFixed, 16)) / 1000;
            Log.i(LOG_TAG_BT, "Distance: " + distance);

            Measure measure = new Measure();
            measure.setMeasureUnit(Constants.MeasureUnits.meters);
            measure.setMeasureType(Constants.MeasureTypes.distance);
            measure.setValue(distance);
            return Arrays.asList(measure);
        } else {
            Log.d(LOG_TAG_BT, "Unnone characteristic received " + aCharacteristic.getUuid());
            return null;
        }
    }

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName,"tlm99s") || "SUB0001".equals(aName) ;
    }

    @Override
    public String getDescription() {
        return "Stanley TLM 99s";
    }

    @Override
    protected List<Constants.MeasureTypes> getSupportedMeasureTypes() {
        // distance only
        return Arrays.asList(distance);
    }

    @Override
    public boolean needCharacteristicPull() {
        return true;
    }
}