package com.astoev.cave.survey.service.bluetooth.device;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.util.StringUtils;

/**
 * Any Bluetooth device.
 */
public abstract class AbstractBluetoothDevice implements Comparable {

    /**
     * Used to filter paired devices by name.
     *
     * @param aName
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
     * Hardware specific information what measures can be performed, e.g. distance only or distance + clino or distance + clino + angle.
     * @param aMeasureType
     * @return
     */
    public abstract boolean isMeasureSupported(Constants.MeasureTypes aMeasureType);


    protected boolean deviceNameStartsWith(String aDeviceName, String aStart) {
        return StringUtils.isNotEmpty(aDeviceName) && aDeviceName.startsWith(aStart);
    }

    protected boolean deviceNameEquals(String aDeviceName, String aStart) {
        return StringUtils.isNotEmpty(aDeviceName) && aDeviceName.equals(aStart);
    }

    @Override
    public int compareTo(Object another) {
        return getDescription().compareTo(((AbstractBluetoothDevice) another).getDescription());
    }
}
