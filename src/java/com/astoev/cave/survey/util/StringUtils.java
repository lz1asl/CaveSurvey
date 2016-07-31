package com.astoev.cave.survey.util;

import android.content.res.Resources;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 3/11/12
 * Time: 10:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtils {

    private static final NumberFormat DECIMAL_FORMAT = buildFormatter();

    private static final String EMPTY_STRING = "";
    public static final String SPACE = " ";

    public static final String RESOURCE_PREFIX_UNITS = "unit_";

    private static NumberFormat buildFormatter() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat)nf;
        df.applyPattern("#####.###");
        return df;
    }

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
        return getFloat(aEditField.getText().toString());
    }

    public static boolean isEmpty(EditText aEditText) {
        return aEditText.getText().toString().trim().equals("");
    }

    public static boolean isNotEmpty(String aString) {
        return !isEmpty(aString);
    }

    public static boolean isEmpty(String aString) {
        return aString == null || "".equals(aString);
    }

    public static void setNotNull(EditText aEditText, Float aValue) {
        if (aValue != null) {
            aEditText.setText(StringUtils.floatToLabel(aValue));
        } else {
            aEditText.setText("");
        }
    }

    public static CharSequence extractDynamicResource(Resources aResources, String aKey) {
        return aResources.getText(aResources.getIdentifier(
                aKey, "string", "com.astoev.cave.survey"));
    }

    public static boolean isStringValidFloat(String aString) {
       return getFloat(aString) != null;
    }

    public static Float getFloat(String aString) {
        try {
            return DECIMAL_FORMAT.parse(fixComasInNumber(aString)).floatValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // because of comma instead of dot for Bulgarian input
    private static String fixComasInNumber(String aString) {
        return aString.replace(',', '.');
    }

    public static String dateToDateTimeString(Date aDate) {
        Locale locale = getCurrLocale();

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
        return df.format(aDate);
    }

    public static String dateToString(Date aDate) {
        Locale locale = getCurrLocale();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        return df.format(aDate);
    }

    private static Locale getCurrLocale() {
        String savedLanguage = ConfigUtil.getStringProperty(ConfigUtil.PREF_LOCALE);
        if (StringUtils.isEmpty(savedLanguage)){
            // use any default formatting
            return Locale.getDefault();
        } else {
            // format depending on the locale
            return new Locale(savedLanguage);
        }
    }

}
