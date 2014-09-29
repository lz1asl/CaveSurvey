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
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.CEMILDMBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.LaserAceBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.TruPulse360BBluetoothDevice;
import com.astoev.cave.survey.util.ConfigUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 10/23/13
 * Time: 10:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class BluetoothService {


    private static final Set<AbstractBluetoothDevice> SUPPORTED_DEVICES = new HashSet<AbstractBluetoothDevice>();

    static {
        SUPPORTED_DEVICES.add(new CEMILDMBluetoothDevice());
        SUPPORTED_DEVICES.add(new LaserAceBluetoothDevice());
//        SUPPORTED_DEVICES.add(new TruPulse360BBluetoothDevice());
    }

    private static ConnectThread mBusyThread = null;
    private static BluetoothDevice mCurrDevice = null;
    private static AbstractBluetoothDevice mCurrDeviceSpec = null;
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
                    Log.i(Constants.LOG_TAG_BT, "Bonded unsupported device");
                    return;
                }

                UIUtilities.showNotification(R.string.bt_paired);
                Log.i(Constants.LOG_TAG_BT, "Paired with " + device.getName());
                mPaired = true;
                mCurrDevice = device;
                mCurrDeviceSpec = getSupportedDevice(device.getName());

                TextView status = (TextView) mCurrContext.findViewById(R.id.bt_status);
                status.setText(BluetoothService.getCurrDeviceStatusLabel(mCurrContext));

            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_BT, "Failed during pair", e);
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
                    Log.i(Constants.LOG_TAG_BT, "Ignore disconnect, no curr device");
                    return;
                }

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!isSupported(device)) {
                    // ignore other devices
                    Log.i(Constants.LOG_TAG_BT, "Ignore disconnect, device not supported");
                    return;
                }

                if (mCurrDevice.getName().equals(device.getName()) && mCurrDevice.getAddress().equals(device.getAddress())) {
                    mCurrDevice = null;
                    mCurrDeviceSpec = null;
                    mPaired = false;
                    UIUtilities.showNotification(R.string.bt_not_paired);
                    Log.i(Constants.LOG_TAG_BT, "Disconnected");

                    stop();

                    TextView status = (TextView) mCurrContext.findViewById(R.id.bt_status);
                    status.setText(BluetoothService.getCurrDeviceStatusLabel(mCurrContext));
                } else {
                    // ignore events for other non paired devices
                    Log.i(Constants.LOG_TAG_BT, "Ignore disconnect, not curr device");
                }

            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_BT, "Failed during disconnect", e);
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

    public static void sendReadMeasureCommand(final ResultReceiver receiver, final Constants.Measures aMeasure, final Constants.Measures[] aMeasuresWelcome) {
        new Thread() {
            public void run() {
                try {
                    Log.i(Constants.LOG_TAG_BT, "Send read command");
                    if (mBusyThread != null) {
                        mBusyThread.cancel();
                    }
                    mBusyThread = new ConnectThread(mCurrDevice, mCurrDeviceSpec);
                    mBusyThread.setReceiver(receiver);

                    List<Constants.MeasureTypes> measureTypes = new ArrayList<Constants.MeasureTypes>();
                    List<Constants.Measures> measureTargets = new ArrayList<Constants.Measures>();

                    measureTypes.add(getType(aMeasure));
                    measureTargets.add(aMeasure);

                    if (aMeasuresWelcome != null) {
                        for (Constants.Measures m : aMeasuresWelcome) {
                            measureTypes.add(getType(m));
                            measureTargets.add(m);
                        }
                    }

                    mBusyThread.setMeasureTypes(measureTypes);
                    mBusyThread.setTargets(measureTargets);
                    mBusyThread.start();
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_BT, "Failed", e);
                    UIUtilities.showNotification(R.string.error);
                    if (mBusyThread != null) {
                        mBusyThread.cancel();
                    }
                }
            }

            private Constants.MeasureTypes getType(Constants.Measures aMeasure) {
                switch (aMeasure) {
                    case distance:
                    case up:
                    case down:
                    case left:
                    case right:
                        return Constants.MeasureTypes.distance;

                    case angle:
                        return Constants.MeasureTypes.angle;

                    case slope:
                        return Constants.MeasureTypes.slope;

                    default:
                        return null;
                }
            }
        }.start();
    }

    public static void stop() {
        if (mBusyThread != null) {
            mBusyThread.cancel();
        }
        mCurrDevice = null;
        mCurrDeviceSpec = null;
        mPaired = false;

        for (BroadcastReceiver r : mRegisteredReceivers) {
            mCurrContext.unregisterReceiver(r);
        }
        mRegisteredReceivers.clear();
    }

    public static void selectDevice(final String aDeviceAddress) {
        mCurrDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(aDeviceAddress);
        mCurrDeviceSpec = getSupportedDevice(mCurrDevice.getName());
        Log.i(Constants.LOG_TAG_BT, "Selected " + aDeviceAddress + " : " + mCurrDevice + " of type " + mCurrDeviceSpec.getDescription());


        if (!mCurrDeviceSpec.isPassiveBTConnection()) {
            // ping the device in background
            new Thread() {
                public void run() {
                    Log.i(Constants.LOG_TAG_BT, "Test device");
                    BluetoothSocket tester = null;
                    try {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
                            tester = mCurrDevice.createRfcommSocketToServiceRecord(mCurrDeviceSpec.getSPPUUID());
                        } else {
                            tester = callSafeCreateInsecureRfcommSocketToServiceRecord(mCurrDevice);
                        }
                        tester.connect();
                        Log.i(Constants.LOG_TAG_BT, "Device found!");
                        UIUtilities.showNotification(R.string.bt_connected);
                        mPaired = true;
                    } catch (Exception e) {
                        mCurrDevice = null;
                        mCurrDeviceSpec = null;
                        UIUtilities.showNotification(R.string.bt_pair_failed);
                        Log.e(Constants.LOG_TAG_BT, "Failed test to new device", e);
                    } finally {
                        if (tester != null) {
                            try {
                                tester.close();
                            } catch (IOException e) {
                                Log.e(Constants.LOG_TAG_BT, "Failed to cleanup tester");
                            }
                        }
                    }
                }
            }.start();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public static BluetoothSocket callSafeCreateInsecureRfcommSocketToServiceRecord(BluetoothDevice deviceaArg) throws IOException {
        return deviceaArg.createInsecureRfcommSocketToServiceRecord(mCurrDeviceSpec.getSPPUUID());
    }

    public static boolean isPaired() {
        return mPaired;
    }

    public static AbstractBluetoothDevice getSupportedDevice(String aDeviceName) {
        for (AbstractBluetoothDevice device : SUPPORTED_DEVICES) {
            if (device.isNameSupported(aDeviceName)) {
                return device;
            }
        }
        return null;
    }

    public static boolean isSupported(String aDeviceName) {
        return getSupportedDevice(aDeviceName) != null;
    }

    public static Set<AbstractBluetoothDevice> getSupportedDevices() {
        return SUPPORTED_DEVICES;
    }

    public static boolean isSupported(BluetoothDevice aDevice) {
        return aDevice != null && isSupported(aDevice.getName());
    }

    public static List<Pair<String, String>> getPairedCompatibleDevices() {
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for (BluetoothDevice d : devices) {
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

    // for the current device
    public static boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        return mCurrDeviceSpec != null && mCurrDeviceSpec.isMeasureSupported(aMeasureType);
    }
}
