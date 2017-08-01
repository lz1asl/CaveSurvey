package com.astoev.cave.survey.test.service.bluetooth.util;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.Mileseeyd5tBluetoothLeDevice;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by astoev on 8/1/17.
 */

public class MileseeyD5ProtocolTest extends AbstractDeviceProtocolTest {

    @Test
    public void testDataPacket() throws IOException {

        byte[] message = "1.2m".getBytes();
        ensureSucces(message, 1.2f, null, null);
        message = "0.123m".getBytes();
        ensureSucces(message, 0.123f, null, null);
    }

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new Mileseeyd5tBluetoothLeDevice();
    }
}
