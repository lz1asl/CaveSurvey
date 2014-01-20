package com.astoev.cave.survey.activity.draw;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.BaseActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.draw.brush.Brush;
import com.astoev.cave.survey.activity.draw.brush.PenBrush;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.FileStorageUtil;

import java.io.ByteArrayOutputStream;


/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 07/11/2010
 * Time: 2:14 AM
 * Link: http://www.tutorialforandroid.com/
 */
public class DrawingActivity extends BaseActivity implements View.OnTouchListener {

	public final static String MAP_FLAG = "com.astoev.cave.survey.MAP_FLAG";
	public final static String SKETCH_BASE = "com.astoev.cave.survey.SKETCH_BASE";
	
    private DrawingSurface drawingSurface;
    private DrawingPath currentDrawingPath;
    private Paint currentPaint;

    private Button redoBtn;
    private Button undoBtn;

    private int currentColor = Color.WHITE;
    private int currentSize = 3;
    private int currentStyle = 0;

    private Brush currentBrush;
    
    /** Helper flag that shows if this drawing is related with map.*/
    private boolean isMap;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_activity);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setCurrentPaint();
        currentBrush = new PenBrush();

        drawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
        drawingSurface.setOnTouchListener(this);
        drawingSurface.previewPath = new DrawingPath();
        drawingSurface.previewPath.path = new Path();
        drawingSurface.previewPath.paint = currentPaint;

        redoBtn = (Button) findViewById(R.id.redoBtn);
        undoBtn = (Button) findViewById(R.id.undoBtn);

        redoBtn.setEnabled(false);
        undoBtn.setEnabled(false);

        try {

            drawingSurface.setOldBitmap(null);

            // preload with image
            byte [] backgroundBytes = getIntent().getByteArrayExtra(SKETCH_BASE);
            if (null != backgroundBytes) {
                drawingSurface.setOldBitmap(BitmapFactory.decodeByteArray(backgroundBytes, 0, backgroundBytes.length, null));
                drawingSurface.invalidate();
            }

            // read the flag if this drawing is related with a map or a point(default)
            Intent intent = getIntent();
            isMap = intent.getBooleanExtra(MAP_FLAG, false);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to load drawing", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void setCurrentPaint() {
        currentPaint = new Paint();
        currentPaint.setDither(true);
        currentPaint.setColor(currentColor);
        currentPaint.setStrokeWidth(currentSize);
        if (0 == currentStyle) {
            currentPaint.setStyle(Paint.Style.STROKE);
            currentPaint.setStrokeJoin(Paint.Join.ROUND);
            currentPaint.setStrokeCap(Paint.Cap.ROUND);
        } else if (1 == currentStyle) {
            currentPaint.setStyle(Paint.Style.FILL);
            currentPaint.setStrokeJoin(Paint.Join.BEVEL);
            currentPaint.setStrokeCap(Paint.Cap.SQUARE);
        } else {
            currentPaint.setStyle(Paint.Style.STROKE);
            currentPaint.setPathEffect(new DashPathEffect(new float[]{2 * currentSize, 4 * currentSize}, 0));
        }
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            drawingSurface.isDrawing = true;

            currentDrawingPath = new DrawingPath();
            currentDrawingPath.paint = currentPaint;
            currentDrawingPath.path = new Path();
            currentBrush.mouseDown(currentDrawingPath.path, motionEvent.getX(), motionEvent.getY());
            currentBrush.mouseDown(drawingSurface.previewPath.path, motionEvent.getX(), motionEvent.getY());

        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            drawingSurface.isDrawing = true;
            currentBrush.mouseMove(currentDrawingPath.path, motionEvent.getX(), motionEvent.getY());
            currentBrush.mouseMove(drawingSurface.previewPath.path, motionEvent.getX(), motionEvent.getY());

        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

            currentBrush.mouseUp(drawingSurface.previewPath.path, motionEvent.getX(), motionEvent.getY());
            drawingSurface.previewPath.path = new Path();
            drawingSurface.addDrawingPath(currentDrawingPath);

            currentBrush.mouseUp(currentDrawingPath.path, motionEvent.getX(), motionEvent.getY());

            undoBtn.setEnabled(true);
            redoBtn.setEnabled(false);
        }

        return true;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.undoBtn:
                drawingSurface.undo();
                if (!drawingSurface.hasMoreUndo()) {
                    undoBtn.setEnabled(false);
                }
                redoBtn.setEnabled(true);
                break;

            case R.id.redoBtn:
                drawingSurface.redo();
                if (!drawingSurface.hasMoreRedo()) {
                    redoBtn.setEnabled(false);
                }

                undoBtn.setEnabled(true);
                break;
        }
    }

    public void saveDrawing(View aView) {

    	drawingSurface.stopToSave();
        try {
            Leg activeLeg = getWorkspace().getActiveOrFirstLeg();
            Point activePoint = activeLeg.getFromPoint();
            DaoUtil.refreshPoint(activePoint);

            // store to SD
            ByteArrayOutputStream buff = new ByteArrayOutputStream();

            if (drawingSurface.getOldBitmap() != null){
                Bitmap image = MapUtilities.combineBitmaps(drawingSurface.getOldBitmap(), drawingSurface.getBitmap());
                image.compress(Bitmap.CompressFormat.PNG, 50, buff);
            } else {
                drawingSurface.getBitmap().compress(Bitmap.CompressFormat.PNG, 50, buff);
            }

            String filePrefix;
            if (isMap){
            	filePrefix = FileStorageUtil.MAP_PREFIX;
            } else {
            	filePrefix = FileStorageUtil.getFilePrefixForPicture(activePoint);
            }
			String path = FileStorageUtil.addProjectMedia(this, getWorkspace().getActiveProject(), filePrefix, buff.toByteArray());

            // create DB record
            Sketch drawing = new Sketch();
            drawing.setPoint(activePoint);
            drawing.setFSPath(path);
            getWorkspace().getDBHelper().getSketchDao().create(drawing);

            UIUtilities.showNotification(R.string.sketch_saved);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed drawing save", e);
            UIUtilities.showNotification(R.string.error);
        }
        
        // go back to parent
        finish();
    }

    public void pickColor(View aView) {
        ColorPickerDialog.OnColorChangedListener listener = new ColorPickerDialog.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                currentColor = color;
                setCurrentPaint();
            }
        };
        Dialog dialog = new ColorPickerDialog(this, listener, currentColor);
        dialog.show();
    }

    public void pickSize(View aView) {

        final CharSequence[] items = new CharSequence[]{"1", "2", "3", "4", "5", "6", "7"};
        int selectedIndex = -1;
        for (int i=0; i< items.length; i++) {
            if (currentSize == Integer.parseInt(items[i].toString())) {
                selectedIndex = i+1;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sketch_pick_size);

        builder.setSingleChoiceItems(items, selectedIndex, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                Log.i(Constants.LOG_TAG_UI, "Selected size " + item);
                currentSize = item;
                setCurrentPaint();
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void pickStyle(View aView) {

        final int[] items = new int[]{R.string.sketch_pick_style_thick, R.string.sketch_pick_style_fill, R.string.sketch_pick_style_dash};
        final CharSequence[] itemsLabels = new CharSequence[items.length];
        int selectedIndex = -1;
        for (int i=0; i< items.length; i++) {
            if (currentStyle == i) {
                selectedIndex = i;
            }
            itemsLabels[i] = getString(items[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sketch_pick_style);

        builder.setSingleChoiceItems(itemsLabels, selectedIndex, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                Log.i(Constants.LOG_TAG_UI, "Selected style " + item);
                currentStyle = item;
                setCurrentPaint();
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

	/**
	 * @see android.support.v7.app.ActionBarActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// stop drawing thread before going back
    	drawingSurface.stopToSave();
		super.onBackPressed();
	}
    
}