package com.astoev.cave.survey.activity.map;

import android.graphics.Color;

/**
 * Created by astoev on 12/31/13.
 */
public class MapUtilities {
    private static final int[] COLORS = new int[]{ Color.BLUE, Color.YELLOW, Color.RED, Color.GRAY, Color.GREEN};

    public static Integer getNextGalleryColor(Integer aGalleryId) {
        // assure predictable colors for the galleries, start repeating colors if too many galleries
        int colorIndex = aGalleryId;
        while (colorIndex >= COLORS.length) {
            colorIndex -= COLORS.length;
        }
        return COLORS[colorIndex];
    }
}
