package com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm;

import com.astoev.cave.survey.Constants;
import com.bosch.mtprotocol.MtMessage;
import com.bosch.mtprotocol.glm100C.message.sync.SyncInputMessage;
import com.bosch.mtprotocol.glm100C.message.sync.SyncOutputMessage;

/**
 * Bosch GLM100c over comm connection.
 * Support for distance, and inclination.
 *
 * Created by astoev on 10/11/17.
 */

public class BoschGLM100CBluetoothDevice extends AbstractBoschGLMBluetoothDevice {

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "BOSCH GLM100C ");
    }

    @Override
    public String getDescription() {
        return "Bosch GLM 100 C";
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        // 100m laser and 360' clino
        return Constants.MeasureTypes.distance.equals(aMeasureType) || Constants.MeasureTypes.slope.equals(aMeasureType);
    }

    @Override
    protected MtMessage createGLMConfigMessage() {
        SyncOutputMessage configMessage = new SyncOutputMessage();
        configMessage.setSyncControl(SyncOutputMessage.MODE_AUTOSYNC_CONTROL_ON);
        configMessage.setMode(getGLMMode());
        return configMessage;
    }

    @Override
    protected int getGLMMode() {
        return SyncInputMessage.MEAS_MODE_SINGLE;
    }

}
