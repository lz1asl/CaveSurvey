package com.astoev.cave.survey.activity.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.draw.CommandManager;
import com.astoev.cave.survey.activity.draw.DrawingOptions;
import com.astoev.cave.survey.activity.draw.DrawingPath;
import com.astoev.cave.survey.activity.draw.LoggedPath;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.model.SketchElement;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.DaoUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/1/12
 * Time: 11:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class MapView extends View {

    public static final int POINT_RADIUS = 3;
    public static final int MIDDLE_POINT_RADIUS = 2;
    public static final int MEASURE_POINT_RADIUS = 2;
    public static final int CURR_POINT_RADIUS = 8;
    private final static int LABEL_DEVIATION_X = 10;
    private final static int LABEL_DEVIATION_Y = 15;
    private static final int [] GRID_STEPS = new int[] {20,10, 5, 5, 2, 2, 2, 2, 1, 1, 1};
    public static final int INITIAL_SCALE = 10;
    private static final int SPACING = 5;

    private final Paint polygonPaint = new Paint();
    private final Paint polygonWidthPaint = new Paint();
    private final Paint overlayPaint = new Paint();
    private final Paint youAreHerePaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint vectorsPaint = new Paint();
    private final Paint vectorPointPaint = new Paint();
    private int scale = INITIAL_SCALE;
    private int mapCenterMoveX = 0;
    private int mapCenterMoveY = 0;
    private float initialMoveX = 0;
    private float initialMoveY = 0;
    private Point northCenter = new Point();

    private List<Integer> processedLegs = new ArrayList<Integer>();
    
    private SparseArray<Point2D> mapPoints = new SparseArray<Point2D>();
    private SparseIntArray galleryColors = new SparseIntArray();
    private SparseArray<String> galleryNames = new SparseArray<String>();

    private boolean horizontalPlan = true;

    private boolean annotateMap = true;
    private boolean mDrawingsVisible = false;

    // stored view positions
    private static MapViewPosition mPlanView, mSectionView;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        polygonPaint.setColor(Color.RED);
        polygonPaint.setStrokeWidth(2);
        polygonWidthPaint.setColor(Color.RED);
        polygonWidthPaint.setStrokeWidth(1);
        overlayPaint.setColor(Color.WHITE);
        youAreHerePaint.setColor(Color.WHITE);
        youAreHerePaint.setAlpha(50);
        // semi transparent white
        gridPaint.setColor(Color.parseColor("#11FFFFFF"));
        gridPaint.setStrokeWidth(1);
        vectorsPaint.setStrokeWidth(1);
        vectorsPaint.setStyle(Paint.Style.STROKE);
        vectorsPaint.setPathEffect(new DashPathEffect(new float[]{2, 6}, 0));
        vectorsPaint.setAlpha(50);
        vectorPointPaint.setStrokeWidth(1);
        vectorPointPaint.setAlpha(50);

        // reuse the view if available
        applyStoredView();

        // need to instruct that changes to the canvas will be made, otherwise the screen might become blank
        // see http://stackoverflow.com/questions/12261435/canvas-does-not-draw-in-custom-view
        setWillNotDraw(false);

    }

    @Override
    public void onDraw(Canvas canvas) {

        // need to call parent
        super.onDraw(canvas);

        try {
            processedLegs.clear();
            mapPoints.clear();
            galleryColors.clear();
            galleryNames.clear();

            // prepare map surface
            int maxX = canvas.getWidth();
            int maxY = canvas.getHeight();

            int centerX;
            int centerY;

            if (horizontalPlan) {
                // starting from the center of the screen
                centerX = maxX / 2;
                centerY = maxY / 2;
            } else {
                // slightly more left for the vertical plan (advancing to right)
                centerX = maxX / 4;
                centerY = maxY / 2;
            }

            String azimuthUnits = Options.getOptionValue(Option.CODE_AZIMUTH_UNITS);
            String slopeUnits = Options.getOptionValue(Option.CODE_SLOPE_UNITS);

            int gridStepIndex = scale/5;

            // grid scale
            int gridStep = GRID_STEPS[gridStepIndex] * scale;

            // grid start
            int gridStartX = mapCenterMoveX % gridStep - SPACING + centerX - (centerX / gridStep) * gridStep;
            int gridStartY = mapCenterMoveY % gridStep - SPACING + centerY - (centerY / gridStep) * gridStep;

            // grid horizontal lines
            for (int x=0; x<maxX/gridStep; x++) {
                canvas.drawLine(x*gridStep + SPACING + gridStartX, SPACING, x*gridStep + SPACING + gridStartX, maxY - SPACING, gridPaint);
            }
            // grid vertical lines
            for (int y=0; y<maxY/gridStep; y++) {
                canvas.drawLine(SPACING, y*gridStep + SPACING + gridStartY, maxX - SPACING, y*gridStep + SPACING + gridStartY, gridPaint);
            }

            // load the points
            List<Leg> legs = DaoUtil.getCurrProjectLegs(true);
            String pointLabel;

            while (processedLegs.size() < legs.size()) {
                for (Leg l : legs) {

                    if (processedLegs.size() == legs.size()) {
                        break;
                    }

                    if (!processedLegs.contains(l.getId())) {
                        // first leg ever
                        Point2D first;

                        if (processedLegs.size() == 0) {
                            if (horizontalPlan) {
                                first = new Point2D(Float.valueOf(centerX), Float.valueOf(centerY), l.getLeft(), l.getRight(), MapUtilities.getAzimuthInDegrees(l.getAzimuth(), azimuthUnits));
                            } else {
                                first = new Point2D(Float.valueOf(centerX), Float.valueOf(centerY), l.getTop(), l.getDown(), MapUtilities.getSlopeInDegrees(l.getSlope(), slopeUnits));
                            }
                        } else {
                            // update previously created point with the correct values for left/right/up/down
                            first = mapPoints.get(l.getFromPoint().getId());
                            if (horizontalPlan) {
                                first.setLeft(l.getLeft());
                                first.setRight(l.getRight());
                                if (l.getAzimuth() != null) {
                                    first.setAngle(MapUtilities.getAzimuthInDegrees(l.getAzimuth(), azimuthUnits));
                                } else {
                                    first.setAngle(null);
                                }
                            } else {
                                first.setLeft(l.getTop());
                                first.setRight(l.getDown());
                                if (l.getSlope() != null) {
                                    first.setAngle(MapUtilities.getSlopeInDegrees(l.getSlope(), slopeUnits));
                                } else {
                                    first.setAngle(null);
                                }
                            }
                        }

                        if (mapPoints.get(l.getFromPoint().getId()) == null) {
                            if (!l.isMiddle()) {
                                mapPoints.put(l.getFromPoint().getId(), first);
                            }

                            // draw first point
                            if (!l.isMiddle()) {
                                //color
                                if (galleryColors.get(l.getGalleryId(), Constants.NOT_FOUND) == Constants.NOT_FOUND) {
                                    galleryColors.put(l.getGalleryId(), MapUtilities.getNextGalleryColor(galleryColors.size()));
                                    Gallery gallery = DaoUtil.getGallery(l.getGalleryId());
                                    galleryNames.put(l.getGalleryId(), gallery.getName());
                                }
                                polygonPaint.setColor(galleryColors.get(l.getGalleryId()));
                                polygonWidthPaint.setColor(galleryColors.get(l.getGalleryId()));
                                vectorsPaint.setColor(galleryColors.get(l.getGalleryId()));
                                vectorPointPaint.setColor(galleryColors.get(l.getGalleryId()));

                                DaoUtil.refreshPoint(l.getFromPoint());

                                pointLabel = galleryNames.get(l.getGalleryId()) + l.getFromPoint().getName();
                                if (scale >= 3) {
                                    canvas.drawText(pointLabel, mapCenterMoveX + first.getX() + LABEL_DEVIATION_X, mapCenterMoveY + first.getY() + LABEL_DEVIATION_Y, polygonPaint);
                                }
                                canvas.drawCircle(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), POINT_RADIUS, polygonPaint);
                            }
                        }

                        float deltaX;
                        float deltaY;
                        if (horizontalPlan) {
                            if (l.getDistance() == null || l.getAzimuth() == null) {
                                deltaX = 0;
                                deltaY = 0;
                            } else {
                                float legDistance;
                                if (l.isMiddle()) {
                                    legDistance = MapUtilities.applySlopeToDistance(l.getMiddlePointDistance(), MapUtilities.getSlopeInDegrees(l.getSlope(), slopeUnits));
                                } else {
                                    legDistance = MapUtilities.applySlopeToDistance(l.getDistance(), MapUtilities.getSlopeInDegrees(l.getSlope(), slopeUnits));
                                }
                                deltaY = -(float) (legDistance * Math.cos(Math.toRadians(MapUtilities.getAzimuthInDegrees(l.getAzimuth(), azimuthUnits)))) * scale;
                                deltaX = (float) (legDistance * Math.sin(Math.toRadians(MapUtilities.getAzimuthInDegrees(l.getAzimuth(), azimuthUnits)))) * scale;
                            }
                        } else {
                            if (l.getDistance() == null || l.getDistance() == 0) {
                                deltaX = 0;
                                deltaY = 0;
                            } else {
                                float legDistance;
                                if (l.isMiddle()) {
                                    legDistance = l.getMiddlePointDistance();
                                } else {
                                    legDistance = l.getDistance();
                                }
                                deltaY = (float) (legDistance * Math.cos(Math.toRadians(MapUtilities.add90Degrees(MapUtilities.getSlopeInDegrees(l.getSlope() == null ? 0 : l.getSlope(), slopeUnits))))) * scale;
                                deltaX = (float) (legDistance * Math.sin(Math.toRadians(MapUtilities.add90Degrees(MapUtilities.getSlopeInDegrees(l.getSlope() == null ? 0 : l.getSlope(), slopeUnits))))) * scale;
                            }
                        }

                        Point2D second = new Point2D(first.getX() + deltaX, first.getY() + deltaY);

                        if (mapPoints.get(l.getToPoint().getId()) == null || l.isMiddle()) {
                            if (!l.isMiddle()) {
                                mapPoints.put(l.getToPoint().getId(), second);
                            }

                            // color
                            if (galleryColors.get(l.getGalleryId(), Constants.NOT_FOUND) == Constants.NOT_FOUND) {
                                galleryColors.put(l.getGalleryId(), MapUtilities.getNextGalleryColor(galleryColors.size()));
                                Gallery gallery = DaoUtil.getGallery(l.getGalleryId());
                                galleryNames.put(l.getGalleryId(), gallery.getName());
                            }
                            polygonPaint.setColor(galleryColors.get(l.getGalleryId()));
                            polygonWidthPaint.setColor(galleryColors.get(l.getGalleryId()));
                            vectorsPaint.setColor(galleryColors.get(l.getGalleryId()));
                            vectorPointPaint.setColor(galleryColors.get(l.getGalleryId()));

//                            Log.i(Constants.LOG_TAG_UI, "Drawing leg " + l.getFromPoint().getName() + ":" + l.getToPoint().getName() + "-" + l.getGalleryId());

                            if (Workspace.getCurrentInstance().getActiveLegId().equals(l.getId())) {
                                // you are here
                                if (l.isMiddle()) {
                                    canvas.drawCircle(mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), CURR_POINT_RADIUS, youAreHerePaint);
                                } else {
                                    canvas.drawCircle(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), CURR_POINT_RADIUS, youAreHerePaint);
                                }

                            }

                            DaoUtil.refreshPoint(l.getToPoint());
                            if (l.isMiddle()) {
                                canvas.drawCircle(mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), MIDDLE_POINT_RADIUS, polygonPaint);
                            } else {
                                pointLabel = galleryNames.get(l.getGalleryId()) + l.getToPoint().getName();
                                if (scale >= 3) {
                                    canvas.drawText(pointLabel, mapCenterMoveX + second.getX() + LABEL_DEVIATION_X, mapCenterMoveY + second.getY() + LABEL_DEVIATION_Y, polygonPaint);
                                }
                                canvas.drawCircle(mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), POINT_RADIUS, polygonPaint);
                            }

                        }

                        // leg
                        if (!l.isMiddle()) {
                            canvas.drawLine(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), polygonPaint);
                        }

                        Leg prevLeg = DaoUtil.getLegByToPointId(l.getFromPoint().getId());

                        if (scale >= 5) {
                            if (horizontalPlan) {
                                // left
                                calculateAndDrawSide(canvas, l, first, second, prevLeg, first.getLeft(), azimuthUnits, true);
                                // right
                                calculateAndDrawSide(canvas, l, first, second, prevLeg, first.getRight(), azimuthUnits, false);
                            } else {
                                // top
//                            calculateAndDrawSide(canvas, l, first, second, prevLeg, first.getLeft(), azimuthUnits, true);
                                // down
//                            calculateAndDrawSide(canvas, l, first, second, prevLeg, first.getRight(), azimuthUnits, false);
                            }
                        }

                        if (!l.isMiddle() && scale >= 10) {
                            // vectors
                            List<Vector> vectors = DaoUtil.getLegVectors(l);
                            if (vectors != null) {
                                for (Vector v : vectors) {
                                    if (horizontalPlan) {
                                        float legDistance = MapUtilities.applySlopeToDistance(v.getDistance(), MapUtilities.getSlopeInDegrees(v.getSlope(), slopeUnits));
                                        deltaY = -(float) (legDistance * Math.cos(Math.toRadians(MapUtilities.getAzimuthInDegrees(v.getAzimuth(), azimuthUnits)))) * scale;
                                        deltaX = (float) (legDistance * Math.sin(Math.toRadians(MapUtilities.getAzimuthInDegrees(v.getAzimuth(), azimuthUnits)))) * scale;
                                    } else {
                                        float legDistance = v.getDistance();
                                        deltaY = (float) (legDistance * Math.cos(Math.toRadians(MapUtilities.add90Degrees(
                                                MapUtilities.getSlopeInDegrees(MapUtilities.getSlopeOrHorizontallyIfMissing(v.getSlope()), slopeUnits))))) * scale;
                                        deltaX = (float) (legDistance * Math.sin(Math.toRadians(MapUtilities.add90Degrees(
                                                MapUtilities.getSlopeInDegrees(MapUtilities.getSlopeOrHorizontallyIfMissing(v.getSlope()), slopeUnits))))) * scale;
                                    }
                                    canvas.drawLine(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), mapCenterMoveX + first.getX() + deltaX, mapCenterMoveY + first.getY() + deltaY, vectorsPaint);
                                    canvas.drawCircle(mapCenterMoveX + first.getX() + deltaX, mapCenterMoveY + first.getY() + deltaY, 2, vectorPointPaint);
                                }
                            }
                        }

                        processedLegs.add(l.getId());
                    }

                }
            }

            // drawings
            if (mDrawingsVisible) {

                Sketch sketch;
                Project currProject = Workspace.getCurrentInstance().getActiveProject();
                if (horizontalPlan) { // plan section
                    sketch = currProject.getSketchPlan();
                } else {
                    sketch = currProject.getSketchSection();
                }

                if (sketch != null) {
                    Collection<SketchElement> elements = Workspace.getCurrentInstance().getDBHelper().getSketchElementDao().queryForEq(SketchElement.COLUMN_SKETCH_ID, sketch.getId());
                    if (elements != null) {

                        // follow the map move
                        canvas.translate(initialMoveX, mapCenterMoveY);

                        for (SketchElement e : elements) {

                            // transform
                            DrawingPath path = new DrawingPath();
                            path.setOptions(DrawingOptions.fromSketchElement(e));
                            path.setPath(LoggedPath.fromSketchElement(e));
                            Path scaledPath = CommandManager.toMovedAndScaledBasicPath(path, scale, maxX, maxY, horizontalPlan);

                            // display
                            canvas.drawPath(scaledPath, DrawingOptions.optionsToPaint(path.getOptions()));
                        }

                        // reset map position
                        canvas.translate(-mapCenterMoveX, -mapCenterMoveY);
                    }
                }
            }


            if (annotateMap) {

                // borders
                //top
                canvas.drawLine(SPACING, SPACING, maxX - SPACING, SPACING, overlayPaint);
                //right
                canvas.drawLine(maxX - SPACING, SPACING, maxX - SPACING, maxY - SPACING, overlayPaint);
                // bottom
                canvas.drawLine(SPACING, maxY - SPACING, maxX - SPACING, maxY - SPACING, overlayPaint);
                //left
                canvas.drawLine(SPACING, maxY - SPACING, SPACING, SPACING, overlayPaint);

                if (horizontalPlan) {
                    // north arrow
                    northCenter.set(maxX - 20, 30);
                    canvas.drawLine(northCenter.x, northCenter.y, northCenter.x + 10, northCenter.y + 10, overlayPaint);
                    canvas.drawLine(northCenter.x + 10, northCenter.y + 10, northCenter.x, northCenter.y - 20, overlayPaint);
                    canvas.drawLine(northCenter.x, northCenter.y - 20, northCenter.x - 10, northCenter.y + 10, overlayPaint);
                    canvas.drawLine(northCenter.x - 10, northCenter.y + 10, northCenter.x, northCenter.y, overlayPaint);
                    canvas.drawText("N", northCenter.x + 5, northCenter.y - 10, overlayPaint);
                } else {
                    //  up wrrow
                    northCenter.set(maxX - 15, 10);
                    canvas.drawLine(northCenter.x + 1, northCenter.y, northCenter.x + 6, northCenter.y + 10, overlayPaint);
                    canvas.drawLine(northCenter.x - 5, northCenter.y + 10, northCenter.x, northCenter.y, overlayPaint);
                    canvas.drawLine(northCenter.x, northCenter.y - 1, northCenter.x, northCenter.y + 20, overlayPaint);
                }

                // scale
                canvas.drawText("x" + scale, 25 + gridStep / 2, 45, overlayPaint);
                canvas.drawLine(30, 25, 30, 35, overlayPaint);
                canvas.drawLine(30, 30, 30 + gridStep, 30, overlayPaint);
                canvas.drawLine(30 + gridStep, 25, 30 + gridStep, 35, overlayPaint);
                canvas.drawText(GRID_STEPS[gridStepIndex] + "m", 25 + gridStep / 2, 25, overlayPaint);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to draw map activity", e);
            UIUtilities.showNotification(R.string.error);
        }
    }


    public void zoomOut() {
        scale--;
        invalidate();
    }

    public void zoomIn() {
        scale++;
        invalidate();
    }

    public boolean canZoomOut() {
        return scale > 1;
    }

    public boolean canZoomIn() {
        return scale < 50;
    }

    public boolean isHorizontalPlan() {
        return horizontalPlan;
    }

    public void setHorizontalPlan(boolean horizontalPlan) {

        preserveView();

        this.horizontalPlan = horizontalPlan;

        if (!applyStoredView()) {
            scale = INITIAL_SCALE;
            mapCenterMoveX = 0;
            mapCenterMoveY = 0;
        }

        invalidate();
    }

    public void resetMove(float aX, float aY) {
        initialMoveX = aX;
        initialMoveY = aY;
    }

    public void move(float x, float y) {
        mapCenterMoveX += (x - initialMoveX);
        initialMoveX = x;
        mapCenterMoveY += (y - initialMoveY);
        initialMoveY = y;
        invalidate();
    }

    public void scale(int aScale) {
        scale = aScale;
    }

   /* public byte[] getPngDump() {

        // render
        Bitmap returnedBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = this.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }
        draw(canvas);

        // crop borders etc
        returnedBitmap = Bitmap.createBitmap(returnedBitmap, 6, 6, this.getWidth() - 50, this.getHeight() - 70);

        // return
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        returnedBitmap.compress(Bitmap.CompressFormat.PNG, 50, buff);
        returnedBitmap.recycle();
        return buff.toByteArray();
    }*/

    private void calculateAndDrawSide(Canvas canvas, Leg l, Point2D first, Point2D second, Leg prevLeg, Float aMeasure, String azimuthUnits, boolean left) {
        double galleryWidthAngle;
        if (aMeasure != null && aMeasure > 0) {

            // first or middle by 90'
            if (prevLeg == null || l.isMiddle()) {
                float angle = first.getAngle();
                if (horizontalPlan) {
                    if (left) {
                        angle = MapUtilities.minus90Degrees(angle);
                    } else {
                        angle = MapUtilities.add90Degrees(angle);
                    }
                }
                galleryWidthAngle = Math.toRadians(angle);
            } else {
                float angle = first.getAngle();
                // each next in the gallery by the bisector
                if (l.getGalleryId().equals(prevLeg.getGalleryId())) {

                    angle = MapUtilities.getMiddleAngle(MapUtilities.getAzimuthInDegrees(prevLeg.getAzimuth(), azimuthUnits), angle);

                    if (horizontalPlan) {
                        if (left) {
                            angle = MapUtilities.minus90Degrees(angle);
                        } else {
                            angle = MapUtilities.add90Degrees(angle);
                        }
                    }
                } else { // new galleries again by 90'
                    if (horizontalPlan) {
                        if (left) {
                            angle = MapUtilities.minus90Degrees(angle);
                        } else {
                            angle = MapUtilities.add90Degrees(angle);
                        }
                    }
                }

                galleryWidthAngle = Math.toRadians(angle);
            }

            float deltaY = -(float) (aMeasure * Math.cos(galleryWidthAngle) * scale);
            float deltaX = (float) (aMeasure * Math.sin(galleryWidthAngle) * scale);
            drawSideMeasurePoint(canvas, l.isMiddle(), first, second, deltaX, deltaY);
        }
    }

    private void drawSideMeasurePoint(Canvas aCanvas, boolean isMiddle, Point2D aFirst, Point2D aSecond, float aDeltaX, float aDeltaY) {
        if (isMiddle) {
            aCanvas.drawCircle(mapCenterMoveX + aSecond.getX() + aDeltaX, mapCenterMoveY + aSecond.getY() + aDeltaY, MEASURE_POINT_RADIUS, polygonWidthPaint);
        } else {
            aCanvas.drawCircle(mapCenterMoveX + aFirst.getX() + aDeltaX, mapCenterMoveY + aFirst.getY() + aDeltaY, MEASURE_POINT_RADIUS, polygonWidthPaint);
        }

    }

    public int getScale() {
        return scale;
    }

    public float getMoveX() {
        return mapCenterMoveX;
    }

    public float getMoveY() {
        return mapCenterMoveY;
    }

    public void setAnnotateMap(boolean annotateMap) {
        this.annotateMap = annotateMap;
    }

    public void setDrawingsVisible(boolean drawingsVisible) {
        mDrawingsVisible = drawingsVisible;
        invalidate();
    }

    /**
     * Currently in plain static field.
     */
    public void preserveView() {
        if (horizontalPlan) {
            mPlanView = new MapViewPosition(mapCenterMoveX, mapCenterMoveY, scale);
        } else {
            mSectionView = new MapViewPosition(mapCenterMoveX, mapCenterMoveY, scale);
        }
    }

    /**
     * View reset when changing the active project or restarting the app.
     */
    public static void resetView() {
        mPlanView = null;
        mSectionView = null;
    }

    private boolean applyStoredView() {
        if (horizontalPlan && mPlanView != null) {
            move(mPlanView.moveX, mPlanView.moveY);
            scale = mPlanView.scale;
            return true;
        } else if (!horizontalPlan && mSectionView != null) {
            move(mSectionView.moveX, mSectionView.moveY);
            scale = mSectionView.scale;
            return true;
        }

        return false;
    }
}
