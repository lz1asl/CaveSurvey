package com.astoev.cave.survey.openstopo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.documentfile.provider.DocumentFile;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StreamUtil;

import java.io.InputStream;

/**
 * Used to embeed OpensTopo http://www.openspeleo.org/openspeleo/openstopo.html.
 */
public class WebViewActivity extends Activity {

    private WebView webView;


    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("unused")
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        initializeView(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        initializeView(savedInstanceState);
    }

    private void initializeView(Bundle savedInstanceState) {
        if (webView == null) {
            webView = findViewById(R.id.webView1);


            final String projectName = getIntent().getStringExtra("projectName");
            final String exportPath = getIntent().getStringExtra("path");
            final CaveSurveyJSInterface jsInterface = new CaveSurveyJSInterface(exportPath, projectName);

            // pass json to the web view
            webView.addJavascriptInterface(jsInterface, "CaveSurveyJSInterface");

            // --------------webview settings------------------------------>
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings()
                    .setJavaScriptCanOpenWindowsAutomatically(true);
            webView.getSettings().setSupportMultipleWindows(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setAllowContentAccess(true);
            webView.getSettings().setAllowFileAccess(true);
            webView.getSettings().setBuiltInZoomControls(true);


            webView.getSettings().setDatabasePath(
                    "/data/data/" + this.getPackageName() + "/databases/");
//            webView.getSettings().setAppCacheEnabled(true);
            webView.getSettings().setDatabaseEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setGeolocationEnabled(true);
            webView.getSettings().setGeolocationDatabasePath(
                    "/data/data/" + this.getPackageName());

            // --------------webview settings------------------------------<


            // ------------load--------------------------------<
            Log.i("SPLX", "Start app");
            webView.loadUrl("file:///android_asset/index.html");

        }
        if (savedInstanceState != null) {
            ((WebView) findViewById(R.id.webView1)).restoreState(savedInstanceState);
        }
    }

    // json interface
    class CaveSurveyJSInterface {

        private Uri caveSurveyFilePath;
        private String projectName;

        public CaveSurveyJSInterface(String aPath, String aProjectName) {
            caveSurveyFilePath = Uri.parse(aPath);
            projectName = aProjectName;
        }

        @JavascriptInterface
        public String getProjectFile() {
            Log.i(Constants.LOG_TAG_UI, "Loading in OpensTopo : " + caveSurveyFilePath);
            return caveSurveyFilePath.getLastPathSegment();
        }

        @JavascriptInterface
        public String getProjectData() {
            Log.i(Constants.LOG_TAG_UI, "Loading data");
            InputStream in = null;
            try {
                in = ConfigUtil.getContext().getContentResolver().openInputStream(caveSurveyFilePath);
                return new String(StreamUtil.read(in));
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_UI, "Failed to load json", e);
            } finally {
                StreamUtil.closeQuietly(in);
            }
            return null;
        }

        @JavascriptInterface
        public void downloadFile(String fileName, String content, String mimeType) throws Exception {
            Log.i(Constants.LOG_TAG_SERVICE, "Downloading " + fileName);
            DocumentFile exportFile = FileStorageUtil.addProjectFile(WebViewActivity.this, Workspace.getCurrentInstance().getActiveProject(), null, fileName, mimeType, content.getBytes(), false);
            UIUtilities.showNotification(WebViewActivity.this, R.string.export_done, FileStorageUtil.getFullRelativePath(exportFile));
        }

        @JavascriptInterface
        public void goBack() {
            Log.i(Constants.LOG_TAG_UI, "Back from OpensTopo");
            finish();
        }
    }

}