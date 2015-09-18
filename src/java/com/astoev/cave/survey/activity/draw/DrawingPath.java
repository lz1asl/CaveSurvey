package com.astoev.cave.survey.activity.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 10/11/2010
 * Time: 12:44 AM
 * Link: http://www.tutorialforandroid.com/
 */
public class DrawingPath {

    public Path path;
    public DrawingOptions options;

    public DrawingPath() {
        path = new Path();
    }

    public DrawingPath(DrawingOptions options) {
        this();
        this.options = options;
    }

    public void draw(Canvas canvas) {
        canvas.drawPath(path, DrawingOptions.optionsToPaint(options));
    }

}
