package com.astoev.cave.survey.test.service.bluetooth.ledevice;

import static org.junit.Assert.assertEquals;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.bric.Bric4BluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.bric.Bric4ErrorCode;

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

    @Test
    public void testErrorCodes() {
        assertEquals("No error detected", new Bric4ErrorCode(0, null, null).getDescription());
        assertEquals("Accelerometer 1 high magnitude. Nominal is 1.\nMagnitude of vector : 1.2", new Bric4ErrorCode(1, 1.2f, null).getDescription());
        assertEquals("Accelerometer disparity error. Significant difference in single axis measurement value " +
                "between both accelerometers.\nDelta : 1.3, Axis X", new Bric4ErrorCode(5, 1.3f, 1f).getDescription());
        assertEquals("Rangefinder unrecognized error.\nRangefinder error code : 1.4", new Bric4ErrorCode(12, 1.4f, null).getDescription());
    }

    @Override
    protected AbstractBluetoothDevice getDeviceSpec() {
        return new Bric4BluetoothLEDevice();
    }
}
