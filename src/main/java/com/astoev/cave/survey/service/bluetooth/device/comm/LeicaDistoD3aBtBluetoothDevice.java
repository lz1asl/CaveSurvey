package com.astoev.cave.survey.service.bluetooth.device.comm;

import static com.astoev.cave.survey.Constants.MeasureTypes.distance;
import static com.astoev.cave.survey.Constants.MeasureTypes.slope;

import com.astoev.cave.survey.Constants.MeasureTypes;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.protocol.AbstractPacketBasedDeviceProtocol;
import com.astoev.cave.survey.service.bluetooth.device.protocol.LeicaDistoD3aProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by astoev on 2/21/14.
 */
public class LeicaDistoD3aBtBluetoothDevice extends AbstractBluetoothRFCOMMDevice {




    @Override
    public boolean isNameSupported(String aName) {
        return aName == null;
    }

    public String getDescription() {
        return "Leica Disto D3a BT";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<MeasureTypes> aMeasures) throws IOException {
        // should not be required
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {
        // nothing to configure
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aDataPacket, List<MeasureTypes> aMeasures) throws DataException {
        return mProtocol.packetToMeasurements(aDataPacket);
    }

    @Override
    protected List<MeasureTypes> getSupportedMeasureTypes() {
        // either distance or distance + slope
        return Arrays.asList(distance, slope);
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        return mProtocol.isFullMessage(aBytesBuffer);
    }

    @Override
    public AbstractPacketBasedDeviceProtocol getProtocol() {
        return new LeicaDistoD3aProtocol();
    }
}
