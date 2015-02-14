package com.astoev.cave.survey.service.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.util.ByteUtils;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 11/22/13
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectThread extends Thread {

    private static final int REC_MODE_PLUS = 9;
    private static final int REC_MODE_MINUS = 10;
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private AbstractBluetoothDevice mDeviceSpec;
    private InputStream mIn;
    private OutputStream mOut = null;
    private boolean running = true;
    private ResultReceiver mReceiver = null;
    private List<Constants.MeasureTypes> mMeasureTypes = null;
    private List<Constants.Measures> mTargets = null;


    public ConnectThread(BluetoothDevice aDevice, AbstractBluetoothDevice aDeviceSpec) throws IOException {
        mDevice = aDevice;
        mDeviceSpec = aDeviceSpec;

        // prepare to read/write
        if (mDeviceSpec.isPassiveBTConnection()) {
            Log.i(Constants.LOG_TAG_BT, "Prepare client passive connection");
            mSocket = createSocketApi10Plus();
            mSocket.connect();
            mIn = mSocket.getInputStream();
        } else {
            Log.i(Constants.LOG_TAG_BT, "Prepare client active connection");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
                mSocket = mDevice.createRfcommSocketToServiceRecord(mDeviceSpec.getSPPUUID());
                if (!mDeviceSpec.isPassiveBTConnection()) {
                    mSocket.connect();
                    mIn = mSocket.getInputStream();
                    mOut = mSocket.getOutputStream();
                }
            } else {
                mSocket = createSocketApi10Plus();
                mSocket.connect();
                mIn = mSocket.getInputStream();
                mOut = mSocket.getOutputStream();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private BluetoothSocket createSocketApi10Plus() throws IOException {
        return mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceSpec.getSPPUUID());
    }

    @Override
    public void run() {
        Log.i(Constants.LOG_TAG_BT, "Start client for " + mDeviceSpec.getDescription());

        byte[] buffer = new byte[1024];

        if (!mDeviceSpec.isPassiveBTConnection()) {
            Log.i(Constants.LOG_TAG_BT, "Trigger measures");
            try {
                mDeviceSpec.triggerMeasures(mOut, mMeasureTypes);
            } catch (IOException e) {
                Log.e(Constants.LOG_TAG_BT, "Error triggering measure", e);
                Bundle b = new Bundle();
                b.putString("error", "Failed to talk to device");
                mReceiver.send(Activity.RESULT_CANCELED, b);
                return;
            }
        }

        Log.i(Constants.LOG_TAG_BT, "Start reading ");
        int numBytes;
        ByteArrayOutputStream message = new ByteArrayOutputStream();

        while (running) {

            try {

                numBytes = mIn.read(buffer);
                if (numBytes > 0 || message.size() > 0) {


                    Log.d(Constants.LOG_TAG_BT, "Got bytes " + new String(ByteUtils.copyBytes(buffer, numBytes)));

                    message.write(buffer, 0, numBytes);

                    if (mIn.available() > 0 || !new String(ByteUtils.copyBytes(buffer, numBytes)).endsWith("\n")) {
                        Log.d(Constants.LOG_TAG_BT, "has more chars " + mIn.available() + " current " + message.size());

                        continue;
                    } else {

                        List<Measure> measures = mDeviceSpec.decodeMeasure(message.toByteArray(), mMeasureTypes);
                        message = new ByteArrayOutputStream();

                        if (measures != null && measures.size() > 0) {
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
            } catch (DataException de) {
                Bundle b = new Bundle();
                b.putString("error", de.getMessage());
                mReceiver.send(Activity.RESULT_CANCELED, b);
                break;
            } catch (Exception connectException) {
                Log.e(Constants.LOG_TAG_BT, "Error client connect", connectException);
                break;
            }
            try {
                sleep(100);
            } catch (InterruptedException e) {
                Log.e(Constants.LOG_TAG_BT, "Error client sleep", e);
            }
        }

        Log.i(Constants.LOG_TAG_BT, "End client");
    }

    public void cancel() {
        try {
            Log.i(Constants.LOG_TAG_BT, "Cancel client");
            running = false;
            if (mSocket != null) {
                mSocket.close();
            }
            IOUtils.closeQuietly(mIn);
            IOUtils.closeQuietly(mOut);
        } catch (IOException e) {
            Log.i(Constants.LOG_TAG_BT, "Error cancel client");
        }
    }

    public void setReceiver(ResultReceiver aReceiver) {
        this.mReceiver = aReceiver;
    }

    public void setMeasureTypes(List<Constants.MeasureTypes> aMeasureTypes) {
        this.mMeasureTypes = aMeasureTypes;
    }

    public void setTargets(List<Constants.Measures> aTargets) {
        mTargets = aTargets;
    }
}
