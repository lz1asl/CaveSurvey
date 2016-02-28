package com.astoev.cave.survey.activity.draw;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.BaseActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.draw.brush.PenBrush;
import com.astoev.cave.survey.activity.draw.colorpicker.ColorChangedListener;
import com.astoev.cave.survey.activity.draw.colorpicker.ColorPickerDialog;
import com.astoev.cave.survey.activity.map.MapView;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.model.SketchElement;
import com.astoev.cave.survey.model.SketchPoint;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.PointUtil;

import java.io.ByteArrayOutputStream;
import java.util.Collection;


/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 07/11/2010
 * Time: 2:14 AM
 * Link: http://www.tutorialforandroid.com/
 */
public class DrawingActivity extends BaseActivity implements View.OnTouchListener {

    public final static String PARAM_MAP_FLAG = "com.astoev.cave.survey.PARAM_MAP_FLAG";
    public final static String PARAM_MAP_MOVEX = "com.astoev.cave.survey.PARAM_MAP_MOVEX";
    public final static String PARAM_MAP_MOVEY = "com.astoev.cave.survey.PARAM_MAP_MOVEY";
    public final static String PARAM_MAP_SCALE = "com.astoev.cave.survey.PARAM_MAP_SCALE";
    public final static String PARAM_MAP_HORIZONTAL = "com.astoev.cave.survey.PARAM_MAP_HORIZONTAL";
    public final static String PARAM_LEG = "com.astoev.cave.survey.LEG";

    // view
    private DrawingSurface drawingSurface;
    private DrawingPath currentDrawingPath;

    // view buttons
    private Button redoBtn;
    private Button undoBtn;
    private ImageButton saveBtn;

    // drawing options
    private DrawingOptions mOptions = new DrawingOptions(Color.WHITE, 3, DrawingOptions.TYPES.THICK);
    private PenBrush currentBrush = new PenBrush();

    // drawing parameters
    private Leg mCurrLeg;
    private float mMoveX;
    private float mMoveY;
    private Integer mScale;
    private Boolean mHorizontal;

