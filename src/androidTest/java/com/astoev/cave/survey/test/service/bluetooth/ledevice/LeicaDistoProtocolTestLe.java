package com.astoev.cave.survey.test.service.bluetooth.ledevice;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.LeicaDistoBluetoothLEDevice;

import org.junit.Test;

/**
 * Created by astoev on 1/15/16.
 */
public class LeicaDistoProtocolTestLe extends AbstractLeDeviceProtocolTest {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testDistance() {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(LeicaDistoBluetoothLEDevice.CHARACTERISTIC_DISTANCE_UUID, 0, 0);
        c.setValue(new byte[] {-128, 72, 79, 63});
        ensureSucces(c, 0.8097f, null, null);

        c.setValue(new byte[] {-70, 73, 92, 63});
        ensureSucces(c, 0.8605f, null, null);

        c.setValue(new byte[] {110, -93, 49, 64});
        ensureSucces(c, 2.7756f, null, null);

        c.setValue(new byte[] {-52, 59, 30, 64});
        ensureSucces(c, 2.4723997f, null, null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testDistanceUnit() {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(LeicaDistoBluetoothLEDevice.CHARACTERISTIC_DISTANCE_UNIT_UUID, 0, 0);
        setInt(c, 0);
        ensureSucces(c, null, null, null);
        setInt(c, 1);
        ensureSucces(c, null, null, null);
        setInt(c, 2);
        ensureSucces(c, null, null, null);
        setInt(c, 3);
        ensureSucces(c, null, null, null);

        setInt(c, 4);
        ensureFailure(c);
        setInt(c, 5);
        ensureFailure(c);
        setInt(c, 6);
        ensureFailure(c);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testAngle() {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(LeicaDistoBluetoothLEDevice.CHARACTERISTIC_ANGLE_UUID, 0, 0);
        c.setValue(new byte[]{124, -14, -81, 58});
        ensureSucces(c, null, null, 0.07691217f);

        c.setValue(new byte[]{18, 46, 35, 59});
        ensureSucces(c, null, null, 0.1426624f);

        c.setValue(new byte[]{-28, 46, 3, 63});
        ensureSucces(c, null, null, 29.36032f);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testAngleUnit() {

        BluetoothGattCharacteristic c = new BluetoothGattCharacteristic(LeicaDistoBluetoothLEDevice.CHARACTERISTIC_ANGLE_UNIT_UUID, 0, 0);
        setInt(c, 0);
        ensureSucces(c, null, null, null);

        setInt(c, 1);
        ensureFailure(c);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void setInt(BluetoothGattCharacteristic aCharacteristic, int aValue) {
        aCharacteristic.setValue(aValue, BluetoothGattCharacteristic.FORMAT_SINT32, 0);
    }

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new LeicaDistoBluetoothLEDevice();
    }
}
