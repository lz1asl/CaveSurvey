package com.astoev.cave.survey.service.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Button;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 10/23/13
 * Time: 10:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class BluetoothService {

    private static ConnectThread mBusyThread = null;
    private static BluetoothDevice mCurrDevice = null;

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
            return false;
        } else {
            return true;
        }
    }

    public static void prepare(final Activity aContext) {

        aContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    UIUtilities.showNotification(aContext, R.string.bt_paired);
                    Button toggle = (Button) aContext.findViewById(R.id.bt_toggle_pair);
                    toggle.setText(R.string.bt_disconnect);
                    Log.i(Constants.LOG_TAG_UI, "Paired with " + mCurrDevice);
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed during pair", e);
                    UIUtilities.showNotification(aContext, R.string.error);
                }
            }
        },
                new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        aContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    UIUtilities.showNotification(aContext, R.string.bt_disconnect);
                    Log.i(Constants.LOG_TAG_UI, "Disconnected");
                    Button toggle = (Button) aContext.findViewById(R.id.bt_toggle_pair);
                    toggle.setText(R.string.bt_pair);
                    mCurrDevice = null;
                    if (mBusyThread != null) {
                        mBusyThread.cancel();
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed during disconnect", e);
                    UIUtilities.showNotification(aContext, R.string.error);
                }
            }
        },
                new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

    }

    public static void sendCommand() {
        new Thread() {
            public void run() {
                try {
                    Log.i(Constants.LOG_TAG_UI, "Test command");
                    mBusyThread = new ConnectThread(mCurrDevice);
                    mBusyThread.sendMessage(getMessage());
                    mBusyThread.start();
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed", e);
                    if (mBusyThread != null) {
                        mBusyThread.cancel();
                    }
                }

            }
        }.start();
    }

    public static void disconnect() {
        if (mBusyThread != null) {
            mBusyThread.cancel();
        }
        mCurrDevice = null;

    }

    public static void selectDevice(BluetoothDevice aDevice) {
        mCurrDevice = aDevice;
    }

    private static byte[] getMessage() {
        // read single measure
        return ByteUtils.hexStringToByte("D5F0E00D");
    }

}
