package com.astoev.cave.survey.service.export;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StringUtils;

/**
 * Created by astoev on 12/7/15.
 */
public class AutoExport {

    public static void processAutoExport() {


        // TODO if enabled

        try {
            if (ConfigUtil.getBooleanProperty(ConfigUtil.PREF_BACKUP)) {
                Log.i(Constants.LOG_TAG_SERVICE, "Start auto export");
                ExcelExport export = new ExcelExport(ConfigUtil.getContext());
                export.setUseUniqueName(false);
                export.setExtension(FileStorageUtil.NAME_DELIMITER + "auto" + export.getExtension());
                String exportPath = export.runExport(Workspace.getCurrentInstance().getActiveProject());
                if (StringUtils.isEmpty(exportPath)) {
                    UIUtilities.showNotification(ConfigUtil.getContext(), R.string.export_io_error, exportPath);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Auto export failed", e);
        }

    }

}
