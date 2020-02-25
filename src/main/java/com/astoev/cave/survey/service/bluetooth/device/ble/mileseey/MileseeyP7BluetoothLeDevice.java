package com.astoev.cave.survey.service.bluetooth.device.ble.mileseey;

/**
 *  Mileseey P7
 */

public class MileseeyP7BluetoothLeDevice extends AbstractMileseeyBluetoothLeDevice {

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Mileseey P7");
    }
    @Override
    public String getDescription() {
        return "Mileseey P7";
    }
}

