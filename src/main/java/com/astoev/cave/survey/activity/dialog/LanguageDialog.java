package com.astoev.cave.survey.activity.dialog;

import static com.astoev.cave.survey.Constants.LOG_TAG_SERVICE;
import static java.util.Locale.ENGLISH;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.home.SplashActivity;
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
    private static final int LANG_POLISH = 5;
    private static final int LANG_SPANISH = 6;
    private static final int LANG_GERMAN = 7;
	private static final int LANG_HUNGARIAN = 8;

    private SplashActivity parent;


    public LanguageDialog() {
    }

    public LanguageDialog(SplashActivity aParent) {
        parent = aParent;
    }

    /**
     * @see DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.lang_title);
        builder.setIcon(R.drawable.ic_baseline_language_24);
        ArrayAdapter<String> langAdapter =
                new ArrayAdapter<>(this.getActivity(),
                        android.R.layout.simple_list_item_1,
                        getResources().getStringArray(R.array.lang_array));

        builder.setAdapter(langAdapter, (dialogArg, whichArg) -> {

            Locale locale;
            switch (whichArg) {
                case LANG_ENGLISH:
                    locale = ENGLISH;
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
                case LANG_POLISH:
                    locale = new Locale("pl", "PL");
                    break;
                case LANG_SPANISH:
                    locale = new Locale("es", "ES");
                    break;
                case LANG_GERMAN:
                    locale = new Locale("de", "DE");
                    break;
				case LANG_HUNGARIAN:
                    locale = new Locale("hu", "HU");
                    break;	

                default:
                    locale = ENGLISH;
                    break;
            }

            // change locale only if it is different
            String savedLanguage = ConfigUtil.getStringProperty(ConfigUtil.PREF_LOCALE);
            if (!locale.getLanguage().equals(savedLanguage)){

                // create preferred locale
                setLocale(locale);

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

    private void setLocale(Locale locale) {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // save settings
        ConfigUtil.setStringProperty(ConfigUtil.PREF_LOCALE, locale.getLanguage());
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        if (parent != null) {
            // user cancelled during initialization, use default language
            Log.i(LOG_TAG_SERVICE, "Use english");
            setLocale(ENGLISH);

            parent.retry(null);
        }
    }
}
