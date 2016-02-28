package com.astoev.cave.survey.activity.draw;

import android.graphics.Path;

import com.astoev.cave.survey.model.SketchElement;
import com.astoev.cave.survey.model.SketchPoint;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by astoev on 9/24/15.
 */
public class LoggedPath extends Path {

    private List<SketchPoint> mPoints = new ArrayList<SketchPoint>();
    private float mMoveX;
    private float mMoveY;
    private float mScale;

    @Override
    public void lineTo(float aX, float aY) {
        // display the line
        super.lineTo(aX, aY);
        // persist the original line to be saved
        mPoints.add(new SketchPoint(null, aX, aY));
    }

    @Override
    public void moveTo(float aX, float aY) {
        // display the line
        super.moveTo(aX, aY);
        // persist the original line
        mPoints.add(new SketchPoint(null, aX, aY));
    }

    public List<SketchPoint> getPoints() {
        return mPoints;
    }

    public static LoggedPath fromSketchElement(SketchElement anElement) throws SQLException {
        LoggedPath path = new LoggedPath();
        path.mMoveX = anElement.getX();
        path.mMoveY = anElement.getY();
        path.mScale = anElement.getScale();

        Collection<SketchPoint> points = anElement.getPoints();
        boolean first = true;
        for (SketchPoint point : points) {
            if (first) {
                path.moveTo(point.getX(), point.getY());
                first = false;
            } else {
                path.lineTo(point.getX(),point.getY());
            }
        }

        return path;
    }

    public float getMoveX() {
        return mMoveX;
    }

    public void setMoveX(float moveX) {
        mMoveX = moveX;
    }

    public float getMoveY() {
        return mMoveY;
    }

    public void setMoveY(float moveY) {
        mMoveY = moveY;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        mScale = scale;
    }
}
