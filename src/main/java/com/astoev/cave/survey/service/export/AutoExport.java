package com.astoev.cave.survey.service.export;

import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.FileStorageUtil;

/**
 * Created by astoev on 12/7/15.
 */
public class AutoExport {


    private static Long lastRunTimestamp = null;

    private static final long FIVE_MINUTES_MILLIS = 1000 * 60 * 5;


    public static void notifyUIActivity() {

        if (!ConfigUtil.getBooleanProperty(ConfigUtil.PREF_AUTO_BACKUP)) {
            // auto backup disabled
            return;
        }

        if (Workspace.getCurrentInstance().getActiveProject() == null) {
            // not inside a project
            return;
        }

        long now = System.currentTimeMillis();

        if (lastRunTimestamp == null || lastRunTimestamp + FIVE_MINUTES_MILLIS < now) {
            // time to run export
            processAutoExport();
            lastRunTimestamp = now;
        }

    }

    private static void processAutoExport() {

        try {
            if (ConfigUtil.getBooleanProperty(ConfigUtil.PREF_AUTO_BACKUP)) {
                Log.i(Constants.LOG_TAG_SERVICE, "Start auto export");
                ExcelExport export = new ExcelExport(ConfigUtil.getContext().getResources());
                DocumentFile exportFile = export.runExport(Workspace.getCurrentInstance().getActiveProject(), "auto", false);
                if (exportFile == null) {
                    UIUtilities.showNotification(ConfigUtil.getContext(), R.string.export_io_error, FileStorageUtil.getFullRelativePath(exportFile));
                }
                Log.i(Constants.LOG_TAG_SERVICE, "Export completed");
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Auto export failed", e);
        }
    }

}
