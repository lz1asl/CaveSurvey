package com.astoev.cave.survey.test.service.bluetooth.util;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.LeicaDistoBluetoothLEDevice;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by astoev on 1/15/16.
 */
public class LeicaDistoProtocolTest extends AbstractDeviceProtocolTest {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testDistance() throws IOException {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(LeicaDistoBluetoothLEDevice.CHARACTERISTIC_DISTANCE_UUID, 0, 0);
        c.setValue("");
        ensureSucces(c, 24.059f, null, null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testDistanceUnit() throws IOException {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(LeicaDistoBluetoothLEDevice.CHARACTERISTIC_DISTANCE_UNIT_UUID, 0, 0);
        c.setValue("");
        ensureSucces(c, null, null, null);
        c.setValue("");
        ensureFailure(c);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testAngle() throws IOException {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(LeicaDistoBluetoothLEDevice.CHARACTERISTIC_ANGLE_UUID, 0, 0);
        c.setValue("");
        ensureSucces(c, 24.059f, null, null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testAngleUnit() throws IOException {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(LeicaDistoBluetoothLEDevice.CHARACTERISTIC_ANGLE_UNIT_UUID, 0, 0);
        c.setValue(0, 18, 0);
        ensureSucces(c, null, null, null);
        c.setValue(1, 18, 0);
        ensureSucces(c, null, null, null);
        c.setValue(2, 18, 0);
        ensureSucces(c, null, null, null);
        c.setValue(3, 18, 0);
        ensureSucces(c, null, null, null);

        c.setValue(4, 18, 0);
        ensureFailure(c);
        c.setValue(5, 18, 0);
        ensureFailure(c);
        c.setValue(6, 18, 0);
        ensureFailure(c);
    }


    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new LeicaDistoBluetoothLEDevice();
    }
}
