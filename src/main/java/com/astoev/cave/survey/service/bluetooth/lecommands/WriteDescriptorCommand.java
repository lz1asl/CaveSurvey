package com.astoev.cave.survey.service.bluetooth.lecommands;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.bluetooth.device.ble.AbstractBluetoothLEDevice;

import static com.astoev.cave.survey.Constants.LOG_TAG_BT;

/**
 * Created by astoev on 1/19/16.
 */
public class WriteDescriptorCommand extends AbstractBluetoothCommand {

    private BluetoothGattDescriptor mDescriptor;
    private AbstractBluetoothLEDevice mDevice;

    public WriteDescriptorCommand(BluetoothGattDescriptor aDescriptor, AbstractBluetoothLEDevice aDevice) {
        mDescriptor = aDescriptor;
        mDevice = aDevice;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void execute(BluetoothGatt aBluetoothGatt) {

        // gatt indications where needed (e.g. Leica devices)
        if (mDevice.needCharacteristicIndication()) {
            Log.d(LOG_TAG_BT, "Enable indication for: " + mDescriptor.getUuid().toString());
            mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            boolean flag = aBluetoothGatt.writeDescriptor(mDescriptor);
            Log.i(LOG_TAG_BT, "Indication success " + flag);

            if (!flag) {
                UIUtilities.showNotification("Device communication error enabling indication");
            }
        } else {
            Log.i(LOG_TAG_BT, "No indication for " + mDevice.getDescription());
        }

        // gatt notifications where needed (e.g. Mileseey devices)
        if (mDevice.needCharacteristicNotification()) {
            Log.d(LOG_TAG_BT, "Enable notification for: " + mDescriptor.getUuid().toString());
            mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean flag = aBluetoothGatt.writeDescriptor(mDescriptor);
            Log.i(LOG_TAG_BT, "Notification success " + flag);

            if (!flag) {
                UIUtilities.showNotification("Device communication error enabling notification");
            }
        } else {
            Log.i(LOG_TAG_BT, "No notification for " + mDevice.getDescription());
        }
    }
}
