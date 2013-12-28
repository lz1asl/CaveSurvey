package com.astoev.cave.survey.service.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Button;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;

import java.util.ArrayList;
import java.util.List;

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
    private static boolean mPaired = false;
    private static Activity mCurrContext = null;
    private static List<BroadcastReceiver> mRegisteredReceivers = new ArrayList<BroadcastReceiver>();

    public static boolean isBluetoothSupported() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        }
        return true;
    }

    public static boolean isDeviceSelected() {
        return mCurrDevice != null;
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

        mCurrContext = aContext;

        BroadcastReceiver connectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    UIUtilities.showNotification(R.string.bt_paired);
                    Button toggle = (Button) aContext.findViewById(R.id.bt_toggle_pair);
//                    toggle.setText(R.string.bt_disconnect);
                    Log.i(Constants.LOG_TAG_UI, "Paired with " + mCurrDevice);
                    mPaired = true;
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed during pair", e);
                    UIUtilities.showNotification(R.string.error);
                }
            }
        };
        BroadcastReceiver disconnectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    UIUtilities.showNotification(R.string.bt_disconnect);
                    Log.i(Constants.LOG_TAG_UI, "Disconnected");
                    Button toggle = (Button) aContext.findViewById(R.id.bt_toggle_pair);
//                    toggle.setText(R.string.bt_pair);
                    mPaired = false;
                    stop();
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed during disconnect", e);
                    UIUtilities.showNotification(R.string.error);
                }
            }
        };

        mRegisteredReceivers.add(connectedReceiver);
        mRegisteredReceivers.add(disconnectedReceiver);

        aContext.registerReceiver(connectedReceiver,
                new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        aContext.registerReceiver(disconnectedReceiver,
                new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

    }

    public static void sendReadDistanceCommand(final ResultReceiver receiver, final Constants.Measures aMeasure) {
        new Thread() {
            public void run() {
                try {
                    Log.i(Constants.LOG_TAG_UI, "Test command");
                    if (mBusyThread != null) {
                        mBusyThread.cancel();
                    }
                    mBusyThread = new ConnectThread(mCurrDevice);
                    mBusyThread.setReceiver(receiver);
                    mBusyThread.setMeasure(aMeasure);
                    mBusyThread.sendMessage(getReadDistanceMessage());
                    mBusyThread.start();
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed", e);
                    UIUtilities.showNotification(R.string.error);
                    if (mBusyThread != null) {
                        mBusyThread.cancel();
                    }
                }
            }
        }.start();
    }

    public static void stop() {
        if (mBusyThread != null) {
            mBusyThread.cancel();
        }
        mCurrDevice = null;

        for (BroadcastReceiver r: mRegisteredReceivers) {
            mCurrContext.unregisterReceiver(r);
        }
        mRegisteredReceivers.clear();
        mCurrContext = null;
    }

    public static void selectDevice(String aDeviceAddress) {
        mCurrDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(aDeviceAddress);
    }

    public static boolean isPaired() {
        return mPaired;
    }

    // read single measure command
    private static byte[] getReadDistanceMessage() {
        return ByteUtils.hexStringToByte("D5F0E00D");
    }



}
