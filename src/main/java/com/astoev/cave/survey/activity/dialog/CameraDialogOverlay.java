package com.astoev.cave.survey.activity.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import static android.graphics.Color.YELLOW;

public class CameraDialogOverlay extends View {

    private boolean cameraMode = false;
    private Paint overlayPaint;
    private Float scale;

    public CameraDialogOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        overlayPaint = new Paint();
        overlayPaint.setColor(YELLOW);
        overlayPaint.setStrokeWidth(2);
        scale = getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cameraMode) {
            canvas.drawLine(0,0, 500, 500, overlayPaint);
        }
    }

    public boolean isCameraMode() {
        return cameraMode;
    }

    public void setCameraMode(boolean cameraMode) {
        this.cameraMode = cameraMode;
    }
}
