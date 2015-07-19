package com.astoev.cave.survey.service.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.util.ByteUtils;
import com.astoev.cave.survey.util.ConfigUtil;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 11/22/13
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectThread extends Thread {

    private static final int KEEP_ALIVE_INTERVAL = 1000 * 60; // 1 minute

    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private AbstractBluetoothDevice mDeviceSpec;
    private InputStream mIn;
    private OutputStream mOut = null;
    private boolean running = true;
    private long lastActiveTimestamp;
    private ResultReceiver mReceiver = null;
    private List<Constants.MeasureTypes> mMeasureTypes = null;
    private List<Constants.Measures> mTargets = null;
    private boolean mPaired = false;
    private static List<BroadcastReceiver> mRegisteredReceivers = new ArrayList<BroadcastReceiver>();



    public ConnectThread(BluetoothDevice aDevice, AbstractBluetoothDevice aDeviceSpec) {
        mDevice = aDevice;
        mDeviceSpec = aDeviceSpec;

        registerListeners(ConfigUtil.getContext());
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private BluetoothSocket createSocketApi10Plus() throws IOException {
        return mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceSpec.getSPPUUID());
    }

    private BroadcastReceiver mConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!BluetoothService.isSupported(device)) {
                    // ignore other devices
                    Log.i(Constants.LOG_TAG_BT, "Bonded unsupported device");
                    return;
                }

                UIUtilities.showNotification(R.string.bt_paired);
                Log.i(Constants.LOG_TAG_BT, "Paired with " + device.getName());
                mPaired = true;
                mDevice = device;
                mDeviceSpec = BluetoothService.getSupportedDevice(device.getName());

                TextView status = (TextView) ConfigUtil.getContext().findViewById(R.id.bt_status);
                status.setText(BluetoothService.getCurrDeviceStatusLabel(ConfigUtil.getContext()));

            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_BT, "Failed during pair", e);
                UIUtilities.showNotification(R.string.error);
            }
        }
    };
    private BroadcastReceiver mDisconnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (mDevice == null) {
                    // ignore event if don't expect to be paired with device
                    Log.i(Constants.LOG_TAG_BT, "Ignore disconnect, no curr device");
                    return;
                }

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!BluetoothService.isSupported(device)) {
                    // ignore other devices
                    Log.i(Constants.LOG_TAG_BT, "Ignore disconnect, device not supported");
                    return;
                }

                if (mDevice.getName().equals(device.getName()) && mDevice.getAddress().equals(device.getAddress())) {
                    mDevice = null;
                    mDeviceSpec = null;
                    mPaired = false;
                    UIUtilities.showNotification(R.string.bt_not_paired);
                    Log.i(Constants.LOG_TAG_BT, "Disconnected");

                    running = false;

                    TextView status = (TextView) ConfigUtil.getContext().findViewById(R.id.bt_status);
                    status.setText(BluetoothService.getCurrDeviceStatusLabel(ConfigUtil.getContext()));
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

    public void registerListeners(final Activity aContext) {

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

    public void unregisterListeners(final Activity aContext) {

        if (mRegisteredReceivers.contains(mConnectedReceiver)) {
            mRegisteredReceivers.remove(mConnectedReceiver);
            aContext.unregisterReceiver(mConnectedReceiver);
        }

        if (mRegisteredReceivers.contains(mDisconnectedReceiver)) {
            mRegisteredReceivers.remove(mDisconnectedReceiver);
            aContext.unregisterReceiver(mDisconnectedReceiver);
        }
    }

    @Override
    public void run() {

        // prepare to read/write
        Log.i(Constants.LOG_TAG_BT, "Start communication thread for " + mDeviceSpec.getDescription());

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
                mSocket = mDevice.createRfcommSocketToServiceRecord(mDeviceSpec.getSPPUUID());
            } else {
                mSocket = createSocketApi10Plus();
            }

            mSocket.connect();
            mIn = mSocket.getInputStream();
            mOut = mSocket.getOutputStream();

            mDeviceSpec.configure(mIn, mOut);

            Log.i(Constants.LOG_TAG_BT, "Device found!");
            UIUtilities.showNotification(R.string.bt_connected);
            UIUtilities.showDeviceConnectedNotification(ConfigUtil.getContext(), mDeviceSpec.getDescription());
            lastActiveTimestamp = System.currentTimeMillis();

            ByteArrayOutputStream message = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            while (running) {

                try {
                    if (mReceiver != null) {
                        // start send/receive cycle

                        Log.i(Constants.LOG_TAG_BT, "Trigger measures");
                        try {
                            mDeviceSpec.triggerMeasures(mOut, mMeasureTypes);
                        } catch (IOException e) {
                            Log.e(Constants.LOG_TAG_BT, "Error triggering measure", e);
                            Bundle b = new Bundle();
                            b.putString("error", "Failed to talk to device");
                            mReceiver.send(Activity.RESULT_CANCELED, b);
                            cancel();
                        }

                        Log.i(Constants.LOG_TAG_BT, "Start reading ");
                        int numBytes = mIn.read(buffer);

                        if (numBytes > 0 || message.size() > 0) {

                            Log.d(Constants.LOG_TAG_BT, "Got bytes " + new String(ByteUtils.copyBytes(buffer, numBytes)));
                            message.write(buffer, 0, numBytes);
                            lastActiveTimestamp = System.currentTimeMillis();

                            if (!mDeviceSpec.isFullPacketAvailable(message.toByteArray())) {
                                // expect more data
                                Log.d(Constants.LOG_TAG_BT, "Expect more chars " + mIn.available() + " current " + message.size());
                                continue;
                            } else {

                                // acknowledge received
                                mDeviceSpec.ack(mOut, message.toByteArray());

                                // process the data
                                Log.i(Constants.LOG_TAG_BT, "Decoding message");
                                List<Measure> measures = mDeviceSpec.decodeMeasure(message.toByteArray(), mMeasureTypes);

                                // reset the buffers for the next message
                                message = new ByteArrayOutputStream();
                                buffer = new byte[1024];

                                if (measures != null && measures.size() > 0) {

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
                                    mReceiver.send(Activity.RESULT_OK, b);
                                }
                            }
                        }

                        sleep(20);

                    } else {
                        if (lastActiveTimestamp + KEEP_ALIVE_INTERVAL < System.currentTimeMillis()) {
                            // no active task and more than a minute passive - wake up the device if supported
                            Log.i(Constants.LOG_TAG_BT, "Send keep alive if supported");
                            mDeviceSpec.keepAlive(mOut, mIn);
                            lastActiveTimestamp = System.currentTimeMillis();
                        }
                    }

                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        Log.e(Constants.LOG_TAG_BT, "Error client sleep", e);
                    }

                } catch (DataException de) {
                    Log.e(Constants.LOG_TAG_BT, "Data exception: " + de.getMessage(), de);
                    // reset messasge in case of error
                    message = new ByteArrayOutputStream();
                    buffer = new byte[1024];

                    Bundle b = new Bundle();
                    b.putString("error", de.getMessage());
                    mReceiver.send(Activity.RESULT_CANCELED, b);
                    mReceiver = null;

                    continue;
                }

            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_BT, "Error client connect", e);
            cancel();
        }

        Log.i(Constants.LOG_TAG_BT, "End client");
    }

    public void cancel() {


        if (mDeviceSpec != null) {
            // display notification only if expected device to be lost
            UIUtilities.showDeviceDisconnectedNotification(ConfigUtil.getContext(), mDeviceSpec.getDescription());
        }

        try {
            Log.i(Constants.LOG_TAG_BT, "Cancel client");
            running = false;
            IOUtils.closeQuietly(mIn);
            IOUtils.closeQuietly(mOut);
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            Log.i(Constants.LOG_TAG_BT, "Error cancel client");
        }

        for (BroadcastReceiver r : mRegisteredReceivers) {
            try {
                ConfigUtil.getContext().unregisterReceiver(r);
            } catch (Exception e) {
                // ignore
            }
        }
        mRegisteredReceivers.clear();
    }

    public void awaitMeasures(List<Constants.MeasureTypes> aMeasureTypes, List<Constants.Measures> aTargets, ResultReceiver aReceiver) {
        mTargets = aTargets;
        mMeasureTypes = aMeasureTypes;
        mReceiver = aReceiver;
    }

    public boolean ismPaired() {
        return mPaired;
    }
}
