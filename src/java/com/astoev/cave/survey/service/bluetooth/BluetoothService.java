package com.astoev.cave.survey.service.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 10/23/13
 * Time: 10:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class BluetoothService {

    public static boolean isBluetoothSupported() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        }
        return true;
    }

    public static boolean askBluetoothOn(Activity aParentActivity) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                aParentActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            aParentActivity.onactivi
             // TODO
            /**
             *  onActivityResult() implementation as the requestCode parameter.
             If enabling Bluetooth succeeds, your activity receives the RESULT_OK result code in the onActivityResult() callback. If Bluetooth was not enabled due to an error (or the user responded "No") then the result code is RESULT_CANCELED.
             Optionally, your application can also listen for the ACTION_STATE_CHANGED broadcast Intent, which the system will broadcast whenever the Bluetooth state has changed. This broadcast contains the extra fields EXTRA_STATE and EXTRA_PREVIOUS_STATE, containing the new and old Bluetooth states, respectively. Possible values for these extra fields are STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF, and STATE_OFF. Listening for this broadcast can be useful to detect changes made to the Bluetooth state while your app is running.
             */
            return false;
        } else {
            return true;
        }
    }

    public static void discoverDevices() {

//
//
    }
}
