package com.astoev.cave.survey.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.astoev.cave.survey.Constants;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by astoev on 10/22/15.
 */
public class NetworkUnil {

    public static boolean isNetworkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) ConfigUtil.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // see http://developer.android.com/reference/java/net/HttpURLConnection.html
    public static void postJSON(String aUrl, String aContent) throws Exception {

        // run in background to avoid NetworkOnMainThreadException
        AsyncTask task = new AsyncTask() {

            @Override
            protected Exception doInBackground(Object[] params) {
                try {
                    // prepare
                    URL url = new URL((String) params[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    OutputStream out = null;
                    try {
                        urlConnection.setRequestProperty("Content-Type", "application/json");
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setDoOutput(true);
                        urlConnection.setChunkedStreamingMode(0);

                        // post
                        out = new BufferedOutputStream(urlConnection.getOutputStream());
                        IOUtils.write((String) params[1], out);
                        out.flush();

                        // verify
                        if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                            throw new IOException("Status code not expected : " + urlConnection.getResponseCode());
                        }

                        // all OK
                        return null;

                    } finally {
                        // clean up
                        IOUtils.closeQuietly(out);
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_SERVICE, "Task error", e);
                    return e;
                }
            }
        };

        task.execute(aUrl, aContent);
        Exception error = (Exception) task.get();
        if (error != null) {
            throw error;
        }

    }
}
