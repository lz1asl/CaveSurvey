package com.astoev.cave.survey.activity.draw;

/**
 * Created by astoev on 3/6/16.
 */
public class MapDrawingActivity extends AbstractDrawingActivity {

    @Override
    protected boolean showToolbar() {
        // no toolbar when annotating the map
        return false;
    }
}
