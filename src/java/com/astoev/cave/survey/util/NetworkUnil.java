package com.astoev.cave.survey.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
    public static void postJSON(String aUrl, String aContent) throws IOException {

        // prepare
        URL url = new URL(aUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        OutputStream out = null;
        try {
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            // post
            out = new BufferedOutputStream(urlConnection.getOutputStream());
            IOUtils.write(aContent, out);
            out.flush();

            // verify
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new IOException("Status code not expected : " + urlConnection.getResponseCode());
            }

        } finally {
            // clean up
            IOUtils.closeQuietly(out);
            urlConnection.disconnect();
        }
    }
}
