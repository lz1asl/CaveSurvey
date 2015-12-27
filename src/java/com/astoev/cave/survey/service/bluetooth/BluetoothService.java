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
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.BoschPLR40CBluetoothLEDevice;
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
import java.util.UUID;

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

        // LE devices
        SUPPORTED_BLUETOOTH_LE_DEVICES.add(new BoschPLR40CBluetoothLEDevice());
    }

    // generic
    private static BluetoothDevice mSelectedDevice = null;
    private static AbstractBluetoothDevice mSelectedDeviceSpec = null;
    private static Activity mCurrContext = null;

    // COMM specific
    private static CommDeviceCommunicationThread mCommunicationThread = null;

    // LE specific
    private static BluetoothAdapter.LeScanCallback leCallback = null;
    private static BluetoothGatt mBluetoothGatt = null;
    private static BluetoothDevice mLastLEDevice = null;
    private static MyBluetoothGattCallback leDataCallback = null;


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

        // measurements requested
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

            Log.i(Constants.LOG_TAG_BT, "Send read command for " + aMeasure);
            mCommunicationThread.awaitMeasures(measureTypes, measureTargets, receiver);
        } else if (mBluetoothGatt != null) {

            Log.i(Constants.LOG_TAG_BT, "Request LE read " + aMeasure);
            leDataCallback.awaitMeasures(measureTypes, measureTargets, receiver);
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
        if (isBluetoothLESupported()) {
            stopLE();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static void stopLE() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
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
                    Log.i(Constants.LOG_TAG_BT, "Connecting LE");
                    // just reconnect
                    if (!mBluetoothGatt.connect()) {
                        Log.e(Constants.LOG_TAG_BT, "Failed to connect");
                    }
                    ;
                } else {
                    Log.i(Constants.LOG_TAG_BT, "Re-connecting LE");
                    // connect with remote device
                    leDataCallback = new MyBluetoothGattCallback();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mSelectedDevice.createBond();
                    }
                    mBluetoothGatt = mSelectedDevice.connectGatt(mCurrContext, false, leDataCallback);
                }

//                TODO ping periodically to detect device gone

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

    public static AbstractBluetoothDevice getSupportedDevice(String aDeviceName) {
        for (AbstractBluetoothRFCOMMDevice device : SUPPORTED_BLUETOOTH_COM_DEVICES) {
            if (device.isNameSupported(aDeviceName)) {
                return device;
            }
        }
        for (AbstractBluetoothLEDevice device : SUPPORTED_BLUETOOTH_LE_DEVICES) {
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
        List<AbstractBluetoothDevice> devicesByDescription = new ArrayList(SUPPORTED_BLUETOOTH_COM_DEVICES);
        devicesByDescription.addAll(new ArrayList(SUPPORTED_BLUETOOTH_LE_DEVICES));
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
        if (mLastLEDevice != null) {
            result.add(new Pair<String, String>(mLastLEDevice.getName(), mLastLEDevice.getAddress()));
        }
        return result;
    }

    public static String getCurrDeviceStatus() {
        if (mSelectedDevice == null) {
            return ConfigUtil.getContext().getString(R.string.bt_state_unknown);
        }

        // TODO check for mBluetoothGatt

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

        leCallback = getLEScanCallback();
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static BluetoothAdapter.LeScanCallback getLEScanCallback()  {

        return new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

                AbstractBluetoothLEDevice deviceSpec = (AbstractBluetoothLEDevice) BluetoothService.getSupportedDevice(device.getName());
                if (deviceSpec != null) {
                    Log.i(Constants.LOG_TAG_BT, "Discovered " + rssi + " : " + device.getName());
                    mLastLEDevice = device;
                    stopDiscoverBluetoothLEDevices();
                    try {
                        Thread.currentThread().sleep(100);
                    } catch (InterruptedException e) {
                        Log.e(Constants.LOG_TAG_BT, "Interrupted");
                    }
//                    ((Refresheable) mCurrContext).refresh();
                    // TODO
                    selectDevice(device.getAddress());
                }
            }
        };
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    static class MyBluetoothGattCallback extends BluetoothGattCallback {

        private List<Constants.MeasureTypes> mMeasureTypes = null;
        private List<Constants.Measures> mTargets = null;
        private ResultReceiver mReceiver = null;

        public void awaitMeasures(List<Constants.MeasureTypes> aMeasureTypes, List<Constants.Measures> aTargets, ResultReceiver aReceiver) {
            mTargets = aTargets;
            mMeasureTypes = aMeasureTypes;
            mReceiver = aReceiver;

            // TODO all UUIDs below hardcoded, get from the device spec based on the requested measurements
            BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(BoschPLR40CBluetoothLEDevice.SERVICE3));

            if (service == null) {
                Log.i(Constants.LOG_TAG_BT, "No service");

                return;
            }

            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(BoschPLR40CBluetoothLEDevice.SERVICE3_CHARACTERISTIC1));
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BoschPLR40CBluetoothLEDevice.SERVICE3_CHARACTERISTIC1_DESCRIPTOR));
            boolean status = mBluetoothGatt.readDescriptor(descriptor);
            Log.i(Constants.LOG_TAG_BT, "requested desc " + status);

            status = mBluetoothGatt.readCharacteristic(characteristic);
            Log.i(Constants.LOG_TAG_BT, "requested char " + status);

        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(Constants.LOG_TAG_BT, "Connected with " + gatt.getDevice().getName());


