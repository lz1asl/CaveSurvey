package com.astoev.cave.survey.test.service.bluetooth.device;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.StanleyBluetoothLeDevice;

import org.junit.Test;

import java.io.IOException;

public class StanleyProtocolTest extends AbstractDeviceProtocolTest {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testDataPacket() throws IOException {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(StanleyBluetoothLeDevice.CHARACTERISTIC_DISTANCE_UUID, 0, 0);

        c.setValue("10010002BD0CA2".getBytes());
        ensureSucces(c, 3.261f, null, null);
        c.setValue("10010002020415".getBytes());
        ensureSucces(c, 1.026f, null, null);
        c.setValue("10010002710D6F".getBytes());
        ensureSucces(c, 3.441f, null, null);
        c.setValue("100100027C0A65".getBytes());
        ensureSucces(c, 2.684f, null, null);
    }

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new StanleyBluetoothLeDevice();
    }
}
