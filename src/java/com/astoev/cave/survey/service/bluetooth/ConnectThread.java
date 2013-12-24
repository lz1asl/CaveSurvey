package com.astoev.cave.survey.service.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import com.astoev.cave.survey.Constants;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 11/22/13
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectThread extends Thread {

    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final int REC_MODE_PLUS = 9;
    private static final int REC_MODE_MINUS = 10;
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private InputStream mIn;
    private OutputStream mOut;
    private boolean running = true;
    private ResultReceiver mReceiver = null;
    private Constants.Measures mMeasure = null;


    public ConnectThread(BluetoothDevice device) throws IOException {
        mDevice = device;

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        Log.i(Constants.LOG_TAG_UI, "Prepare client");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
            mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
        } else {
//            mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
        	mSocket = createSocketApi10Plus();
        }
        mSocket.connect();
        mIn = mSocket.getInputStream();
        mOut = mSocket.getOutputStream();
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private BluetoothSocket createSocketApi10Plus() throws IOException{
    	return mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
    }

    public void sendMessage(byte[] aMessage) throws IOException {
        mOut.write(aMessage);
    }

    @Override
    public void run() {
        Log.i(Constants.LOG_TAG_UI, "Start client");

        byte[] buffer = new byte[1024];
        Log.i(Constants.LOG_TAG_UI, "Start reading ");
        while (running) {
            try {
                int i = mIn.read(buffer);


                if (i < 25) {
                    Log.i(Constants.LOG_TAG_UI, "Got bytes " + i);
                    break;
                }

                if (buffer[24] != 13) {
                    Log.i(Constants.LOG_TAG_UI, "Data validation failed ");
                    break;
                }

                if (buffer[4] != 0) {
                    Log.i(Constants.LOG_TAG_UI, "error code" + buffer[4]);
                    break;
                }
                Log.i(Constants.LOG_TAG_UI, "rec mode" + buffer[5]);
                // 2 from bottom of the devise, 1 from top of the devise
//                Log.i(Constants.LOG_TAG_UI, "measure location" + buffer[6]);

                Log.d(Constants.LOG_TAG_UI, "units " + new String[]{" ", "m", "in", "in+", "ft", "ft&in"}[buffer[7]]);
                if (1 != buffer[7]) {
                    Log.i(Constants.LOG_TAG_UI, "Please measure in meters!");
                    break;
                }

                for (int j = 0; j < 4; j++) {
                    float measure = (0xFF000000 & buffer[(8 + j * 4)] << 24
                            | 0xFF0000 & buffer[(9 + j * 4)] << 16
                            | 0xFF00 & buffer[(10 + j * 4)] << 8
                            | 0xFF & buffer[(11 + j * 4)]);
                    if (j == 0 && measure > -26843545) {
                        Log.i(Constants.LOG_TAG_UI, "Got angle " + measure / 10);
                        Bundle b = new Bundle();
                        b.putFloat("result", measure / 10);
                        b.putString("type", Constants.Measures.angle.toString());
                        mReceiver.send(Activity.RESULT_OK, b);
                    }

                    if (j == 2 && measure > -26843545) {
                        Log.i(Constants.LOG_TAG_UI, "Got distance " + measure / 1000);
                        Bundle b = new Bundle();
                        b.putFloat("result", measure / 1000);
                        b.putString("type", mMeasure.toString());
                        mReceiver.send(Activity.RESULT_OK, b);
                    }
                }

                sleep(20);

            } catch (Exception connectException) {
                Log.e(Constants.LOG_TAG_UI, "Error client connect", connectException);
                break;
            }
            try {
                sleep(100);
            } catch (InterruptedException e) {
                Log.e(Constants.LOG_TAG_UI, "Error client sleep", e);
            }
        }

        Log.i(Constants.LOG_TAG_UI, "End client");
    }

    public void cancel() {
        try {
            Log.i(Constants.LOG_TAG_UI, "Cancel client");
            running = false;
            if (mSocket != null) {
//                if (mSocket.isConnected()) {
                mSocket.close();
//                }
            }
            IOUtils.closeQuietly(mIn);
            IOUtils.closeQuietly(mOut);
        } catch (IOException e) {
            Log.i(Constants.LOG_TAG_UI, "Error cancel client");
        }
    }

    public void setReceiver(ResultReceiver aReceiver) {
        this.mReceiver = aReceiver;
    }

    public void setMeasure(Constants.Measures measure) {
        this.mMeasure = measure;
    }
}
