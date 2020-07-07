package com.astoev.cave.survey.activity.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

import static android.graphics.Color.WHITE;
import static android.graphics.Paint.Style.STROKE;

public class CameraDialogOverlay extends View {

    private boolean cameraMode = false;
    private Paint overlayPaint;
    private DisplayMetrics displayMetrics;
    private Point center;

    private float smallCircleRadius;
    private float bigCircleRadius;
    private float crossSize;
    private float centerCircle;

    public CameraDialogOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        displayMetrics = getResources().getDisplayMetrics();

        overlayPaint = new Paint();
        overlayPaint.setColor(WHITE);
        overlayPaint.setStrokeWidth(1 * displayMetrics.density);
        overlayPaint.setStyle(STROKE);

        smallCircleRadius = 10 * displayMetrics.density;
        bigCircleRadius = 30 * displayMetrics.density;
        crossSize = 40 * displayMetrics.density;
        centerCircle = 3 * displayMetrics.density;

        int crossHalfSize = (int) (crossSize / 2);
        center = new Point(displayMetrics.widthPixels / 2 - crossHalfSize, displayMetrics.heightPixels / 2 - crossHalfSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cameraMode) {
            // circles
            canvas.drawCircle(center.x, center.y, smallCircleRadius, overlayPaint);
            canvas.drawCircle(center.x, center.y, bigCircleRadius, overlayPaint);
            // lines
            canvas.drawLine(center.x - centerCircle, center.y - centerCircle, center.x - crossSize, center.y - crossSize, overlayPaint);
            canvas.drawLine(center.x + centerCircle, center.y + centerCircle, center.x + crossSize, center.y + crossSize, overlayPaint);
            canvas.drawLine(center.x - centerCircle, center.y + centerCircle, center.x - crossSize, center.y + crossSize, overlayPaint);
            canvas.drawLine(center.x + centerCircle, center.y - centerCircle, center.x + crossSize, center.y - crossSize, overlayPaint);
        }
    }

    public void setCameraMode(boolean cameraMode) {
        this.cameraMode = cameraMode;
        setWillNotDraw(!cameraMode);
    }
}
