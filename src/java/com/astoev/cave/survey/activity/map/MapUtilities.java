package com.astoev.cave.survey.activity.map;

import android.graphics.Color;

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
}
