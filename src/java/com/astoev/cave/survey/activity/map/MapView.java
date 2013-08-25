package com.astoev.cave.survey.activity.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/1/12
 * Time: 11:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class MapView extends View {

    public static final int POINT_RADIUS = 3;
    public static final int CURR_POINT_RADIUS = 8;
    private final static int LABEL_DEVIATION_X = 5;
    private final static int LABEL_DEVIATION_Y = 5;
    private static final int[] COLORS = new int[]{ Color.BLUE, Color.YELLOW, Color.RED, Color.GRAY, Color.GREEN};
    Paint polygonPaint = new Paint();
    Paint polygonWidthPaint = new Paint();
    Paint overlayPaint = new Paint();
    Paint youAreHerePaint = new Paint();
    Workspace mWorkspace;
    private Window mWindow;
    private float scale = 10;
    private int mapCenterMoveX = 0;
    private int mapCenterMoveY = 0;
    private float initialMoveX = 0;
    private float initialMoveY = 0;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        polygonPaint.setColor(Color.RED);
        polygonPaint.setStrokeWidth(2);
        polygonWidthPaint.setColor(Color.RED);
        polygonWidthPaint.setStrokeWidth(1);
        overlayPaint.setColor(Color.WHITE);
        youAreHerePaint.setColor(Color.WHITE);
        youAreHerePaint.setAlpha(50);
        mWorkspace = Workspace.getCurrentInstance();
    }

    @Override
    public void onDraw(Canvas canvas) {

        try {

            // prepare map surface
            int maxX = canvas.getWidth();
            int maxY = canvas.getHeight();

            int centerX = maxX / 2;
            int centerY = maxY / 2;

            int spacing = 5;


            // load the points
            List<Leg> legs = mWorkspace.getCurrProjectLegs();

            List<Integer> processedLegs = new ArrayList<Integer>();
            Map<Integer, Point2D> mapPoints = new HashMap<Integer, Point2D>();
            Map<Integer, Integer> galleryColors = new HashMap<Integer, Integer>();

            while (processedLegs.size() < legs.size()) {
                for (Leg l : legs) {

                    if (processedLegs.size() == legs.size()) {
                        break;
                    }

                    if (processedLegs.contains(l.getId())) {
                        // skip processed
                        continue;
                    } else {

                        // first leg ever
                        Point2D first;

                        if (processedLegs.size() == 0) {
                            first = new Point2D(Float.valueOf(centerX), Float.valueOf(centerY), l.getLeft(), l.getRight(), l.getAzimuth());
                        } else {
                            first = mapPoints.get(l.getFromPoint().getId());
                        }

                        if (!mapPoints.containsKey(l.getFromPoint().getId())) {
                            mapPoints.put(l.getFromPoint().getId(), first);

                            //color
                            if (!galleryColors.containsKey(l.getGalleryId())) {
                                galleryColors.put(l.getGalleryId(), getNextColor(l.getGalleryId()));
                            }
                            polygonPaint.setColor(galleryColors.get(l.getGalleryId()));

                            mWorkspace.getDBHelper().getPointDao().refresh(l.getFromPoint());
                            canvas.drawText(l.getFromPoint().getName(), mapCenterMoveX + first.getX() + LABEL_DEVIATION_X, mapCenterMoveY + first.getY() + LABEL_DEVIATION_Y, polygonPaint);
                            canvas.drawCircle(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), POINT_RADIUS, polygonPaint);
                        }

                        float deltaX;
                        float deltaY;
                        if (l.getDistance() == null || l.getAzimuth() == null) {
                            deltaX = 0;
                            deltaY = 0;
                        } else {
                            // todo slope in the distance
                            deltaY = -(float) (l.getDistance() * Math.cos(Math.toRadians(getAzimuthInDegrees(l.getAzimuth())))) * scale;
                            deltaX = (float) (l.getDistance() * Math.sin(Math.toRadians(getAzimuthInDegrees(l.getAzimuth())))) * scale;
                        }

                        Point2D second = new Point2D(first.getX() + deltaX, first.getY() + deltaY);
                        if (l.getLeft() != null) {
                            second.setLeft(l.getLeft());
                        }
                        if (l.getRight() != null) {
                            second.setRight(l.getRight());
                        }
                        if (l.getAzimuth() != null) {
                            second.setAzimuth(l.getAzimuth());
                        }

                        if (!mapPoints.containsKey(l.getToPoint().getId())) {
                            mapPoints.put(l.getToPoint().getId(), second);

                            // color
                            if (!galleryColors.containsKey(l.getGalleryId())) {
                                galleryColors.put(l.getGalleryId(), getNextColor(l.getGalleryId()));
                            }
                            polygonPaint.setColor(galleryColors.get(l.getGalleryId()));
                            polygonWidthPaint.setColor(galleryColors.get(l.getGalleryId()));

//                            Log.i(Constants.LOG_TAG_UI, "Drawing leg " + l.getFromPoint().getName() + ":" + l.getToPoint().getName() + "-" + l.getGalleryId());

                            if (mWorkspace.getActiveLegId().equals(l.getId())) {
                                // you are here
                                canvas.drawCircle(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), CURR_POINT_RADIUS, youAreHerePaint);
                            }

                            mWorkspace.getDBHelper().getPointDao().refresh(l.getToPoint());
                            canvas.drawText(l.getToPoint().getName(), mapCenterMoveX + second.getX() + LABEL_DEVIATION_X, mapCenterMoveY + second.getY() + LABEL_DEVIATION_Y, polygonPaint);
                            canvas.drawCircle(mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), POINT_RADIUS, polygonPaint);
                        }

                        // leg
                        canvas.drawLine(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), mapCenterMoveX + second.getX(), mapCenterMoveY + second.getY(), polygonPaint);

                        // left
                        if (first.getLeft() != null && first.getLeft()> 0) {
                            deltaY = -(float) (first.getLeft() * Math.cos(Math.toRadians(getAzimuthInDegrees(first.getAzimuth() )- 90))) * scale;
                            deltaX = (float) (first.getLeft() * Math.sin(Math.toRadians(getAzimuthInDegrees(first.getAzimuth() )- 90))) * scale;
                            canvas.drawLine(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), mapCenterMoveX + first.getX() + deltaX, mapCenterMoveY + first.getY() + deltaY, polygonWidthPaint);
                        }

                        // right
                        if (first.getRight() != null && first.getRight()> 0) {
                            deltaY = -(float) (first.getRight() * Math.cos(Math.toRadians(getAzimuthInDegrees(first.getAzimuth()) + 90))) * scale;
                            deltaX = (float) (first.getRight() * Math.sin(Math.toRadians(getAzimuthInDegrees(first.getAzimuth() )+ 90))) * scale;
                            canvas.drawLine(mapCenterMoveX + first.getX(), mapCenterMoveY + first.getY(), mapCenterMoveX + first.getX() + deltaX, mapCenterMoveY + first.getY() + deltaY, polygonWidthPaint);
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

            // north
            Point northCenter = new Point(maxX - 20, 30);
            canvas.drawLine(northCenter.x, northCenter.y, northCenter.x + 10, northCenter.y + 10, overlayPaint);
            canvas.drawLine(northCenter.x + 10, northCenter.y + 10, northCenter.x, northCenter.y - 20, overlayPaint);
            canvas.drawLine(northCenter.x, northCenter.y - 20, northCenter.x - 10, northCenter.y + 10, overlayPaint);
            canvas.drawLine(northCenter.x - 10, northCenter.y + 10, northCenter.x, northCenter.y, overlayPaint);
            canvas.drawText("N", northCenter.x + 5, northCenter.y - 10, overlayPaint);

            // scale
//            canvas.drawLine(20, maxY - 55, 55, maxY - 55, overlayPaint);
//            canvas.drawLine(20, maxY - 53, 20, maxY - 52, overlayPaint);
//            canvas.drawLine(55, maxY - 53, 55, maxY - 52, overlayPaint);
//            canvas.drawLine(55, maxY - 53, 55, maxY - 52, overlayPaint);

            canvas.drawText("x" + (int)scale, 20, 25, overlayPaint);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to draw map activity", e);
            UIUtilities.showNotification(getContext(), R.string.error);
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

    public void setWindow(Window aWindow) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private Integer getNextColor(Integer aGalleryId) {
        // assure predictable colors for the galleries, start repeating colors if too many galleries
        int colorIndex = aGalleryId;
        while (colorIndex >= COLORS.length) {
            colorIndex -= COLORS.length;
        }
        return COLORS[colorIndex];
    }

    private Float getAzimuthInDegrees(Float anAzimuth) {
        if (null == anAzimuth) {
            return null;
        }

        if (Option.UNIT_DEGREES.equals(Options.getOptionValue(Option.CODE_AZIMUTH_UNITS))) {
            return anAzimuth;
        } else {
            // convert from grads to degrees
            return anAzimuth * Option.MAX_VALUE_DEGREES / Option.MAX_VALUE_GRADS;
        }
    }
}
