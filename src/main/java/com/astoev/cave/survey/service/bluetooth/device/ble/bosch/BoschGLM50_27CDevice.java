package com.astoev.cave.survey.service.bluetooth.device.ble.bosch;

import static com.astoev.cave.survey.Constants.MeasureTypes.distance;
import static com.astoev.cave.survey.Constants.MeasureTypes.slope;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.bosch.mtprotocol.MtMessage;
import com.bosch.mtprotocol.glm100C.message.edc.EDCInputMessage;
import com.bosch.mtprotocol.glm100C.message.edc.EDCOutputMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BoschGLM50_27CDevice extends AbstractBoschGLMBleDevice {

    protected static final UUID SERVICE_UUID = UUID.fromString("02a6c0d1-0451-4000-b000-fb3210111989");

    @Override
    public String getDescription() {
        return "Bosch GLM 50-27 C";
    }

    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Bosch PLR40C"); // TODO
    }

    @Override
    public int getGLMModesLabel() {
        return R.string.bt_device_mode_single_or_indirect;
    }

    @Override
    protected List<Constants.MeasureTypes> getSupportedMeasureTypes() {
        // distance and inclination supported
        return Arrays.asList(distance, slope);
    }

    @Override
    public List<UUID> getServices() {
        return Arrays.asList(SERVICE_UUID);
    }

    @Override
    public List<UUID> getCharacteristics() {
        return List.of(SERVICE_UUID);
    }

    @Override
    public List<UUID> getDescriptors() {
        return Collections.emptyList();
    }

    @Override
    public UUID getService(Constants.MeasureTypes aMeasureType) {
        return SERVICE_UUID;
    }

    @Override
    public UUID getCharacteristic(Constants.MeasureTypes aMeasureType) {
        return null;
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
        return Arrays.asList(EDCInputMessage.MODE_INDIRECT_LENGTH,
                EDCInputMessage.MODE_SINGLE_DISTANCE);
    }

}
