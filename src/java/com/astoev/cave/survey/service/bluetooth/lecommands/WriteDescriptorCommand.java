package com.astoev.cave.survey.service.bluetooth.lecommands;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;

/**
 * Created by astoev on 1/19/16.
 */
public class WriteDescriptorCommand extends AbstractBluetoothCommand {

    private BluetoothGattDescriptor mDescriptor;

    public WriteDescriptorCommand(BluetoothGattDescriptor descriptor) {
        mDescriptor = descriptor;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void execute(BluetoothGatt aBluetoothGatt) {
        Log.d(Constants.LOG_TAG_BT, "Enable indication for: " + mDescriptor.getUuid().toString());
        mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        boolean flag = aBluetoothGatt.writeDescriptor(mDescriptor);
        Log.i(Constants.LOG_TAG_BT, "Indication success " + flag);
    }
}
