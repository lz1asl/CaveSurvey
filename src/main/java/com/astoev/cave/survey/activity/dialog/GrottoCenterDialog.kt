package com.astoev.cave.survey.activity.dialog

import android.Manifest.permission
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.astoev.cave.survey.R
import com.astoev.cave.survey.activity.UIUtilities
import com.astoev.cave.survey.service.export.zip.ZipExport
import com.astoev.cave.survey.service.export.zip.ZipType
import com.astoev.cave.survey.task.GrottoCenterFileUploadTask
import com.astoev.cave.survey.util.PermissionUtil

class GrottoCenterDialog : DialogFragment() {

    private val PERMISSION_REQUEST_INTERNET: Int = 1001

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.grottocenter_dialog_title))
        builder.setIcon(R.drawable.grottocenter_logo)

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.grottocenter_upload, null)
        builder.setView(view)


        // possible values
        val uploadMode = view.findViewById<Spinner>(R.id.grottocenter_upload_type)
        val adapterShareModes: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            view.context,
            R.array.share_type,
            android.R.layout.simple_spinner_item
        )
        adapterShareModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        uploadMode.adapter = adapterShareModes

        val loginButton = view.findViewById<Button>(R.id.grottocenter_upload)
        loginButton.setOnClickListener {

            if (PermissionUtil.requestPermission(permission.INTERNET, this.activity, PERMISSION_REQUEST_INTERNET)) {

                val usernameField = view.findViewById<EditText>(R.id.grottocenter_username)
                if (!UIUtilities.validateLength(usernameField, 3))
                    return@setOnClickListener

                val passwordField = view.findViewById<EditText>(R.id.grottocenter_password)
                if (!UIUtilities.validateLength(passwordField, 8))
                    return@setOnClickListener

                loginButton.isEnabled = false

                val entranceId = view.findViewById<EditText>(R.id.grottocenter_entrance_id).text.toString()

                val export = ZipExport(this.resources)
                export.setZipType(ZipType.fromIndex(uploadMode.selectedItemPosition))

                val uploadTask = GrottoCenterFileUploadTask(usernameField.text.toString(), passwordField.text.toString(), entranceId,
                    view.context, export, object : GrottoCenterFileUploadTask.UploadListener {

                    override fun onUploadProgress(progress: Int) {
                        // Update UI with upload progress
                    }

                    override fun onUploadComplete(success: Boolean) {
                        if (success) {
                            UIUtilities.showNotification(R.string.success)
                            dismiss()
                        } else {
                            UIUtilities.showNotification(R.string.error)
                            loginButton.isEnabled = true
                        }
                    }
                })
                uploadTask.execute()
            }
        }

        // create the Dialog
        return builder.create()
    }

}
