package com.astoev.cave.survey.service.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.ResultReceiver;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.util.ConfigUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 10/23/13
 * Time: 10:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class BluetoothService {

    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final Set<String> SUPPORTED_DEVICES = new HashSet<String>();
    static {
        SUPPORTED_DEVICES.add("iLDM-150");
    }

    private static ConnectThread mBusyThread = null;
    private static BluetoothDevice mCurrDevice = null;
    private static boolean mPaired = false;
    private static Activity mCurrContext = null;
    private static List<BroadcastReceiver> mRegisteredReceivers = new ArrayList<BroadcastReceiver>();

    private static BroadcastReceiver mConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!isSupported(device)) {
                    // ignore other devices
                    Log.i(Constants.LOG_TAG_SERVICE, "Bonded unsupported device");
                    return;
                }

                UIUtilities.showNotification(R.string.bt_paired);
                Log.i(Constants.LOG_TAG_UI, "Paired with " + device.getName());
                mPaired = true;
                mCurrDevice = device;

                TextView status = (TextView) mCurrContext.findViewById(R.id.bt_status);
                status.setText(BluetoothService.getCurrDeviceStatusLabel(mCurrContext));

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
                if (mCurrDevice == null) {
                    // ignore event if don't expect to be paired with device
                    Log.i(Constants.LOG_TAG_SERVICE, "Ignore disconnect, no curr device");
                    return;
                }

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!isSupported(device)) {
                    // ignore other devices
                    Log.i(Constants.LOG_TAG_SERVICE, "Ignore disconnect, device not supported");
                    return;
                }

                if (mCurrDevice.getName().equals(device.getName()) && mCurrDevice.getAddress().equals(device.getAddress())) {
                    mCurrDevice = null;
                    mPaired = false;
                    UIUtilities.showNotification(R.string.bt_not_paired);
                    Log.i(Constants.LOG_TAG_UI, "Disconnected");

                    stop();

                    TextView status = (TextView) mCurrContext.findViewById(R.id.bt_status);
                    status.setText(BluetoothService.getCurrDeviceStatusLabel(mCurrContext));
                } else {
                    // ignore events for other non paired devices
                    Log.i(Constants.LOG_TAG_SERVICE, "Ignore disconnect, not curr device");
                }

            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_UI, "Failed during disconnect", e);
                UIUtilities.showNotification(R.string.error);
            }
        }
    };

    public static boolean isBluetoothSupported() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean isDeviceSelected() {
        return mCurrDevice != null;
    }

    public static boolean askBluetoothOn(Activity aParentActivity) {
        if (isBluetoothSupported()) {
            return BluetoothAdapter.getDefaultAdapter().isEnabled();
        }
        return false;
    }

    public static void registerListeners(final Activity aContext) {

        mCurrContext = aContext;

        if (!mRegisteredReceivers.contains(mConnectedReceiver)) {
            mRegisteredReceivers.add(mConnectedReceiver);
            mCurrContext.registerReceiver(mConnectedReceiver,
                    new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        }

        if (!mRegisteredReceivers.contains(mDisconnectedReceiver)) {
            mRegisteredReceivers.add(mDisconnectedReceiver);
            mCurrContext.registerReceiver(mDisconnectedReceiver,
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
        mPaired = false;

        for (BroadcastReceiver r : mRegisteredReceivers) {
            mCurrContext.unregisterReceiver(r);
        }
        mRegisteredReceivers.clear();
        mCurrContext = null;
    }

    public static void selectDevice(String aDeviceAddress) {
        mCurrDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(aDeviceAddress);
        Log.i(Constants.LOG_TAG_SERVICE, "Selected " + aDeviceAddress + " : " + mCurrDevice);

       // ping the device in background
        new Thread() {
            public void run() {
                Log.i(Constants.LOG_TAG_UI, "Test device");
                BluetoothSocket tester = null;
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
                        tester = mCurrDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                    } else {
                        tester = callSafeCreateInsecureRfcommSocketToServiceRecord(mCurrDevice);
                    }
                    tester.connect();
                    Log.i(Constants.LOG_TAG_UI, "Device found!");
                    UIUtilities.showNotification(R.string.bt_paired);
                    mPaired = true;
                } catch (Exception e) {
                    mCurrDevice = null;
                    UIUtilities.showNotification(R.string.bt_pair_failed);
                    Log.e(Constants.LOG_TAG_SERVICE, "Failed test to new device");
                } finally {
                    if (tester != null) {
                        try {
                            tester.close();
                        } catch (IOException e) {
                            Log.e(Constants.LOG_TAG_SERVICE, "Failed to cleanup tester");
                        }
                    }
                }
            }
        }.start();
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public static BluetoothSocket callSafeCreateInsecureRfcommSocketToServiceRecord(BluetoothDevice deviceaArg) throws IOException{
        return deviceaArg.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
    }

    public static boolean isPaired() {
        return mPaired;
    }

    public static boolean isSupported(String aDeviceName) {
        return SUPPORTED_DEVICES.contains(aDeviceName);
    }

    public static boolean isSupported(BluetoothDevice aDevice) {
        return aDevice != null && isSupported(aDevice.getName());
    }

    // read single measure command
    private static byte[] getReadDistanceMessage() {
        return ByteUtils.hexStringToByte("D5F0E00D");
    }


    public static List<Pair<String, String>> getPairedCompatibleDevices() {
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for(BluetoothDevice d: devices) {
            if (isSupported(d.getName())) {
                result.add(new Pair<String, String>(d.getName(), d.getAddress()));
            }
        }
        return result;
    }

    public static String getCurrDeviceStatus() {
        if (mCurrDevice == null) {
            return ConfigUtil.getContext().getString(R.string.bt_state_unknown);
        }

        switch (mCurrDevice.getBondState()) {
            case BluetoothDevice.BOND_BONDED:
                return mCurrContext.getString(R.string.bt_state_bonded);

            case BluetoothDevice.BOND_BONDING:
                return mCurrContext.getString(R.string.bt_state_bonding);

            case BluetoothDevice.BOND_NONE:
                return mCurrContext.getString(R.string.bt_state_none);

            default:
                return mCurrContext.getString(R.string.bt_state_unknown);
        }


    }

    public static String getCurrDeviceStatusLabel(Context aContext) {
        StringBuilder statusText = new StringBuilder();
        statusText.append(getCurrDeviceStatus());
        statusText.append(" : ");
        statusText.append(BluetoothService.isPaired() ? aContext.getString(R.string.bt_paired) : aContext.getString(R.string.bt_not_paired));
        return statusText.toString();
    }
}
