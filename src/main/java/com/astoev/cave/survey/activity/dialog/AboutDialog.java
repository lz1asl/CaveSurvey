package com.astoev.cave.survey.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Fragment that shows about dialog
 *
 * @author Zhivko Mitrev
 */
public class AboutDialog extends DialogFragment  {

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

        StringBuilder userGuideText = new StringBuilder("<a href=\"");
        userGuideText.append(getString(R.string.about_help));
        userGuideText.append("\">");
        userGuideText.append(getString(R.string.about_user_guide));
        userGuideText.append("</a>");
        TextView userGuide = (TextView) view.findViewById(R.id.aboutUserGuide);
        userGuide.setText(Html.fromHtml(userGuideText.toString()));
        userGuide.setMovementMethod(LinkMovementMethod.getInstance());

        return  builder.create();
    }


    private String getBuildDate() {
        try {
            Context c = ConfigUtil.getContext();
            PackageInfo packageInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            String appBuildDate = SimpleDateFormat.getDateInstance().format(new Date(packageInfo.lastUpdateTime));
            return " / " + appBuildDate;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to get build date", e);
            return "";
        }
    }

}
