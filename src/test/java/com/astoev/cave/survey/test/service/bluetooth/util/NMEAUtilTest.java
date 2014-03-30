package com.astoev.cave.survey.test.service.bluetooth.util;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.util.NMEAUtil;

import junit.framework.TestCase;

import java.util.List;

public class NMEAUtilTest extends TestCase {

    public void testDecodeErrors() {

        ensureFails(null);
        ensureFails("asdf");
        ensureFails("$PTNLA,HV,,");
        ensureFails("$PTNLA,HV,1.87,M,343.2,D");
        ensureFails("$PTNLA,HV,1.87,M,343.2,D,17.02,D,1.96,M*78");
        ensureFails("$PTNLA,HV,1.87,M,343.2,D,17.02,D,1.96,M*77\n234");
    }

    public void testDecodeSuccess() {
        ensureSucces("$PTNLA,HV,0.60,M,291.1,D,-1.12,D,0.60,M*61", 0.6f, 291.1f, -1.12f);
        ensureSucces("$PTNLA,HV,,M,287.5,D,-1.11,D,,M*61", null, 287.5f, -1.11f);
        ensureSucces("$PTNLA,HV,1.87,M,343.2,D,17.02,D,1.96,M*77", 1.96f, 343.2f, 17.02f);
        ensureSucces("$PTNLA,HV,1.58,M,341.5,D,9.15,D,1.60,M*40", 1.60f, 341.5f, 9.15f);
        ensureSucces("$PTNLA,HV,1.60,M,356.2,D,8.59,D,1.62,M*41", 1.62f, 356.2f, 8.59f);
        ensureSucces("$PTNLA,HV,1.62,M,351.5,D,8.45,D,1.64,M*48", 1.64f, 351.5f, 8.45f);
        ensureSucces("$PTNLA,HV,1.62,M,351.5,D,8.45,D,1.64,M*48\n", 1.64f, 351.5f, 8.45f);
    }

    private void ensureFails(String aBadMessage) {
        try {
            List<Measure> measures;
            if (aBadMessage == null) {
                measures = NMEAUtil.decode(null);
            } else {
                measures = NMEAUtil.decode(aBadMessage.getBytes());
            }

            if (measures != null) {
                fail("Measures returned for bad input");
            }
        } catch (DataException de) {
            // error expected here
        }
    }

    private void ensureSucces(String aMessage, Float aDistance, float anAzimuth, float anAngle) {
        try {
            List<Measure> measures = NMEAUtil.decode(aMessage.getBytes());
            assertNotNull(measures);

            for (Measure m: measures) {
                switch (m.getMeasureType()) {
                    case distance:
                        if (aDistance == null) {
                            fail("Distance not expected");
                        } else {
                            assertEquals(aDistance, m.getValue());
                            assertEquals(Constants.MeasureUnits.meters, m.getMeasureUnit());
                        }
                        break;
                    case angle:
                        assertEquals(anAzimuth, m.getValue());
                        assertEquals(Constants.MeasureUnits.degrees, m.getMeasureUnit());
                        break;
                    case slope:
                        assertEquals(anAngle, m.getValue());
                        assertEquals(Constants.MeasureUnits.degrees, m.getMeasureUnit());
                        break;
                }
            }

        } catch (DataException de) {
            fail("Message not recognized: " + de.getMessage());
        }
    }
}