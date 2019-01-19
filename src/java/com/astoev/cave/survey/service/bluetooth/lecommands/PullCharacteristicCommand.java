package com.astoev.cave.survey.service.bluetooth.lecommands;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.service.bluetooth.device.ble.AbstractBluetoothLEDevice;

import static com.astoev.cave.survey.Constants.LOG_TAG_BT;

public class PullCharacteristicCommand extends AbstractBluetoothCommand {

    private BluetoothGattCharacteristic mCharacteristic;
    private AbstractBluetoothLEDevice mDevice;

    public PullCharacteristicCommand(BluetoothGattCharacteristic aCharacteristic, AbstractBluetoothLEDevice aDevice) {
        mCharacteristic = aCharacteristic;
        mDevice = aDevice;
    }


    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void execute(BluetoothGatt aBluetoothGatt) {
        if (mDevice.needCharacteristicPull()) {
            Log.d(LOG_TAG_BT, "Pull value from " + mCharacteristic.getUuid().toString());
            boolean flag = aBluetoothGatt.readCharacteristic(mCharacteristic);
            Log.i(LOG_TAG_BT, "Pull command success: " + flag);
        } else {
            Log.i(LOG_TAG_BT, "No pull for " + mDevice.getDescription());
        }
    }

    @Override
    public boolean canProceedWithoutAnswer() {
        return true;
    }
}
