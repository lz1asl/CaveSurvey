package com.astoev.cave.survey.activity;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/24/12
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class UIUtilities {

    private static int mNotificationId = 1000;

    private static final String NOTIFICATION_CHANNEL_CAVE_SURVEY = "CaveSurvey";

    public static void showNotification(int aResourceId) {
        showNotification(ConfigUtil.getContext(), aResourceId, null);
    }

    public static void showNotification(int aResourceId, String aParams) {
        showNotification(ConfigUtil.getContext(), aResourceId, aParams);
    }

    public static void showNotification(String aMessage) {
        showNotification(ConfigUtil.getContext(), aMessage, null);
    }

    public static void showNotification(final Activity aContext, final int aResourceId, final Object aParams) {
        aContext.runOnUiThread(() -> {
            Toast toast = Toast.makeText(aContext, aContext.getString(aResourceId, aParams), Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    public static void showNotification(final Activity aContext, final String aMessage, final Object aParams) {
        aContext.runOnUiThread(() -> {
            Toast toast = Toast.makeText(aContext, aMessage, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    public static void showRawMessage(Context aContext, String rawMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(aContext);
        builder.setTitle("Debug");
        builder.setMessage(rawMessage);
        builder.show();
    }

    public static void showAlertDialog(Context aContext, int aTitleId, int aMemoId, Object... params) {
        AlertDialog.Builder builder = new AlertDialog.Builder(aContext);
        builder.setTitle(aTitleId);
        builder.setMessage(aContext.getResources().getString(aMemoId, params));
        builder.show();
    }

    public static void reportException(Context aContext, Exception e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(aContext);
        builder.setTitle(R.string.error);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        builder.setMessage(sw.toString());
        builder.show();
    }

    public static ProgressDialog showBusy(Context aContext, String title) {
        ProgressDialog dialog = ProgressDialog.show(aContext, title,
                aContext.getString(R.string.busy), true);
        dialog.show();
        return dialog;
    }

    public static boolean validateNumber(EditText aEditField, boolean isRequired) {
        if (StringUtils.isEmpty(aEditField)) {
            if (isRequired) {
                aEditField.setError(aEditField.getContext().getString(R.string.required));
                aEditField.requestFocus();
                return false;
            }
            return true;
        } else {
            if (StringUtils.isStringValidFloat(aEditField.getText().toString().trim())) {
                return true;
            } else {
                aEditField.setError(aEditField.getContext().getString(R.string.invalid));
                aEditField.requestFocus();
                return false;
            }
        }
    }

    public static boolean checkAzimuth(EditText aEditText) {
        boolean valid = MapUtilities.isAzimuthValid(StringUtils.getFromEditTextNotNull(aEditText));

        if (!valid) {
            aEditText.setError(aEditText.getContext().getString(R.string.invalid));
            aEditText.requestFocus();
        }

        return valid;
    }

    public static boolean checkSlope(EditText aEditText) {
        Float slope = StringUtils.getFromEditTextNotNull(aEditText);
        boolean valid = slope == null || MapUtilities.isSlopeValid(slope);

        if (!valid) {
            aEditText.setError(aEditText.getContext().getString(R.string.invalid));
        }

        return valid;
    }

    private static void showStatusBarMessage(Context aContext, int aIcon, Class anActivityClass, String aMessage, int color) {

        Intent resultIntent = new Intent(aContext, anActivityClass);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(aContext);
        stackBuilder.addParentStack(anActivityClass);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(aContext, NOTIFICATION_CHANNEL_CAVE_SURVEY)
                .setSmallIcon(aIcon)
                .setContentTitle(aContext.getString(R.string.app_name))
                .setContentText(aMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultPendingIntent);

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            builder.setColor(color);
        }
        hideNotifications(aContext);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(aContext);
        notificationManager.notify(mNotificationId++, builder.build());
    }

    @NonNull
    public static void hideNotifications(Context aContext) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(aContext);
        notificationManager.cancelAll();
    }

    public static void createNotificationChannel(Context aContext) {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            CharSequence name = aContext.getString(R.string.app_name);
            String description = aContext.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_CAVE_SURVEY, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = aContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showDeviceConnectedNotification(Context aContext, String aDevice) {
        try {
            showStatusBarMessage(aContext, R.drawable.ic_cave_survey, BTActivity.class, aContext.getString(R.string.bt_device_connected, aDevice), Color.rgb(255, 234, 0));
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Notification error", e);
        }
    }

    public static void showDeviceDisconnectedNotification(Context aContext, String aDevice) {
        showStatusBarMessage(aContext, R.drawable.ic_no_cave_survey, BTActivity.class, aContext.getString(R.string.bt_device_lost, aDevice), Color.rgb(125, 125, 125));
    }

    public static void cleanStatusBarMessages(Context aContext) {
        NotificationManager notificationManager =
                (NotificationManager) aContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();
    }

}
