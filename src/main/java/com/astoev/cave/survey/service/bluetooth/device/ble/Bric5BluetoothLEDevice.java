package com.astoev.cave.survey.service.bluetooth.device.ble;

import com.astoev.cave.survey.service.bluetooth.device.ble.bric4.Bric4BluetoothLEDevice;

/**
 * BRIC5, same protocol as BRIC4 except the Bluetooth name.
 */

public class Bric5BluetoothLEDevice extends Bric4BluetoothLEDevice {


    @Override
    public boolean isNameSupported(String aName) {
        // 'BRIC5_XXXX'
        return deviceNameStartsWith(aName, "BRIC5_");
    }

    @Override
    public String getDescription() {
        return "BRIC5";
    }

}
