package com.astoev.cave.survey.service.bluetooth.device;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by astoev on 2/21/14.
 */
public abstract class AbstractBluetoothDevice {

    public abstract boolean isNameSupported(String aName);

    protected abstract String getSPPUUIDString();

    public abstract String getDescription();

    public abstract void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException;

    public abstract List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws IOException, DataException;

    public abstract boolean isPassiveBTConnection();

    public UUID getSPPUUID() {
        return UUID.fromString(getSPPUUIDString());
    }

}
