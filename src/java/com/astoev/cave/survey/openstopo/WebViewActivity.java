/**
 * @author Alessandro Vernassa
 *
 */
package com.astoev.cave.survey.openstopo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.util.StreamUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Used to embeed OpensTopo http://www.openspeleo.org/openspeleo/openstopo.html.
 */
public class WebViewActivity extends Activity {

    private WebView webView;
    private ValueCallback<Uri> mUploadMessage;

    // ---------------------------Downloadmanager------------------------------------------>
    // https://groups.google.com/forum/?fromgroups#!topic/android-developers/DW-LVc97W8Y
    public void downloadfileto(String fileurl, String filename) {
        String strDestination = filename;

        if (fileurl.startsWith("file:")) {
            InputStream in = null;
            OutputStream out = null;
            try {
                AssetManager assetManager = getAssets();
                in = assetManager.open(fileurl.replace(
                        "file:///android_asset/", ""));
                out = new FileOutputStream(strDestination);
                copyFile(in, out);
                UIUtilities.showNotification(this, strDestination, null);
            } catch (Exception e) {
                Log.i("SPLX", "Failed to copy asset file: " + filename);
                Log.i("SPLX", e.toString());
            } finally {
                StreamUtil.closeQuietly(in);
                StreamUtil.closeQuietly(out);
            }
            return;
        }
        if (fileurl.startsWith("data:")) {
            // intent.setData( Uri.parse( "data:text/html;charset=utf-8;base64,"
            // + Base64.encodeToString( html.getBytes(), Base64.NO_WRAP )));
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                    final byte[] contents = Base64.decode(
                            fileurl.split(";base64,")[1], Base64.DEFAULT);
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(strDestination);
                        out.write(contents);
                        out.flush();
                    } finally {
                        StreamUtil.closeQuietly(out);
                    }
                    UIUtilities.showNotification(this, strDestination, null);
                } else {
                    UIUtilities.showNotification(this, "Not supported before Android 2.2", null);
                }
            } catch (Exception e) {
                Log.i("SPLX", "Destination = : " + strDestination);
                Log.i("SPLX", "Failed to write file: " + filename);
                Log.i("SPLX", e.toString());
            }
            return;
        }
        if (fileurl.startsWith("http:")) { // non viene chiamata perche passa
            // dal browser
            return;
        }
        // altro:
        try {
            Toast.makeText(this.getBaseContext(), "TODO:" + fileurl,
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileurl));
            startActivity(intent);
        } catch (Exception e) {
            Log.i("SPLX", e.toString());
        }
    }

    // ---------------------------Downloadmanager------------------------------------------<
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("unused")
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        if (webView == null) {
            webView = (WebView) findViewById(R.id.webView1);

            // ------------DownloadListener-------------------------------->
            webView.setDownloadListener(new DownloadListener() {
                String Url_da_scaricare = "";
                // root
                OnHandleFileListener mSaveFileListener = new OnHandleFileListener() {
                    @Override
                    public void handleFile(final String filePath) {
                        String dirName = new File(filePath).getPath();
                        String fileName = new File(filePath).getName();
                        Toast.makeText(WebViewActivity.this,
                                "Load: " + filePath, Toast.LENGTH_SHORT).show();
                        Log.i("SPLX", "Scarico" + Url_da_scaricare + " in "
                                + dirName);
                        downloadfileto(Url_da_scaricare, filePath);
                    }
                };

                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimetype,
                                            long contentLength) {
                    String fileName = URLUtil.guessFileName(url,
                            contentDisposition, mimetype);
                    if (fileName.startsWith("octet-stream;base64")) {
                        fileName = "OpensTopoExport";
                    }
                    Log.i("SPLX", "Start download:" + url);
                    Log.i("SPLX", "contentDisposition:" + contentDisposition);
                    Log.i("SPLX", "userAgent:" + userAgent);
                    Log.i("SPLX", "filename:" + fileName);

                    // ---save file dialog--------------------->
                    final String[] mFileFilter = {"*.*", ".jpeg", ".txt",
                            ".png"};
                    Url_da_scaricare = url;
                    FileSelector s = new FileSelector(WebViewActivity.this,
                            mSaveFileListener, mFileFilter,
                            fileName);
                    s.show();
                    // ---save file dialog---------------------<
                }
            });

            // ------------DownloadListener--------------------------------<
            webView.loadUrl("file:///android_asset/index.html");

            // --------------webview settings------------------------------>
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings()
                    .setJavaScriptCanOpenWindowsAutomatically(true);
            webView.getSettings().setSupportMultipleWindows(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            // TODO check if this is ok with older devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                webView.getSettings().setAllowContentAccess(true);
            }
            webView.getSettings().setAllowFileAccess(true);
//            webView.getSettings().setAllowFileAccessFromFileURLs(true);
//            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            webView.getSettings().setBuiltInZoomControls(true);


            webView.getSettings().setDatabasePath(
                    "/data/data/" + this.getPackageName() + "/databases/");
            // webView.getSettings().setSupportZoom(false);
            webView.getSettings().setAppCacheEnabled(true);
            webView.getSettings().setDatabaseEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setGeolocationEnabled(true);
            webView.getSettings().setGeolocationDatabasePath(
                    "/data/data/" + this.getPackageName());

            // pass json to the web view
            webView.addJavascriptInterface(new CaveSurveyJSInterface(getIntent().getStringExtra("path")), "CaveSurveyJSInterface");

            // --------------webview settings------------------------------<
            Log.i("SPLX", "Start app");
        }
        if (savedInstanceState != null)
            ((WebView) findViewById(R.id.webView1)).restoreState(savedInstanceState);
    }

    // json interface
    class CaveSurveyJSInterface {

        private String caveSurveyFilePath;

        public CaveSurveyJSInterface(String aPath) {
            caveSurveyFilePath = aPath;
        }

        @JavascriptInterface
        public String getProjectFile() {
            return caveSurveyFilePath;
        }

        @JavascriptInterface
        public String getProjectData() {
            InputStream in = null;
            try {
                in = new FileInputStream(caveSurveyFilePath);
                return new String(StreamUtil.read(in));
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_UI, "Failed to load json", e);
            } finally {
                StreamUtil.closeQuietly(in);
            }
            return null;
        }
    }

}