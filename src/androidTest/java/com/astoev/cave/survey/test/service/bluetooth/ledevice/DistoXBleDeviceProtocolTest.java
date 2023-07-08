package com.astoev.cave.survey.test.service.bluetooth.ledevice;

import static org.junit.Assert.fail;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.DistoXBleDevice;

import org.junit.Test;

public class DistoXBleDeviceProtocolTest extends AbstractLeDeviceProtocolTest {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testMeasurementPrimaryCharacteristics() {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(DistoXBleDevice.READ_SERVICE_UUID, 0, 0);
//        c.setValue( new byte[] {-27, 7, 2, 13, 0, 29, 14, 97, -123, -21, 57, 64, -118, -102, 13, 66, 46, 116, -59, 65 });
//        ensureSucces(c, 2.905f, 35.400917f, 24.681728f);
        fail("TODO");
    }

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new DistoXBleDevice();
    }
}
