package com.astoev.cave.survey.activity.draw;

import android.graphics.Canvas;
import android.graphics.Path;

import com.astoev.cave.survey.activity.map.MapView;
import com.astoev.cave.survey.model.SketchPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 15/11/2010
 * Time: 12:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommandManager {

    private List<DrawingPath> currentStack;
    private List<DrawingPath> redoStack;

    public CommandManager() {
        currentStack = Collections.synchronizedList(new ArrayList<DrawingPath>());
        redoStack = Collections.synchronizedList(new ArrayList<DrawingPath>());
    }

    public void addCommand(DrawingPath command) {
        redoStack.clear();
        currentStack.add(command);
    }

    public void undo() {
        final int length = currentStackLength();

        if (length > 0) {
            final DrawingPath undoCommand = currentStack.get(length - 1);
            currentStack.remove(length - 1);
            redoStack.add(undoCommand);
        }
    }

    public int currentStackLength() {
        return currentStack.toArray().length;
    }


    public void executeAll(Canvas aCanvas, MapView aMapView) {
        if (currentStack != null) {
            synchronized (currentStack) {

                for (DrawingPath path : currentStack) {

                    // transform
                    Path scaledPath = toMovedAndScaledBasicPath(path, aMapView, aCanvas);

                    // display
                    aCanvas.translate(-path.getPath().getMoveX(), -path.getPath().getMoveY());

                    aCanvas.drawPath(scaledPath, DrawingOptions.optionsToPaint(path.getOptions()));
                    aCanvas.translate(path.getPath().getMoveX(), path.getPath().getMoveY());
                }


            }
        }
    }

    private Path toMovedAndScaledBasicPath(DrawingPath aPath, MapView aMapView, Canvas aCanvas) {
        Path scaledBasicPath = new Path();
        LoggedPath sourcePath = aPath.getPath();

        boolean first = true;

        float scale = aMapView.getScale() / sourcePath.getScale();

        float adjX = aCanvas.getWidth() / 2 - aCanvas.getWidth() / 2 *scale;
        float adjY = aCanvas.getHeight() / 2 - aCanvas.getHeight() / 2*scale;

        for (SketchPoint point : sourcePath.getPoints()) {

            float x = point.getX() * scale + adjX;
            float y = point.getY() * scale + adjY;

            if (first) {
                scaledBasicPath.moveTo(x, y);
                first = false;
            } else {
                scaledBasicPath.lineTo(x, y);
            }

        }

        return scaledBasicPath;
    }

    public boolean hasMoreRedo() {
        return redoStack.toArray().length > 0;
    }

    public boolean hasMoreUndo() {
        return currentStack.toArray().length > 0;
    }

    public void redo() {
        final int length = redoStack.toArray().length;
        if (length > 0) {
            final DrawingPath redoCommand = redoStack.get(length - 1);
            redoStack.remove(length - 1);
            currentStack.add(redoCommand);
        }
    }

    public List<DrawingPath> getCurrentStack() {
        return currentStack;
    }
}
