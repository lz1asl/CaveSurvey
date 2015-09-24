package com.astoev.cave.survey.activity.draw;

import android.graphics.DashPathEffect;
import android.graphics.Paint;

import com.astoev.cave.survey.model.SketchElement;

/**
 * Drawing options that can be saved for reuse.
 *
 */
public class DrawingOptions {

    public enum TYPES { THICK, FILL, DASH };

    private int mSize;
    private TYPES mType;
    private int mColor;

    public DrawingOptions() {
    }

    public DrawingOptions(int color, int size, TYPES type) {
        mColor = color;
        mSize = size;
        mType = type;
    }

    public static Paint optionsToPaint(DrawingOptions anOptions) {
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setColor(anOptions.mColor);
        paint.setStrokeWidth(anOptions.mSize);
        paint.setAlpha(200);
        switch (anOptions.mType) {
            case THICK:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeCap(Paint.Cap.ROUND);
                break;
            case FILL:
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeJoin(Paint.Join.BEVEL);
                paint.setStrokeCap(Paint.Cap.SQUARE);
                break;
            case DASH:
                paint.setStyle(Paint.Style.STROKE);
                paint.setPathEffect(new DashPathEffect(new float[]{2 * anOptions.mSize, 4 * anOptions.mSize}, 0));
                break;
        }
        return paint;
    }

    public static DrawingOptions fromSketchElement(SketchElement aElement) {
        DrawingOptions options = new DrawingOptions();
        options.setColor(aElement.getColor());
        options.setSize(aElement.getSize());
        options.setType(aElement.getType());

        return options;
    }

    public static DrawingOptions copy(DrawingOptions anOptions) {
        DrawingOptions options = new DrawingOptions();
        options.setType(anOptions.getType());
        options.setSize(anOptions.getSize());
        options.setColor(anOptions.getColor());
        return options;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public TYPES getType() {
        return mType;
    }

    public void setType(TYPES type) {
        mType = type;
    }
}
