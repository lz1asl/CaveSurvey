package com.astoev.cave.survey.activity.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.BaseActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.bluetooth.BluetoothService;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.StringUtils;

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


    final List<Pair<String, String>> devices = new ArrayList<Pair<String, String>>();

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {

                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.i("Bluetooth new device ", device.getName() + "\n" + device.getAddress());
                    Pair<String, String> newDevice = new Pair(device.getName(), device.getAddress());
                    if (!devices.contains(newDevice)) {
                        devices.add(newDevice);
                    }
                    refreshDevicesList();
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_UI, "Failed during receive", e);
                UIUtilities.showNotification(R.string.error);
            }
        }
    };

    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);

        prepareUI();
        BluetoothService.registerListeners(this);
    }

    private void prepareUI() {
        try {
            // BT disabled?
            if (!BluetoothService.askBluetoothOn(this)) {
                Log.i(Constants.LOG_TAG_UI, "BT disabled");
                UIUtilities.showNotification(R.string.bt_not_on);
                finish();
                return;
            }

            String selectedBtDeviceAddress = ConfigUtil.getStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_ADDRESS);
            if (StringUtils.isNotEmpty(selectedBtDeviceAddress)) {
                String selectedBtDeviceName = ConfigUtil.getStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_NAME);
                Pair<String, String> newDevice = new Pair(selectedBtDeviceName, selectedBtDeviceAddress);
                if (!devices.contains(newDevice)) {
                    devices.add(newDevice);
                    refreshDevicesList();
                    if (BluetoothService.isPaired()) {
                        Button searchButton = (Button) findViewById(R.id.bt_search);
                        searchButton.setEnabled(false);
                    }
                }
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed during create", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        prepareUI();
        BluetoothService.registerListeners(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        prepareUI();
        BluetoothService.registerListeners(this);
    }
    
    /**
	 * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
	 */
	@Override
	protected String getScreenTitle() {
		return getString(R.string.bt_devices);
	}

	public void togglePair(View aView) {
        Button toggle = (Button) aView.findViewById(R.id.bt_toggle_pair);
        toggle.setEnabled(false);

        // stop BT discovery and events
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        safeUnregisterReceiver();

        // get selected
        Log.i(Constants.LOG_TAG_UI, "Pair");
        Spinner devicesChooser = (Spinner) findViewById(R.id.bt_devices);
        Pair<String, String> device = devices.get(devicesChooser.getSelectedItemPosition());
        Log.i(Constants.LOG_TAG_UI, "Try to use " + device.first + ":" + device.second);

        // store & propagate
        BluetoothService.selectDevice(device.second);
        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_NAME, device.first);
        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_ADDRESS, device.second);

        // no need to stay here more
//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
    }

    public void searchDevices(View aView) {

        try {
            Log.i(Constants.LOG_TAG_UI, "Searching devices");
            if (!BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                registerReceiver(mReceiver, filter);
                BluetoothAdapter.getDefaultAdapter().startDiscovery();
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed during search", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void refreshDevicesList() {
        Spinner devicesChooser = (Spinner) findViewById(R.id.bt_devices);

        List<String> devicesList = new ArrayList<String>();
        for (final Pair<String, String> device : devices) {
            devicesList.add(buildDeviceName(device));
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devicesList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devicesChooser.setAdapter(spinnerArrayAdapter);

        Button toggle = (Button) findViewById(R.id.bt_toggle_pair);
        if (!BluetoothService.isPaired() && devices.size() > 0) {
            toggle.setEnabled(true);
        } else {
            toggle.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        safeUnregisterReceiver();
        super.onBackPressed();
    }

    private void safeUnregisterReceiver() {
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            // ignore, might already been unregistered
        }
    }

    private String buildDeviceName(Pair<String, String> aDevice) {
        return aDevice.first + " : " + aDevice.second;
    }

}
