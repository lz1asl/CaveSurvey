package com.astoev.cave.survey.service.bluetooth.device.comm;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.util.ByteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

/**
 * Bluetooth device using standard serial communication port.
 */
public abstract class AbstractBluetoothRFCOMMDevice extends AbstractBluetoothDevice {

    /**
     * Used in case of active connection (!isPassiveBTConnection()) to prepare device/trigger measure.
     *
     * @param aStream
     * @param aMeasures
     * @throws IOException
     */
    public abstract void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException;

    /**
     * Called once after connection to the device.
     *
     * @param anInput
     * @param anOutput
     * @throws IOException
     */
    public abstract void configure(InputStream anInput, OutputStream anOutput) throws IOException;

    /**
     * Device specific implementation to decode data packet into measures.
     *
     * @param aResponseBytes
     * @param aMeasures
     * @return
     * @throws IOException
     * @throws DataException
     */
    public abstract List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws DataException;


    protected String getSPPUUIDString() {
        return "00001101-0000-1000-8000-00805F9B34FB";
    }

    public UUID getSPPUUID() {
        return UUID.fromString(getSPPUUIDString());
    }

    /**
     * Protocol specific logic how to detect full data packet from device.
     * Used to help assemble message from chunks.
     *
     * @param aBytesBuffer
     * @return
     */
    public abstract boolean isFullPacketAvailable(byte[] aBytesBuffer);

    /**
     * Used to acknowledge message received.
     * Need to be overriden only in case actual ack performed.
     * @param aStream
     * @param aMessage the original packet
     */
    public void ack(OutputStream aStream, byte[] aMessage) throws IOException {
        // no default implementation
    }

    /**
     * May be used from implementations to keep the remote device on.
     * Streams should not be closed.
     * @param aStreamOut
     * @param aStreamIn
     */
    public void keepAlive(OutputStream aStreamOut, InputStream aStreamIn) throws IOException {

    }

    protected void sendLogged(String aMessage, OutputStream aStream, boolean aHexString) throws IOException {
        Log.d(Constants.LOG_TAG_BT, "Send to device " + aMessage + " : " + aHexString);
        if (aHexString) {
            aStream.write(ByteUtils.hexStringToByte("D5F0E00D"));
        } else {
            aStream.write(aMessage.getBytes());
        }
        aStream.flush();
    }

    /**
     * Own logic to read bytes from the streams.
     *
     * @return
     */
    public boolean useOwnRead() {
        return false;
    }

    /**
     * Own logic need reinitialization flag.
     *
     * @return
     */
    public boolean getOwnReadError() {
        return false;
    }

    /**
     * Used with combination with useOwnRead() to clean the reader state.
     *
     * @return
     */
    public void onError() {

    }

}
