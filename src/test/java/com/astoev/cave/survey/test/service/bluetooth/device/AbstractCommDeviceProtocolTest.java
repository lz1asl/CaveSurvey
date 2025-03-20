package com.astoev.cave.survey.test.service.bluetooth.device;

import static org.junit.jupiter.api.Assertions.fail;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.comm.AbstractBluetoothRFCOMMDevice;

import java.util.Arrays;
import java.util.List;


/**
 * Created by astoev on 7/19/15.
 */
public abstract class AbstractCommDeviceProtocolTest extends AbstractDeviceProtocolTest {

    protected void ensureSucces(String aMessage, Float aDistance, Float anAzimuth, Float anAngle)  {
        ensureSucces(aMessage.getBytes(), aDistance, anAzimuth, anAngle);
    }

    protected void ensureSucces(byte[] aMessage, Float aDistance, Float anAzimuth, Float anAngle)  {
        try {
            List<Constants.MeasureTypes> types = Arrays.asList(Constants.MeasureTypes.distance,
                    Constants.MeasureTypes.angle, Constants.MeasureTypes.slope);

            List<Measure> measures = ((AbstractBluetoothRFCOMMDevice) getDeviceSpec()).decodeMeasure(aMessage, types);
            assertMeasurements(aDistance, anAzimuth, anAngle, measures);

        } catch (DataException de) {
            fail("Message not recognized: " + de.getMessage());
        }
    }

    protected void ensureFails(String aBadMessage) {
        try {
            List<Measure> measures;
            if (aBadMessage == null) {
                measures = getDeviceSpec().getProtocol().packetToMeasurements(null);
            } else {
                measures = getDeviceSpec().getProtocol().packetToMeasurements(aBadMessage.getBytes());
            }

            if (measures != null) {
                fail("Measures returned for bad input");
            }
        } catch (DataException de) {
            // error expected here
        }
    }
}
