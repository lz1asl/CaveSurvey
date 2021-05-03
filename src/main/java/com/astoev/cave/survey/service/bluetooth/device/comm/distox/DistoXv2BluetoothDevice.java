package com.astoev.cave.survey.service.bluetooth.device.comm.distox;

public class DistoXv2BluetoothDevice extends AbstractDistoXBluetoothDevice {

    @Override
    public boolean isNameSupported(String aName) {
        // DistoX-xxxx
        return hasLength(aName, 11) && deviceNameStartsWith(aName, "DistoX-");
    }

    @Override
    public String getDescription() {
        return "DistoX v2";
    }
}
