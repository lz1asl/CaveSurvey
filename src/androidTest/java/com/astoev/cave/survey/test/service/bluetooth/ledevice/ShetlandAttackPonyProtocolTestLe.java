package com.astoev.cave.survey.test.service.bluetooth.ledevice;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.ShetlandAttackPonyLeDevice;

import org.junit.Test;

public class ShetlandAttackPonyProtocolTestLe extends AbstractLeDeviceProtocolTest {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testMeasurementPrimaryCharacteristics() {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(ShetlandAttackPonyLeDevice.READ_CHARACTERISTIC_UUID, 0, 0);
        c.setValue(new byte[] {1, -5, 93, -5, 113, 51, -52, 105});
        ensureSucces(c, 24.059f, 160.285f, -72.845f);
    }

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new ShetlandAttackPonyLeDevice();
    }
}
