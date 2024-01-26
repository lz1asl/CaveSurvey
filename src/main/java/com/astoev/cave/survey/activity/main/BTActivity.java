package com.astoev.cave.survey.activity.main;

import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.widget.AdapterView.INVALID_POSITION;
import static com.astoev.cave.survey.Constants.LOG_TAG_UI;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
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

        resetBT(this);

        if (ConfigUtil.getBooleanProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT)) {
            float adjustment = ConfigUtil.getFloatProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT_VALUE);
            UIUtilities.showAlertDialog(this, R.string.title_warning, R.string.measurements_adjustment_warning, adjustment);
        }

        prepareUI();
    }

    private void resetBT(BTActivity aSavedInstanceState) {
        Log.i(LOG_TAG_UI, "BT reset");

        BluetoothService.restart();
        BluetoothService.registerListeners(aSavedInstanceState);
        refreshDevicesList();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothService.discoverBluetoothLEDevices();
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
            devicesChooser.setSelected(false);
            devicesChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (INVALID_POSITION != position) {
                            DiscoveredBluetoothDevice device = devices.get(position);
                            Log.i(LOG_TAG_UI, "Try to use " + device.getDisplayName());

                            BluetoothService.selectDevice(device);
                        }
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
                if (SDK_INT < JELLY_BEAN_MR2 && DEVICE_TYPE_CLASSIC == device.getDeviceType()) {
                    deviceLabel.setPaintFlags(deviceLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                devicesList.addView(deviceLabel);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        resetBT(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        resetBT(this);
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

            case R.id.bt_refresh: {
                resetBT(this);
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
        runOnUiThread(() -> refreshDevicesList());
    }
}
