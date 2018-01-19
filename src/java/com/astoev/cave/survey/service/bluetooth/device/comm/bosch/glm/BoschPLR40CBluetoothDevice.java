package com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.bosch.mtprotocol.MtMessage;
import com.bosch.mtprotocol.glm100C.message.edc.EDCInputMessage;
import com.bosch.mtprotocol.glm100C.message.edc.EDCOutputMessage;

import java.util.Arrays;
import java.util.List;

/**
 * Bosch PLR 40 C over comm.
 * Created by astoev on 12/24/15.
 */
public class BoschPLR40CBluetoothDevice extends AbstractBoschGLMBluetoothDevice {


    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Bosch PLR40C");
    }

    @Override
    public String getDescription() {
        return "Bosch PLR 40 C";
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
    protected List<Integer> getGLMModes() {
        return Arrays.asList(EDCInputMessage.MODE_SINGLE_DISTANCE);
    }

    @Override
    protected int getGLMModesLabel() {
        return R.string.bt_device_mode_single;
    }
}
