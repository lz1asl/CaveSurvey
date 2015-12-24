package com.astoev.cave.survey.test.service.bluetooth.util;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.CEMILDMBluetoothDevice;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by astoev on 8/10/15.
 */
public class CEMProtocolTest extends AbstractDeviceProtocolTest {

    @Test
    public void testDataPacket() throws IOException {

        // distance read
        ensureSucces(new byte[] {-43, -16, 0, 20, 0, 1, 2, 1, 0, 0, 4, -26, 0, 0, 4, -12, 0, 0, 28, -58, -17, -1, -1, -2, 13 }, 7.366f, null, null);
        ensureSucces(new byte[] {-43, -16, 0, 20, 0, 1, 2, 1, 0, 0, 32, -4, 0, 0, 34, -126, 0, 0, 38, 34, -17, -1, -1, -2, 13 }, 9.762f, null, null);

        // slope read
        ensureSucces(new byte[] {-43, -16, 0, 20, 0, 8, 2, 1, -1, -1, -1, -68, 0, 0, 0, 120, 0, 0, 3, -18, 0, 0, 3, -10, 13 }, null, null, -6.8f);
        ensureSucces(new byte[] {-43, -16, 0, 20, 0, 8, 2, 1, -1, -1, -1, -50, -17, -1, -1, -2, -17, -1, -1, -2, -17, -1, -1, -2, 13 }, null, null, -5.0f);
        ensureSucces(new byte[] {-43, -16, 0, 20, 0, 8, 2, 1, 0, 0, 0, 29, -17, -1, -1, -2, -17, -1, -1, -2, -17, -1, -1, -2, 13 }, null, null, 2.9f);
    }

    @Override
    protected AbstractBluetoothRFCOMMDevice getDeviceSpec() {
        return new CEMILDMBluetoothDevice();
    }
}
