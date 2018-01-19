package com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.bosch.mtprotocol.MtMessage;
import com.bosch.mtprotocol.glm100C.message.edc.EDCInputMessage;
import com.bosch.mtprotocol.glm100C.message.edc.EDCOutputMessage;

import java.util.Arrays;
import java.util.List;

import static com.astoev.cave.survey.Constants.MeasureTypes.distance;
import static com.astoev.cave.survey.Constants.MeasureTypes.slope;

/**
 * Bosch GLM50c over comm connection.
 *
 * Created by astoev on 12/13/17.
 */

public class BoschGLM50CBluetoothDevice extends AbstractBoschGLMBluetoothDevice {

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Bosch GLM50C ");
    }

    @Override
    public String getDescription() {
        return "Bosch GLM 50 C";
    }


    @Override
    protected List<Constants.MeasureTypes> getSupportedMeasureTypes() {
        // 50m laser and 360' clino
        return Arrays.asList(distance, slope);
    }

    @Override
    protected MtMessage createGLMConfigMessage() {
        EDCOutputMessage configMessage = new EDCOutputMessage();
        configMessage.setSyncControl(EDCOutputMessage.MODE_AUTOSYNC_CONTROL_ON);
        configMessage.setDevMode(EDCOutputMessage.READ_ONLY_MODE);
        // TODO auto enter indirect length mode
        return configMessage;
    }

    @Override
    protected List<Integer> getGLMModes() {
        return Arrays.asList(EDCInputMessage.MODE_INDIRECT_LENGTH,
                EDCInputMessage.MODE_SINGLE_DISTANCE);
    }

    @Override
    protected int getGLMModesLabel() {
        return R.string.bt_device_mode_single_or_indirect;
    }
}
