package com.astoev.cave.survey.service.bluetooth.device;

import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_DUAL;

import android.bluetooth.BluetoothDevice;
import android.os.Build;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.bluetooth.device.protocol.AbstractDeviceProtocol;
import com.astoev.cave.survey.util.StringUtils;

import java.util.List;

/**
 * Any Bluetooth device.
 */
public abstract class AbstractBluetoothDevice implements Comparable {

    protected AbstractDeviceProtocol mProtocol = null;


    public AbstractBluetoothDevice() {
        mProtocol = getProtocol();
    }

    /**
     * Used to filter paired devices by name.
     *
     * @param aName name of the device
     * @return
     */
    public abstract boolean isNameSupported(String aName);

    /**
     * Used to display information about the device.
     *
     * @return
     */
    public abstract String getDescription();

    /**
     * Bluetooth class as from BluetoothDevice.getType()
     * @return
     */
    public abstract int getDeviceType();

    /**
     * Hardware specific information what measures can be performed, e.g. distance only or distance + clino or distance + clino + angle.
     * @param aMeasureType
     * @return
     */
    public final boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        return getSupportedMeasureTypes().contains(aMeasureType);
    }

    protected abstract List<Constants.MeasureTypes> getSupportedMeasureTypes();

    protected boolean deviceNameStartsWith(String aDeviceName, String aStart) {
        return StringUtils.isNotEmpty(aDeviceName) && aDeviceName.startsWith(aStart);
    }

    protected boolean deviceNameEquals(String aDeviceName, String aName) {
        return StringUtils.isNotEmpty(aDeviceName) && aDeviceName.equals(aName);
    }

    protected boolean hasLength(String aDeviceName, int length) {
        return StringUtils.isNotEmpty(aDeviceName) && aDeviceName.length() == length;
    }

    public boolean isTypeCompatible(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // newer devices with LE support of the same type
            return device.getType() == getDeviceType() || device.getType() == DEVICE_TYPE_DUAL;
        } else {
            // older devices will simply not see LE
            return getDeviceType() == DEVICE_TYPE_CLASSIC;
        }
    }

    public AbstractDeviceProtocol getProtocol() {
        return null;
    }

    @Override
    public int compareTo(Object another) {
        return getDescription().compareTo(((AbstractBluetoothDevice) another).getDescription());
    }
}
