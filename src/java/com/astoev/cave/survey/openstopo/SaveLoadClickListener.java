package com.astoev.cave.survey.openstopo;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.astoev.cave.survey.R;

import java.io.File;

/**
 * This Listener handles Save or Load button clicks.
 */
public class SaveLoadClickListener implements OnClickListener {

    /** Performed operation. */

    /**
     * FileSelector in which you used SaveLoadClickListener
     */
    private final FileSelector mFileSelector;

    private final Context mContext;

    /**
     * @param fileSelector The FileSeletor which used this Listener.
     * @param context      context.
     */
    public SaveLoadClickListener(final FileSelector fileSelector, final Context context) {
        mFileSelector = fileSelector;
        mContext = context;
    }

    @Override
    public void onClick(final View view) {
        final String text = mFileSelector.getSelectedFileName();
        if (checkFileName(text)) {
            final String filePath = mFileSelector.getCurrentLocation().getAbsolutePath() + File.separator + text;
            final File file = new File(filePath);
            int messageText = 0;
            // Check file access rights.
            if ((file.exists()) && (!file.canWrite())) {
                messageText = R.string.cannotSaveFileMessage;
            }
            if (messageText != 0) {
                // Access denied.
                final Toast t = Toast.makeText(mContext, messageText, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            } else {
                // Access granted.
                mFileSelector.mOnHandleFileListener.handleFile(filePath);
                mFileSelector.dismiss();
            }
        }
    }

    /**
     * Check if file name is correct, e.g. if it isn't empty.
     *
     * @return False, if file name is empty true otherwise.
     */
    boolean checkFileName(String text) {
        if (text.length() == 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.information);
            builder.setMessage(R.string.fileNameFirstMessage);
            builder.setNeutralButton(R.string.okButtonText, null);
            builder.show();
            return false;
        }
        return true;
    }
}
