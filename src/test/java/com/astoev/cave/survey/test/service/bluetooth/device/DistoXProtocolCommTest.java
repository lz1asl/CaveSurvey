package com.astoev.cave.survey.test.service.bluetooth.device;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.astoev.cave.survey.service.bluetooth.device.comm.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.distox.DistoXv1BluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.util.DistoXProtocol;

import org.junit.jupiter.api.Test;

/**
 * Created by astoev on 7/19/15.
 */
public class DistoXProtocolCommTest extends AbstractCommDeviceProtocolTest {

    @Test
    public void testDataPacket() {

        byte[] message = new byte[] {1, -5, 93, -5, 113, 51, -52, 105};
        ensureSucces(message, 24.059f, 160.285f, -72.845f);
    }

    @Test
    public void testAck() {
        byte[] message = new byte[] {1, -5, 93, -5, 113, 51, -52, 105};
        byte[] ack = DistoXProtocol.createAcknowledgementPacket(message);
        assertEquals(1, ack.length);
        assertEquals(85, ack[0]);
    }

    @Override
    protected AbstractBluetoothRFCOMMDevice getDeviceSpec() {
        return new DistoXv1BluetoothDevice();
    }
}
