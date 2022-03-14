package com.astoev.cave.survey.service.export.zip;

import static com.astoev.cave.survey.service.export.zip.ZipType.DATA;
import static com.astoev.cave.survey.util.FileStorageUtil.JPG_FILE_EXTENSION;
import static com.astoev.cave.survey.util.FileStorageUtil.PNG_FILE_EXTENSION;

import android.content.res.Resources;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.export.AbstractExport;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipExport extends AbstractExport {

    private static final String MIME_ZIP = "application/zip";
    private static final String EXTENSION_ZIP = ".zip";

    private ZipType mZipType;


    public ZipExport(Resources aResources) {
        super(aResources);
    }

    public void setZipType(ZipType aZipType) {
        mZipType = aZipType;
    }

    @Override
    protected String getExtension() {
        return EXTENSION_ZIP;
    }

    @Override
    protected String getMimeType() {
        return MIME_ZIP;
    }

    @Override
    protected void prepare(Project aProject) {
    }

    @Override
    protected void exportData(Project aProject) throws SQLException {

    }

    @Override
    protected void writeTo(Project aProject, OutputStream aStream) throws IOException {
        DocumentFile projectRoot = FileStorageUtil.getProjectHome(aProject.getName());
        if (projectRoot != null) {
            DocumentFile[] files = projectRoot.listFiles();
            if (files != null) {
                ZipOutputStream out = new ZipOutputStream(aStream);
                for (DocumentFile file : files) {
                    if (file.getName().endsWith(EXTENSION_ZIP)) {
                        continue;
                    }
                    if (DATA.equals(mZipType)
                            && (file.getName().endsWith(JPG_FILE_EXTENSION)
                            || file.getName().endsWith(PNG_FILE_EXTENSION))) {
                        Log.i(Constants.LOG_TAG_SERVICE, "Skip " + file.getName());
                        continue;
                    }
                    Log.i(Constants.LOG_TAG_SERVICE, "Add " + file.getName());
                    out.putNextEntry(new ZipEntry(file.getName()));
                    try (InputStream in = ConfigUtil.getContext().getContentResolver().openInputStream(file.getUri())) {
                        out.write(StreamUtil.read(in));
                        out.closeEntry();
                    }
                }
                out.close();
            }
        }
    }

}
