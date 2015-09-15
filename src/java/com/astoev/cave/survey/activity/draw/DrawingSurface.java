package com.astoev.cave.survey.activity.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 07/11/2010
 * Time: 2:15 AM
 * Link: http://www.tutorialforandroid.com/
 */
public class DrawingSurface extends View {

    public DrawingPath previewPath;
    private Bitmap mOldBitmap;
    private CommandManager commandManager;

    public DrawingSurface(Context context, AttributeSet attrs) {
        super(context, attrs);

        commandManager = new CommandManager();


        // disable optimizations causing drawing path not visible
        setWillNotDraw(false);
    }

    @Override
    public void onDraw(Canvas aCanvas) {

        // need to call parent
        super.onDraw(aCanvas);

        try {
            drawOldBitmapAndCurrentPaths(aCanvas);
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to draw", e);
            UIUtilities.showNotification(R.string.error);
        }

    }

    private void drawOldBitmapAndCurrentPaths(Canvas aCanvas) {
        aCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        // old bitmap, e.g. map below the drawing
        if (mOldBitmap != null) {
            int left = (aCanvas.getWidth() - mOldBitmap.getWidth()) / 2;
            int top = (aCanvas.getHeight() - mOldBitmap.getHeight()) / 2;
            aCanvas.drawBitmap(mOldBitmap, left, top, null);
        }

        // user notes above
        commandManager.executeAll(aCanvas);
        previewPath.draw(aCanvas);
    }


    public void addDrawingPath(DrawingPath drawingPath) {
        commandManager.addCommand(drawingPath);
    }

    public boolean hasMoreRedo() {
        return commandManager.hasMoreRedo();
    }

    public void redo() {
        commandManager.redo();
    }


    public void undo() {
        commandManager.undo();
    }

    public boolean hasMoreUndo() {
        return commandManager.hasMoreUndo();
    }

    public Bitmap getBitmap() {

        // repeat the drawing in a buffer and return to be saved
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawOldBitmapAndCurrentPaths(canvas);
        return bitmap;
    }

    public Bitmap getOldBitmap() {
        return mOldBitmap;
    }


    public void setOldBitmap(Bitmap aOldBitmap) {
        mOldBitmap = aOldBitmap;
    }

}
