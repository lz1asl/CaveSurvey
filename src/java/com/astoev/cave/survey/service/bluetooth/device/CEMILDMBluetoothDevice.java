package com.astoev.cave.survey.service.bluetooth.device;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import static com.astoev.cave.survey.Constants.MeasureTypes;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.service.bluetooth.Measure;
import com.astoev.cave.survey.util.ByteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * CEM iLDM Laser distance meters
 * Created by astoev on 2/21/14.
 */
public class CEMILDMBluetoothDevice extends AbstractBluetoothDevice {


    @Override
    public boolean isNameSupported(String aName) {
        return deviceNameEquals(aName, "iLDM-150");
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
    public void configure(InputStream anInput, OutputStream anOutput) throws IOException {
        // TODO
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
                m.setMeasureType(MeasureTypes.slope);
                return singleMeasureToResult(m);
            }

            if (j == 2 && measure > -26843545) {
                Log.i(Constants.LOG_TAG_BT, "Read distance " + measure / 1000);
                if (isMeasureRequested(aMeasures, Constants.MeasureTypes.distance)) {
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
    public boolean isMeasureSupported(Constants.MeasureTypes aMeasureType) {
        return MeasureTypes.distance.equals(aMeasureType) || MeasureTypes.slope.equals(aMeasureType);
    }

    @Override
    public boolean isFullPacketAvailable(byte[] aBytesBuffer) {
        // 25 bytes messages for CEM iLDM
        return aBytesBuffer.length >= 25;
    }

    private boolean isMeasureRequested(List<Constants.MeasureTypes> aMeasures, Constants.MeasureTypes aType) {
        for(Constants.MeasureTypes measure: aMeasures) {
            if (measure.equals(aType)) {
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


    @Override
    public void keepAlive(OutputStream aStreamOut, InputStream aStreamIn) {
        /**
         * AH!  if you send:            (1 Times / Minute)
         byte[] bout = new byte[25];
         bout[0]  = (byte) 213;
         bout[1]  = (byte) 239;        // or other
         bout[2]  = (byte) 225;
         bout[3]  = (byte) 130;
         ...

         the Device stays on
         */
    }
}
