package com.astoev.cave.survey.activity.dialog

import android.Manifest.permission
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.astoev.cave.survey.Constants
import com.astoev.cave.survey.R
import com.astoev.cave.survey.activity.UIUtilities
import com.astoev.cave.survey.util.PermissionUtil
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

class GrottoCenterDialog : DialogFragment() {

    private val PERMISSION_REQUEST_INTERNET: Int = 1001;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.grottocenter_dialog_title))
        builder.setIcon(R.drawable.grottocenter_logo)

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.grottocenter_upload, null)
        builder.setView(view)

        val loginButton = view.findViewById<Button>(R.id.grottocenter_login)
        loginButton.setOnClickListener {

            if (PermissionUtil.requestPermission(permission.INTERNET, this.activity, PERMISSION_REQUEST_INTERNET)) {

                val usernameField = view.findViewById<EditText>(R.id.grottocenter_username)
                if (!UIUtilities.validateLength(usernameField, 3))
                    return@setOnClickListener

                val passwordField = view.findViewById<EditText>(R.id.grottocenter_password)
                if (!UIUtilities.validateLength(passwordField, 8))
                    return@setOnClickListener

                loginButton.isEnabled = false

                exchangeCredentialsForToken(usernameField.text.toString(), passwordField.text.toString()) { token ->
                    //                uploadFiles(token)

                }
            }
        }

        // create the Dialog
        return builder.create()
    }

    private fun exchangeCredentialsForToken(username: String, password: String, onTokenReceived: (String) -> Unit) {
        Thread {
            Log.i(Constants.LOG_TAG_SERVICE, "Authenticate $username")
            val url = URL("https://api.grottocenter.org/api/v1/login")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8")
            connection.doOutput = true

            val postData = "{\"email\": \"${username}\", \"password\": \"${password}\"}"

            try {
                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(postData)
                outputStream.flush()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val scanner = Scanner(inputStream).useDelimiter("\\A")
                    val response = if (scanner.hasNext()) scanner.next() else ""
                    val token = JSONObject(response).get("token").toString();
                    Log.i(Constants.LOG_TAG_SERVICE, "authenticated")

                    onTokenReceived(token)
                } else {
                    Log.e(Constants.LOG_TAG_SERVICE, "got $responseCode with ${connection.responseMessage}")
                    UIUtilities.showNotification("Got $responseCode")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG_SERVICE, "got ${e.message} ", e)
                UIUtilities.showNotification("Got ${e.message}")
            } finally {
                connection.disconnect()
            }
        }.start()
    }
}
