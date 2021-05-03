package com.astoev.cave.survey.service.bluetooth.device.comm.distox;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.comm.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.util.DistoXProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * DistoX v1 or v2 common implementation.
 *
 * Created by astoev on 3/26/15.
 */
public abstract class AbstractDistoXBluetoothDevice extends AbstractBluetoothRFCOMMDevice {


    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
        // not needed, measures sent automatically
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {
        // not needed
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws DataException {

        Log.i(Constants.LOG_TAG_BT, "Data packet : " + DistoXProtocol.isDataPacket(aResponseBytes));
        if (DistoXProtocol.isDataPacket(aResponseBytes)) {
            Log.i(Constants.LOG_TAG_BT, DistoXProtocol.describeDataPacket(aResponseBytes));
            return DistoXProtocol.parseDataPacket(aResponseBytes);
        }
        return null;
    }

    @Override
    protected List<Constants.MeasureTypes> getSupportedMeasureTypes() {
        // v1 and v2 with same measure types
        // all measures available on each shot
        return Arrays.asList(Constants.MeasureTypes.values());
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        // full message expected
//        Log.i(Constants.LOG_TAG_BT, "Check package " + DistoXProtocol.describeDataPacket(aBytesBuffer));
        return true;
    }

    @Override
    public void ack(OutputStream aStream, byte[] aBytesBffer) throws IOException {
        byte [] ack = DistoXProtocol.createAcknowledgementPacket(aBytesBffer);
        Log.i(Constants.LOG_TAG_BT, "Generate ack " + DistoXProtocol.describeAcknowledgementPacket(ack));
        aStream.write(ack);
        aStream.flush();
    }
}
