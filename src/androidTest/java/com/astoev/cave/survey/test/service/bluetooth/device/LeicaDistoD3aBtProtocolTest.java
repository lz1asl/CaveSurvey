package com.astoev.cave.survey.test.service.bluetooth.device;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.comm.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.LeicaDistoD3aBtBluetoothDevice;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LeicaDistoD3aBtProtocolTest extends AbstractDeviceProtocolTest {

    private LeicaDistoD3aBtBluetoothDevice device = new LeicaDistoD3aBtBluetoothDevice();

    @Test
    public void testLeicaD3aDecodeErrors() {
        ensureFails(null);
        ensureFails("asdf");
        ensureFails("@E203");
        ensureFails("31..00+00000");
        ensureFails("22..01+00001285 31..00+000023");
    }

    @Test
    public void testLeicaD3aDecodeSuccess() {
        ensureSucces("31..00+00000788", 78.8f, null, null);
        ensureSucces("31..00+00000583", 58.3f, null, null);
        ensureSucces("22..01+00002130 31..00+00000605", 60.5f, null, 21.3f);
        ensureSucces("22..01+00002235 31..00+00000611", 61.1f, null, 22.35f);
        ensureSucces("22..01+00001835 31..00+00002596", 259.6f, null, 18.35f);
        ensureSucces("22..01-00001285 31..00+00002370", 237f, null, -12.85f);
        ensureSucces("22..01+00000895 31..00+00002896", 289.6f, null, 8.95f);
    }

    private void ensureFails(String aBadMessage) {
        try {
            List<Measure> measures;
            if (aBadMessage == null) {
                measures = device.decodeMeasure(null, Arrays.asList(Constants.MeasureTypes.values()));
            } else {
                measures = device.decodeMeasure(aBadMessage.getBytes(), Arrays.asList(Constants.MeasureTypes.values()));
            }

            if (measures != null) {
                fail("Measures returned for bad input");
            }
        } catch (DataException de) {
            // error expected here
        }
    }

    @Override
    protected AbstractBluetoothRFCOMMDevice getDeviceSpec() {
        return new LeicaDistoD3aBtBluetoothDevice();
    }
}