package com.astoev.cave.survey.service.bluetooth.device.ble.mileseey;

/**
 * Created by astoev on 8/26/17.
 */

public class MileseeyT7BluetoothLeDevice extends AbstractMileseeyBluetoothLeDevice {

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Mileseey T7");
    }

    @Override
    public String getDescription() {
        return "Mileseey T7";
    }
}
