package com.astoev.cave.survey.service.bluetooth.util;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.bluetooth.Measure;

import java.util.ArrayList;
import java.util.List;

public class DistoXBLEProtocol extends DistoXProtocol {


    /*
    In the disto x2's protocol, two packets are sent for one set of shot data. In disto xble, these two packets are combined into a single 17-byte packet.

    1 byte              2 -9 byte               10 -17 byte
    Packet identifier   1 packet of disto x2    2 packet of disto x2
     */
    public static List<Measure> parseBleDataPacket(byte[] dataPacket) {

        List<Measure> measures = new ArrayList<>();

        byte type = dataPacket[1];
        int op = type & 0x3F;
        if (isDataPacket(dataPacket)) {
            double distance = ((float) (getUnsigned(dataPacket, 2) + (getUnsigned(dataPacket, 3) << 8))) / 1000;
            double azimuth = (getUnsigned(dataPacket, 4) + (getUnsigned(dataPacket, 5) << 8)) * 180.0 / 32768.0;
            double inclinationRad = (getUnsigned(dataPacket, 6) + (getUnsigned(dataPacket, 7) << 8));
            double inclination = inclinationRad >= 32768 ? (65536 - inclinationRad) * (-90.0) / 16384.0 :  inclinationRad * 90.0 / 16384.0;

            if (op == 1) { // survey data
                // 17 bit distance
                distance = distance + ((type & 0x40) << 10);
                // cm resolution above 100m
                if (distance > 100000) distance = (distance - 90000) * 10;

                Measure angleMeasure = new Measure(Constants.MeasureTypes.angle, Constants.MeasureUnits.degrees, (float) azimuth);
                measures.add(angleMeasure);
                Log.i(Constants.LOG_TAG_BT, "Got angle " + angleMeasure);

                Measure slopeMeasure = new Measure(Constants.MeasureTypes.slope, Constants.MeasureUnits.degrees, (float) inclination);
                measures.add(slopeMeasure);
                Log.i(Constants.LOG_TAG_BT, "Got slope " + slopeMeasure);

                Measure distanceMeasure = new Measure(Constants.MeasureTypes.distance, Constants.MeasureUnits.meters, (float) distance);
                measures.add(distanceMeasure);
                Log.i(Constants.LOG_TAG_BT, "Got distance " + distanceMeasure);
            }

        }
        return measures;
    }

    private static int getUnsigned(byte[] data, int index) {
        int single = data[index]  & 0xff;
        return single < 0 ? single + 256 : single;
    }

  /*  public static byte[] createAcknowledgementPacket(byte[] dataPacket) {
        byte [] ack = new byte [1];
        ack[0] = (byte) (dataPacket[1] & 0x80 | 0x55);
        return ack;
    }*/

}
