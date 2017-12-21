package com.astoev.cave.survey.service.bluetooth.device.comm.bosch.glm;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.bosch.mtprotocol.glm100C.connection.MtAsyncConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by astoev on 12/13/17.
 */

public class GLMBluetoothConnectionWrapper implements MtAsyncConnection {

    private InputStream in;
    private OutputStream out;
    private AbstractBoschGLMBluetoothDevice deviceSpec;

    public GLMBluetoothConnectionWrapper(AbstractBoschGLMBluetoothDevice aDevice, InputStream aIn, OutputStream aOut) {
        in = aIn;
        out = aOut;
        deviceSpec = aDevice;
    }

    @Override
    public boolean isOpen() {
        // streams open before me
        return true;
    }

    @Override
    public void openConnection() {
        // stream already open
    }

    @Override
    public void closeConnection() {
        // error occured internally
        Log.e(Constants.LOG_TAG_BT, "Close connection requested");
        deviceSpec.onError();
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        if (in != null) {
            return in.read(buffer);
        }
        return 0;
    }

    @Override
    public void write(byte[] data) throws IOException {
        if (out != null) {
            out.write(data);
        }
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void addObserver(MTAsyncConnectionObserver observer) {
        // connection state managed outside me
    }

    @Override
    public void removeObserver(MTAsyncConnectionObserver observer) {
        // connection state managed outside me
    }
}
