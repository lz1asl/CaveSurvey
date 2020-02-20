package com.astoev.cave.survey.service.bluetooth;

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
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.util.Pair;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.activity.main.Refresheable;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.AbstractBluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.LeicaDistoBluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.StanleyBluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.MileseeyP7BluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.MileseeyT7BluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.Mileseeyd5tBluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.mileseey.SuaokiP7BluetoothLeDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.CEMILDMBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.DistoXBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.LaserAceBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.LeicaDistoD3aBtBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.TruPulse360BluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschGLM100CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschGLM50CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschPLR30CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschPLR40CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.BoschPLR50CBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.lecommands.AbstractBluetoothCommand;
import com.astoev.cave.survey.service.bluetooth.lecommands.EnableNotificationCommand;
import com.astoev.cave.survey.service.bluetooth.lecommands.PullCharacteristicCommand;
import com.astoev.cave.survey.service.bluetooth.lecommands.WriteDescriptorCommand;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.content.Context.BLUETOOTH_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.astoev.cave.survey.Constants.LOG_TAG_BT;
import static java.lang.Thread.sleep;

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
        SUPPORTED_BLUETOOTH_COM_DEVICES.add(new DistoXBluetoothDevice());
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
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new StanleyBluetoothLeDevice());
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new SuaokiP7BluetoothLeDevice());
    }

    // generic
    private static BluetoothDevice mSelectedDevice = null;
    private static AbstractBluetoothDevice mSelectedDeviceSpec = null;
    private static Activity mCurrContext = null;

    // COMM specific
    private static CommDeviceCommunicationThread mCommunicationThread = null;

    // LE specific
    private static BluetoothAdapter.LeScanCallback leCallback = null; // older LE callback
    private static ScanCallback leCallbackLollipop = null; // newer LE callback
    private static BluetoothGatt mBluetoothGatt = null;
    private static BluetoothDevice mLastLEDevice = null;
    private static MyBluetoothGattCallback leDataCallback = null;
    private static int mLeDeviceState = R.string.bt_state_none;

    // needed by le command queueing, details and credit http://www.brendanwhelan.net/2015/bluetooth-command-queuing-for-android
    private static LinkedList<AbstractBluetoothCommand> mCommandQueue = new LinkedList<AbstractBluetoothCommand>();
    private static Executor mCommandExecutor = Executors.newSingleThreadExecutor();
    private static Semaphore mCommandLock = new Semaphore(1,true);
    private static boolean expectingMeasurement = false;

    // compile time switch to allow processing of all device characteristics
    private static final boolean DEVELOPMENT_MODE = false;

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

        // communication
        if (mCommunicationThread != null) {

            Log.i(LOG_TAG_BT, "Send read command for " + aMeasure);
            mCommunicationThread.awaitMeasures(measureTypes, measureTargets, receiver);
        } else if (mBluetoothGatt != null) {

            Log.i(LOG_TAG_BT, "Request LE read " + aMeasure);
            leDataCallback.awaitMeasures(measureTypes, measureTargets, receiver);

            AbstractBluetoothLEDevice leDevice = (AbstractBluetoothLEDevice) mSelectedDeviceSpec;
            if (leDevice.needCharacteristicPull() && SDK_INT >= JELLY_BEAN_MR2) {
                Log.i(LOG_TAG_BT, "Request LE pull");
                Constants.MeasureTypes type = getMeasureTypeFromTarget(aMeasure);
                BluetoothGattCharacteristic c = mBluetoothGatt.getService(leDevice.getService(type)).getCharacteristic(leDevice.getCharacteristic(type));
                enqueueCommand(new PullCharacteristicCommand(c, leDevice));
            }

        } else {
            Log.d(LOG_TAG_BT, "Drop BT command : inactive communication : " + aMeasure);
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

    public static synchronized void selectDevice(final String aDeviceAddress) {
        if (StringUtils.isEmpty(aDeviceAddress)) {
            Log.i(LOG_TAG_BT, "No device selected");
            return;
        }
        mSelectedDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(aDeviceAddress);
        mSelectedDeviceSpec = getSupportedDevice(mSelectedDevice);
        if (mSelectedDeviceSpec == null) {
            Log.i(LOG_TAG_BT, "No spec found");
            return;
        }

        Log.i(LOG_TAG_BT, "Selected " + aDeviceAddress + " : " + mSelectedDevice + " of type " + mSelectedDeviceSpec.getDescription());

        if (mSelectedDeviceSpec instanceof AbstractBluetoothRFCOMMDevice) {
            if (mCommunicationThread != null) {
                mCommunicationThread.cancel();
                try {
                    mCommunicationThread.join();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG_BT, "Interrupted waiting old thread to complete", e);
                }
            }

            mCommunicationThread = new CommDeviceCommunicationThread(mSelectedDevice, (AbstractBluetoothRFCOMMDevice) mSelectedDeviceSpec);
            mCommunicationThread.start();
        } else {
            // require newer android to work with LE devices
            if (SDK_INT >= JELLY_BEAN_MR2) {

                // clean up the queue
                mCommandQueue.clear();
                mCommandExecutor = Executors.newSingleThreadExecutor();
                mCommandLock.release();

                // check if we need to connect from scratch or just reconnect to previous device
                if (mBluetoothGatt != null) {// && aDeviceAddress.equals(mBluetoothGatt.getDevice().getAddress())) {
                    Log.i(LOG_TAG_BT, "Reset LE");
                    mBluetoothGatt.close();
                }
                Log.i(LOG_TAG_BT, "Connecting LE");
                // connect with remote device
                leDataCallback = new MyBluetoothGattCallback();
                mBluetoothGatt = mSelectedDevice.connectGatt(mCurrContext, false, leDataCallback);
                updateLeDeviceState(R.string.bt_state_connecting);
            } else {
                Log.i(LOG_TAG_BT, "Unsupported version ");
                UIUtilities.showNotification("Unsupported Android version for BLE");
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

    public static AbstractBluetoothDevice getSupportedDevice(BluetoothDevice aDevice) {
        Log.d(LOG_TAG_BT, "Search supported device for " + aDevice.getName());
        for (AbstractBluetoothRFCOMMDevice device : SUPPORTED_BLUETOOTH_COM_DEVICES) {
            if (device.isTypeCompatible(aDevice) && device.isNameSupported(aDevice.getName())) {
                return device;
            }
        }
        for (AbstractBluetoothLEDevice device : SUPPORTED_BLUETOOTH_LE_DEVICES) {
            if (device.isTypeCompatible(aDevice) && device.isNameSupported(aDevice.getName())) {
                return device;
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
        return aDevice != null && getSupportedDevice(aDevice) != null;
    }

    public static Set<Pair<String, String>> getPairedCompatibleDevices() {
        Set<Pair<String, String>> result = new HashSet<>();
        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for (BluetoothDevice d : devices) {
            if (isSupported(d)) {
                result.add(new Pair<>(d.getName(), d.getAddress()));
            }
        }
        if (mLastLEDevice != null) {
            Pair<String, String> lastLeDevice = new Pair<String, String>(mLastLEDevice.getName(), mLastLEDevice.getAddress());
            if (!devices.contains(lastLeDevice)) {
                result.add(lastLeDevice);
            }
        }
        return result;
    }

    public static String getCurrDeviceStatus() {
        if (mSelectedDevice == null && mLastLEDevice == null) {
            return mCurrContext.getString(R.string.bt_state_unknown);
        }

        if (mLeDeviceState != R.string.bt_state_none) {
            return mCurrContext.getString(mLeDeviceState);
        }

        if (mSelectedDeviceSpec instanceof AbstractBluetoothRFCOMMDevice) {
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
        BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                handleDeviceDiscovered(device, rssi);
            }
        };
        BluetoothAdapter.getDefaultAdapter().startLeScan(leCallback);
        return callback;
    }

    @TargetApi(LOLLIPOP)
    private static ScanCallback startLEScanCallbackLollipop() {
        Log.i(LOG_TAG_BT, "Start discovery for Lollipop+ Bluetooth LE devices");

        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                handleDeviceDiscovered(result.getDevice(), result.getRssi());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    handleDeviceDiscovered(result.getDevice(), result.getRssi());
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

    private static void handleDeviceDiscovered(BluetoothDevice device, int rssi) {
        Log.d(LOG_TAG_BT, "Discovered: " + device.getName());
        AbstractBluetoothDevice deviceParent = BluetoothService.getSupportedDevice(device);

        if (deviceParent != null && deviceParent instanceof AbstractBluetoothLEDevice) {
            AbstractBluetoothLEDevice deviceSpec = (AbstractBluetoothLEDevice) deviceParent;

            Log.i(LOG_TAG_BT, "Discovered LE device " + rssi + " : " + device.getName());

            mLastLEDevice = device;
            ((Refresheable) mCurrContext).refresh();
        } else {
            Log.i(LOG_TAG_BT, "Discovered unsupported LE device " + rssi + " : " + device.getName());
        }
    }

    // will run the command in async using synchronized queue
    public static void enqueueCommand(final AbstractBluetoothCommand aCommand){

        Log.d(LOG_TAG_BT, "Enqueue command " + aCommand.getClass().getSimpleName());
        synchronized (mCommandQueue) {
            mCommandQueue.add(aCommand);
            mCommandExecutor.execute(new Runnable() {
                @Override
                public void run() {
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
                }
            });
        }
    }

    public static void cancelReadCommands() {
        expectingMeasurement = false;
    }

    @TargetApi(GINGERBREAD)
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
                Log.i(LOG_TAG_BT, "Connected with " + gatt.getDevice().getName());

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
                UIUtilities.showDeviceDisconnectedNotification(ConfigUtil.getContext(), mSelectedDeviceSpec.getDescription());

                updateLeDeviceState(R.string.bt_state_none);
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
                UIUtilities.showDeviceConnectedNotification(ConfigUtil.getContext(), mSelectedDeviceSpec.getDescription());

                // instruct characteristics to notify on change
                AbstractBluetoothLEDevice leDevice = (AbstractBluetoothLEDevice) mSelectedDeviceSpec;
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
                    Log.d(LOG_TAG_BT, "processing " + characteristic.getUuid());
                    // decode
                    Measure measure = ((AbstractBluetoothLEDevice) mSelectedDeviceSpec).characteristicToMeasure(characteristic, mMeasureTypes);

                    // consume
                    sendMeasureToUI(measure);
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
}