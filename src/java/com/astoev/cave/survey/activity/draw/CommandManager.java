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


    public void executeAll(Canvas aCanvas, MapView aMapView, Boolean horizontal) {
        if (currentStack != null) {
            synchronized (currentStack) {

                int mapScale = MapView.INITIAL_SCALE;
                if (aMapView != null) {
                    mapScale = aMapView.getScale();
                }

                for (DrawingPath path : currentStack) {

                    // transform
                    Path scaledPath = toMovedAndScaledBasicPath(path, mapScale, aCanvas.getWidth(), aCanvas.getHeight(), horizontal);

                    // display
                    aCanvas.drawPath(scaledPath, DrawingOptions.optionsToPaint(path.getOptions()));
                }
            }
        }
    }

    public static Path toMovedAndScaledBasicPath(DrawingPath aPath, float aMapScale, float aWidth, float aHeight, Boolean aHorizontal) {
        final Path scaledBasicPath = new Path();
        final LoggedPath sourcePath = aPath.getPath();

        final float scale = aMapScale / sourcePath.getScale();
        final float drawingHalfWidth, drawingHalfHeight = aHeight / 2;;

        if (aHorizontal) {
            drawingHalfWidth = aWidth / 2 ;
        } else {
            drawingHalfWidth = aWidth / 4 ;
        }

        final float adjX = drawingHalfWidth - (drawingHalfWidth + sourcePath.getMoveX()) * scale;
        final float adjY = drawingHalfHeight - (drawingHalfHeight + sourcePath.getMoveY()) * scale;

        boolean first = true;
        float x, y;

        for (SketchPoint point : sourcePath.getPoints()) {

            x = point.getX() * scale + adjX;
            y = point.getY() * scale + adjY;

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
