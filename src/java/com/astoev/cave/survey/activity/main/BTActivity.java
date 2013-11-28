package com.astoev.cave.survey.activity.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 10/23/13
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class BTActivity extends BaseActivity {


    final List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    BluetoothDevice device = null;

    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.bluetooth);

            // BT disabled?
            if (!BluetoothService.askBluetoothOn(this)) {
//            if (true) {
                Log.i(Constants.LOG_TAG_UI, "BT disabled");
                UIUtilities.showNotification(this, R.string.bt_not_on);
                finish();
                return;
            }

            BluetoothService.prepare(this);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed during create", e);
            UIUtilities.showNotification(BTActivity.this, R.string.error);
        }
    }

    public void togglePair(View aView) {
        if (device == null) {
            Log.i(Constants.LOG_TAG_UI, "Pair");
            Spinner devicesChooser = (Spinner) findViewById(R.id.bt_devices);
            device = devices.get(devicesChooser.getSelectedItemPosition());
            Log.i(Constants.LOG_TAG_UI, "Try to use " + device.getName() + ":" + device.getAddress());
            BluetoothService.selectDevice(device);

        } else {
            Log.i(Constants.LOG_TAG_UI, "Disconnect ");
            BluetoothService.disconnect();
        }
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


}
