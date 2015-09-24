package com.astoev.cave.survey.activity.draw;

import android.graphics.Canvas;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 10/11/2010
 * Time: 12:44 AM
 * Link: http://www.tutorialforandroid.com/
 */
public class DrawingPath {

    private LoggedPath mPath;
    private DrawingOptions mOptions;

    public DrawingPath() {
        mPath = new LoggedPath();
    }

    public DrawingPath(DrawingOptions aOptions) {
        this();
        this.mOptions = DrawingOptions.copy(aOptions);
    }

    public void draw(Canvas aCanvas) {
        aCanvas.drawPath(mPath, DrawingOptions.optionsToPaint(mOptions));
    }

    public DrawingOptions getOptions() {
        return mOptions;
    }

    public void setOptions(DrawingOptions options) {
        mOptions = options;
    }

    public LoggedPath getPath() {
        return mPath;
    }

    public void setPath(LoggedPath aPath) {
        mPath = aPath;
    }
}
