package com.astoev.cave.survey.service.export;

import android.content.res.Resources;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.FileStorageUtil;

import org.apache.poi.util.IOUtils;
import org.json.JSONException;

import java.io.OutputStream;
import java.sql.SQLException;

public abstract class AbstractExport {

    protected Resources mResources;

    public AbstractExport(Resources aResources) {
        mResources = aResources;
    }

    // exported file info
    protected abstract String getExtension();
    protected abstract String getMimeType();

    protected abstract void prepare(Project aProject) throws SQLException;
    protected abstract void writeTo(Project aProject, OutputStream aStream) throws JSONException, Exception;



    // public method for starting export
    public DocumentFile runExport(Project aProject, String suffix, boolean unique) throws Exception {

        OutputStream out = null;
        try {
            prepare(aProject);

            String exportSuffix = suffix == null ? getExtension() : FileStorageUtil.NAME_DELIMITER + suffix + getExtension();
            DocumentFile exportFile = FileStorageUtil.prepareProjectExport(aProject, getMimeType(), exportSuffix, unique);
            out = ConfigUtil.getContext().getContentResolver().openOutputStream(exportFile.getUri());

            writeTo(aProject, out);

            return exportFile;
        } catch (Exception t) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failed with export", t);
            throw t;
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

}
