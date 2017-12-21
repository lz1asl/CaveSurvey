package com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm;

import com.astoev.cave.survey.Constants;
import com.bosch.mtprotocol.glm100C.message.edc.EDCOutputMessage;
import com.bosch.mtprotocol.glm100C.message.sync.SyncInputMessage;

/**
 * Bosch PLR 50 C over comm.
 * Created by astoev on 12/24/15.
 */
public class BoschPLR50CBluetoothDevice extends AbstractBoschGLMBluetoothDevice {


    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameStartsWith(aName, "Bosch PLR50C");
    }

    @Override
    public String getDescription() {
        return "Bosch PLR 50 C";
    }

    @Override
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        // 50m laser and 360' clino
        return Constants.MeasureTypes.distance.equals(aMeasureType) || Constants.MeasureTypes.slope.equals(aMeasureType);
    }

    @Override
    protected void turnAutoSyncOn() {
        EDCOutputMessage requestEDCSync = new EDCOutputMessage();
        requestEDCSync.setSyncControl(EDCOutputMessage.MODE_AUTOSYNC_CONTROL_ON);
        requestEDCSync.setDevMode(EDCOutputMessage.READ_ONLY_MODE);
        protocol.sendMessage(requestEDCSync);
    }

    @Override
    protected void configureGLMMode() {
        // TODO set mode
    }

    @Override
    protected int getGLMMode() {
        return SyncInputMessage.MEAS_MODE_INDIRECT_LENGTH;
    }
}
