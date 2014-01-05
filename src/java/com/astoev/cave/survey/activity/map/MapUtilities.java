package com.astoev.cave.survey.activity.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

/**
 * Created by astoev on 12/31/13.
 */
public class MapUtilities {
    private static final int[] COLORS = new int[]{Color.YELLOW, Color.RED, Color.GRAY, Color.GREEN, Color.BLUE};

    public static int getNextGalleryColor(int currentCountArg) {
        // assure predictable colors for the galleries, start repeating colors if too many galleries
        int colorIndex = currentCountArg % COLORS.length;
        return COLORS[colorIndex];
    }

    public static Bitmap combineBitmaps(Bitmap first, Bitmap second) {
        Bitmap bmOverlay;
        if (first != null) {
            bmOverlay = Bitmap.createBitmap(first.getWidth(), first.getHeight(), first.getConfig());
        } else {
            bmOverlay = Bitmap.createBitmap(second.getWidth(), second.getHeight(), second.getConfig());
        }
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(first, new Matrix(), null);
        if (second != null) {
            canvas.drawBitmap(second, 0, 0, null);
        }

        return bmOverlay;
    }
}
