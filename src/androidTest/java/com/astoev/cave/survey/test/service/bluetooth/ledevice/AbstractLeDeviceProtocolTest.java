package com.astoev.cave.survey.test.service.bluetooth.ledevice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.bluetooth.BluetoothGattCharacteristic;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.ble.AbstractBluetoothLEDevice;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by astoev on 7/19/15.
 */
public abstract class AbstractLeDeviceProtocolTest {

    protected abstract AbstractBluetoothDevice getDeviceSpec();


    protected void ensureSucces(BluetoothGattCharacteristic aCharacteristic, Float aDistance, Float anAzimuth, Float anAngle)  {
        try {
            List<Constants.MeasureTypes> types = Arrays.asList(Constants.MeasureTypes.distance,
                    Constants.MeasureTypes.angle, Constants.MeasureTypes.slope);

            List<Measure> measures = ((AbstractBluetoothLEDevice) getDeviceSpec()).characteristicToMeasures(aCharacteristic, types);
            if (measures != null || aDistance != null || anAzimuth != null || anAngle != null) {
                assertMeasurements(aDistance, anAzimuth, anAngle, measures);
            }

        } catch (DataException de) {
            fail("Message not recognized: " + de.getMessage());
        }
    }

    protected void ensureFailure(BluetoothGattCharacteristic aCharacteristic)  {
        try {
            List<Constants.MeasureTypes> types = Arrays.asList(Constants.MeasureTypes.distance,
                    Constants.MeasureTypes.angle, Constants.MeasureTypes.slope);

            ((AbstractBluetoothLEDevice) getDeviceSpec()).characteristicToMeasures(aCharacteristic, types);
            fail("Not expected to return value");

        } catch (DataException de) {
            // ignore, expected
        }
    }

    private void assertMeasurements(Float aDistance, Float anAzimuth, Float anAngle, List<Measure> measures) {
        if (measures != null && measures.size() > 0) {
            assertNotNull("Measurements expected", measures);
        }

        // 3 minus the nulls passed results expected
        int numMeasuresExpected = 3 - Collections.frequency(Arrays.asList(aDistance, anAzimuth, anAngle), null);
        assertEquals(numMeasuresExpected, measures.size());
        Set<Constants.MeasureTypes> measuresProcessed = new HashSet<>();

        for (Measure m : measures) {
            switch (m.getMeasureType()) {
                case distance:
                    if (aDistance == null) {
                        fail("Distance not expected");
                    } else {
                        assertEquals(aDistance, m.getValue(), 0.001);
                        assertEquals(Constants.MeasureUnits.meters, m.getMeasureUnit());
                        if (measuresProcessed.contains(m.getMeasureType())) {
                            fail();
                        } else {
                            measuresProcessed.add(m.getMeasureType());
                        }
                    }
                    break;
                case angle:
                    if (anAzimuth == null) {
                        fail("Angle not detected");
                    } else {
                        assertEquals(anAzimuth, m.getValue(), 0.1);
                        assertEquals(Constants.MeasureUnits.degrees, m.getMeasureUnit());
                        if (measuresProcessed.contains(m.getMeasureType())) {
                            fail();
                        } else {
                            measuresProcessed.add(m.getMeasureType());
                        }
                    }
                    break;
                case slope:
                    if (anAngle == null) {
                        fail("Slope not expected");
                    } else {
                        assertEquals(anAngle, m.getValue(), 0.1);
                        assertEquals(Constants.MeasureUnits.degrees, m.getMeasureUnit());
                        if (measuresProcessed.contains(m.getMeasureType())) {
                            fail();
                        } else {
                            measuresProcessed.add(m.getMeasureType());
                        }
                    }
                    break;
            }
        }
    }
}
