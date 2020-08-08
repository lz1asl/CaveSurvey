package com.astoev.cave.survey.service.bluetooth.device.ble.mileseey;

/**
 * Created by astoev on 8/08/20.
 */

public class HerschLEM50BluetoothLeDevice extends AbstractMileseeyBluetoothLeDevice {

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Laser Distance Meter");
    }

    @Override
    public String getDescription() {
        return "Hersch LEM50";
    }
}
