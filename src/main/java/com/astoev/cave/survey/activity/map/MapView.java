package com.astoev.cave.survey.activity.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.DaoUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.astoev.cave.survey.model.Option.CODE_DISTANCE_UNITS;
import static com.astoev.cave.survey.model.Option.UNIT_FEET;
import static com.astoev.cave.survey.model.Option.UNIT_METERS;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/1/12
 * Time: 11:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class MapView extends View {

    public static float pointRadius;
    public static float middlePointRadius;
    public static float measurePointRadius;
    public static float currPointRadius;
    public static float sidePointRadius;
    private float labelDeviationX;
    private float labelDeviationY;
    private float spacing;
    private static float [] GRID_STEPS;

    private final Paint polygonPaint = new Paint();
    private final Paint polygonWidthPaint = new Paint();
    private final Paint overlayPaint = new Paint();
    private final Paint youAreHerePaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint vectorsPaint = new Paint();
    private final Paint vectorPointPaint = new Paint();
    private int scale = 10;
    private int mapCenterMoveX = 0;
    private int mapCenterMoveY = 0;
    private float initialMoveX = 0;
    private float initialMoveY = 0;
    private Point northCenter = new Point();

    private List<Integer> processedLegs = new ArrayList<>();
    
    private SparseArray<Point2D> mapPoints = new SparseArray<>();
    private SparseIntArray galleryColors = new SparseIntArray();
    private SparseArray<String> galleryNames = new SparseArray<>();

    private boolean horizontalPlan = true;

    private static float screenScale;
    private static String distanceUnits;


    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        screenScale = getResources().getDisplayMetrics().density;
        pointRadius = 3 * screenScale;
        middlePointRadius = 2 * screenScale;
        measurePointRadius = 2 * screenScale;
        currPointRadius = 8 * screenScale;
        sidePointRadius = 1 * screenScale;
        labelDeviationX = 10 * screenScale;
        labelDeviationY = 15 * screenScale;
        spacing = 5 * screenScale;

        GRID_STEPS = new float[] {20 * screenScale,
                10 * screenScale,
                5 * screenScale,
                5 * screenScale,
                2 * screenScale,
                2 * screenScale,
                2 * screenScale,
                2 * screenScale,
                1 * screenScale,
                1 * screenScale,
                1 * screenScale};

        polygonPaint.setColor(Color.RED);
        polygonPaint.setStrokeWidth(2);
        polygonWidthPaint.setColor(Color.RED);
        polygonWidthPaint.setStrokeWidth(1);
        polygonWidthPaint.setAlpha(50);
        polygonPaint.setTextSize(polygonPaint.getTextSize() * screenScale);
        overlayPaint.setColor(Color.WHITE);
        overlayPaint.setTextSize(overlayPaint.getTextSize() * screenScale);
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

        distanceUnits = "m";
        switch (Options.getOptionValue(CODE_DISTANCE_UNITS)) {
            case UNIT_METERS:
                distanceUnits = "m";
                break;
            case UNIT_FEET:
                distanceUnits = "ft";
                break;
            default:
                throw new RuntimeException("Unit not implemented");
        }

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
            int gridStep = (int) (GRID_STEPS[gridStepIndex] * scale);

            // grid start
            int gridStartX = mapCenterMoveX % gridStep - (int) spacing + centerX - (centerX / gridStep) * gridStep;
            int gridStartY = mapCenterMoveY % gridStep - (int) spacing + centerY - (centerY / gridStep) * gridStep;

            // grid horizontal lines
            for (int x=0; x<maxX/gridStep; x++) {
                canvas.drawLine(x*gridStep + spacing + gridStartX, spacing, x*gridStep + spacing + gridStartX, maxY - spacing, gridPaint);
            }
            // grid vertical lines
            for (int y=0; y<maxY/gridStep; y++) {
                canvas.drawLine(spacing, y*gridStep + spacing + gridStartY, maxX - spacing, y*gridStep + spacing + gridStartY, gridPaint);
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
                                    canvas.drawText(pointLabel, mapCenterMoveX + first.getX() + labelDeviationX, mapCenterMoveY + first.getY() + labelDeviationY, polygonPaint);
                                }
                                canvas.drawCircle(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), pointRadius, polygonPaint);
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
                                    canvas.drawCircle(mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), currPointRadius, youAreHerePaint);
                                } else {
                                    canvas.drawCircle(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), currPointRadius, youAreHerePaint);
                                }

                            }

                            DaoUtil.refreshPoint(l.getToPoint());
                            if (l.isMiddle()) {
                                canvas.drawCircle(mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), middlePointRadius, polygonPaint);
                            } else {
                                pointLabel = galleryNames.get(l.getGalleryId()) + l.getToPoint().getName();
                                if (scale >= 3) {
                                    canvas.drawText(pointLabel, mapCenterMoveX + second.getX() + labelDeviationX, mapCenterMoveY + second.getY() + labelDeviationY, polygonPaint);
                                }
                                canvas.drawCircle(mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), pointRadius, polygonPaint);
                            }

                        }

                        // leg
                        if (!l.isMiddle()) {
                            canvas.drawLine(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), polygonPaint);
                        }

                        Leg prevLeg = DaoUtil.getLegByToPoint(l.getFromPoint());

                        if (scale >= 5) {
                            if (horizontalPlan) {
                                // left
                                calculateAndDrawSide(canvas, l, first, second, prevLeg, first.getLeft(), azimuthUnits, true);
                                // right
                                calculateAndDrawSide(canvas, l, first, second, prevLeg, first.getRight(), azimuthUnits, false);
                            } else {
                                // top
                                calculateAndDrawSide(canvas, l, first, second, prevLeg, first.getLeft(), slopeUnits, true);
                                // down
                                calculateAndDrawSide(canvas, l, first, second, prevLeg, first.getRight(), slopeUnits, false);
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
                                    canvas.drawCircle(mapCenterMoveX + first.getX() + deltaX, mapCenterMoveY + first.getY() + deltaY, sidePointRadius, vectorPointPaint);
                                }
                            }
                        }

                        processedLegs.add(l.getId());
                    }

                }
            }


            // borders
            //top
            canvas.drawLine(spacing, spacing, maxX - spacing, spacing, overlayPaint);
            //right
            canvas.drawLine(maxX - spacing, spacing, maxX - spacing, maxY - spacing, overlayPaint);
            // bottom
            canvas.drawLine(spacing, maxY - spacing, maxX - spacing, maxY - spacing, overlayPaint);
            //left
            canvas.drawLine(spacing, maxY - spacing, spacing, spacing, overlayPaint);

            float scaled30 = 30 * screenScale;
            float scaled20 = 20 * screenScale;
            float scaled10 = 10 * screenScale;

            if (horizontalPlan) {
                // north arrow
                northCenter.set((int) (maxX - scaled20), (int) (scaled30));
                canvas.drawLine(northCenter.x, northCenter.y, northCenter.x + scaled10, northCenter.y + scaled10, overlayPaint);
                canvas.drawLine(northCenter.x + scaled10, northCenter.y + scaled10, northCenter.x, northCenter.y - scaled20, overlayPaint);
                canvas.drawLine(northCenter.x, northCenter.y - scaled20, northCenter.x - scaled10, northCenter.y + scaled10, overlayPaint);
                canvas.drawLine(northCenter.x - scaled10, northCenter.y + scaled10, northCenter.x, northCenter.y, overlayPaint);
                canvas.drawText("N", northCenter.x + 5 * screenScale, northCenter.y - scaled10, overlayPaint);
            } else {
                //  up awrrow
                northCenter.set((int) (maxX - 15 * screenScale), (int) scaled10);
                canvas.drawLine(northCenter.x + 1 * screenScale, northCenter.y, northCenter.x + 6 * screenScale, northCenter.y + scaled10, overlayPaint);
                canvas.drawLine(northCenter.x - 5 * screenScale, northCenter.y + scaled10, northCenter.x, northCenter.y, overlayPaint);
                canvas.drawLine(northCenter.x, northCenter.y - 1 * screenScale, northCenter.x, northCenter.y + scaled20, overlayPaint);
            }

            // scale
            canvas.drawText("x" + scale, (20 * screenScale + gridStep/2), 45 * screenScale, overlayPaint);
            canvas.drawLine(scaled30, 25 * screenScale, scaled30, 35 * screenScale, overlayPaint);
            canvas.drawLine(scaled30, scaled30, scaled30 + gridStep, scaled30, overlayPaint);
            canvas.drawLine(scaled30 + gridStep, 25 * screenScale, scaled30 + gridStep, 35 * screenScale, overlayPaint);
            canvas.drawText(GRID_STEPS[gridStepIndex]  + " " + distanceUnits , 15 * screenScale + gridStep/2, 25 * screenScale, overlayPaint);

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
        this.horizontalPlan = horizontalPlan;
        scale = 10;
        mapCenterMoveX = 0;
        mapCenterMoveY = 0;
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

    public byte[] getPngDump() {

        // render
        Bitmap returnedBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = this.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }
        draw(canvas);

        // crop borders etc
        int crop = (int) (1.5 * spacing); // remove the border
        returnedBitmap = Bitmap.createBitmap(returnedBitmap, crop, crop, this.getWidth() - crop * 2, this.getHeight() - crop * 2);

        // return
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        returnedBitmap.compress(Bitmap.CompressFormat.PNG, 50, buff);
        return buff.toByteArray();
    }

    private void calculateAndDrawSide(Canvas canvas, Leg l, Point2D first, Point2D second, Leg prevLeg, Float aMeasure, String anUnits, boolean left) {
        double galleryWidthAngle;
        if (aMeasure != null && aMeasure > 0) {

            // first or middle by 90'
            if (prevLeg == null || l.isMiddle()) {
                float angle;
                if (horizontalPlan) {
                    if (left) {
                        angle = MapUtilities.minus90Degrees(first.getAngle());
                    } else {
                        angle = MapUtilities.add90Degrees(first.getAngle());
                    }
                } else {
                    if (left) {
                        angle = Option.MIN_VALUE_AZIMUTH;
                    } else {
                        angle = Option.MAX_VALUE_AZIMUTH_DEGREES / 2;
                    }
                }
                galleryWidthAngle = Math.toRadians(angle);
            } else {
                float angle = first.getAngle() == null ? 0 : first.getAngle();
                // each next in the gallery by the bisector
                if (l.getGalleryId().equals(prevLeg.getGalleryId())) {

                    if (horizontalPlan) {
                        angle = MapUtilities.getMiddleAngle(MapUtilities.getAzimuthInDegrees(prevLeg.getAzimuth(), anUnits), angle);
                        if (left) {
                            angle = MapUtilities.minus90Degrees(angle);
                        } else {
                            angle = MapUtilities.add90Degrees(angle);
                        }
                    } else {
                        if (left) {
                            angle = Option.MIN_VALUE_AZIMUTH;
                        } else {
                            angle = Option.MAX_VALUE_AZIMUTH_DEGREES / 2;
                        }
                    }
                } else { // new galleries again by 90'
                    if (horizontalPlan) {
                        if (left) {
                            angle = MapUtilities.minus90Degrees(angle);
                        } else {
                            angle = MapUtilities.add90Degrees(angle);
                        }
                    } else {
                        if (left) {
                            angle = Option.MIN_VALUE_AZIMUTH;
                        } else {
                            angle = Option.MAX_VALUE_AZIMUTH_DEGREES / 2;
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
            aCanvas.drawCircle(mapCenterMoveX + aSecond.getX() + aDeltaX, mapCenterMoveY + aSecond.getY() + aDeltaY, sidePointRadius, polygonWidthPaint);
        } else {
            aCanvas.drawCircle(mapCenterMoveX + aFirst.getX() + aDeltaX, mapCenterMoveY + aFirst.getY() + aDeltaY, sidePointRadius, polygonWidthPaint);
        }

    }

}
