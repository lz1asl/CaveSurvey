package com.astoev.cave.survey.activity.draw;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 07/11/2010
 * Time: 2:15 AM
 * Link: http://www.tutorialforandroid.com/
 */
public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {

    private Boolean _run;
    protected DrawThread thread;
    private Bitmap mBitmap;
    private Bitmap mOldBitmap;
    public boolean isDrawing = true;
    public DrawingPath previewPath;

    private CommandManager commandManager;
    private Handler previewDoneHandler;

    public DrawingSurface(Context context, AttributeSet attrs) {
        super(context, attrs);

        getHolder().addCallback(this);

        commandManager = new CommandManager();
        thread = new DrawThread(getHolder());
        previewDoneHandler = new PreviewDoneHandler(this);
    }

    class DrawThread extends Thread {
        private SurfaceHolder mSurfaceHolder;


        public DrawThread(SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;

        }

        public void setRunning(boolean run) {
            _run = run;
        }


        @Override
        public void run() {
            Canvas canvas = null;
            while (_run) {
                if (isDrawing) {
                    try {
                        canvas = mSurfaceHolder.lockCanvas(null);

                        if (mBitmap == null) {
                            mBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                        }
                        final Canvas c = new Canvas(mBitmap);

                        c.drawColor(0, PorterDuff.Mode.CLEAR);
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

                        if (mOldBitmap != null) {
                            canvas.drawBitmap(mOldBitmap, 0, 0, null);
                        }

                        commandManager.executeAll(c, previewDoneHandler);
                        previewPath.draw(c);

                        canvas.drawBitmap(mBitmap, 0, 0, null);
                    } finally {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }


    public void addDrawingPath(DrawingPath drawingPath) {
        commandManager.addCommand(drawingPath);
    }

    public boolean hasMoreRedo() {
        return commandManager.hasMoreRedo();
    }

    public void redo() {
        isDrawing = true;
        commandManager.redo();


    }
    
    /**
     * Helper method that stops the draw thread to save the drawing
     */
    public void stopToSave(){
    	isDrawing = false;
    	thread.setRunning(false);
    }

    public void undo() {
        isDrawing = true;
        commandManager.undo();
    }

    public boolean hasMoreUndo() {
        return commandManager.hasMoreUndo();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public Bitmap getOldBitmap() {
        return mOldBitmap;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }


    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }

    public void setOldBitmap(Bitmap aOldBitmap) {
        mOldBitmap = aOldBitmap;
    }
    
    /**
     * Handler implementation
     * 
     * @author jmitrev
     */
    public static class PreviewDoneHandler extends Handler{
    	
    	private WeakReference<DrawingSurface> reference;
    	
    	public PreviewDoneHandler(DrawingSurface surfaceArg)
    	{
    		reference = new WeakReference<DrawingSurface>(surfaceArg);
    	}
    	
        @Override
        public void handleMessage(Message msg) {
        	DrawingSurface drawingSurface = reference.get();
        	if (drawingSurface != null){
        		drawingSurface.isDrawing = false;	
        	}
        }
    }
}
