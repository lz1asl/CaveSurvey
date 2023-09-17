package com.astoev.cave.survey.activity.main;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static com.astoev.cave.survey.Constants.LOG_TAG_UI;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.bluetooth.BluetoothService;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.DiscoveredBluetoothDevice;
import com.astoev.cave.survey.util.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 10/23/13
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class BTActivity extends MainMenuActivity implements Refresheable {

    List<DiscoveredBluetoothDevice> devices = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);

        BluetoothService.registerListeners(this);
        prepareUI();

        if (ConfigUtil.getBooleanProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT)) {
            float adjustment = ConfigUtil.getFloatProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT_VALUE);
            UIUtilities.showAlertDialog(this, R.string.title_warning, R.string.measurements_adjustment_warning, adjustment);
        }
    }

    private void prepareUI() {
        try {
            // BT disabled?
            if (!BluetoothService.askBluetoothOn(this)) {
                Log.i(LOG_TAG_UI, "BT disabled");
                UIUtilities.showNotification(R.string.bt_not_on);
                finish();
                return;
            }

            Spinner devicesChooser = findViewById(R.id.bt_devices);
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
            Log.e(LOG_TAG_UI, "Failed during create", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    public void showDevicesHelp(View aView) {
        Log.d(LOG_TAG_UI, "Displaying the devices help");
        Intent i = new Intent(Intent.ACTION_VIEW);
        String url  = getString(R.string.bt_devices_help_url);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void displaySupportedDevices() {
        LinearLayout devicesList = findViewById(R.id.bt_container);

        if (devicesList.getChildCount() <= 3) { // don't duplicate

            TextView preparing = findViewById(R.id.bt_preparing);
            preparing.setVisibility(View.INVISIBLE);

            for (AbstractBluetoothDevice device : BluetoothService.getSupportedDevices()) {
                TextView deviceLabel = new TextView(getApplicationContext());
                deviceLabel.setText(String.format("\t\u2022 %s", device.getDescription()));
                deviceLabel.setTextColor(Color.WHITE);
                devicesList.addView(deviceLabel);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BluetoothService.registerListeners(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothService.discoverBluetoothLEDevices();
        }

        prepareUI();
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
        String selectedBtDeviceAddress = ConfigUtil.getStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_ADDRESS);

        List<String> devicesList = new ArrayList<>();
        int index = 0;
        int selectedDeviceIndex = -1;
        for (final DiscoveredBluetoothDevice device : devices) {
            devicesList.add(device.getDisplayName());
            if (device.address.equals(selectedBtDeviceAddress)) {
                selectedDeviceIndex = index;
            }
            index++;
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, devicesList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner devicesChooser = findViewById(R.id.bt_devices);
        devicesChooser.setAdapter(spinnerArrayAdapter);
        if (selectedDeviceIndex >= 0) {
            devicesChooser.setSelection(selectedDeviceIndex);
        }

        updateDeviceStatus();
    }

    private void updateDeviceStatus() {
        // display status
        TextView status = findViewById(R.id.bt_status);
        status.setText(BluetoothService.getCurrDeviceStatusLabel(this));
    }

    public void pairNewDevice() {
        Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentOpenBluetoothSettings);
    }

    public void selectDevice() {
        // get selected
        Spinner devicesChooser = findViewById(R.id.bt_devices);
        DiscoveredBluetoothDevice device = devices.get(devicesChooser.getSelectedItemPosition());
        Log.i(LOG_TAG_UI, "Try to use " + device.getDisplayName());

        UIUtilities.showNotification(R.string.bt_device_connecting, device.getDisplayName());

        // store & propagate
        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_NAME, device.name);
        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_ADDRESS, device.address);
        ConfigUtil.setStringProperty(ConfigUtil.PROP_CURR_BT_DEVICE_DEFINITION, device.definition.getClass().getName());
        BluetoothService.selectDevice(device);
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

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        BluetoothService.unregisterListeners(this);
        if (BluetoothService.isBluetoothLESupported()) {
            BluetoothService.stopDiscoverBluetoothLEDevices();
        }
        super.onPause();
    }

    @Override
    public void refresh() {
        runOnUiThread(() -> updateDeviceStatus());
    }
}
