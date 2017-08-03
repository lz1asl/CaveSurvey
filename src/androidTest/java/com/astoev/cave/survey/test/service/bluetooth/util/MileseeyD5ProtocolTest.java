package com.astoev.cave.survey.test.service.bluetooth.util;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.Mileseeyd5tBluetoothLeDevice;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by astoev on 8/1/17.
 */

public class MileseeyD5ProtocolTest extends AbstractDeviceProtocolTest {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testDataPacket() throws IOException {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(Mileseeyd5tBluetoothLeDevice.CHAR_DATA_UUID, 0, 0);
        c.setValue("1.2m".getBytes());
        ensureSucces(c, 1.2f, null, null);
        c.setValue("0.123m".getBytes());
        ensureSucces(c, 0.123f, null, null);
    }

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new Mileseeyd5tBluetoothLeDevice();
    }
}
