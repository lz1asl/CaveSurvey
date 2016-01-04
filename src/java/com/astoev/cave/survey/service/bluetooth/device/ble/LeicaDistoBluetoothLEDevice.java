package com.astoev.cave.survey.service.bluetooth.device.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothLEDevice;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Created by astoev on 1/4/16.
 */
public class LeicaDistoBluetoothLEDevice extends AbstractBluetoothLEDevice {

//    01-04 22:22:28.362: I/CaveSurveyBT(27162): Service 00001800-0000-1000-8000-00805f9b34fb 0 android.bluetooth.BluetoothGattService@3c451793
//    01-04 22:22:28.362: I/CaveSurveyBT(27162): Service 00001801-0000-1000-8000-00805f9b34fb 0 android.bluetooth.BluetoothGattService@54c03d0
//    01-04 22:22:28.362: I/CaveSurveyBT(27162): Service 3ab10100-f831-4395-b29d-570977d5bf94 0 android.bluetooth.BluetoothGattService@2d672fc9
//    01-04 22:22:28.362: I/CaveSurveyBT(27162): Service 00001812-0000-1000-8000-00805f9b34fb 0 android.bluetooth.BluetoothGattService@3f4c6ce
//    01-04 22:22:28.363: I/CaveSurveyBT(27162): Service 0000180f-0000-1000-8000-00805f9b34fb 0 android.bluetooth.BluetoothGattService@151f99ef
//            01-04 22:22:28.364: I/CaveSurveyBT(27162): Service 00001813-0000-1000-8000-00805f9b34fb 0 android.bluetooth.BluetoothGattService@1d358cfc
//    01-04 22:22:28.364: I/CaveSurveyBT(27162): Service 0000180a-0000-1000-8000-00805f9b34fb 0 android.bluetooth.BluetoothGattService@7a9ff85


//    01-04 22:33:06.377: I/CaveSurveyBT(29049): ----------------------------onCharacteristicChanged 3ab10101-f831-4395-b29d-570977d5bf94
//    01-04 22:33:06.377: I/CaveSurveyBT(29049): -----DISTANCE: 2.2728
//            01-04 22:33:06.377: I/CaveSurveyBT(29049): -----DISTANCE UNIT:30094


//    01-04 23:14:55.122: I/CaveSurveyBT(32543): Service 00001812-0000-1000-8000-00805f9b34fb 0 android.bluetooth.BluetoothGattService@239685c5
//    01-04 23:14:55.122: I/CaveSurveyBT(32543): Characteristic 00002a4d-0000-1000-8000-00805f9b34fb null
//            01-04 23:14:55.122: I/CaveSurveyBT(32543): Descriptor 00002902-0000-1000-8000-00805f9b34fb
//    01-04 23:14:55.122: I/CaveSurveyBT(32543): Descriptor 00002908-0000-1000-8000-00805f9b34fb
//
//
//    01-04 23:14:55.213: I/CaveSurveyBT(32543): onDescriptorWrite
//    01-04 23:14:55.213: I/CaveSurveyBT(32543): Success 00002902-0000-1000-8000-00805f9b34fb


    @Override
    public String getService(Constants.MeasureTypes aMeasureType) {
        switch (aMeasureType) {
            case distance:
                return "3ab10100-f831-4395-b29d-570977d5bf94";
        }

        return null;
    }

    @Override
    public String getMeasurementCharacteristics(Constants.MeasureTypes aMeasureType) {
        switch (aMeasureType) {
            case distance:
                return "3ab10101-f831-4395-b29d-570977d5bf94";
        }

        return null;
    }

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "DISTO");
    }

    @Override
    public String getDescription() {
        return "Leica DISTO";
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public Measure characteristicToMeasure(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) {

        // TODO service hardcoded

        float f = ByteBuffer.wrap(aCharacteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        Log.i(Constants.LOG_TAG_BT, "-----DISTANCE: " + f);

        Log.i(Constants.LOG_TAG_BT, "-----DISTANCE UNIT:" + aCharacteristic.getIntValue(18, 0).intValue());

        Measure measure = new Measure();
        measure.setMeasureUnit(Constants.MeasureUnits.meters);
        measure.setMeasureType(Constants.MeasureTypes.distance);
        measure.setValue(f);

        return measure;
    }
}
