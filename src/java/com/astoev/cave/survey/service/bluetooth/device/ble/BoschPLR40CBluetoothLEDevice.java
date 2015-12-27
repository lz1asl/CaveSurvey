package com.astoev.cave.survey.service.bluetooth.device.ble;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothLEDevice;

/**
 * Bosch PLR 40C with Bluetooth 4.0
 * Created by astoev on 12/24/15.
 */
public class BoschPLR40CBluetoothLEDevice extends AbstractBluetoothLEDevice {

    public static final String DEVICE_INFO_SERVICE = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String SERVICE2 = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String SERVICE3 = "00005301-0000-0041-5253-534f46540000";

    public static final String SERVICE1_DEVICE_NAME_CHARACTERISTIC = "00002a00-0000-1000-8000-00805f9b34fb";
    public static final String SERVICE1_CHARACTERISTIC2 = "00002a01-0000-1000-8000-00805f9b34fb";

    public static final String SERVICE2_CHARACTERISTIC1 = "00002a29-0000-1000-8000-00805f9b34fb";
    public static final String SERVICE2_CHARACTERISTIC2 = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String SERVICE2_CHARACTERISTIC3 = "00002a23-0000-1000-8000-00805f9b34fb";

    public static final String SERVICE3_CHARACTERISTIC1 = "00004301-0000-0041-5253-534f46540000";

    public static final String SERVICE3_CHARACTERISTIC1_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    public static final String[] ALL_SERVICES = new String[] { DEVICE_INFO_SERVICE, SERVICE2, SERVICE3 };
    public static final String[] ALL_CHARACTERISTICS = new String[] {SERVICE1_DEVICE_NAME_CHARACTERISTIC,SERVICE1_CHARACTERISTIC2, SERVICE2_CHARACTERISTIC1,
            SERVICE2_CHARACTERISTIC2, SERVICE2_CHARACTERISTIC3, SERVICE3_CHARACTERISTIC1 };


    @Override
    public String getService() {
        return null;
    }

    @Override
    public String getMeasurementCharacteristics(Constants.MeasureTypes aMeasureType) {
        return null;
    }

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Bosch PLR40C");
    }

    @Override
    public String getDescription() {
        return "Bosch PLR 40C";
    }

    @Override
    public boolean isMeasureSupported(String aName, Constants.MeasureTypes aMeasureType) {
        // only distance
        return Constants.MeasureTypes.distance.equals(aMeasureType);
    }
}
