package com.astoev.cave.survey.test.service.bluetooth.util;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.DistoXBluetoothDevice;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by astoev on 7/19/15.
 */
public class DistoXProtocolTest extends AbstractDeviceProtocolTest {

    @Test
    public void testDataPacket() throws IOException {

        byte[] message = new byte[] {1, -5, 93, -5, 113, 51, -52, 105};
        ensureSucces(message, 24.059f, 160.285f, -72.845f);
    }

    @Override
    protected AbstractBluetoothRFCOMMDevice getDeviceSpec() {
        return new DistoXBluetoothDevice();
    }
}
