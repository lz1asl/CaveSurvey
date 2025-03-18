package com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.comm.AbstractBluetoothRFCOMMDevice;
import com.astoev.cave.survey.service.bluetooth.device.protocol.BoschGlmDeviceProtocol;
import com.astoev.cave.survey.util.ConfigUtil;
import com.bosch.mtprotocol.MtMessage;
import com.bosch.mtprotocol.MtProtocol;
import com.bosch.mtprotocol.glm100C.MtProtocolImpl;
import com.bosch.mtprotocol.glm100C.event.MtProtocolFatalErrorEvent;
import com.bosch.mtprotocol.glm100C.event.MtProtocolReceiveMessageEvent;
import com.bosch.mtprotocol.glm100C.event.MtProtocolRequestTimeoutEvent;
import com.bosch.mtprotocol.glm100C.message.edc.EDCInputMessage;
import com.bosch.mtprotocol.glm100C.message.sync.SyncInputMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Bosch GLM protocol based devices.
 */

public abstract class AbstractBoschGLMBluetoothDevice extends AbstractBluetoothRFCOMMDevice
        implements MtProtocol.MTProtocolEventObserver {

    protected MtProtocol protocol;
    private MtMessage lastMessage = null;
    private boolean initSyncRequest;
    private boolean ownReadError = false;

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
        // no need to perform actions
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws DataException {

        try {
            return BoschGlmDeviceProtocol.decodeMessage(lastMessage, this);
        } finally {
            lastMessage = null;
        }
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        return lastMessage != null || ownReadError;
    }

    @Override
    public void configure(InputStream anInput, OutputStream anOutput) {

        // initialize the internal Bosch protocol
        protocol = new MtProtocolImpl();
        protocol.addObserver(this);
        protocol.setTimeout(5000);
        protocol.initialize(new GLMBluetoothConnectionWrapper(this, anInput, anOutput));

        // instruct device to send events automatically
        initSyncRequest = true;
        MtMessage configMessage = createGLMConfigMessage();
        protocol.sendMessage(configMessage);
    }

    // turn sync on and enter desired mode
    protected abstract MtMessage createGLMConfigMessage();

    // used to check measurement mode is valid, e.g. not changed meanwhile
    protected abstract List<Integer> getGLMModes();

    // displayed to the user
    public abstract int getGLMModesLabel();

    @Override
    public void onEvent(MtProtocol.MTProtocolEvent event) {

        // something happening on the device
        Log.i(Constants.LOG_TAG_BT, "Got " + event.getClass().getSimpleName());

        if(event instanceof MtProtocolFatalErrorEvent){

            // fatal error
            Log.e(Constants.LOG_TAG_BT, "Received MtProtocolFatalErrorEvent");
            UIUtilities.showNotification(R.string.error);
        } else if(event instanceof MtProtocolReceiveMessageEvent) {

            MtMessage message = ((MtProtocolReceiveMessageEvent) event).getMessage();

            if (message instanceof SyncInputMessage) {
                SyncInputMessage syncMessage = (SyncInputMessage) message;

                if(initSyncRequest) { // Ignore first response
                    initSyncRequest = false;
                    Log.d(Constants.LOG_TAG_BT, "Ignore syncMessage = " + syncMessage);
                    return;
                }

                Log.i(Constants.LOG_TAG_BT, "SyncInputMessageReceived: " + syncMessage.toString());
                if (getGLMModes().contains(syncMessage.getMode())) {
                    if (syncMessage.getLaserOn() == 1) {
                        Log.d(Constants.LOG_TAG_BT, "Ignore laser 1 message");
                        lastMessage = null;
                    } else{
                        Log.d(Constants.LOG_TAG_BT, "Store message");
                        lastMessage = message;
                    }
                } else {
                    warnToUseProperMode();
                }

            } else if(message instanceof EDCInputMessage) {

                if (initSyncRequest) { // Ignore first response
                    initSyncRequest = false;
                    Log.d(Constants.LOG_TAG_BT, "Ignore syncMessage = " + message);
                    return;
                }
                Log.d(Constants.LOG_TAG_BT, "Received EDC: " + message.toString());
                EDCInputMessage edcMessage = (EDCInputMessage) message;
                Log.d(Constants.LOG_TAG_BT, "EDCInputMessageReceived: " + edcMessage.toString());
                if(getGLMModes().contains(edcMessage.getDevMode())) {
                    if (edcMessage.getLaserOn() == 1) {
                        Log.d(Constants.LOG_TAG_BT, "Ignore laser 1 message");
                        lastMessage = null;
                    } else{
                        lastMessage = message;
                    }
                } else {
                    warnToUseProperMode();
                }
            } else {
                Log.d(Constants.LOG_TAG_BT, "Received Unknown message");
                UIUtilities.showNotification(R.string.error);
            }
        } else if(event instanceof MtProtocolRequestTimeoutEvent){
            Log.d(Constants.LOG_TAG_BT, "Received MtProtocolRequestTimeoutEvent");
            UIUtilities.showNotification("Timeout");
        } else {
            Log.e(Constants.LOG_TAG_BT, "Received unknown event");
            UIUtilities.showNotification(R.string.error);
        }
        initSyncRequest = false;
    }

    private void warnToUseProperMode() {
        // warn user to use proper device mode
        String requiredMode = ConfigUtil.getContext().getString(getGLMModesLabel());
        UIUtilities.showNotification(R.string.bt_device_mode, requiredMode);
    }

    @Override
    public boolean useOwnRead() {
        // Bosch protocol responsible for reading the data
        return true;
    }

    @Override
    public boolean getOwnReadError() {
        return ownReadError;
    }

    @Override
    public void onError() {
        Log.e(Constants.LOG_TAG_BT, "Own protocol error");
        protocol.destroy();
        ownReadError = true;
    }
}
