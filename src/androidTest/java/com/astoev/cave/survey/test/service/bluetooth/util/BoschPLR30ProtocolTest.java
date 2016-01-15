package com.astoev.cave.survey.test.service.bluetooth.util;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.BoschPLR30CBluetoothDevice;

import java.io.IOException;

/**
 * Created by astoev on 12/24/15.
 */
public abstract class BoschPLR30ProtocolTest extends AbstractDeviceProtocolTest {

    //@Test
    public void testDataPacket() throws IOException {

        // TODO fix me
        byte[] message = new byte[] {1, -5, 93, -5, 113, 51, -52, 105};
        ensureSucces(message, 24.059f, 160.285f, -72.845f);
    }

    @Override
    protected AbstractBluetoothRFCOMMDevice getDeviceSpec() {
        return new BoschPLR30CBluetoothDevice();
    }
}
