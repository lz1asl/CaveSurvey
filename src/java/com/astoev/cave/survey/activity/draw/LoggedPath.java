package com.astoev.cave.survey.activity.draw;

import android.graphics.Path;

import com.astoev.cave.survey.activity.map.MapView;
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
    private Integer mScale;

    @Override
    public void lineTo(float aX, float aY) {
        // display the line scaled
        super.lineTo(moveAndScale(mMoveX, aX), moveAndScale(mMoveY, aY));
        // persist the original line
        mPoints.add(new SketchPoint(null, aX, aY));
    }

    @Override
    public void moveTo(float aX, float aY) {
        // display the line scaled
        super.moveTo(moveAndScale(mMoveX, aX), moveAndScale(mMoveY, aY));
        // persist the original line
        mPoints.add(new SketchPoint(null, aX, aY));
    }

    public List<SketchPoint> getPoints() {
        return mPoints;
    }

    public static LoggedPath fromSketchElement(SketchElement anElement, float moveX, float moveY, Integer scale) throws SQLException {
        LoggedPath path = new LoggedPath();
        path.mMoveX = moveX;
        path.mMoveY = moveY;
        path.mScale = scale;

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

    private float moveAndScale(float aMove, float aCoordinate) {
        float scale = 1;
        if (mScale != null) {
            scale = (mScale/ MapView.INITIAL_SCALE);
        }
        return aMove + aCoordinate * scale;
    }
}
