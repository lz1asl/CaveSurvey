package com.astoev.cave.survey.service.bluetooth;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.content.Context.BLUETOOTH_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.astoev.cave.survey.Constants.LOG_TAG_BT;
import static java.lang.Thread.sleep;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.ResultReceiver;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.activity.main.Refresheable;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.DiscoveredBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.AbstractBluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.Bric4BluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.Bric5BluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.DistoXBleDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.LeicaDistoBluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.StanleyBluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.HerschLEM50BluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.MileseeyP7BluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.MileseeyT7BluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.Mileseeyd5tBluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.MileseeydM120LeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.SuaokiP7BluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.CEMILDMBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.LaserAceBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.LeicaDistoD3aBtBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.TruPulse360BluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschGLM100CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschGLM50CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschPLR30CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschPLR40CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschPLR50CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.distox.DistoXv1BluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.distox.DistoXv2BluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.lecommands.AbstractBluetoothCommand;
import com.astoev.cave.survey.service.bluetooth.lecommands.EnableNotificationCommand;
import com.astoev.cave.survey.service.bluetooth.lecommands.PullCharacteristicCommand;
import com.astoev.cave.survey.service.bluetooth.lecommands.WriteDescriptorCommand;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.StringUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new TruPulse360BluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new DistoXv1BluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new DistoXv2BluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new BoschGLM50CBluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new BoschGLM100CBluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new BoschPLR30CBluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new BoschPLR40CBluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new BoschPLR50CBluetoothDevice());
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new LeicaDistoD3aBtBluetoothDevice());

        // LE devices
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new LeicaDistoBluetoothLEDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new Mileseeyd5tBluetoothLeDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new MileseeyP7BluetoothLeDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new MileseeyT7BluetoothLeDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new MileseeydM120LeDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new StanleyBluetoothLeDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new SuaokiP7BluetoothLeDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new HerschLEM50BluetoothLeDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new Bric4BluetoothLEDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new Bric5BluetoothLEDevice());
//        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new ShetlandAttackPonyLeDevice()); TODO not yet tested
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new DistoXBleDevice());

    }

    // generic
    private static DiscoveredBluetoothDevice mSelectedDevice = null;
    private static Activity mCurrContext = null;

    // COMM specific
    private static CommDeviceCommunicationThread mCommunicationThread = null;

    // LE specific
    private static BluetoothAdapter.LeScanCallback leCallback = null; // older LE callback
    private static ScanCallback leCallbackLollipop = null; // newer LE callback
    private static BluetoothGatt mBluetoothGatt = null;
    private static List<DiscoveredBluetoothDevice> mCurrentDevices = null;
    private static List<String> mIgnoredDevices = new ArrayList<>();
    private static boolean mConnectingDevice = false;
    private static MyBluetoothGattCallback leDataCallback = null;
    private static int mLeDeviceState = R.string.bt_state_none;

    // needed by le command queueing, details and credit http://www.brendanwhelan.net/2015/bluetooth-command-queuing-for-android
    private static LinkedList<AbstractBluetoothCommand> mCommandQueue = new LinkedList<>();
    private static Executor mCommandExecutor = Executors.newSingleThreadExecutor();
    private static Semaphore mCommandLock = new Semaphore(1,true);
    private static boolean expectingMeasurement = false;

    // compile time switch to allow processing of all device characteristics
    private static final boolean DEVELOPMENT_MODE = true;

    public static boolean isBluetoothSupported() {
        return mCurrContext != null
                && (mCurrContext.getSystemService(BLUETOOTH_SERVICE) != null || BluetoothAdapter.getDefaultAdapter() != null);
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

        // measurements requested
        expectingMeasurement = true;
        List<Constants.MeasureTypes> measureTypes = new ArrayList<>();
        List<Constants.Measures> measureTargets = new ArrayList<>();

        measureTypes.add(getMeasureTypeFromTarget(aMeasure));
        measureTargets.add(aMeasure);

        if (aMeasuresWelcome != null) {
            for (Constants.Measures m : aMeasuresWelcome) {
                measureTypes.add(getMeasureTypeFromTarget(m));
                measureTargets.add(m);
            }
        }

        // communication
        if (mCommunicationThread != null) {

            Log.i(LOG_TAG_BT, "Send read command for " + aMeasure);
            mCommunicationThread.awaitMeasures(measureTypes, measureTargets, receiver);
        } else if (mBluetoothGatt != null) {

            Log.i(LOG_TAG_BT, "Request LE read " + aMeasure);
            leDataCallback.awaitMeasures(measureTypes, measureTargets, receiver);

            AbstractBluetoothLEDevice leDevice = (AbstractBluetoothLEDevice) mSelectedDevice.definition;
            if (SDK_INT >= JELLY_BEAN_MR2 && leDevice.needCharacteristicPull()) {
                Log.i(LOG_TAG_BT, "Request LE pull");
                // device specific
                Constants.MeasureTypes type = getMeasureTypeFromTarget(aMeasure);
                AbstractBluetoothCommand deviceCommand = leDevice.getReadCharacteristicCommand(type);
                if (deviceCommand == null) {
                    // generic pull command
                    BluetoothGattCharacteristic c = mBluetoothGatt.getService(leDevice.getService(type)).getCharacteristic(leDevice.getCharacteristic(type));
                    deviceCommand = new PullCharacteristicCommand(c, leDevice);
                }
                enqueueCommand(deviceCommand);
            }

        } else {
            Log.d(LOG_TAG_BT, "Drop BT command : inactive communication : " + aMeasure);
        }
    }

    public static void startScanning(BTMeasureResultReceiver receiver) {

        if (mBluetoothGatt != null) {
            AbstractBluetoothLEDevice leDevice = (AbstractBluetoothLEDevice) mSelectedDevice.definition;
            AbstractBluetoothCommand startScanCommand = leDevice.getStartScanCommand();
            if (startScanCommand != null) {
                Log.i(LOG_TAG_BT, "Request LE stop scan");
                expectingMeasurement = true;
                List<Constants.MeasureTypes> measureTypes = Arrays.asList(Constants.MeasureTypes.distance, Constants.MeasureTypes.angle, Constants.MeasureTypes.slope);
                List<Constants.Measures> measureTargets = Arrays.asList(Constants.Measures.distance, Constants.Measures.angle, Constants.Measures.slope);
                leDataCallback.awaitMeasures(measureTypes, measureTargets, receiver);
                enqueueCommand(startScanCommand);
            } else {
                Log.d(LOG_TAG_BT, "Device don't support scanning");
            }
        } else {
            Log.d(LOG_TAG_BT, "Drop BT command : inactive communication : ");
        }
    }

    public static void stopScanning() {
        expectingMeasurement = false;

        if (mBluetoothGatt != null) {
            AbstractBluetoothLEDevice leDevice = (AbstractBluetoothLEDevice) mSelectedDevice.definition;
            AbstractBluetoothCommand stopScanCommand = leDevice.getStopScanCommand();
            if (stopScanCommand != null) {
                Log.i(LOG_TAG_BT, "Request LE stop scan");
                enqueueCommand(stopScanCommand);
            } else {
                Log.d(LOG_TAG_BT, "Device don't support scanning");
            }
        } else {
            Log.d(LOG_TAG_BT, "Drop BT command : inactive communication : ");
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
        if (isBluetoothLESupported()) {
            stopLE();
        }
    }

    @TargetApi(JELLY_BEAN_MR2)
    private static void stopLE() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
    }

    public static synchronized void selectDevice(final DiscoveredBluetoothDevice aDevice) {
        if (StringUtils.isEmpty(aDevice.address)) {
            Log.i(LOG_TAG_BT, "No device selected");
            return;
        }

        if (mSelectedDevice != null && mSelectedDevice.address.equals(aDevice.address)) {
            Log.d(LOG_TAG_BT, "Ignore selection of the same device");
            return;
        }

        synchronized (mCurrentDevices) {
            if (mConnectingDevice) {
                Log.d(LOG_TAG_BT, "Currently connecting device, ignore request for " + aDevice.address);
                return;
            } else {
                mConnectingDevice = true;
            }
        }

        mSelectedDevice = aDevice;
        BluetoothDevice deviceRef = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(aDevice.address);
        aDevice.device = deviceRef;

        Log.i(LOG_TAG_BT, "Selected " + aDevice.address + " : " + mSelectedDevice + " of type " + aDevice.definition.getDescription());

        if (aDevice.definition instanceof AbstractBluetoothRFCOMMDevice) {
            if (mCommunicationThread != null) {
                mCommunicationThread.cancel();
                try {
                    mCommunicationThread.join();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG_BT, "Interrupted waiting old thread to complete", e);
                }
            }

            mCommunicationThread = new CommDeviceCommunicationThread(aDevice);
            mCommunicationThread.start();
        } else {
            // require newer android to work with LE devices
            if (SDK_INT >= JELLY_BEAN_MR2) {

                // clean up the queue
                mCommandQueue.clear();
                mCommandExecutor = Executors.newSingleThreadExecutor();
                mCommandLock.release();

                // check if we need to connect from scratch or just reconnect to previous device
               /* if (mBluetoothGatt != null *//*&& aDevice.address.equals(mBluetoothGatt.getDevice().getAddress())*//*) {
                    Log.i(LOG_TAG_BT, "Stop LE discovery");
                    stopDiscoverBluetoothLEDevices();
//                    mBluetoothGatt.close();
                }*/
                Log.i(LOG_TAG_BT, "Connecting LE");
                // connect with remote device
                leDataCallback = new MyBluetoothGattCallback();
                try {
                    mBluetoothGatt = deviceRef.connectGatt(mCurrContext, false, leDataCallback);
                    storeConnectedDevice(mSelectedDevice);
                } catch (IllegalArgumentException exception) {
                    Log.i(LOG_TAG_BT, "Device not found " + exception.getMessage());
                }
                updateLeDeviceState(R.string.bt_state_connecting);

            } else {
                Log.i(LOG_TAG_BT, "Unsupported version ");
                UIUtilities.showNotification("Unsupported Android version for BLE");
            }
        }
    }

    public static boolean isPaired() {
        return mCommunicationThread != null && mCommunicationThread.ismPaired();
    }

    public static AbstractBluetoothDevice getSupportedDevice(BluetoothDevice aDevice, List<ParcelUuid> leServices) {
        String name = aDevice.getName();
        if (StringUtils.isNotEmpty(name)) {
            Log.d(LOG_TAG_BT, "Search supported COM device for " + name);
            for (AbstractBluetoothRFCOMMDevice device : SUPPORTED_BLUETOOTH_COM_DEVICES) {
                if (device.isTypeCompatible(aDevice) && device.isNameSupported(name)) {
                    return device;
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(LOG_TAG_BT, "Search supported LE device for name " + name + " and services " + leServices);
            for (AbstractBluetoothLEDevice device : SUPPORTED_BLUETOOTH_LE_DEVICES) {
                if (/*device.isTypeCompatible(aDevice) &&*/
                        ((StringUtils.isNotEmpty(name) && device.isNameSupported(name)) || device.isServiceSupported(leServices))) {
                    return device;
                }
            }
        }
        return null;
    }

    public static List<AbstractBluetoothDevice> getSupportedDevices() {
        List<AbstractBluetoothDevice> devicesByDescription = new ArrayList(SUPPORTED_BLUETOOTH_COM_DEVICES);
        devicesByDescription.addAll(new ArrayList(SUPPORTED_BLUETOOTH_LE_DEVICES));
        Collections.sort(devicesByDescription);
        return devicesByDescription;
    }

    public static boolean isSupported(BluetoothDevice aDevice) {
        return aDevice != null && getSupportedDevice(aDevice, null) != null;
    }

    public static List<DiscoveredBluetoothDevice> getPairedCompatibleDevices() {
        if (mCurrentDevices == null) {
            mCurrentDevices = new ArrayList<>();
            Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
            for (BluetoothDevice d : devices) {
                AbstractBluetoothDevice deviceDefinition = getSupportedDevice(d, null);
                if (deviceDefinition != null) {
                    mCurrentDevices.add(new DiscoveredBluetoothDevice(deviceDefinition, d.getName(), d.getAddress()));
                }
            }
            if (mSelectedDevice != null && !mCurrentDevices.contains(mSelectedDevice)) {
                mCurrentDevices.add(mSelectedDevice);
            }
        }

        return mCurrentDevices;
    }

    public static String getCurrDeviceStatus() {
        if (mSelectedDevice == null) {
            return mCurrContext.getString(R.string.bt_state_unknown);
        }

        if (mLeDeviceState != R.string.bt_state_none) {
            return mCurrContext.getString(mLeDeviceState);
        }

        if (mSelectedDevice != null && mSelectedDevice.definition instanceof AbstractBluetoothRFCOMMDevice) {
            switch (mSelectedDevice.device.getBondState()) {
                case BluetoothDevice.BOND_BONDED:
                    return mCurrContext.getString(R.string.bt_state_bonded);

                case BluetoothDevice.BOND_BONDING:
                    return mCurrContext.getString(R.string.bt_state_bonding);

                case BluetoothDevice.BOND_NONE:
                    return mCurrContext.getString(R.string.bt_state_none);

                default:
                    return mCurrContext.getString(R.string.bt_state_unknown);
            }
        } else {
            return mCurrContext.getString(mLeDeviceState);
        }

    }

    public static String getCurrDeviceStatusLabel(Context aContext) {

        StringBuilder statusText = new StringBuilder();
        statusText.append(getCurrDeviceStatus());
        if (mLeDeviceState == R.string.bt_state_none) {
            statusText.append(" : ");
            statusText.append(BluetoothService.isPaired() ? aContext.getString(R.string.bt_paired) : aContext.getString(R.string.bt_not_paired));
        }
        return statusText.toString();
    }

    public static void updateLeDeviceState(int aStateLabel) {
        Log.i(Constants.LOG_TAG_BT, "Device state now: " + ConfigUtil.getContext().getString(aStateLabel));
        mLeDeviceState = aStateLabel;
        ((Refresheable) mCurrContext).refresh();
    }

    private static boolean isLeDeviceActive() {
        return R.string.bt_state_connected == mLeDeviceState;
    }

    // for the current device
    public static boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        return mSelectedDevice != null && mSelectedDevice.definition.isMeasureSupported(aMeasureType);
    }

    public static void registerListeners(BTActivity btActivity) {
        mCurrContext = btActivity;
        if (mCommunicationThread != null) {
            mCommunicationThread.registerListeners(btActivity);
        }
        mCurrentDevices = null;
        mConnectingDevice = false;
        mIgnoredDevices.clear();
    }

    public static void unregisterListeners(BTActivity btActivity) {
        mCurrContext = btActivity;
        if (mCommunicationThread != null) {
            mCommunicationThread.unregisterListeners(btActivity);
        }
    }


    @TargetApi(18)
    public static void discoverBluetoothLEDevices() {

        if (SDK_INT >= LOLLIPOP) {
            leCallbackLollipop = startLEScanCallbackLollipop();
        } else {
            leCallback = startLEScanCallback();
        }
    }

    @TargetApi(18)
    public static void stopDiscoverBluetoothLEDevices() {
        Log.i(LOG_TAG_BT, "Stop discovery for Bluetooth LE devices");

        if (leCallback != null) {
            BluetoothAdapter.getDefaultAdapter().stopLeScan(leCallback);
            leCallback = null;
        }
        if (leCallbackLollipop != null && SDK_INT >= LOLLIPOP) {
            final BluetoothManager btManager = (BluetoothManager) ConfigUtil.getContext().getSystemService(BLUETOOTH_SERVICE);
            btManager.getAdapter().getBluetoothLeScanner().stopScan(leCallbackLollipop);
            leCallbackLollipop = null;
        }
    }

    @TargetApi(JELLY_BEAN_MR2)
    public static BluetoothAdapter.LeScanCallback startLEScanCallback()  {

        Log.i(LOG_TAG_BT, "Start discovery for Bluetooth LE devices");
        BluetoothAdapter.LeScanCallback callback = (device, rssi, scanRecord) -> handleDeviceDiscovered(device, rssi, null);
        BluetoothAdapter.getDefaultAdapter().startLeScan(leCallback);
        return callback;
    }

    @TargetApi(LOLLIPOP)
    private static ScanCallback startLEScanCallbackLollipop() {
        Log.i(LOG_TAG_BT, "Start discovery for Lollipop+ Bluetooth LE devices");

        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                handleDeviceDiscovered(result.getDevice(), result.getRssi(), result.getScanRecord());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    handleDeviceDiscovered(result.getDevice(), result.getRssi(), result.getScanRecord());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.i(LOG_TAG_BT, "Scan failed: " + errorCode);
                if (2 == errorCode) {
                    // https://developer.android.com/reference/android/bluetooth/le/ScanCallback.html#SCAN_FAILED_APPLICATION_REGISTRATION_FAILED
                    Log.i(LOG_TAG_BT, "Error code 2, bluetooth stack need a restart");
                    UIUtilities.showNotification(R.string.bt_scan_failed);
                }
            }
        };

        final BluetoothManager btManager = (BluetoothManager) ConfigUtil.getContext().getSystemService(BLUETOOTH_SERVICE);
        btManager.getAdapter().getBluetoothLeScanner().startScan(callback);
        return callback;
    }

    private static void handleDeviceDiscovered(BluetoothDevice device, int rssi, ScanRecord aScanRecord) {

        String name = device.getName();
        if (!mIgnoredDevices.contains(device.getAddress()) && !isCurrentDeviceAddress(device.getAddress())
                && (mSelectedDevice == null || (mSelectedDevice != null && !mSelectedDevice.address.equals(device.getAddress())))) { // skip multiple events for the same device

            synchronized (device.getAddress()) {
                Log.d(LOG_TAG_BT, "Discovered: " + name + " : " + device.getAddress());
                AbstractBluetoothDevice deviceSpec = BluetoothService.getSupportedDevice(device, aScanRecord.getServiceUuids());

                if (deviceSpec != null && deviceSpec instanceof AbstractBluetoothLEDevice) {

                    Log.i(LOG_TAG_BT, "Discovered LE device " + rssi + " : " + name + " : " + aScanRecord.getServiceUuids());
                    mCurrentDevices.add(new DiscoveredBluetoothDevice(deviceSpec, name, device.getAddress(), device));
                    ((Refresheable) mCurrContext).refresh();
                } else {
                    Log.i(LOG_TAG_BT, "Discovered unsupported device " + rssi + " : " + name + " : " + device.getAddress() + ", ignoring");
                    mIgnoredDevices.add(device.getAddress());
                }
            }
        } else {
            Log.d(LOG_TAG_BT, "Already known : " + name + " : " + device.getAddress());
        }
    }

    private static boolean isCurrentDeviceAddress(String address) {
        if (CollectionUtils.isNotEmpty(mCurrentDevices)) {
            for (DiscoveredBluetoothDevice device : mCurrentDevices) {
                if (address.equals(device.address)) {
                    return true;
                }
            }
        }
        return false;
    }

    // will run the command in async using synchronized queue
    public static void enqueueCommand(final AbstractBluetoothCommand aCommand){

        if (mBluetoothGatt == null) {
            Log.d(LOG_TAG_BT, "Not connected");
        }

        Log.d(LOG_TAG_BT, "Enqueue command " + aCommand.getClass().getSimpleName());
        synchronized (mCommandQueue) {
            mCommandQueue.add(aCommand);
            mCommandExecutor.execute(() -> {
                Log.i(LOG_TAG_BT, "Execute command");
                mCommandLock.acquireUninterruptibly();

                try {
                    Thread.currentThread().sleep(200);
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG_BT, "interrupted");
                }

                aCommand.execute(mBluetoothGatt);

                if (aCommand.canProceedWithoutAnswer()) {
                    dequeueCommand();
                }

                // schedule again
                if (isLeDeviceActive() && expectingMeasurement && aCommand instanceof PullCharacteristicCommand) {
                    try {
                        sleep(500);
                    } catch (InterruptedException aE) {
                        aE.printStackTrace();
                    }
                    enqueueCommand(aCommand);
                }
            });
        }
    }

    public static void cancelReadCommands() {
        expectingMeasurement = false;
    }

    public static void dequeueCommand(){
        Log.d(LOG_TAG_BT, "Dequeue command");
        if (!mCommandQueue.isEmpty()) {
            mCommandQueue.pop();
        }
        mCommandLock.release();
    }

    @TargetApi(JELLY_BEAN_MR2)
    static class MyBluetoothGattCallback extends BluetoothGattCallback {

        private List<Constants.MeasureTypes> mMeasureTypes = null;
        private List<Constants.Measures> mTargets = null;
        private ResultReceiver mReceiver = null;

        public void awaitMeasures(List<Constants.MeasureTypes> aMeasureTypes, List<Constants.Measures> aTargets, ResultReceiver aReceiver) {
            expectingMeasurement = true;
            // persist expected measurements
            mTargets = aTargets;
            mMeasureTypes = aMeasureTypes;
            mReceiver = aReceiver;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(LOG_TAG_BT, "Connected with " + gatt.getDevice().getAddress());

                boolean flag = mBluetoothGatt.discoverServices();
                if (!flag) {
                    Log.i(LOG_TAG_BT, "Retry get services ");
                    try {
                        Thread.currentThread().sleep(300);
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG_BT, "interrupted");
                    }
                    flag = mBluetoothGatt.discoverServices();
                    Log.i(LOG_TAG_BT, "Services " + flag);
                }
                updateLeDeviceState(R.string.bt_state_connecting);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(LOG_TAG_BT, "LE device disconnected");
                UIUtilities.showDeviceDisconnectedNotification(ConfigUtil.getContext(), mSelectedDevice.definition.getDescription());

                // break the connect loop if there is a problem
                if (gatt.getDevice() != null && gatt.getDevice().getAddress() != null && gatt.getDevice().getAddress().equals(ConfigUtil.getStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_ADDRESS))) {
                    ConfigUtil.removeProperty(ConfigUtil.PROP_CURR_BT_DEVICE_NAME);
                    ConfigUtil.removeProperty(ConfigUtil.PROP_CURR_BT_DEVICE_ADDRESS);
                    ConfigUtil.removeProperty(ConfigUtil.PROP_CURR_BT_DEVICE_DEFINITION);
                    mCurrentDevices.remove(mSelectedDevice);
                    mSelectedDevice = null;
                }

                ((Refresheable) mCurrContext).refresh();

