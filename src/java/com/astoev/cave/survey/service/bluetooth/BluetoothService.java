package com.astoev.cave.survey.service.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ResultReceiver;
import android.util.Log;
import android.util.Pair;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.BoschPLR30CBluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.BoschPLR30CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.CEMILDMBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.DistoXBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.LaserAceBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.TruPulse360BBluetoothDevice;
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


    private static final Set<AbstractBluetoothRFCOMMDevice> SUPPORTED_BLUETOOTH_COM_DEVICES = new HashSet<>();
    private static final Set<AbstractBluetoothLEDevice> SUPPORTED_BLUETOOTH_LE_DEVICES = new HashSet<>();

    static {

        // COMM devices
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new CEMILDMBluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new LaserAceBluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new TruPulse360BBluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new DistoXBluetoothDevice());
//        SUPPORTED_DEVICES.add(new LeicaDistoBluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new BoschPLR30CBluetoothDevice());

        // LE devices
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new BoschPLR30CBluetoothLEDevice());
    }

    // generic
    private static BluetoothDevice mSelectedDevice = null;
    private static AbstractBluetoothDevice mSelectedDeviceSpec = null;
    private static Activity mCurrContext = null;

    // COMM specific
    private static CommDeviceCommunicationThread mCommunicationThread = null;

    // LE specific
    private static BluetoothLECallback leCallback = null;
    private static BluetoothGatt mBluetoothGatt = null;



    public static boolean isBluetoothSupported() {
        return mCurrContext.getSystemService(Context.BLUETOOTH_SERVICE) != null && BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean isBluetoothLESupported() {
        return isBluetoothSupported() && mCurrContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
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

        if (mSelectedDeviceSpec instanceof AbstractBluetoothRFCOMMDevice) {
            if (mCommunicationThread != null) {
                mCommunicationThread.cancel();
                try {
                    mCommunicationThread.join();
                } catch (InterruptedException e) {
                    Log.e(Constants.LOG_TAG_BT, "Interrupted waiting old thread to complete, e");
                }
            }

            mCommunicationThread = new CommDeviceCommunicationThread(mSelectedDevice, (AbstractBluetoothRFCOMMDevice) mSelectedDeviceSpec);
            mCommunicationThread.start();
        } else {
            // require newer android to work with LE devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // check if we need to connect from scratch or just reconnect to previous device
                if (mBluetoothGatt != null) {
                    Log.i(Constants.LOG_TAG_BT, "Connecting ");
                    // just reconnect
                    if (!mBluetoothGatt.connect()) {
                        Log.e(Constants.LOG_TAG_BT, "Failed to connect");
                    }
                    ;
                } else {
                    Log.i(Constants.LOG_TAG_BT, "Re-connecting ");
                    // connect with remote device
                    mBluetoothGatt = mSelectedDevice.connectGatt(mCurrContext, false, getGattCallBack());
                }
            } else {
                Log.i(Constants.LOG_TAG_BT, "Unsupported version ");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public static BluetoothSocket callSafeCreateInsecureRfcommSocketToServiceRecord(BluetoothDevice deviceaArg) throws IOException {
        return deviceaArg.createInsecureRfcommSocketToServiceRecord(((AbstractBluetoothRFCOMMDevice) mSelectedDeviceSpec).getSPPUUID());
    }

    public static boolean isPaired() {
        return mCommunicationThread != null && mCommunicationThread.ismPaired();
    }

    public static AbstractBluetoothRFCOMMDevice getSupportedDevice(String aDeviceName) {
        for (AbstractBluetoothRFCOMMDevice device : SUPPORTED_BLUETOOTH_COM_DEVICES) {
            if (device.isNameSupported(aDeviceName)) {
                return device;
            }
        }
        return null;
    }

    public static boolean isSupported(String aDeviceName) {
        return getSupportedDevice(aDeviceName) != null;
    }

    public static List<AbstractBluetoothRFCOMMDevice> getSupportedCommDevices() {
        List<AbstractBluetoothRFCOMMDevice> devicesByDescription = new ArrayList(SUPPORTED_BLUETOOTH_COM_DEVICES);
        Collections.sort(devicesByDescription);
        return devicesByDescription;
    }

    public static List<AbstractBluetoothLEDevice> getSupportedLEDevices() {
        List<AbstractBluetoothLEDevice> devicesByDescription = new ArrayList(SUPPORTED_BLUETOOTH_LE_DEVICES);
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
        return mSelectedDeviceSpec != null && mSelectedDeviceSpec.isMeasureSupported(mSelectedDevice.getName(), aMeasureType);
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


    @TargetApi(18)
    public static void discoverBluetoothLEDevices() {
        Log.i(Constants.LOG_TAG_BT, "Start discovery for Bluetooth LE devices");

        leCallback = new BluetoothLECallback();
        BluetoothAdapter.getDefaultAdapter().startLeScan(leCallback);

/* todo replace for newer versions with
        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btManager.getAdapter().getBluetoothLeScanner().startScan(new ScanCallback(){
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.i(Constants.LOG_TAG_BT, "got " + callbackType + " : " + result.toString());
                System.out.println("re = " + result.getDevice().getName());
            }
        });
*/
    }

    @TargetApi(18)
    public static void stopDiscoverBluetoothLEDevices() {
        Log.i(Constants.LOG_TAG_BT, "Stop discovery for Bluetooth LE devices");

        BluetoothAdapter.getDefaultAdapter().stopLeScan(leCallback);
    }

    @TargetApi(18)
    /* callbacks called for any action on particular Ble Device */
    private static BluetoothGattCallback getGattCallBack() {
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(Constants.LOG_TAG_BT, "Connected");

                    // now we can start talking with the device, e.g.
                    mBluetoothGatt.readRemoteRssi();
                    // response will be delivered to callback object!

                    // in our case we would also like automatically to call for services discovery
//                startServicesDiscovery();

                    // and we also want to get RSSI value to be updated periodically
//                startMonitoringRssiValue();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                mConnected = false;
//                mUiCallback.uiDeviceDisconnected(mBluetoothGatt, mBluetoothDevice);
                    Log.i(Constants.LOG_TAG_BT, "Disconnected");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // now, when services discovery is finished, we can call getServices() for Gatt
//                getSupportedServices();
                    Log.i(Constants.LOG_TAG_BT, "Got services");
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                // we got response regarding our request to fetch characteristic value
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // and it success, so we can get the value
//                getCharacteristicValue(characteristic);
                    Log.i(Constants.LOG_TAG_BT, "onCharacteristicRead");
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                Log.i(Constants.LOG_TAG_BT, "onCharacteristicChanged");
                // characteristic's value was updated due to enabled notification, lets get this value
                // the value itself will be reported to the UI inside getCharacteristicValue
//            getCharacteristicValue(characteristic);
//            // also, notify UI that notification are enabled for particular characteristic
//            mUiCallback.uiGotNotification(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.i(Constants.LOG_TAG_BT, "onCharacteristicrWrite");
//            String deviceName = gatt.getDevice().getName();
//            String serviceName = BleNamesResolver.resolveServiceName(characteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault()));
//            String charName = BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault()));
//            String description = "Device: " + deviceName + " Service: " + serviceName + " Characteristic: " + charName;
//
//            // we got response regarding our request to write new value to the characteristic
//            // let see if it failed or not
//            if(status == BluetoothGatt.GATT_SUCCESS) {
//                mUiCallback.uiSuccessfulWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description);
//            }
//            else {
//                mUiCallback.uiFailedWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description + " STATUS = " + status);
//            }
            }

            ;

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // we got new value of RSSI of the connection, pass it to the UI
//                mUiCallback.uiNewRssiAvailable(mBluetoothGatt, mBluetoothDevice, rssi);
                    Log.i(Constants.LOG_TAG_BT, "onReadRemoteRssi");
                }
            }

            ;

            // Added by Akiba
            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.i(Constants.LOG_TAG_BT, "onDescriptorWrite");
//            String deviceName = gatt.getDevice().getName();
//            String serviceName = BleNamesResolver.resolveServiceName(descriptor.getCharacteristic().getService().getUuid().toString().toLowerCase(Locale.getDefault()));
//            String charName = BleNamesResolver.resolveCharacteristicName(descriptor.getCharacteristic().getUuid().toString().toLowerCase(Locale.getDefault()));
//            String description = "Device: " + deviceName + " Service: " + serviceName + " Characteristic: " + charName;
//
//            // we got response regarding our request to write new value to the characteristic
//            // let see if it failed or not
//            if(status == BluetoothGatt.GATT_SUCCESS) {
//                mUiCallback.uiSuccessfulWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, descriptor.getCharacteristic(), description);
//            }
//            else {
//                mUiCallback.uiFailedWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, descriptor.getCharacteristic(), description + " STATUS = " + status);
//            }
            }

            ;
        };
    }
}
