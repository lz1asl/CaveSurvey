package com.astoev.cave.survey.service.bluetooth.device.protocol;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice;
import com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm.AbstractBoschGLMBluetoothDevice;
import com.astoev.cave.survey.util.ConfigUtil;
import com.bosch.mtprotocol.MtMessage;
import com.bosch.mtprotocol.glm100C.message.edc.EDCInputMessage;
import com.bosch.mtprotocol.glm100C.message.sync.SyncInputMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoschGlmDeviceProtocol {

    public static List<Measure> decodeMessage(MtMessage aMessage, AbstractBluetoothDevice aDevice) throws DataException {

        if (aMessage instanceof SyncInputMessage syncMessage) {

            Log.d(Constants.LOG_TAG_BT, "Decoding sync message: " + syncMessage);

            return Arrays.asList(
                    new Measure(Constants.MeasureTypes.distance, Constants.MeasureUnits.meters, syncMessage.getResult()),
                    new Measure(Constants.MeasureTypes.slope, Constants.MeasureUnits.degrees, syncMessage.getAngle()));

        } else if (aMessage instanceof EDCInputMessage message) {

            Log.d(Constants.LOG_TAG_BT, "Decoding edc message: " + message);

            List<Measure> measures = new ArrayList<>();
            if (EDCInputMessage.MODE_SINGLE_DISTANCE == message.getDevMode()) {
                // PLR 30 C and PLR 40 C - only distance available
                measures.add(new Measure(Constants.MeasureTypes.distance, Constants.MeasureUnits.meters, message.getResult()));
            } else if (EDCInputMessage.MODE_INDIRECT_LENGTH == message.getDevMode()) {
                // PLR 50 C, GLM 50 C - distance and clino in indirect length mode
                measures.add(new Measure(Constants.MeasureTypes.distance, Constants.MeasureUnits.meters, message.getComp1()));
                if (aDevice.isMeasureSupported(Constants.MeasureTypes.slope)) { // just in case
                    measures.add(new Measure(Constants.MeasureTypes.slope, Constants.MeasureUnits.degrees, message.getComp2()));
                }
            } else {
                warnToUseProperMode(aDevice);
            }
            return measures;
        }

       return null;
    }

    private static void warnToUseProperMode(AbstractBluetoothDevice aDevice) {
        // warn user to use proper device mode
        String requiredMode = ConfigUtil.getContext().getString(((AbstractBoschGLMBluetoothDevice)aDevice).getGLMModesLabel());
        UIUtilities.showNotification(R.string.bt_device_mode, requiredMode);
    }
}
