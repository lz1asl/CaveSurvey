package com.astoev.cave.survey.service.bluetooth.device;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.util.ByteUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CEM iLDM Laser distance meters
 * Created by astoev on 2/21/14.
 */
public class CEMILDMBluetoothDevice extends AbstractBluetoothDevice {

    private static final Set<String> SUPPORTED_DEVICES = new HashSet<String>();
    static {
        SUPPORTED_DEVICES.add("iLDM-150");
    }

    @Override
    public boolean isNameSupported(String aName) {
        return SUPPORTED_DEVICES.contains(aName);
    }

    @Override
    public String getSPPUUIDString() {
        return "00001101-0000-1000-8000-00805F9B34FB";
    }

    @Override
    public String getDescription() {
        return "CEM iLDM 150";
    }

    @Override
    public void triggerMeasures(OutputStream aStream, List<Constants.MeasureTypes> aMeasures) throws IOException {
        // TODO map types to the message?, here single read is asked
        aStream.write(ByteUtils.hexStringToByte("D5F0E00D"));
    }

    @Override
    public List<Measure> decodeMeasure(byte[] aResponseBytes, List<Constants.MeasureTypes> aMeasures) throws DataException {

        if (aResponseBytes.length < 25) {
            Log.i(Constants.LOG_TAG_BT, "Got bytes " + aResponseBytes.length);
            throw new DataException("Bad data");
        }

        if (aResponseBytes[24] != 13) {
            Log.i(Constants.LOG_TAG_BT, "Data validation failed ");
            throw new DataException("Bad data");
        }

        if (aResponseBytes[4] != 0) {
            Log.i(Constants.LOG_TAG_BT, "Error code" + aResponseBytes[4]);
            throw new DataException("Error code " + aResponseBytes[4]);
        }
        Log.i(Constants.LOG_TAG_BT, "Rec mode" + aResponseBytes[5]);

        Log.d(Constants.LOG_TAG_BT, "units " + new String[]{" ", "m", "in", "in+", "ft", "ft&in"}[aResponseBytes[7]]);
        if (1 != aResponseBytes[7]) {
            Log.i(Constants.LOG_TAG_BT, "Please measure in meters!");
            throw new DataException("Please use meters");
        }

        for (int j = 0; j < 4; j++) {
            float measure = (0xFF000000 & aResponseBytes[(8 + j * 4)] << 24
                    | 0xFF0000 & aResponseBytes[(9 + j * 4)] << 16
                    | 0xFF00 & aResponseBytes[(10 + j * 4)] << 8
                    | 0xFF & aResponseBytes[(11 + j * 4)]);

            // accept angle only when measuring angle also
            if (j == 0 && measure > -26843545 && aResponseBytes[5] == 8) {
                Log.i(Constants.LOG_TAG_BT, "Read angle " + measure / 10);
                Measure m = new Measure();
                m.setValue(measure / 10);
                m.setMeasureUnit(Constants.MeasureUnits.degrees);
                m.setMeasure(Constants.Measures.slope);
                return singleMeasureToResult(m);
            }

            if (j == 2 && measure > -26843545) {
                Log.i(Constants.LOG_TAG_BT, "Read distance " + measure / 1000);
                if (!isMeasureRequested(aMeasures, Constants.MeasureTypes.slope)) {
                    Measure m = new Measure();

                    m.setValue(measure / 1000);
                    m.setMeasureUnit(Constants.MeasureUnits.meters);
                    m.setMeasureType(Constants.MeasureTypes.distance);
                    return singleMeasureToResult(m);
                }
            }
        }

        Log.i(Constants.LOG_TAG_BT, "Packet not usefull");
        return null;
    }

    @Override
    public boolean isPassiveBTConnection() {
        return false;
    }

    private boolean isMeasureRequested(List<Constants.MeasureTypes> aMeasures, Constants.MeasureTypes aType) {
        for(Constants.MeasureTypes measure: aMeasures) {
            if (measure == aType) {
                return true;
            }
        }
        return false;
    }

    private List<Measure> singleMeasureToResult(Measure m) {
        List<Measure> measures = new ArrayList<Measure>();
        measures.add(m);
        return measures;
    }

}
