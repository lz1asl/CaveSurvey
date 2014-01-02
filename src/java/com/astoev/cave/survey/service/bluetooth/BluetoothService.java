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
import android.widget.CheckBox;

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

    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private static ConnectThread mBusyThread = null;
    private static BluetoothDevice mCurrDevice = null;
    private static boolean mPaired = false;
    private static Activity mCurrContext = null;
    private static List<BroadcastReceiver> mRegisteredReceivers = new ArrayList<BroadcastReceiver>();

    private static BroadcastReceiver mConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                UIUtilities.showNotification(R.string.bt_paired);
//                Button toggle = (Button) mCurrContext.findViewById(R.id.bt_toggle_pair);
                Log.i(Constants.LOG_TAG_UI, "Paired with " + mCurrDevice);
                mPaired = true;

                CheckBox deviceStatus = (CheckBox) mCurrContext.findViewById(R.id.bt_device_status);
                deviceStatus.setChecked(true);
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_UI, "Failed during pair", e);
                UIUtilities.showNotification(R.string.error);
            }
        }
    };
    private static BroadcastReceiver mDisconnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                UIUtilities.showNotification(R.string.bt_disconnect);
                Log.i(Constants.LOG_TAG_UI, "Disconnected");
//                Button toggle = (Button) mCurrContext.findViewById(R.id.bt_toggle_pair);
                mPaired = false;

                CheckBox deviceStatus = (CheckBox) mCurrContext.findViewById(R.id.bt_device_status);
                deviceStatus.setChecked(false);

                stop();
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_UI, "Failed during disconnect", e);
                UIUtilities.showNotification(R.string.error);
            }
        }
    };

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

    public static void registerListeners(final Activity aContext) {

        mCurrContext = aContext;

        if (!mRegisteredReceivers.contains(mConnectedReceiver)) {
            mRegisteredReceivers.add(mConnectedReceiver);
            aContext.registerReceiver(mConnectedReceiver,
                    new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        }

        if (!mRegisteredReceivers.contains(mDisconnectedReceiver)) {
            mRegisteredReceivers.add(mDisconnectedReceiver);
            aContext.registerReceiver(mDisconnectedReceiver,
                    new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        }
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

        for (BroadcastReceiver r : mRegisteredReceivers) {
            mCurrContext.unregisterReceiver(r);
        }
        mRegisteredReceivers.clear();
        mCurrContext = null;
    }

    public static void selectDevice(String aDeviceAddress) {
        mCurrDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(aDeviceAddress);
        Log.i(Constants.LOG_TAG_SERVICE, "Selected " + aDeviceAddress + " : " + mCurrDevice);

       /* // ping the device in background
        new Thread() {
            public void run() {
                Log.i(Constants.LOG_TAG_UI, "Test device");
                BluetoothSocket tester = null;
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
                        tester = mCurrDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                    } else {
                        tester = mCurrDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                    }
                    tester.connect();
                    Log.i(Constants.LOG_TAG_UI, "Device found!");
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_SERVICE, "Failed to test device ", e);
                } finally {
                    if (tester != null) {
                        try {
                            tester.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();*/
    }

    public static boolean isPaired() {
        return mPaired;
    }

    // read single measure command
    private static byte[] getReadDistanceMessage() {
        return ByteUtils.hexStringToByte("D5F0E00D");
    }


}