//                mBluetoothGatt.readRemoteRssi();

                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(Constants.LOG_TAG_BT, "LE device disconnected");
                UIUtilities.showDeviceDisconnectedNotification(ConfigUtil.getContext(), mSelectedDeviceSpec.getDescription());

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(Constants.LOG_TAG_BT, "Got services");
                UIUtilities.showDeviceConnectedNotification(ConfigUtil.getContext(), mSelectedDeviceSpec.getDescription());

                List<BluetoothGattService> services = mBluetoothGatt.getServices();
                for (BluetoothGattService service : services) {
                    Log.i(Constants.LOG_TAG_BT, "Service " + service.getUuid().toString() + " " + service.getType() + " " + service.toString());
                    List<BluetoothGattCharacteristic> chars = service.getCharacteristics();
                    if (!service.getUuid().equals(UUID.fromString(BoschPLR40CBluetoothLEDevice.SERVICE3))) {
                        continue;
                    }

                    for (BluetoothGattCharacteristic c : chars) {
                        Log.i(Constants.LOG_TAG_BT, "Characteristic " + c.getUuid().toString() + " " + c.getValue());

                        if (c.getUuid().toString().equals(BoschPLR40CBluetoothLEDevice.SERVICE3_CHARACTERISTIC1)) {
                            Log.i(Constants.LOG_TAG_BT, "Enable notifications ");

                            Log.i(Constants.LOG_TAG_BT, "Enable notifications " + gatt.setCharacteristicNotification(c, true));

                            BluetoothGattDescriptor descriptor = c.getDescriptor(UUID.fromString(BoschPLR40CBluetoothLEDevice.SERVICE3_CHARACTERISTIC1_DESCRIPTOR));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            Log.i(Constants.LOG_TAG_BT, "write descriptor " + gatt.writeDescriptor(descriptor));
                        }


                            break;

                        }
                    }
                }
            } else {
                Log.i(Constants.LOG_TAG_BT, "Got services error");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                gatt.discoverServices();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            // we got response regarding our request to fetch characteristic value
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // and it success, so we can get the value
                Log.i(Constants.LOG_TAG_BT, "Got characteristic " + characteristic.getUuid() + " for " + characteristic.getService().getUuid().toString() );
                for (BluetoothGattDescriptor desc : characteristic.getDescriptors()) {
                    Log.i(Constants.LOG_TAG_BT, "Descriptor " + desc.getUuid().toString() + " " + desc.toString());
                }
                Log.i(Constants.LOG_TAG_BT, "Got Value " + String.valueOf(characteristic.getValue()));

                Log.i(Constants.LOG_TAG_BT, "Got Value string " + characteristic.getStringValue(0));

                // try as int
                int index = ((characteristic.getValue()[0] & 0x01) == 1) ? 2 : 1;
                // also we need to define format
                int format = (index == 1) ? BluetoothGattCharacteristic.FORMAT_UINT8 : BluetoothGattCharacteristic.FORMAT_UINT16;
                // now we have everything, get the value
                int intValue = characteristic.getIntValue(format, index);

                Log.i(Constants.LOG_TAG_BT, "Got Value as int" + intValue);


                /*// TODO process and send back
                List<Measure> measures = mDeviceSpec.decodeMeasure(message.toByteArray(), mMeasureTypes);
                // populate the result
                Bundle b = new Bundle();
                float[] valuesArray = new float[measures.size()];
                String[] typesArray = new String[measures.size()];
                String[] unitsArray = new String[measures.size()];
                String[] targetsArray = new String[measures.size()];
                int actualMeasuresCount = 0;

                for (int i = 0; i < measures.size(); i++) {
                    Measure m = measures.get(i);
                    if (!mMeasureTypes.contains(m.getMeasureType())) {
                        continue;
                    }

                    valuesArray[actualMeasuresCount] = m.getValue();
                    typesArray[actualMeasuresCount] = m.getMeasureType().toString();
                    unitsArray[actualMeasuresCount] = m.getMeasureUnit().toString();
                    targetsArray[actualMeasuresCount] = mTargets.get(mMeasureTypes.indexOf(m.getMeasureType())).toString();
                    actualMeasuresCount++;
                }
                b.putFloatArray(Constants.MEASURE_VALUE_KEY, ByteUtils.copyBytes(valuesArray, actualMeasuresCount));
                b.putStringArray(Constants.MEASURE_TYPE_KEY, ByteUtils.copyBytes(typesArray, actualMeasuresCount));
                b.putStringArray(Constants.MEASURE_UNIT_KEY, ByteUtils.copyBytes(unitsArray, actualMeasuresCount));
                b.putStringArray(Constants.MEASURE_TARGET_KEY, ByteUtils.copyBytes(targetsArray, actualMeasuresCount));
                mReceiver.send(Activity.RESULT_OK, b);*/

            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(Constants.LOG_TAG_BT, "----------------------------onDescriptorRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(Constants.LOG_TAG_BT, "----------------------------onCharacteristicChanged");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(Constants.LOG_TAG_BT, "onCharacteristicrWrite");
        }

        ;

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(Constants.LOG_TAG_BT, "RemoteRssi " + rssi);
            }
        }

        ;

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(Constants.LOG_TAG_BT, "onDescriptorWrite");
            if(status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(Constants.LOG_TAG_BT, "Success " + descriptor.getUuid());
            }
            else {
                Log.i(Constants.LOG_TAG_BT, "Failure " + descriptor.getUuid());
            }
        }

        ;
    };
}




