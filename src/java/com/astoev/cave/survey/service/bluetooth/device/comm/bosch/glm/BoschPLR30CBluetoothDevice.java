package com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm;

import com.astoev.cave.survey.Constants;
import com.bosch.mtprotocol.MtMessage;
import com.bosch.mtprotocol.glm100C.message.edc.EDCInputMessage;
import com.bosch.mtprotocol.glm100C.message.edc.EDCOutputMessage;

/**
 * Bosch PLR 30 C over comm.
 * Created by astoev on 12/24/15.
 */
public class BoschPLR30CBluetoothDevice extends AbstractBoschGLMBluetoothDevice {


    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Bosch PLR30C");
    }

    @Override
    public String getDescription() {
        return "Bosch PLR 30 C";
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        // only distance
        return Constants.MeasureTypes.distance.equals(aMeasureType);
    }

    @Override
    protected MtMessage createGLMConfigMessage() {
        EDCOutputMessage configMessage = new EDCOutputMessage();
        configMessage.setSyncControl(EDCOutputMessage.MODE_AUTOSYNC_CONTROL_ON);
        configMessage.setDevMode(EDCOutputMessage.READ_ONLY_MODE);
        return configMessage;
    }

    @Override
    protected int getGLMMode() {
        return EDCInputMessage.MODE_SINGLE_DISTANCE;
    }
}
