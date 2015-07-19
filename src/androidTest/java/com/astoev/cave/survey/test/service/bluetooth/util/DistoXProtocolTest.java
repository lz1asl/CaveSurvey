package com.astoev.cave.survey.test.service.bluetooth.util;

import com.astoev.cave.survey.service.bluetooth.device.DistoXBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.TruPulse360BBluetoothDevice;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by astoev on 7/19/15.
 */
public class DistoXProtocolTest extends AbstractDeviceProtocolTest {

    @Test
    public void testDataPacket() throws IOException {

        byte[] message = new byte[] {1, -5, 93, -5, 113, 51, -52, 105};
        ensureDistoXSucces(message, 24.059f, 160.285f, -72.845f);
    }


    private void ensureDistoXSucces(byte[] aMessage, Float aDistance, Float anAzimuth, Float anAngle) throws IOException {
        ensureSucces(aMessage, aDistance, anAzimuth, anAngle, new DistoXBluetoothDevice());
    }

}
