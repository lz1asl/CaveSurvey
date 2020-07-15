package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.util.ConfigUtil;

import java.util.Locale;

/**
 * Dialog for choosing Language
 * 
 * @author jmitrev
 */
public class LanguageDialog extends DialogFragment {
    
    private static final int LANG_ENGLISH = 0;
    private static final int LANG_BULGARIAN = 1;
    private static final int LANG_CHINESE = 2;
    private static final int LANG_RUSSIAN = 3;
    private static final int LANG_GREEK = 4;

    /**
     * @see DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.lang_title);
        ArrayAdapter<String> langAdapter =
                new ArrayAdapter<>(this.getActivity(),
                        android.R.layout.simple_list_item_1,
                        getResources().getStringArray(R.array.lang_array));
        
        builder.setAdapter(langAdapter, (dialogArg, whichArg) -> {

            Locale locale;
            switch (whichArg) {
                case LANG_ENGLISH:
                    locale = Locale.ENGLISH;
                    break;
                case LANG_BULGARIAN:
                    locale = new Locale("bg");
                    break;
                case LANG_CHINESE:
                    locale = Locale.SIMPLIFIED_CHINESE;
                    break;
                case LANG_RUSSIAN:
                    locale = new Locale("ru","RU");
                    break;
                case LANG_GREEK:
                    locale = new Locale("el", "GR");
                    break;

                default:
                    locale = Locale.ENGLISH;
                    break;
            }

            // change locale only if it is different
            String savedLanguage = ConfigUtil.getStringProperty(ConfigUtil.PREF_LOCALE);
            if (!locale.getLanguage().equals(savedLanguage)){

                // create prefurred locale
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;

                Resources resources = ((Dialog)dialogArg).getOwnerActivity().getBaseContext().getResources();
                resources.updateConfiguration(config, resources.getDisplayMetrics());

                // save settings
                ConfigUtil.setStringProperty(ConfigUtil.PREF_LOCALE, locale.getLanguage());

                // restart parent activity
                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);
            }
        });
        
        builder.setNegativeButton(android.R.string.cancel, (dialogArg, whichArg) -> {
            // cancel
            LanguageDialog.this.getDialog().cancel();
        });
        
        return builder.create();
    }

}
