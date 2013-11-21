package com.astoev.cave.survey.activity.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.BaseActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 10/23/13
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class BTActivity extends BaseActivity {

    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    final List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    BluetoothDevice device = null;
    BTClientThread mClientThread;

    private static byte[] hexStringToByte(String aMessage) {
        int i = aMessage.length() / 2;
        byte[] arrayOfByte = new byte[i];
        char[] arrayOfChar = aMessage.toCharArray();
        for (int j = 0; j < i; j++) {
            int k = j * 2;
            arrayOfByte[j] = ((byte) (toByte(arrayOfChar[k]) << 4 | toByte(arrayOfChar[(k + 1)])));
        }
        return arrayOfByte;
    }

    private static byte toByte(char paramChar) {
        return (byte) "0123456789ABCDEF".indexOf(paramChar);
    }

    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.bluetooth);

            // BT disabled?
//            if (!BluetoothService.askBluetoothOn(this)) {
            if (true) {
                Log.i(Constants.LOG_TAG_UI, "BT disabled");
                UIUtilities.showNotification(this, R.string.bt_not_on);
                finish();
                return;
            }

            listenBTEvents();

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed during create", e);
            UIUtilities.showNotification(BTActivity.this, R.string.error);
        }
    }

    private void listenBTEvents() {

        getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    UIUtilities.showNotification(BTActivity.this, R.string.bt_paired);
                    Button toggle = (Button) findViewById(R.id.bt_toggle_pair);
                    toggle.setText(R.string.bt_disconnect);
                    Log.i(Constants.LOG_TAG_UI, "Paired with " + device);
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed during pair", e);
                    UIUtilities.showNotification(BTActivity.this, R.string.error);
                }
            }
        },
                new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    UIUtilities.showNotification(BTActivity.this, R.string.bt_disconnect);
                    Log.i(Constants.LOG_TAG_UI, "Disconnected");
                    Button toggle = (Button) findViewById(R.id.bt_toggle_pair);
                    /*toggle.setText(R.string.bt_pair);
                    device = null;
                    if (mClientThread != null) {
                        mClientThread.cancel();
                    }*/
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed during disconnect", e);
                    UIUtilities.showNotification(BTActivity.this, R.string.error);
                }
            }
        },
                new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

    }

    public void togglePair(View aView) {
        if (device == null) {
            Log.i(Constants.LOG_TAG_UI, "Pair");
            Spinner devicesChooser = (Spinner) findViewById(R.id.bt_devices);
            device = devices.get(devicesChooser.getSelectedItemPosition());
            Log.i(Constants.LOG_TAG_UI, "Try connect to " + device.getName() + ":" + device.getAddress());

            Log.i(Constants.LOG_TAG_UI, "Communication started");

            new Thread() {
                public void run() {
//
//   for (int i = 0; i < 50; i++) {
                    int i=0;
                        try {
                            Thread.currentThread().sleep(3000);
                            Log.i(Constants.LOG_TAG_UI, "Test " + i);
                            mClientThread = new BTClientThread(device, i < 2);
                            mClientThread.sendMessage(getMessage());
                            mClientThread.start();
                        } catch (Exception e) {
                            Log.e(Constants.LOG_TAG_UI, i + " failed", e);
//                            if (mClientThread != null) {
//                                mClientThread.cancel();
//                            }
                        }

                    }
//                }
            }.start();


        } else {


            Log.i(Constants.LOG_TAG_UI, "Disconnect ");
            mClientThread.cancel();
            device = null;

        }
    }

    private byte[] getMessage() {
        // read single measure
        return hexStringToByte("D5F0E00D");
    }

    public void searchDevices(View aView) {

        try {
            Log.i(Constants.LOG_TAG_UI, "Searching devices");

            final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    try {

                        String action = intent.getAction();
                        // When discovery finds a device
                        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            Log.i("BlueTooth Testing", device.getName() + "\n" + device.getAddress());
                            if (!devices.contains(device)) {
                                devices.add(device);
                            }
                            refreshDevicesList();
                        }
                    } catch (Exception e) {
                        Log.e(Constants.LOG_TAG_UI, "Failed during receive", e);
                        UIUtilities.showNotification(BTActivity.this, R.string.error);
                    }
                }
            };

//            String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
//            startActivityForResult(new Intent(aDiscoverable), 0);
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed during search", e);
            UIUtilities.showNotification(BTActivity.this, R.string.error);
        }
    }

    private void refreshDevicesList() {
        Spinner devicesChooser = (Spinner) findViewById(R.id.bt_devices);

        List<String> devicesList = new ArrayList<String>();
        for (final BluetoothDevice device : devices) {
            devicesList.add(device.getName() + " : " + device.getAddress());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devicesList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devicesChooser.setAdapter(spinnerArrayAdapter);

        Button toggle = (Button) findViewById(R.id.bt_toggle_pair);
        if (device != null || devices.size() > 0) {
            toggle.setEnabled(true);
        } else {
            toggle.setEnabled(false);
        }
    }

    class BTClientThread extends Thread {

        private BluetoothSocket mSocket;
        private BluetoothDevice mDevice;
        private InputStream mIn;
        private OutputStream mOut;
        private boolean running = true;

        public BTClientThread(BluetoothDevice device, boolean pairFlag) throws IOException {
            mDevice = device;

//            try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }

            Log.i(Constants.LOG_TAG_UI, "Prepare client");
                if (Build.VERSION.SDK_INT < 10) {
                    mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                } else {
                    mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                }
                if (pairFlag) {
                    mSocket.connect();
                }
                mIn = mSocket.getInputStream();
                mOut = mSocket.getOutputStream();


        }

        public void sendMessage(byte[] aMessage) throws IOException {
                mOut.write(aMessage);
        }

        @Override
        public void run() {
            Log.i(Constants.LOG_TAG_UI, "Start client");

            byte[] buffer = new byte[1024];
            Log.i(Constants.LOG_TAG_UI, "Start reading ");
//            while (running) {
            try {
                int i = mIn.read(buffer);
//                    if (i == -1) {
//                        // nothing from the stream
//                        break;
//                    }

                Log.i(Constants.LOG_TAG_UI, "Got bytes " + i);
                sleep(100);

            } catch (Exception connectException) {
                Log.e(Constants.LOG_TAG_UI, "Error client connect", connectException);
            }
//            }

            Log.i(Constants.LOG_TAG_UI, "End client");
        }

        public void cancel() {
            try {
                Log.i(Constants.LOG_TAG_UI, "Cancel client");
                if (mSocket != null) {
                    if (mSocket.isConnected()) {
                        mSocket.close();
                    }
                }
                IOUtils.closeQuietly(mIn);
                IOUtils.closeQuietly(mOut);
            } catch (IOException e) {
                Log.i(Constants.LOG_TAG_UI, "Error cancel client");
            }
        }
    }

}
