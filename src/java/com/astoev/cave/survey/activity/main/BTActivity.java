package com.astoev.cave.survey.activity.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
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
public class BTActivity extends Activity {

    final List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    boolean btRunning = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);


        if (!BluetoothService.askBluetoothOn(this)) {
            UIUtilities.showNotification(this, R.string.bt_not_on);
            finish();
            return;
        }
    }

    public void searchDevices(View aView) {
//        ListView lv1 = (ListView) findViewById(R.id.myListView1);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
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
            }
        };

        String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
        startActivityForResult(new Intent(aDiscoverable), 0);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();
    }

    public void disconnect(View aView) {
        btRunning = false;
        UIUtilities.showNotification(this, R.string.todo);
    }

    private void refreshDevicesList() {
        TableLayout table = (TableLayout) findViewById(R.id.btDevices);

        table.removeAllViews();
        for (final BluetoothDevice device : devices) {

            TableRow row = new TableRow(BTActivity.this);
            TextView label = new TextView(BTActivity.this);
            label.setText(device.getName() + " : " + device.getAddress());
            row.addView(label);

            Button pairButton = new Button(this);
            pairButton.setText(R.string.bt_pair);
            pairButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Cancel discovery because it's costly and we're about to connect
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBluetoothAdapter.cancelDiscovery();

                    getApplicationContext().registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            UIUtilities.showNotification(BTActivity.this, R.string.bt_paired);
                        }
                    },
                            new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
                    getApplicationContext().registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            UIUtilities.showNotification(BTActivity.this, R.string.bt_disconnect);
                        }
                    },
                            new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

                    new Thread(){
                        public void run() {
                            try {
                                UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//                                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                                BluetoothServerSocket socket = mBluetoothAdapter.device.createRfcommSocketToServiceRecord(MY_UUID);

                                socket.connect();

                                Log.i(Constants.LOG_TAG_SERVICE, "Connected: " + socket.isConnected());
                                InputStream in = null;
                                try {
                                    in = socket.getInputStream();
                                    DataInputStream data = new DataInputStream(in);
                                    while (btRunning) {
                                        Thread.sleep(5);
                                        Log.i(Constants.LOG_TAG_SERVICE, "|" + data.readChar() + "|");
                                    }
                                } finally {
                                    IOUtils.closeQuietly(in);
                                    socket.close();
                                }

                            } catch (Exception e) {
                                UIUtilities.showNotification(BTActivity.this, "Connect failed");
                            }

                            Log.i(Constants.LOG_TAG_SERVICE, "End read thread");

                        }
                    }.start();

                }
            });
            row.addView(pairButton);
            table.addView(row);
        }
    }
}
