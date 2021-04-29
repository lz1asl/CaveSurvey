package com.astoev.cave.survey.service.bluetooth.device.ble.mileseey;

/**
 * Created by astoev on 27/04/21.
 */

public class MileseeydM120LeDevice extends AbstractMileseeyBluetoothLeDevice {


    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameEquals(aName, "M120");
    }

    @Override
    public String getDescription() {
        return "Mileseey M120";
    }

}
