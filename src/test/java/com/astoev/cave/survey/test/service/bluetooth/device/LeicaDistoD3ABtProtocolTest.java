package com.astoev.cave.survey.test.service.bluetooth.device;

import com.astoev.cave.survey.service.bluetooth.device.comm.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.LeicaDistoD3aBtBluetoothDevice;

import org.junit.jupiter.api.Test;

public class LeicaDistoD3ABtProtocolTest extends AbstractCommDeviceProtocolTest {

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
        ensureSucces("31..00+00000788", 0.788f, null, null);
        ensureSucces("31..00+00000583", 0.583f, null, null);
        ensureSucces("22..01+00002130 31..00+00000605", 0.605f, null, 21.3f);
        ensureSucces("22..01+00002235 31..00+00000611", 0.611f, null, 22.35f);
        ensureSucces("22..01+00001835 31..00+00002596", 2.596f, null, 18.35f);
        ensureSucces("22..01-00001285 31..00+00002370", 2.37f, null, -12.85f);
        ensureSucces("22..01+00000895 31..00+00002896", 2.896f, null, 8.95f);
    }

    @Override
    protected AbstractBluetoothRFCOMMDevice getDeviceSpec() {
        return new LeicaDistoD3aBtBluetoothDevice();
    }
}