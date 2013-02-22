package com.astoev.cave.survey.util;

import android.widget.EditText;

import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 3/11/12
 * Time: 10:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtils {

    private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance();
    private static final String EMPTY_STRING = "";


    public static String floatToLabel(Float aFloat) {
        if (null == aFloat) {
            return EMPTY_STRING;
        } else {
            return DECIMAL_FORMAT.format(aFloat);
        }
    }

    public static String intToLabel(int anInt) {
        return DECIMAL_FORMAT.format(anInt);
    }

    public static Float getFromEditTextNotNull(EditText aUp) {
        String text = aUp.getText().toString();
        if (null == text || "".equals(text)) {
            return null;
        }
        return Float.parseFloat(text);
    }
}
