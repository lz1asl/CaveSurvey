package com.astoev.cave.survey.activity.draw;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;

import java.sql.SQLException;

/**
 * Created by astoev on 3/6/16.
 */
public class PointDrawingActivity extends AbstractDrawingActivity {

    @Override
    protected boolean showToolbar() {
        // keep leg toolbar while drawing the point
        return true;
    }

    @Override
    protected String getScreenTitle() {
        try {
            StringBuilder builder = new StringBuilder(getString(R.string.leg));
            builder.append(getWorkspace().getActiveLeg().buildLegDescription(true));
            builder.append(": ");
            builder.append(getString(R.string.main_add_sketch));

            return builder.toString();
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to create activity's name", e);
        }
        return null;
    }
}
