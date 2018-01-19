package com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.bosch.mtprotocol.MtMessage;
import com.bosch.mtprotocol.glm100C.message.sync.SyncInputMessage;
import com.bosch.mtprotocol.glm100C.message.sync.SyncOutputMessage;

import java.util.Arrays;
import java.util.List;

import static com.astoev.cave.survey.Constants.MeasureTypes.distance;
import static com.astoev.cave.survey.Constants.MeasureTypes.slope;

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
    protected List<Constants.MeasureTypes> getSupportedMeasureTypes() {
        // 100m laser and 360' clino
        return Arrays.asList(distance, slope);
    }

    @Override
    protected MtMessage createGLMConfigMessage() {
        SyncOutputMessage configMessage = new SyncOutputMessage();
        configMessage.setSyncControl(SyncOutputMessage.MODE_AUTOSYNC_CONTROL_ON);
        configMessage.setMode(getGLMModes().get(0));
        return configMessage;
    }

    @Override
    protected List<Integer> getGLMModes() {
        return Arrays.asList(SyncInputMessage.MEAS_MODE_SINGLE);
    }

    @Override
    protected int getGLMModesLabel() {
        return R.string.bt_device_mode_single;
    }

}
