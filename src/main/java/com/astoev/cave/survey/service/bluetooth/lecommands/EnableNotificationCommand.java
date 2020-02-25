package com.astoev.cave.survey.service.bluetooth.lecommands;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;

/**
 * Created by astoev on 1/19/16.
 */
public class EnableNotificationCommand extends AbstractBluetoothCommand {

    private BluetoothGattCharacteristic mCharacteristic;

    public EnableNotificationCommand(BluetoothGattCharacteristic characteristic) {
        mCharacteristic = characteristic;
    }

    @Override
    public boolean canProceedWithoutAnswer() {
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void execute(BluetoothGatt aBluetoothGatt) {
        Log.d(Constants.LOG_TAG_BT, "Enable notification for: " + mCharacteristic.getUuid().toString());
        boolean flag = aBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);
        Log.i(Constants.LOG_TAG_BT, "Notification success: " + flag);
    }
}
