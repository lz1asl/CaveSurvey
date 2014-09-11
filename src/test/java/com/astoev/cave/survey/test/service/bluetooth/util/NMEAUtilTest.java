package com.astoev.cave.survey.test.service.bluetooth.util;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.LaserAceBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.TruPulse360BBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.util.NMEAUtil;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class NMEAUtilTest extends TestCase {

    @Test
    public void testLaserAceDecodeErrors() {

        ensureFails(null);
        ensureFails("asdf");
        ensureFails("$PTNLA,HV,,");
        ensureFails("$PTNLA,HV,1.87,M,343.2,D");
        ensureFails("$PTNLA,HV,1.87,M,343.2,D,17.02,D,1.96,M*78");
        ensureFails("$PTNLA,HV,1.87,M,343.2,D,17.02,D,1.96,M*77\n234");
    }

    @Test
    public void testLaserAceDecodeSuccess() {
        ensureLASucces("$PTNLA,HV,0.60,M,291.1,D,-1.12,D,0.60,M*61", 0.6f, 291.1f, -1.12f);
        ensureLASucces("$PTNLA,HV,,M,287.5,D,-1.11,D,,M*61", null, 287.5f, -1.11f);
        ensureLASucces("$PTNLA,HV,1.87,M,343.2,D,17.02,D,1.96,M*77", 1.96f, 343.2f, 17.02f);
        ensureLASucces("$PTNLA,HV,1.58,M,341.5,D,9.15,D,1.60,M*40", 1.60f, 341.5f, 9.15f);
        ensureLASucces("$PTNLA,HV,1.60,M,356.2,D,8.59,D,1.62,M*41", 1.62f, 356.2f, 8.59f);
        ensureLASucces("$PTNLA,HV,1.62,M,351.5,D,8.45,D,1.64,M*48", 1.64f, 351.5f, 8.45f);
        ensureLASucces("$PTNLA,HV,1.62,M,351.5,D,8.45,D,1.64,M*48\n", 1.64f, 351.5f, 8.45f);
        ensureLASucces("$PTNLA,HV,,M,085.0,D,-0.7,D,9999.99,9999.99,M*7E", null, 85f, -0.7f);
    }

    @Test
    public void testTruPulseSuccess() {
        ensureTruPulseSucces(|"$PLTIT,HV,18.00,F,185.20,D,6.90,D,18.00,F*66", , , );
        ensureTruPulseSucces("$PLTIT,HV,7.01,M,0.00,D,3.00,D,7.01,M*64", , , );
        ensureTruPulseSucces("$PLTIT,HV,,,187.10,D,8.40,D,,*64", , , );
        ensureTruPulseSucces("$PLTIT,HV,,,347.20,D,,,,*3F", , , );
        ensureTruPulseSucces("$PLTIT,HV,6.00,Y,179.40,D,7.20,D,6.10,Y*68", , , );
        ensureTruPulseSucces("$PLTIT,HV,5.90,Y,265.70,D,11.60,D,6.00,Y*5D", , , );
    }

    private void ensureFails(String aBadMessage) {
        try {
            List<Measure> measures;
            if (aBadMessage == null) {
                measures = NMEAUtil.decodeTrimbleLaserAce(null);
            } else {
                measures = NMEAUtil.decodeTrimbleLaserAce(aBadMessage.getBytes());
            }

            if (measures != null) {
                fail("Measures returned for bad input");
            }
        } catch (DataException de) {
            // error expected here
        }
    }

    private void ensureLASucces(String aMessage, Float aDistance, float anAzimuth, float anAngle) {
        ensureSucces(aMessage, aDistance, anAzimuth, anAngle, LaserAceBluetoothDevice);
    }

    private void ensureTruPulseSucces(String aMessage, Float aDistance, float anAzimuth, float anAngle) {
        ensureSucces(aMessage, aDistance, anAzimuth, anAngle, TruPulse360BBluetoothDevice);
    }

    private void ensureSucces(String aMessage, Float aDistance, float anAzimuth, float anAngle, AbstractBluetoothDevice aDeviceSpec) {
        try {
            List<Constants.MeasureTypes> types = new ArrayList<Constants.MeasureTypes>();
            types.add(Constants.MeasureTypes.distance);
            types.add(Constants.MeasureTypes.angle);
            types.add(Constants.MeasureTypes.slope);

            List<Measure> measures = aDeviceSpec.decodeMeasure(aMessage.getBytes(), types);
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