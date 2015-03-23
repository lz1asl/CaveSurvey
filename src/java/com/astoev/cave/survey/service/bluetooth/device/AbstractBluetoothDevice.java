package com.astoev.cave.survey.service.bluetooth.device;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by astoev on 2/21/14.
 */
public abstract class AbstractBluetoothDevice {

    /**
     * Used to filter paired devices by name.
     *
     * @param aName
     * @return
     */
    public abstract boolean isNameSupported(String aName);

    protected abstract String getSPPUUIDString();

    /**
     * Used to display information about the device.
     *
     * @return
     */
    public abstract String getDescription();

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
    public abstract List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws IOException, DataException;

    /**
     * Does the device need command to send measures or they are streamed automatically?
     *
     * @return
     */
    public abstract boolean isPassiveBTConnection();

    /**
     * Hardware specific information what measures can be performed, e.g. distance only or distance + clino or distance + clino + angle.
     * @param aMeasureType
     * @return
     */
    public abstract boolean isMeasureSupported(Constants.MeasureTypes aMeasureType);

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


    protected boolean deviceNameStartsWith(String aDeviceName, String aStart) {
        return StringUtils.isNotEmpty(aDeviceName) && aDeviceName.startsWith(aStart);
    }

    protected boolean deviceNameEquals(String aDeviceName, String aStart) {
        return StringUtils.isNotEmpty(aDeviceName) && aDeviceName.equals(aStart);
    }
}
