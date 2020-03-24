package com.astoev.cave.survey.service.bluetooth.lecommands;

import android.bluetooth.BluetoothGatt;

/**
 * Created by astoev on 1/19/16.
 */
public abstract class AbstractBluetoothCommand {

    public boolean canProceedWithoutAnswer() {
        return false;
    }

    public abstract void execute(BluetoothGatt aBluetoothGatt);
}
