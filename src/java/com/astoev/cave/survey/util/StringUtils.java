package com.astoev.cave.survey.util;

import android.content.res.Resources;
import android.widget.EditText;

import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 3/11/12
 * Time: 10:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtils {

    private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#####.###");
    private static final String EMPTY_STRING = "";
    public static final String SPACE = " ";

    public static final String RESOURCE_PREFIX_UNITS = "units_";


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

    public static Float getFromEditTextNotNull(EditText aEditField) {
        if (StringUtils.isEmpty(aEditField)) {
            return null;
        }
        return Float.parseFloat(aEditField.getText().toString());
    }
    
    public static boolean isEmpty(EditText aEditText){
        return aEditText.getText().toString().trim().equals("");
    }

    public static boolean isNotEmpty(String aString){
        return !isEmpty(aString);
    }

    public static boolean isEmpty(String aString){
        return aString == null || "".equals(aString);
    }

    public static CharSequence extractDynamicResource(Resources aResources, String aKey) {
        return aResources.getText(aResources.getIdentifier(
                aKey, "string", "com.astoev.cave.survey"));
    }
}
