package com.astoev.cave.survey.test.service.bluetooth.ledevice;

import static org.junit.Assert.assertEquals;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.DistoXBleDevice;
import com.astoev.cave.survey.service.bluetooth.device.protocol.DistoXBLEProtocol;

import org.junit.Test;

public class DistoXBleDeviceProtocolTest extends AbstractLeDeviceProtocolTest {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testMeasurementPrimaryCharacteristics() {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(DistoXBleDevice.READ_SERVICE_UUID, 0, 0);

        c.setValue( new byte[] {1, 1, 25, 12, 97, -9, -39, 20, 17, -124, 8, 99, 97, 70, 71, -41, -111});
        ensureSucces(c, 3.097f, 347.9f, 29.3f);

        c.setValue( new byte[] {1, 1, 62, 28, 19, -37, 29, 6, 34, -124, 45, 96, 28, 72, 6, -42, 100});
        ensureSucces(c, 7.23f, 308.1f, 8.6f);

        c.setValue( new byte[] {1, 1, -65, 1, -102, -47, -70, 11, 2, -124, 78, 97, 16, 61, -76, -46, 90});
        ensureSucces(c, 0.447f, 294.7522f, 16.49f);

        c.setValue( new byte[] {1, 1, -19, 2, 119, -65, 22, -11, 6, -124, -67, 96, 50, 56, 58, -41, -32});
        ensureSucces(c, 0.749f, 269.24744f, -15.3f);

        c.setValue( new byte[] {1, 1, 93, 16, -15, 73, -67, 20, -17, -124, 109, 98, 55, 66, 101, -50, -94});
        ensureSucces(c,4.189f, 103.9801f, 29.163208f);

        c.setValue( new byte[] {1, 1, -2, 5, 67, -19, -9, -32, 71, -124, 125, 93, 103, 69, -56, -45, 66});
        ensureSucces(c, 1.534f, 333.6f, -43.6f);
    }

    @Test
    public void testAck() {
        byte[] message = new byte[] {1, 1, -2, 5, 67, -19, -9, -32, 71, -124, 125, 93, 103, 69, -56, -45, 66};
        byte [] ack = DistoXBLEProtocol.createAcknowledgementPacket(message);
        assertEquals(9, ack.length);
        byte [] expectedAck =  new byte [] { 0x64, 0x61, 0x74, 0x61, 0x3a, 0x01, 85, 0x0d, 0x0a};
        for (int i=0; i<9; i++) {
            assertEquals(expectedAck[i], ack[i]);
        }

    }

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new DistoXBleDevice();
    }
}
