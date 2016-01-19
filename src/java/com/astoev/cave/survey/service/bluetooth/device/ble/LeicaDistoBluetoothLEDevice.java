package com.astoev.cave.survey.service.bluetooth.device.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothLEDevice;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Tested with Leica Disto D810 but will probably work with the rest of the LE distos.
 *
 * Created by astoev on 1/4/16.
 */
public class LeicaDistoBluetoothLEDevice extends AbstractBluetoothLEDevice {

    private static final UUID SERVICE_UUID = UUID.fromString("3ab10100-f831-4395-b29d-570977d5bf94");

    public static final UUID CHARACTERISTIC_DISTANCE_UUID = UUID.fromString("3ab10101-f831-4395-b29d-570977d5bf94");
    public static final UUID CHARACTERISTIC_DISTANCE_UNIT_UUID = UUID.fromString("3ab10102-f831-4395-b29d-570977d5bf94");
    public static final UUID CHARACTERISTIC_ANGLE_UUID = UUID.fromString("3ab10103-f831-4395-b29d-570977d5bf94");
    public static final UUID CHARACTERISTIC_ANGLE_UNIT_UUID = UUID.fromString("3ab10104-f831-4395-b29d-570977d5bf94");

    @Override
    public List<UUID> getServices() {
        return Arrays.asList(SERVICE_UUID);
    }

    @Override
    public List<UUID> getCharacteristics() {
        return Arrays.asList(CHARACTERISTIC_ANGLE_UUID, CHARACTERISTIC_ANGLE_UNIT_UUID, CHARACTERISTIC_DISTANCE_UUID, CHARACTERISTIC_DISTANCE_UNIT_UUID);
    }

    @Override
    public List<UUID> getDescriptors() {
        return Arrays.asList(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("00002908-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("00002908-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("00002908-0000-1000-8000-00805f9b34fb")
        );
    }

    @Override
    public UUID getCharacteristic(Constants.MeasureTypes aMeasureType) {
        switch (aMeasureType) {
            case distance:
                return CHARACTERISTIC_DISTANCE_UUID;

            case slope:
                return CHARACTERISTIC_ANGLE_UNIT_UUID;

            default:
                return null;
        }
    }

    @Override
    public UUID getService(Constants.MeasureTypes aMeasureType) {
        switch (aMeasureType) {
            case distance:
            case slope:
                return SERVICE_UUID;

            default:
                return null;
        }
    }

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "DISTO ");
    }

    @Override
    public String getDescription() {
        return "Leica DISTO LE";
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        // distance and inclination supported
        return Constants.MeasureTypes.distance.equals(aMeasureType) || Constants.MeasureTypes.slope.equals(aMeasureType);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public Measure characteristicToMeasure(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) throws DataException {

        if (CHARACTERISTIC_DISTANCE_UUID.equals(aCharacteristic.getUuid())) {
            Float distance = asFloat(aCharacteristic, ByteOrder.LITTLE_ENDIAN);
            Log.i(Constants.LOG_TAG_BT, "DISTANCE: " + distance);

            Measure measure = new Measure();
            measure.setMeasureUnit(Constants.MeasureUnits.meters);
            measure.setMeasureType(Constants.MeasureTypes.distance);
            measure.setValue(distance);
            return measure;
        } else if (CHARACTERISTIC_ANGLE_UUID.equals(aCharacteristic.getUuid())) {

            Float slopeInRadians = asFloat(aCharacteristic, ByteOrder.LITTLE_ENDIAN);
            if (slopeInRadians != null) {
                Float slope = (float) Math.toDegrees(slopeInRadians);
                Log.i(Constants.LOG_TAG_BT, "SLOPE: " + slope);

                Measure measure = new Measure();
                measure.setMeasureUnit(Constants.MeasureUnits.degrees);
                measure.setMeasureType(Constants.MeasureTypes.slope);
                measure.setValue(slope);
                return measure;
            }
        } else if (CHARACTERISTIC_DISTANCE_UNIT_UUID.equals(aCharacteristic.getUuid())) {
            // only check the unit
            Integer unit = asInt(aCharacteristic);
            Log.i(Constants.LOG_TAG_BT, "DISTANCE UNIT: " + unit);

            // 4 different decimal meter formats
            List<Integer> meterUnits = Arrays.asList(0, 1, 2, 3);

            if (!meterUnits.contains(unit)) {
                throw new DataException("Please use meters");
            }

        } else if (CHARACTERISTIC_ANGLE_UNIT_UUID.equals(aCharacteristic.getUuid())) {
            // only check the unit

            Integer unit = asInt(aCharacteristic);
            Log.i(Constants.LOG_TAG_BT, "SLOPE UNIT: " + unit);

            if (0 != unit) {
                throw new DataException("Please use degrees");
            }
        }

        return null;
    }

}
