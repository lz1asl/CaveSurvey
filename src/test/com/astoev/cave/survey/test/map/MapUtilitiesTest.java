package com.astoev.cave.survey.test.map;

import com.astoev.cave.survey.activity.map.MapUtilities;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by astoev on 1/20/14.
 */
public class MapUtilitiesTest extends TestCase {

    @Test
    public void testGetNextGalleryColor() {
        int count = (int) (Math.random() * 5);
        int initial = MapUtilities.getNextGalleryColor(count);

        for (int i=0; i<3; i++) {
            // predictable for same input
            assertEquals(initial, MapUtilities.getNextGalleryColor(count));

            // different for new input
            assertNotSame(initial, MapUtilities.getNextGalleryColor(i));
        }
    }
}
