package com.astoev.cave.survey.activity.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.astoev.cave.survey.service.bluetooth.BluetoothService;
import org.apache.commons.io.IOUtils;

import java.io.DataInputStream;
import java.io.InputStream;
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

    final List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    boolean btRunning = true;
    BluetoothDevice mPairedDevice;

    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.bluetooth);

            if (!BluetoothService.askBluetoothOn(this)) {
                UIUtilities.showNotification(this, R.string.bt_not_on);
                finish();
                return;
            }

            Button pairButton = (Button) findViewById(R.id.bt_toggle_pair);
            pairButton.setText(R.string.bt_pair);
            pairButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.i(Constants.LOG_TAG_UI, "Toggle pair");
                    // Cancel discovery because it's costly and we're about to connect
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                    mBluetoothAdapter.cancelDiscovery();

                    getApplicationContext().registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            try {
                                UIUtilities.showNotification(BTActivity.this, R.string.bt_paired);
                                Button toggle = (Button) findViewById(R.id.bt_toggle_pair);
                                toggle.setText(R.string.bt_disconnect);

                                Bundle extras = intent.getExtras();
                                for (String k : extras.keySet()) {
                                    Log.i(Constants.LOG_TAG_UI, "BT Extra: "+ extras.get(k).toString());
                                }

                                btRunning = true;

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
                                Button toggle = (Button) findViewById(R.id.bt_toggle_pair);
                                toggle.setText(R.string.bt_pair);
                                btRunning = false;
                            } catch (Exception e) {
                                Log.e(Constants.LOG_TAG_UI, "Failed during disconnect", e);
                                UIUtilities.showNotification(BTActivity.this, R.string.error);
                            }
                        }
                    },
                            new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));


                    startCommunication();
                }
            });
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed during create", e);
            UIUtilities.showNotification(BTActivity.this, R.string.error);
        }
    }

    private void startCommunication() {
                        new Thread(){
                    public void run() {
                        try {

                            Log.i(Constants.LOG_TAG_UI, "Connect thread");


                            while(!btRunning) {
                                Log.i(Constants.LOG_TAG_UI, "wait");

                                sleep(5000);
                            }

                            Log.i(Constants.LOG_TAG_UI, "Try connect");
                            Spinner devicesChooser = (Spinner) findViewById(R.id.bt_devices);
                            BluetoothDevice device = devices.get(devicesChooser.getSelectedItemPosition());
                            BluetoothSocket socket = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress())
                                    .createRfcommSocketToServiceRecord(UUID.randomUUID());

//                                BluetoothServerSocket socket = mBluetoothAdapter.device.createRfcommSocketToServiceRecord(MY_UUID);

                            socket.connect();

                            Log.i(Constants.LOG_TAG_UI, "Connected: " + socket.isConnected());
                            if (!socket.isConnected()) {
                                Log.i(Constants.LOG_TAG_UI, "Failed to connect ");
                                return;
                            }
                            InputStream in = null;
                            try {
                                in = socket.getInputStream();
                                DataInputStream data = new DataInputStream(in);
                                while (btRunning) {
                                    Thread.sleep(2000);
                                    Log.i(Constants.LOG_TAG_UI, "|" + data.readChar() + "|");
                                }
                            } finally {
                                IOUtils.closeQuietly(in);
                                socket.close();
                            }
                            Log.i(Constants.LOG_TAG_UI, "End read");

                        }
                        catch (Exception e) {
                            Log.e(Constants.LOG_TAG_UI, "Failed during connect", e);
                            UIUtilities.showNotification(BTActivity.this, "Connect failed");
                        }

                        Log.i(Constants.LOG_TAG_UI, "End read thread");
                    }
                }.start();

    }

    public void searchDevices(View aView) {
//        ListView lv1 = (ListView) findViewById(R.id.myListView1);

        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


            final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    try {

                        String action = intent.getAction();
                        // When discovery finds a device
                        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                            // Get the BluetoothDevice object from the Intent
                            BluetoothDevice device = intent.getParcelableExtra(
                                    BluetoothDevice.EXTRA_DEVICE);
                            Log.i("BlueTooth Testing", device.getName() + "\n"
                                    + device.getAddress());
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

            String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
            startActivityForResult(new Intent(aDiscoverable), 0);
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            mBluetoothAdapter.startDiscovery();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed during search", e);
            UIUtilities.showNotification(BTActivity.this, R.string.error);
        }
    }

    public void disconnect(View aView) {
        btRunning = false;
        UIUtilities.showNotification(this, R.string.todo);
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
        if (devices.size() > 0) {

            toggle.setEnabled(true);
        } else {
            toggle.setEnabled(false);
        }
    }

}
