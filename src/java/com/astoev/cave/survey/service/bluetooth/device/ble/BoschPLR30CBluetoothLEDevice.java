package com.astoev.cave.survey.service.bluetooth.device.ble;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothLEDevice;

/**
 * Bosch PLR 30C with Bluetooth 4.0
 * Created by astoev on 12/24/15.
 */
public class BoschPLR30CBluetoothLEDevice extends AbstractBluetoothLEDevice {

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
        return deviceNameStartsWith(aName, "TODO");
    }

    @Override
    public String getDescription() {
        return "Bosch PLR 30C";
    }

    @Override
    public boolean isMeasureSupported(String aName, Constants.MeasureTypes aMeasureType) {
        // only distance
        return Constants.MeasureTypes.distance.equals(aMeasureType);
    }
}
