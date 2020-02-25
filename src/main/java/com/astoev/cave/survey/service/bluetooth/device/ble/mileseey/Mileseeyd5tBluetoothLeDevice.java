package com.astoev.cave.survey.service.bluetooth.device.ble.mileseey;

/**
 * Created by astoev on 6/9/17.
 */

public class Mileseeyd5tBluetoothLeDevice extends AbstractMileseeyBluetoothLeDevice {


    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Mileseey D5T");
    }

    @Override
    public String getDescription() {
        return "Mileseey dTAPE 5t";
    }

}
