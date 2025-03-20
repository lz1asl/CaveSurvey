package com.astoev.cave.survey.test.service.bluetooth.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by astoev on 7/19/15.
 */
public abstract class AbstractDeviceProtocolTest {

    protected abstract AbstractBluetoothDevice getDeviceSpec();

    protected void assertMeasurements(Float aDistance, Float anAzimuth, Float anAngle, List<Measure> measures) {
        if (measures != null && measures.size() > 0) {
            assertNotNull(measures, "Measurements expected");
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
                        assertEquals(anAzimuth, m.getValue(), 0.001);
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
                        assertEquals(anAngle, m.getValue(), 0.001);
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
