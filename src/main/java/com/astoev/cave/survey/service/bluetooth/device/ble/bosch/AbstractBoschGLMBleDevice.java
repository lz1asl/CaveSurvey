package com.astoev.cave.survey.service.bluetooth.device.ble.bosch;

import static com.astoev.cave.survey.service.bluetooth.device.protocol.BoschGlmDeviceProtocol.warnToUseProperMode;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.service.bluetooth.device.ble.AbstractBluetoothLEDevice;
import com.astoev.cave.survey.service.bluetooth.device.protocol.BoschGlmDeviceProtocol;
import com.bosch.mtprotocol.MtMessage;
import com.bosch.mtprotocol.MtProtocol;
import com.bosch.mtprotocol.glm100C.MtProtocolBLEImpl;
import com.bosch.mtprotocol.glm100C.connection.MtAsyncConnection;
import com.bosch.mtprotocol.glm100C.event.MtProtocolFatalErrorEvent;
import com.bosch.mtprotocol.glm100C.event.MtProtocolReceiveMessageEvent;
import com.bosch.mtprotocol.glm100C.message.sync.SyncInputMessage;

import java.util.List;

public abstract class AbstractBoschGLMBleDevice extends AbstractBluetoothLEDevice implements MtProtocol.MTProtocolEventObserver  {

    protected MtProtocol protocol;
    private MtMessage lastMessage = null;

    private MtAsyncConnection connection;

    private boolean initSyncRequest;
    private boolean ownReadError = false;


    @Override
    public boolean useServiceMatch() {
        return true;
    }

    @Override
    public boolean needCharacteristicIndication() {
        return true;
    }

    @Override
    public List<Measure> characteristicToMeasures(BluetoothGattCharacteristic aCharacteristic, List<Constants.MeasureTypes> aMeasureTypes) throws DataException {
        try {
            return BoschGlmDeviceProtocol.decodeMessage(lastMessage, this);
        } finally {
            lastMessage = null;
        }
    }


    @Override
    public void configure(BluetoothDevice aDevice) {

        // initialize the internal Bosch protocol
        protocol = new MtProtocolBLEImpl();
        protocol.addObserver(this);
        protocol.setTimeout(5000);
        protocol.initialize(new BLEConnection(new MTBluetoothDevice(aDevice, getDescription()), this);

        // instruct device to send events automatically
        initSyncRequest = true;
        MtMessage configMessage = createGLMConfigMessage();
        protocol.sendMessage(configMessage);





    /*    if(bluetoothDevice!=null) {
            if (BluetoothUtils.validateGLM100Name(bluetoothDevice)) {
                // GLM 100 device
                final SyncOutputMessage requestDoSync = new SyncOutputMessage();
                requestDoSync.setSyncControl(SyncOutputMessage.MODE_AUTOSYNC_CONTROL_ON);
                this.protocol.sendMessage(requestDoSync);
                Log.d(TAG, "Sync started GLM 100...");
            } else if (BluetoothUtils.validateEDCDevice(bluetoothDevice)) {
                // Exchange Data Container (EDC) based device
                final EDCOutputMessage requestEDCSync = new EDCOutputMessage();
                requestEDCSync.setSyncControl(EDCOutputMessage.MODE_AUTOSYNC_CONTROL_ON);
                requestEDCSync.setDevMode(EDCOutputMessage.READ_ONLY_MODE);
                this.protocol.sendMessage(requestEDCSync);
                Log.d(TAG, "Sync started EDC device...");
            }
        }*/
    }


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
                    warnToUseProperMode(this);
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

    public void onError() {
        Log.e(Constants.LOG_TAG_BT, "Own protocol error");
        protocol.destroy();
        ownReadError = true;
    }

    protected abstract MtMessage createGLMConfigMessage();

    protected abstract List<Integer> getGLMModes();


}
