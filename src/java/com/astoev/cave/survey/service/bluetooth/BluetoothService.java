package com.astoev.cave.survey.service.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.ResultReceiver;
import android.util.Log;
import android.util.Pair;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.CEMILDMBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.LaserAceBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.TruPulse360BBluetoothDevice;
import com.astoev.cave.survey.util.ConfigUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
        SUPPORTED_DEVICES.add(new TruPulse360BBluetoothDevice());
//        SUPPORTED_DEVICES.add(new DistoXBluetoothDevice());
    }

    private static ConnectThread mCommunicationThread = null;
    private static BluetoothDevice mSelectedDevice = null;
    private static AbstractBluetoothDevice mSelectedDeviceSpec = null;
    private static Activity mCurrContext = null;




    public static boolean isBluetoothSupported() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean isDeviceSelected() {
        return mSelectedDevice != null;
    }

    public static boolean askBluetoothOn(Activity aParentActivity) {
        if (isBluetoothSupported()) {
            return BluetoothAdapter.getDefaultAdapter().isEnabled();
        }
        return false;
    }



    public static void sendReadMeasureCommand(final ResultReceiver receiver, final Constants.Measures aMeasure, final Constants.Measures[] aMeasuresWelcome) {

        if (mCommunicationThread != null) {

            Log.i(Constants.LOG_TAG_BT, "Send read command for " + aMeasure);

            List<Constants.MeasureTypes> measureTypes = new ArrayList<Constants.MeasureTypes>();
            List<Constants.Measures> measureTargets = new ArrayList<Constants.Measures>();

            measureTypes.add(getMeasureTypeFromTarget(aMeasure));
            measureTargets.add(aMeasure);

            if (aMeasuresWelcome != null) {
                for (Constants.Measures m : aMeasuresWelcome) {
                    measureTypes.add(getMeasureTypeFromTarget(m));
                    measureTargets.add(m);
                }
            }

            mCommunicationThread.awaitMeasures(measureTypes, measureTargets, receiver);
        } else {
            Log.d(Constants.LOG_TAG_BT, "Drop BT command : inactive communication : " + aMeasure);
        }
    }

    public static Constants.MeasureTypes getMeasureTypeFromTarget(Constants.Measures aMeasure) {
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

    public static void stop() {
        if (mCommunicationThread != null) {
            mCommunicationThread.cancel();
        }
    }

    public static synchronized void selectDevice(final String aDeviceAddress) {
        mSelectedDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(aDeviceAddress);
        mSelectedDeviceSpec = getSupportedDevice(mSelectedDevice.getName());

        Log.i(Constants.LOG_TAG_BT, "Selected " + aDeviceAddress + " : " + mSelectedDevice + " of type " + mSelectedDeviceSpec.getDescription());

        if (mCommunicationThread != null) {
            mCommunicationThread.cancel();
            try {
                mCommunicationThread.join();
            } catch (InterruptedException e) {
                Log.e(Constants.LOG_TAG_BT, "Interrupted waiting old thread to complete, e");
            }
        }

        mCommunicationThread = new ConnectThread(mSelectedDevice, mSelectedDeviceSpec);
        mCommunicationThread.start();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public static BluetoothSocket callSafeCreateInsecureRfcommSocketToServiceRecord(BluetoothDevice deviceaArg) throws IOException {
        return deviceaArg.createInsecureRfcommSocketToServiceRecord(mSelectedDeviceSpec.getSPPUUID());
    }

    public static boolean isPaired() {
        return mCommunicationThread != null && mCommunicationThread.ismPaired();
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

    public static List<AbstractBluetoothDevice> getSupportedDevices() {
        List<AbstractBluetoothDevice> devicesByDescription = new ArrayList(SUPPORTED_DEVICES);
        Collections.sort(devicesByDescription);
        return devicesByDescription;
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
        if (mSelectedDevice == null) {
            return ConfigUtil.getContext().getString(R.string.bt_state_unknown);
        }

        switch (mSelectedDevice.getBondState()) {
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
        return mSelectedDeviceSpec != null && mSelectedDeviceSpec.isMeasureSupported(aMeasureType);
    }

    public static void registerListeners(BTActivity btActivity) {
        mCurrContext = btActivity;
        if (mCommunicationThread != null) {
            mCommunicationThread.registerListeners(btActivity);
        }
    }

    public static void unregisterListeners(BTActivity btActivity) {
        mCurrContext = btActivity;
        if (mCommunicationThread != null) {
            mCommunicationThread.unregisterListeners(btActivity);
        }
    }


}
