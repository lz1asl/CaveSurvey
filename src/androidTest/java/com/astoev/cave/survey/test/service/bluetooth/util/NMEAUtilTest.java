package com.astoev.cave.survey.test.service.bluetooth.util;

import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.LaserAceBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.TruPulse360BBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.util.NMEAUtil;

import org.junit.Test;

import java.util.List;

public class NMEAUtilTest extends AbstractDeviceProtocolTest {

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
        ensureTruPulseSucces("$PLTIT,HV,7.01,M,0.00,D,3.00,D,7.01,M*64", 7.01f, 0f, 3f);
        ensureTruPulseSucces("$PLTIT,HV,0.60,M,115.90,D,1.80,D,0.60,M*62", 0.6f, 115.9f, 1.8f);

        ensureTruPulseSucces("$OK", null, null, null);
        ensureTruPulseSucces("$PLTIT,HV,0.40,M,64.10,D,2.00,D,0.40,M*56", 0.4f, 64.1f, 2f);
        ensureTruPulseSucces("$PLTIT,HV,0.20,M,95.70,D,-42.90,D,0.20,M*4E", 0.2f, 95.7f, -42.9f);
        ensureTruPulseSucces("$PLTIT,HV,0.20,M,93.60,D,-33.60,D,0.30,M*41", 0.3f, 93.6f, -33.6f);
        ensureTruPulseSucces("$PLTIT,HV,0.10,M,128.30,D,-63.30,D,0.30,M*76", 0.3f, 128.3f, -63.3f);
        ensureTruPulseSucces("$PLTIT,HV,0.10,M,58.60,D,-52.50,D,0.20,M*40", 0.2f, 58.6f, -52.5f);
        ensureTruPulseSucces("$PLTIT,HV,1.80,M,29.20,D,-8.80,D,1.80,M*73", 1.8f, 29.2f, -8.8f);
        ensureTruPulseSucces("$PLTIT,HV,2.80,M,70.80,D,-6.10,D,2.80,M*72", 2.8f, 70.8f, -6.1f);
        ensureTruPulseSucces("$PLTIT,HV,5.00,M,100.00,D,-3.40,D,5.00,M*4C", 5f, 100f, -3.4f);
        ensureTruPulseSucces("$PLTIT,HV,5.01,M,112.70,D,-2.60,D,5.01,M*4B", 5.01f, 112.7f, -2.6f);
        ensureTruPulseSucces("$PLTIT,HV,0.10,M,128.90,D,-61.60,D,0.20,M*7A", 0.2f, 128.9f, -61.6f);
        ensureTruPulseSucces("$PLTIT,HV,3.00,M,80.50,D,1.20,D,3.00,M*59", 3f, 80.5f, 1.2f);
        ensureTruPulseSucces("$PLTIT,HV,2.90,M,73.70,D,-2.80,D,2.90,M*73", 2.9f, 73.7f, -2.8f);
        ensureTruPulseSucces("$PLTIT,HV,3.00,M,68.10,D,-1.80,D,3.00,M*7C", 3f, 68.1f, -1.8f);
        ensureTruPulseSucces("$PLTIT,HV,2.40,M,52.30,D,-1.10,D,2.40,M*7E", 2.4f, 52.3f, -1.1f);
        ensureTruPulseSucces("$PLTIT,HV,2.80,M,75.60,D,-3.60,D,2.80,M*7B", 2.8f, 75.6f, -3.6f);
        ensureTruPulseSucces("$PLTIT,HV,3.00,M,73.50,D,-1.70,D,3.00,M*7D", 3.0f, 73.5f, -1.7f);
        ensureTruPulseSucces("$PLTIT,HV,3.10,M,69.50,D,1.50,D,3.10,M*59", 3.1f, 69.5f, 1.5f);
        ensureTruPulseSucces("$PLTIT,HV,1.80,M,18.90,D,13.00,D,1.90,M*64", 1.9f, 18.9f, 13.0f);
        ensureTruPulseSucces("$PLTIT,HV,2.00,M,28.00,D,6.60,D,2.00,M*5D", 2f, 28f, 6.6f);
        ensureTruPulseSucces("$PLTIT,HV,3.00,M,74.80,D,2.60,D,3.00,M*58", 3f, 74.8f, 2.6f);
        ensureTruPulseSucces("$PLTIT,HV,6.30,M,156.50,D,-3.50,D,6.30,M*4B", 6.3f, 156.5f, -3.5f);
        ensureTruPulseSucces("$PLTIT,HV,,,153.50,D,-8.10,D,,*41", null, 153.5f, -8.1f);
        ensureTruPulseSucces("$PLTIT,HV,,,152.70,D,-7.70,D,,*4B", null, 152.7f, -7.7f);
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

    private void ensureLASucces(String aMessage, Float aDistance, Float anAzimuth, float anAngle) {
        ensureSucces(aMessage.getBytes(), aDistance, anAzimuth, anAngle, new LaserAceBluetoothDevice());
    }

    private void ensureTruPulseSucces(String aMessage, Float aDistance, Float anAzimuth, Float anAngle) {
        ensureSucces(aMessage.getBytes(), aDistance, anAzimuth, anAngle, new TruPulse360BBluetoothDevice());
    }
}