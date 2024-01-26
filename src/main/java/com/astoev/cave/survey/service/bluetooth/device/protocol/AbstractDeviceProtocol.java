package com.astoev.cave.survey.service.bluetooth.device.protocol;

import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.util.List;

public abstract class AbstractDeviceProtocol {

    public abstract List<Measure> packetToMeasurements(byte[] dataPacket) throws DataException;

    public boolean isFullMessage(byte[] dataPacket) {
        return true;
    }

}
