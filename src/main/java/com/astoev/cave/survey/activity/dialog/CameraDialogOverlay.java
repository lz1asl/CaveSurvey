package com.astoev.cave.survey.activity.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.graphics.Paint.Style.STROKE;

public class CameraDialogOverlay extends View {

    private boolean cameraMode = false;
    private Paint overlayLightPaint;
    private Paint overlayDarkPaint;
    private DisplayMetrics displayMetrics;
    private Point center;

    private float smallCircleRadius;
    private float bigCircleRadius;
    private float crossSize;
    private float centerCircle;
    private float crossesOffset;

    public CameraDialogOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        displayMetrics = getResources().getDisplayMetrics();

        overlayLightPaint = new Paint();
        overlayLightPaint.setColor(WHITE);
        overlayLightPaint.setStrokeWidth(1 * displayMetrics.density);
        overlayLightPaint.setStyle(STROKE);

        overlayDarkPaint = new Paint();
        overlayDarkPaint.setColor(BLACK);
        overlayDarkPaint.setStrokeWidth(1 * displayMetrics.density);
        overlayDarkPaint.setStyle(STROKE);

        smallCircleRadius = 10 * displayMetrics.density;
        bigCircleRadius = 30 * displayMetrics.density;
        crossSize = 40 * displayMetrics.density;
        centerCircle = 3 * displayMetrics.density;

        int crossHalfSize = (int) (crossSize / 2);
        center = new Point(displayMetrics.widthPixels / 2 - crossHalfSize, displayMetrics.heightPixels / 2 - crossHalfSize);
        crossesOffset = 2 * displayMetrics.density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cameraMode) {
            // circles
            canvas.drawCircle(center.x, center.y, smallCircleRadius, overlayLightPaint);
            canvas.drawCircle(center.x, center.y, bigCircleRadius, overlayLightPaint);
            canvas.drawCircle(center.x, center.y, smallCircleRadius + crossesOffset, overlayDarkPaint);
            canvas.drawCircle(center.x, center.y, bigCircleRadius + crossesOffset, overlayDarkPaint);

            // crosses
            canvas.drawLine(center.x - centerCircle, center.y - centerCircle, center.x - crossSize, center.y - crossSize, overlayLightPaint);
            canvas.drawLine(center.x + centerCircle, center.y + centerCircle, center.x + crossSize, center.y + crossSize, overlayLightPaint);
            canvas.drawLine(center.x - centerCircle, center.y + centerCircle, center.x - crossSize, center.y + crossSize, overlayLightPaint);
            canvas.drawLine(center.x + centerCircle, center.y - centerCircle, center.x + crossSize, center.y - crossSize, overlayLightPaint);

            canvas.drawLine(center.x - centerCircle - crossesOffset, center.y - centerCircle, center.x - crossSize - crossesOffset, center.y - crossSize, overlayDarkPaint);
            canvas.drawLine(center.x + centerCircle - crossesOffset, center.y + centerCircle, center.x + crossSize - crossesOffset, center.y + crossSize, overlayDarkPaint);
            canvas.drawLine(center.x - centerCircle - crossesOffset, center.y + centerCircle, center.x - crossSize - crossesOffset, center.y + crossSize, overlayDarkPaint);
            canvas.drawLine(center.x + centerCircle - crossesOffset, center.y - centerCircle, center.x + crossSize - crossesOffset, center.y - crossSize, overlayDarkPaint);
        }
    }

    public void setCameraMode(boolean cameraMode) {
        this.cameraMode = cameraMode;
        setWillNotDraw(!cameraMode);
    }
}
