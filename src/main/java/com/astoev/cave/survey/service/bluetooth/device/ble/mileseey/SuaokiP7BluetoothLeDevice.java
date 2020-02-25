package com.astoev.cave.survey.service.bluetooth.device.ble.mileseey;

/**
 *  Suaoki P7, almost identical to a Mileseey P7 but without the camera
 */

public class SuaokiP7BluetoothLeDevice extends AbstractMileseeyBluetoothLeDevice {

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Suaoki P7");
    }
    @Override
    public String getDescription() {
        return "Suaoki P7";
    }
}

