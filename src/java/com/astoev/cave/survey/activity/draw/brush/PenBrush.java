package com.astoev.cave.survey.activity.draw.brush;

import com.astoev.cave.survey.activity.draw.LoggedPath;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 01/12/2010
 * Time: 10:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class PenBrush {

    public void mouseDown(LoggedPath aPath, float aX, float aY) {
        aPath.moveTo(aX, aY);
        aPath.lineTo(aX, aY);
    }

    public void mouseMove(LoggedPath aPath, float aX, float aY) {
        aPath.lineTo(aX, aY);
    }

    public void mouseUp(LoggedPath aPath, float aX, float aY) {
        aPath.lineTo(aX, aY);
    }
}
