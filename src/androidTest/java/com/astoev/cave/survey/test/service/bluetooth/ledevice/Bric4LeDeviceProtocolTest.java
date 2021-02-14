package com.astoev.cave.survey.test.service.bluetooth.ledevice;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.Bric4BluetoothLEDevice;

import org.junit.Test;

public class Bric4LeDeviceProtocolTest extends AbstractLeDeviceProtocolTest {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testMeasurementPrimaryCharacteristics() {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(Bric4BluetoothLEDevice.MEASUREMENT_PRIMARY_CHARACTERISTIC_UUID, 0, 0);
        c.setValue( new byte[] {-27, 7, 2, 13, 0, 29, 14, 97, -123, -21, 57, 64, -118, -102, 13, 66, 46, 116, -59, 65 });
        ensureSucces(c, 2.905f, 35.400917f, 24.681728f);
        c.setValue( new byte[] {-27, 7, 2, 13, 0, 29, 20, 20, -127, -107, 19, 64, -64, -58, 77, 65, 126, 113, -109, 65 });
        ensureSucces(c, 2.306f, 12.861023f, 18.430416f);
    }

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new Bric4BluetoothLEDevice();
    }
}