    // existing sketch data
    private Sketch mExistingSketch;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_activity);

        drawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
        drawingSurface.setOnTouchListener(this);

        drawingSurface.mPreviewPath = new DrawingPath(mOptions);

        redoBtn = (Button) findViewById(R.id.redoBtn);
        undoBtn = (Button) findViewById(R.id.undoBtn);
        saveBtn = (ImageButton) findViewById(R.id.saveDrawingBtn);


        try {

            Intent intent = getIntent();

            // map drawing options
            mMoveX = intent.getFloatExtra(PARAM_MAP_MOVEX, 0);
            mMoveY = intent.getFloatExtra(PARAM_MAP_MOVEY, 0);
            mScale = intent.getIntExtra(PARAM_MAP_SCALE, MapView.INITIAL_SCALE);
            mHorizontal = intent.getBooleanExtra(PARAM_MAP_HORIZONTAL, true);
            drawingSurface.setHorizontal(mHorizontal);

            if (intent.getBooleanExtra(PARAM_MAP_FLAG, false)) {
                // draw the same map as a base
                MapView currMap = new MapView(this, null);
                currMap.setHorizontalPlan(mHorizontal);
                currMap.move(mMoveX, mMoveY);
                currMap.scale(mScale);
                currMap.setAnnotateMap(false);
                drawingSurface.setMap(currMap);
            }



            Integer legId = getIntent().getIntExtra(PARAM_LEG, -1);
            Log.i(Constants.LOG_TAG_UI, "Start drawing activity for leg " + legId);
            if (legId != -1) {
                // sketch attached to leg
                mCurrLeg = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryForId(legId);
                mExistingSketch = mCurrLeg.getSketch();
            } else {
                // sketch attached to project
                Project currProject = Workspace.getCurrentInstance().getActiveProject();
                if (mHorizontal) {
                    mExistingSketch = currProject.getSketchPlan();
                } else {
                    mExistingSketch = currProject.getSketchSection();
                }
            }

            if (mExistingSketch != null) {

                Collection<SketchElement> elements = getWorkspace().getDBHelper().getSketchElementDao().queryForEq(SketchElement.COLUMN_SKETCH_ID, mExistingSketch.getId());
                if (elements != null) {
                    Log.i(Constants.LOG_TAG_UI, "Existing sketch found with " + elements.size() + " elements");
                    for (SketchElement e : elements) {
                        DrawingPath path = new DrawingPath();
                        path.setOptions(DrawingOptions.fromSketchElement(e));
                        path.setPath(LoggedPath.fromSketchElement(e));
                        drawingSurface.getPathElements().add(path);
                    }
                }
            } else {
                mExistingSketch = new Sketch();
            }

            redoBtn.setEnabled(false);
            if (drawingSurface.hasMoreUndo()) {
                undoBtn.setEnabled(true);
            } else {
                undoBtn.setEnabled(false);
            }

            // nothing to save yet
            saveBtn.setEnabled(false);

            drawingSurface.invalidate();

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to load drawing", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            currentDrawingPath = new DrawingPath(mOptions);
            currentDrawingPath.getPath().setMoveX(mMoveX);
            currentDrawingPath.getPath().setMoveY(mMoveY);
            currentDrawingPath.getPath().setScale(mScale);
            currentBrush.mouseDown(currentDrawingPath.getPath(), motionEvent.getX(), motionEvent.getY());
            currentBrush.mouseDown(drawingSurface.mPreviewPath.getPath(), motionEvent.getX(), motionEvent.getY());
            drawingSurface.invalidate();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            currentBrush.mouseMove(currentDrawingPath.getPath(), motionEvent.getX(), motionEvent.getY());
            currentBrush.mouseMove(drawingSurface.mPreviewPath.getPath(), motionEvent.getX(), motionEvent.getY());
            drawingSurface.invalidate();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            currentBrush.mouseUp(drawingSurface.mPreviewPath.getPath(), motionEvent.getX(), motionEvent.getY());
            currentBrush.mouseUp(currentDrawingPath.getPath(), motionEvent.getX(), motionEvent.getY());
            drawingSurface.mPreviewPath.setPath(new LoggedPath());
            drawingSurface.addDrawingPath(currentDrawingPath);
            undoBtn.setEnabled(true);
            redoBtn.setEnabled(false);
            saveBtn.setEnabled(true);
            drawingSurface.invalidate();
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
                saveBtn.setEnabled(true);
                break;

            case R.id.redoBtn:
                drawingSurface.redo();
                if (!drawingSurface.hasMoreRedo()) {
                    redoBtn.setEnabled(false);
                }

                undoBtn.setEnabled(true);
                saveBtn.setEnabled(true);
                break;
        }
        drawingSurface.invalidate();
    }

    public void saveDrawing(View aView) {

        try {

            // store to SD
            ByteArrayOutputStream buff = new ByteArrayOutputStream();

            Bitmap currDrawing = drawingSurface.getBitmap();
            currDrawing.compress(Bitmap.CompressFormat.PNG, 50, buff);

            String filePrefix;
            if (mCurrLeg == null) {
                filePrefix = FileStorageUtil.MAP_PREFIX;
            } else {
                Point activePoint = mCurrLeg.getFromPoint();
                String galleryName = PointUtil.getGalleryNameForFromPoint(activePoint, mCurrLeg.getGalleryId());
                filePrefix = FileStorageUtil.getFilePrefixForPicture(activePoint, galleryName);
            }
            String path = FileStorageUtil.addProjectMedia(this, getWorkspace().getActiveProject(), filePrefix, buff.toByteArray());

            // can already clean
            buff = null;
            currDrawing.recycle();

            // persist the sketch path
            mExistingSketch.setFSPath(path);
            getWorkspace().getDBHelper().getSketchDao().createOrUpdate(mExistingSketch);

            // delete old sketch data, not yet clever enough to replace
            if (mExistingSketch.getId() != null) {
                Collection<SketchElement> elements = getWorkspace().getDBHelper().getSketchElementDao().queryForEq(SketchElement.COLUMN_SKETCH_ID, mExistingSketch.getId());
                for (SketchElement element : elements) {
                    element.getPoints().clear();
                    getWorkspace().getDBHelper().getSketchElementDao().update(element);
                    getWorkspace().getDBHelper().getSketchElementDao().delete(element);
                }
            }

            // insert the current vector elements
            int elementsCount = 0;
            for (DrawingPath currPath : drawingSurface.getPathElements()) {
                SketchElement element = new SketchElement(mExistingSketch);
                element.setOrderBy(elementsCount++);
                element.setColor(currPath.getOptions().getColor());
                element.setSize(currPath.getOptions().getSize());
                element.setType(currPath.getOptions().getType());
                LoggedPath currPathPath = currPath.getPath();
                element.setX(currPathPath.getMoveX());
                element.setY(currPathPath.getMoveY());
                element.setScale(currPathPath.getScale());
                getWorkspace().getDBHelper().getSketchElementDao().create(element);

                // points
                int pointsCount = 0;
                for (SketchPoint p : currPath.getPath().getPoints()) {
                    p.setElement(element);
                    p.setOrderBy(pointsCount++);
                    p.setElement(element);
                    getWorkspace().getDBHelper().getSketchPointDao().create(p);
                }
            }

            // attach to parent
            if (mCurrLeg != null) {
                mCurrLeg.setSketch(mExistingSketch);
                getWorkspace().getDBHelper().getLegDao().update(mCurrLeg);
            } else {
                Project project = getWorkspace().getActiveProject();
                if (mHorizontal) {
                    project.setSketchPlan(mExistingSketch);
                } else {
                    project.setSketchSection(mExistingSketch);
                }
                getWorkspace().getDBHelper().getProjectDao().update(project);
            }

            // success
            UIUtilities.showNotification(R.string.sketch_saved);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed drawing save", e);
            UIUtilities.showNotification(R.string.error);
        }

        // go back to parent
        finish();
    }

    public void pickColor(View aView) {
        ColorChangedListener listener = new ColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                mOptions.setColor(color);
            }
        };
        Dialog dialog = new ColorPickerDialog(this, listener, mOptions.getColor());
        dialog.show();
    }

    public void pickSize(View aView) {

        final CharSequence[] items = new CharSequence[]{"1", "2", "3", "4", "5", "6", "7"};
        int selectedIndex = mOptions.getSize() - 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sketch_pick_size);

        builder.setSingleChoiceItems(items, selectedIndex, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                Log.i(Constants.LOG_TAG_UI, "Selected size " + item);
                mOptions.setSize(item + 1);
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
        for (int i=0; i<items.length; i++) {
            itemsLabels[i] = getString(items[i]);
            if (DrawingOptions.TYPES.values()[i].equals(mOptions.getType())) {
                selectedIndex = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sketch_pick_style);

        builder.setSingleChoiceItems(itemsLabels, selectedIndex, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                Log.i(Constants.LOG_TAG_UI, "Selected style " + item);
                mOptions.setType(DrawingOptions.TYPES.values()[item]);
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}