/**
 * @author Alessandro Vernassa
 * 
 */
package com.mkyong.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.astoev.cave.survey.R;

public class WebViewActivity extends Activity {
	private WebView webView;
	private ValueCallback<Uri> mUploadMessage;
	private final static int FILECHOOSER_RESULTCODE = 1;

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
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
				Toast.makeText(this.getBaseContext(), strDestination,
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.i("SPLX", "Failed to copy asset file: " + filename);
				Log.i("SPLX", e.toString());
			}
			return;
		}
		if (fileurl.startsWith("data:")) {
			// intent.setData( Uri.parse( "data:text/html;charset=utf-8;base64,"
			// + Base64.encodeToString( html.getBytes(), Base64.NO_WRAP )));
			try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                    final byte[] Contents = Base64.decode(
                            fileurl.split(";base64,")[1], Base64.DEFAULT);
                    FileOutputStream out = new FileOutputStream(strDestination);
                    out.write(Contents);
                    out.flush();
                    out.close();
                    out = null;
                    Toast.makeText(this.getBaseContext(), strDestination,
                            Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(this.getBaseContext(), "Not supported before Android 2.2",
                            Toast.LENGTH_LONG).show();
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

			// ------------WebChromeClient--------------------------------->
			webView.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onGeolocationPermissionsShowPrompt(String origin,
						GeolocationPermissions.Callback callback) {
					Log.i("SPLX", " GeolocationPermissions");
					callback.invoke(origin, true, false);
				}
				public void onConsoleMessage(String message, int lineNumber, String sourceID) {
				    Log.d("SPLX", message + " -- From line "
				                         + lineNumber + " of "
				                         + sourceID);
				  }
				
				// serve per riottenere il focus dopo aver scelto il file da
				// aprire
				public void openFileChooser(ValueCallback<Uri> uploadMsg) {
					mUploadMessage = uploadMsg;
					// ---save file dialog--------------------->
					final String[] mFileFilter = { "*.*", ".jpeg", ".txt",
							".png" };
					FileSelector s = new FileSelector(WebViewActivity.this,
							FileOperation.LOAD, mLoadFileListener, mFileFilter,
							"");
					s.show();
					// ---save file dialog---------------------<
				}

				// For Android 3.0+
				public void openFileChooser(ValueCallback uploadMsg,
						String acceptType) {
					mUploadMessage = uploadMsg;
					// ---save file dialog--------------------->
					final String[] mFileFilter = { "*.*", ".jpeg", ".txt",
							".png" };
					FileSelector s = new FileSelector(WebViewActivity.this,
							FileOperation.LOAD, mLoadFileListener, mFileFilter,
							"");
					s.show();
					// ---save file dialog---------------------<
				}

				// For Android 4.1
				public void openFileChooser(ValueCallback<Uri> uploadMsg,
						String acceptType, String capture) {
					mUploadMessage = uploadMsg;
					// ---save file dialog--------------------->
					final String[] mFileFilter = { "*.*", ".jpeg", ".txt",
							".png" };
					FileSelector s = new FileSelector(WebViewActivity.this,
							FileOperation.LOAD, mLoadFileListener, mFileFilter,
							"");
					s.show();
					// ---save file dialog---------------------<
				}

				// root
				OnHandleFileListener mLoadFileListener = new OnHandleFileListener() {
					@Override
					public void handleFile(final String filePath) {
						Toast.makeText(WebViewActivity.this,
								"Load: " + filePath, Toast.LENGTH_SHORT).show();
						try {
							if (null == mUploadMessage)
								return;
							mUploadMessage.onReceiveValue(Uri.parse(filePath));
							mUploadMessage = null;
						} catch (Exception e) {
							Log.i("SPLX", "Error:" + e.toString());
						}
					}
				};

			});
			// ------------WebChromeClient---------------------------------<

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
					Log.i("SPLX", "Start download:" + url);
					Log.i("SPLX", "contentDisposition:" + contentDisposition);
					Log.i("SPLX", "userAgent:" + userAgent);
					Log.i("SPLX", "filename:" + fileName);

					// ---save file dialog--------------------->
					final String[] mFileFilter = { "*.*", ".jpeg", ".txt",
							".png" };
					Url_da_scaricare = url;
					FileSelector s = new FileSelector(WebViewActivity.this,
							FileOperation.SAVE, mSaveFileListener, mFileFilter,
							fileName);
					s.show();
					// ---save file dialog---------------------<
				}
			});

			// ------------DownloadListener--------------------------------<
			// -----------load index.html---------------------------------->
			webView.loadUrl("file:///android_asset/index.html");
			// ----------load index.html----------------------------------<
			// --------------webview settings------------------------------>
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings()
					.setJavaScriptCanOpenWindowsAutomatically(true);
			webView.getSettings().setSupportMultipleWindows(true);
			// webView.getSettings().setLoadWithOverviewMode(true);
            // TODO check if this is ok with older devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                webView.getSettings().setAllowContentAccess(true);
            }
			 webView.getSettings().setAllowFileAccess(true);
			// webView.getSettings().setAllowFileAccessFromFileURLs(true);
			// webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
			// webView.getSettings().setBuiltInZoomControls(false);
			
			webView.getSettings().setDatabasePath(
					"/data/data/" + this.getPackageName() + "/databases/");
			// webView.getSettings().setSupportZoom(false);
			webView.getSettings().setAppCacheEnabled(true);
		    webView.getSettings().setDatabaseEnabled(true);
		    webView.getSettings().setDomStorageEnabled(true);
			webView.getSettings().setGeolocationEnabled(true);
			webView.getSettings().setGeolocationDatabasePath(
					"/data/data/" + this.getPackageName()); 
			// --------------webview settings------------------------------<
			Log.i("SPLX", "Start app");
		}
		if (savedInstanceState != null)
	        ((WebView)findViewById(R.id.webView1)).restoreState(savedInstanceState);
	}

}