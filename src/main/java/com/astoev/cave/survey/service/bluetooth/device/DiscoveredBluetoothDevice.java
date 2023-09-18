package com.astoev.cave.survey.service.bluetooth.device;

import android.bluetooth.BluetoothDevice;

public class DiscoveredBluetoothDevice {

    public AbstractBluetoothDevice definition;
    public String name;
    public String address;
    public BluetoothDevice device;

    public DiscoveredBluetoothDevice(AbstractBluetoothDevice aDefinition, String aName, String aAddress) {
        definition = aDefinition;
        name = aName;
        address = aAddress;
    }

    public String getDisplayName() {
        if (name != null) {
            return name + " : " + address;
        } else {
            return definition.getDescription() + " : " + address;
        }
    }
}
