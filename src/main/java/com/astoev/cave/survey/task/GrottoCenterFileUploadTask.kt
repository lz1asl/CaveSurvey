package com.astoev.cave.survey.task

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.astoev.cave.survey.Constants
import com.astoev.cave.survey.R
import com.astoev.cave.survey.activity.UIUtilities
import com.astoev.cave.survey.service.Workspace
import com.astoev.cave.survey.service.export.zip.ZipExport
import com.astoev.cave.survey.util.AndroidUtil
import com.astoev.cave.survey.util.ConfigUtil
import com.astoev.cave.survey.util.FileStorageUtil
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

class GrottoCenterFileUploadTask(
    private val username: String,
    private val password: String,
    private val context: Context,
    private val export: ZipExport,
    private val listener: UploadListener
    ) : AsyncTask<Void, Int, Boolean>() {

        companion object {
            const val GROTTOCENTER_URL_PREFIX = "https://api.grottocenter.org/"  // for testing "http://192.168.2.35:1337"
        }
        private lateinit var progressDialog: ProgressDialog

        interface UploadListener {
            fun onUploadProgress(progress: Int)
            fun onUploadComplete(success: Boolean)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage(context.getString(R.string.grottocenter_authenticating))
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

    override fun doInBackground(vararg params: Void?): Boolean {

            Log.i(Constants.LOG_TAG_SERVICE, "Authenticate $username")
            val authConnection = URL(GROTTOCENTER_URL_PREFIX + "/api/v1/login").openConnection() as HttpURLConnection
            authConnection.requestMethod = "POST"
            authConnection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8")
            authConnection.doOutput = true

            val postData = "{\"email\": \"${username}\", \"password\": \"${password}\"}"

            try {
                val outputStream = OutputStreamWriter(authConnection.outputStream)
                outputStream.write(postData)
                outputStream.flush()

                val responseCode = authConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = authConnection.inputStream
                    val scanner = Scanner(inputStream).useDelimiter("\\A")
                    val response = if (scanner.hasNext()) scanner.next() else ""
                    val token = JSONObject(response).get("token").toString();
                    Log.i(Constants.LOG_TAG_SERVICE, "authenticated")
                    authConnection.disconnect()

                    progressDialog.setMessage(context.getString(R.string.grottocenter_preparing))
                    publishProgress(10)

                    Log.i(Constants.LOG_TAG_SERVICE, "Compressing folder")
                    var exportFile: DocumentFile? = null
                    try {
                        exportFile = export.runExport(Workspace.getCurrentInstance().activeProject, null, false)
                        if (exportFile == null) {
                            UIUtilities.showNotification(ConfigUtil.getContext(), R.string.export_io_error, FileStorageUtil.getFullRelativePath(exportFile))
                            return false
                        } else {
                            progressDialog.setMessage(context.getString(R.string.grottocenter_uploading))
                            publishProgress(20)

                            Log.i(Constants.LOG_TAG_SERVICE, "Uploading $exportFile.uri")
                            val boundary = "*****"
                            val lineEnd = "\r\n"
                            val twoHyphens = "--"
                            val uploadConnection = URL("$GROTTOCENTER_URL_PREFIX/api/v1/documents").openConnection() as HttpURLConnection
                            uploadConnection.doInput = true
                            uploadConnection.doOutput = true
                            uploadConnection.useCaches = false
                            uploadConnection.requestMethod = "POST"
                            uploadConnection.setRequestProperty("Connection", "Keep-Alive")
                            uploadConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary; charset=UTF-8")
                            uploadConnection.setRequestProperty("Authorization", "Bearer $token")

                            val projectName = Workspace.getCurrentInstance().activeProject.name;
                            val formData = mutableMapOf(
                                "type" to "Dataset",
                                "title" to "$projectName survey files",
                                "description" to "CaveSurvey ${AndroidUtil.getAppVersion()} generated export",
                                "mainLanguage" to getLanguage(),
                                "option" to "Author created this document",
                                "license" to "{ \"id\": 1, \"isCopyrighted\": true, \"name\": \"CC-BY-SA\", \"text\": \"Attribution-ShareAlike\", \"url\": \"https://creativecommons.org/licenses/by-sa/3.0/\" }"
                            )

//                            addLocation(formData);

                            try {
                             val outputStream = DataOutputStream(uploadConnection.outputStream)

                                for ((key, value) in formData) {
                                    outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                                    outputStream.writeBytes("Content-Disposition: form-data; name=\"$key\"$lineEnd")
                                    outputStream.writeBytes(lineEnd)
                                    outputStream.writeBytes(value + lineEnd)
                                }

                                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                                outputStream.writeBytes("Content-Disposition: form-data; name=\"files\";filename=\"${exportFile.name}\"$lineEnd")
                                outputStream.writeBytes(lineEnd)

                                val fileInputStream = ConfigUtil.getContext().contentResolver.openInputStream(exportFile.uri)

                                if (fileInputStream == null) {
                                    Log.e(Constants.LOG_TAG_SERVICE, "Failed to open stream to the archive ${exportFile.uri}")
                                    return false
                                }

                                val totalSize = exportFile.length()
                                var uploadedSize = 0
                                val buffer = ByteArray(1024)
                                var bytesRead: Int
                                while (fileInputStream.read(buffer).also { bytesRead = it } > 0) {
                                    outputStream.write(buffer, 0, bytesRead)
                                    uploadedSize += bytesRead
                                    publishProgress((uploadedSize * 100 / totalSize).toInt())
                                }

                                outputStream.writeBytes(lineEnd)
                                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                                fileInputStream.close()
                                outputStream.flush()
                                outputStream.close()

                                val responseCode = uploadConnection.responseCode
                                if (responseCode == HttpURLConnection.HTTP_OK) {
                                    return true
                                } else {
                                    Log.e(Constants.LOG_TAG_SERVICE, "got $responseCode with ${uploadConnection.responseMessage}")
                                    UIUtilities.showNotification("Got HTTP $responseCode, ${uploadConnection.responseMessage}")
                                    return false
                                }

                            } catch (e: Exception) {
                                Log.e(Constants.LOG_TAG_SERVICE, "got ${e.message} ", e)
                                UIUtilities.showNotification("Got ${e.message}")
                                return false
                            } finally {
                                uploadConnection.disconnect()
                            }
                        }
                    } catch (aE: java.lang.Exception) {
                        Log.e(Constants.LOG_TAG_SERVICE, "GrottoCenter upload failed", aE)
                        UIUtilities.showNotification(ConfigUtil.getContext(), R.string.export_io_error, FileStorageUtil.getFullRelativePath(exportFile))
                        return false
                    }

                } else {
                    Log.e(Constants.LOG_TAG_SERVICE, "got $responseCode with ${authConnection.responseMessage}")
                    UIUtilities.showNotification("Got HTTP $responseCode, ${authConnection.responseMessage}")
                    return false
                }
            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG_SERVICE, "got ${e.message} ", e)
                UIUtilities.showNotification("Got ${e.message}")
                return false
            } finally {
                authConnection.disconnect()
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            values[0]?.let {
                progressDialog.progress = it
                listener.onUploadProgress(it)
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            listener.onUploadComplete(result)
        }

        private fun getLanguage() : String {

            return when (ConfigUtil.getStringProperty(ConfigUtil.PREF_LOCALE)) {
                "bg" -> "bul"
                "de" -> "ger"
                "el" -> "gre"
                "en" -> "eng"
                "es" -> "spa"
                else -> "000" // hu, pl, ru, zh not present in GrottoCenter
            }
        }

   /* private fun addLocation(formData: MutableMap<String, String>) {

        DaoUtil.getCurrProjectLegs(false).forEach {
            val location = DaoUtil.getLocationByPoint(it.fromPoint)
            if (location != null) {
                formData["latitude"] = LocationUtil.formatLatitude(location.latitude)
                formData["longitude"] = LocationUtil.formatLongitude(location.longitude)
                return
            }
        }
    }*/

}
