package com.astoev.cave.survey.service.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.util.Log;

import com.astoev.cave.survey.Constants;

/**
 * Used to get data back from LE devices.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLECallback implements BluetoothAdapter.LeScanCallback {

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(Constants.LOG_TAG_BT, "got " + rssi + " : " + device.getName());
    }
}