//                updateLeDeviceState(R.string.bt_state_none);
//                if (mBluetoothGatt != null) {
//                    mBluetoothGatt.close();
//                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == GATT_SUCCESS) {
                // notify connected
                Log.i(LOG_TAG_BT, "Got services");
                UIUtilities.showDeviceConnectedNotification(ConfigUtil.getContext(), mSelectedDevice.definition.getDescription());

                // instruct characteristics to notify on change
                AbstractBluetoothLEDevice leDevice = (AbstractBluetoothLEDevice) mSelectedDevice.definition;
                for (BluetoothGattService service : mBluetoothGatt.getServices()) {
                    Log.d(LOG_TAG_BT, "Service " + service.getUuid().toString() + " " + service.getType());

                    if (!DEVELOPMENT_MODE && !leDevice.getServices().contains(service.getUuid())) {
                        Log.d(LOG_TAG_BT, "Ignored");
                        continue;
                    }

                    for (BluetoothGattCharacteristic c : service.getCharacteristics()) {

                        Log.d(LOG_TAG_BT, "Characteristics " + c.getUuid().toString());
                        if (!DEVELOPMENT_MODE && !leDevice.getCharacteristics().contains(c.getUuid())) {
                            Log.d(LOG_TAG_BT, "Ignored");
                            continue;
                        }

                        if (leDevice.needCharacteristicIndication() || leDevice.needCharacteristicNotification()) {
                            enqueueCommand(new EnableNotificationCommand(c));
                        }
                        if (leDevice.needCharacteristicPull() && leDevice.getCharacteristics().contains(c.getUuid())) {
                            Log.i(LOG_TAG_BT, "Request current characteristic data");
                            enqueueCommand(new PullCharacteristicCommand(c, leDevice));
                        }


                        for (BluetoothGattDescriptor descriptor : c.getDescriptors()) {

                            Log.d(LOG_TAG_BT, "Descriptor " + descriptor.getUuid().toString());
                            if (!DEVELOPMENT_MODE && !leDevice.getDescriptors().contains(descriptor.getUuid())) {
                                Log.d(LOG_TAG_BT, "Ignored");
                                continue;
                            }

                            if (leDevice.needCharacteristicIndication() || leDevice.needCharacteristicNotification()) {
                                enqueueCommand(new WriteDescriptorCommand(descriptor, leDevice));
                            }
                        }
                    }
                }

                stopDiscoverBluetoothLEDevices();
                updateLeDeviceState(R.string.bt_state_connected);
            } else {
                Log.i(LOG_TAG_BT, "Got services error, retry in a while");
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG_BT, "Interrupted: ", e);
                }
                gatt.discoverServices();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(LOG_TAG_BT, "onCharacteristicChanged " + characteristic.getUuid());

            try {
                if (mReceiver != null) {

                    AbstractBluetoothLEDevice leDevice = (AbstractBluetoothLEDevice) mSelectedDevice.definition;
                    if (leDevice.getCharacteristics().contains(characteristic.getUuid())) {

                        Log.d(LOG_TAG_BT, "processing " + characteristic.getUuid());
                        // decode
                        List<Measure> measures = (leDevice.characteristicToMeasures(characteristic, mMeasureTypes));

                        // acknowledge
                        AbstractBluetoothCommand ackCommand = leDevice.getAcknowledgeCommand(characteristic);
                        if (ackCommand != null) {
                            enqueueCommand(ackCommand);
                        }

                        // consume
                        if (measures != null) {
                            for (Measure measure : measures) {
                                sendMeasureToUI(measure);
                            }
                        }
                    } else {
                        Log.i(LOG_TAG_BT, "Ignore characteristic update: " + characteristic.getUuid() + " : " + new String(characteristic.getValue()));
                    }
                } else {
                    Log.d(LOG_TAG_BT, "No receiver");
                }
            } catch (DataException e) {
                Log.e(LOG_TAG_BT, "Fail to read data: ", e);
                UIUtilities.showNotification(e.getMessage());
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            Log.i(LOG_TAG_BT, "Got descriptor " + descriptor.getUuid() + " back with status: " + status);
            dequeueCommand();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (GATT_SUCCESS == status) {
                onCharacteristicChanged(gatt, characteristic);
            }
        }

        private void sendMeasureToUI(Measure aMeasure) {
            if (aMeasure != null && expectingMeasurement) {
                // consume
                Bundle b = new Bundle();
                b.putFloatArray(Constants.MEASURE_VALUE_KEY,  new float[] {aMeasure.getValue()});
                b.putStringArray(Constants.MEASURE_TYPE_KEY, new String[] {aMeasure.getMeasureType().toString()});
                b.putStringArray(Constants.MEASURE_UNIT_KEY, new String[]{aMeasure.getMeasureUnit().toString()});
                b.putStringArray(Constants.MEASURE_TARGET_KEY, new String[] {mTargets.get(mMeasureTypes.indexOf(aMeasure.getMeasureType())).toString()});
                mReceiver.send(Activity.RESULT_OK, b);
            }
        }

    }

    public static void storeConnectedDevice(DiscoveredBluetoothDevice aDevice) {

        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_NAME, aDevice.name);
        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_ADDRESS, aDevice.address);
        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_DEFINITION, aDevice.definition.getClass().getName());
        mConnectingDevice = false;
    }

}

