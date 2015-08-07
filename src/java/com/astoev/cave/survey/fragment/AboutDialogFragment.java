package com.astoev.cave.survey.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.util.StringUtils;

/**
 * Fragment that shows about dialog
 *
 * @author Zhivko Mitrev
 */
public class AboutDialogFragment extends DialogFragment {

    /**
     * @see DialogFragment#onCreateDialog(Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Activity activity = getActivity();
        Context context = getActivity().getApplicationContext();

        // create the title
        String title = getString(R.string.about_title);

        try {
            title = title + StringUtils.SPACE + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.LOG_TAG_UI, "Failed get version for about dialog", e);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        TextView titleView = new TextView(context);
        titleView.setText(title);
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

        return  builder.create();

    }

}
