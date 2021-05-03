package com.astoev.cave.survey.service.bluetooth.device.comm.distox;

public class DistoXv1BluetoothDevice extends AbstractDistoXBluetoothDevice {

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameEquals(aName, "DistoX");
    }

    @Override
    public String getDescription() {
        return "DistoX";
    }
}
