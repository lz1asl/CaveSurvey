package com.astoev.cave.survey.service.bluetooth.lecommands;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import java.util.UUID;

import static com.astoev.cave.survey.Constants.LOG_TAG_BT;

public class WriteCharacteristicCommand extends AbstractBluetoothCommand {

    private UUID service;
    private UUID characteristic;
    private byte[] value;


    public WriteCharacteristicCommand(UUID aService, UUID aCharacteristic, byte[] aValue) {
        service = aService;
        characteristic = aCharacteristic;
        value = aValue;
    }


    @Override
    public boolean canProceedWithoutAnswer() {
        return true;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void execute(BluetoothGatt aBluetoothGatt) {
        Log.d(LOG_TAG_BT, "Send to " + service + ":" + characteristic + " : "  + new String(value));
        BluetoothGattCharacteristic targetChar = aBluetoothGatt.getService(service).getCharacteristic(characteristic);
        if (targetChar != null) {
            boolean success = targetChar.setValue(value) && aBluetoothGatt.writeCharacteristic(targetChar);
            if (!success) {
                Log.e(LOG_TAG_BT, "Failed to set characteristic value");
            }
        } else {
            Log.e(LOG_TAG_BT, "Not found");
        }
    }
}
