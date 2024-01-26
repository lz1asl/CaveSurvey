package com.astoev.cave.survey.test.service.bluetooth.ledevice;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.Bric5BluetoothLEDevice;

public class Bric5LeDeviceProtocolTest extends Bric4LeDeviceProtocolTest {

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new Bric5BluetoothLEDevice();
    }
}
