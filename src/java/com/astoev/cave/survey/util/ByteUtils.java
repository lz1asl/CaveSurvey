package com.astoev.cave.survey.util;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 11/23/13
 * Time: 12:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class ByteUtils {
    public static byte[] hexStringToByte(String aMessage) {
        int i = aMessage.length() / 2;
        byte[] arrayOfByte = new byte[i];
        char[] arrayOfChar = aMessage.toCharArray();
        for (int j = 0; j < i; j++) {
            int k = j * 2;
            arrayOfByte[j] = ((byte) (toByte(arrayOfChar[k]) << 4 | toByte(arrayOfChar[(k + 1)])));
        }
        return arrayOfByte;
    }

    public static byte toByte(char paramChar) {
        return (byte) "0123456789ABCDEF".indexOf(paramChar);
    }
}
