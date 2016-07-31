package com.astoev.cave.survey.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.astoev.cave.survey.BuildConfig;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Fragment that shows about dialog
 *
 * @author Zhivko Mitrev
 */
public class AboutDialog extends DialogFragment {

    /**
     * @see DialogFragment#onCreateDialog(Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Activity activity = getActivity();
        Context context = getActivity().getApplicationContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        TextView titleView = new TextView(context);
        titleView.setText(R.string.about_title);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(10, 10, 10, 10);
        titleView.setTextSize(22);
        titleView.setTextColor(Color.WHITE);
        builder.setCustomTitle(titleView);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.about, null);
        builder.setView(view);

        TextView url = (TextView) view.findViewById(R.id.aboutUrl);
        Linkify.addLinks(url, Linkify.WEB_URLS);
        TextView url2 = (TextView) view.findViewById(R.id.aboutUrl2);
        Linkify.addLinks(url2, Linkify.WEB_URLS);

        StringBuilder versionText = new StringBuilder("v");
        try {
            versionText.append(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.LOG_TAG_UI, "Failed get version for about dialog", e);
        }

        String buildDate = getBuildDate();
        versionText.append(buildDate);

        if (BuildConfig.DEBUG) {
            versionText.append(" (Debug)");
        }

        TextView version = (TextView) view.findViewById(R.id.aboutVersion);
        version.setText(versionText.toString());

        return  builder.create();
    }

    // kind of ugly build date looking at the time the dex files were last updated
    private String getBuildDate() {
        Activity context = ConfigUtil.getContext();
        ZipFile zf = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            return " / " + StringUtils.dateToString(new Date(time));

        } catch(Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed get build date", e);
            return "";
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed close file", e);
                }
            }
        }
    }

}
