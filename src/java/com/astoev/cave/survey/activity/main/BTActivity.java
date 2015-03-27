package com.astoev.cave.survey.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.bluetooth.BluetoothService;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
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
public class BTActivity extends MainMenuActivity {

    List<Pair<String, String>> devices = new ArrayList<Pair<String, String>>();

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

            Spinner devicesChooser = (Spinner) findViewById(R.id.bt_devices);
            devicesChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectDevice();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            refreshDevicesList();

            displaySupportedDevices();

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed during create", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void displaySupportedDevices() {
        TextView devicesLabel = (TextView) findViewById(R.id.bt_supproted_devices);

        StringBuilder devicesList = new StringBuilder();
        for (AbstractBluetoothDevice device : BluetoothService.getSupportedDevices()) {
            if (devicesList.length() > 0) {
                devicesList.append(", ");
            }
            devicesList.append(device.getDescription());
        }

        devicesLabel.setText(getString(R.string.bt_supported_devices, devicesList.toString()));
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

    private void refreshDevicesList() {

        devices = BluetoothService.getPairedCompatibleDevices();

        String selectedDeviceName = null;
        String selectedBtDeviceAddress = ConfigUtil.getStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_ADDRESS);
        if (StringUtils.isNotEmpty(selectedBtDeviceAddress)) {
            String selectedBtDeviceName = ConfigUtil.getStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_NAME);
            selectedDeviceName = buildDeviceName(new Pair<String, String>(selectedBtDeviceName, selectedBtDeviceAddress));
        }

        List<String> devicesList = new ArrayList<String>();
        int index = 0;
        int selectedDeviceIndex = -1;
        String tempName;
        for (final Pair<String, String> device : devices) {
            tempName = buildDeviceName(device);
            devicesList.add(tempName);
            if (tempName.equals(selectedDeviceName)) {
                selectedDeviceIndex = index;
            }
            index++;
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devicesList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner devicesChooser = (Spinner) findViewById(R.id.bt_devices);
        devicesChooser.setAdapter(spinnerArrayAdapter);
        if (selectedDeviceIndex >= 0) {
            devicesChooser.setSelection(selectedDeviceIndex);
        }

        updateDeviceStatus();

    }

    private void updateDeviceStatus() {
        // display status
        TextView status = (TextView) findViewById(R.id.bt_status);
        status.setText(BluetoothService.getCurrDeviceStatusLabel(this));
    }

    public void pairNewDevice() {
        Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentOpenBluetoothSettings);
    }

    private String buildDeviceName(Pair<String, String> aDevice) {
        return aDevice.first + " : " + aDevice.second;
    }

    public void selectDevice() {
        // get selected
        Spinner devicesChooser = (Spinner) findViewById(R.id.bt_devices);
        Pair<String, String> device = devices.get(devicesChooser.getSelectedItemPosition());
        Log.i(Constants.LOG_TAG_UI, "Try to use " + device.first + ":" + device.second);

        // store & propagate
//        BluetoothService.selectDevice(device.second); // TODO
        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_NAME, device.first);
        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_ADDRESS, device.second);

    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
     */
    @Override
    protected int getChildsOptionsMenu() {
        return R.menu.btmenu;
    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.bt_new: {
                pairNewDevice();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
