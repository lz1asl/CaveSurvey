package com.astoev.cave.survey.test.util;

import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.util.PointUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PointUtilTest  {

    @Test
    public void testCreateFirstPoint() {
        Point p = PointUtil.createFirstPoint();
        assertEquals("0", p.getName());
    }

    @Test
    public void testCreateSecondPoint() {
        Point p = PointUtil.createSecondPoint();
        assertEquals("1", p.getName());
    }

    @Test
    public void testGetPointGalleryNameAndIndex() {
        String name = "A0";
        assertEquals("A", PointUtil.getPointGalleryName(name));
        assertEquals("0", PointUtil.getPointName(name));

        name = "A11";
        assertEquals("A", PointUtil.getPointGalleryName(name));
        assertEquals("11", PointUtil.getPointName(name));

        name = "B5";
        assertEquals("B", PointUtil.getPointGalleryName(name));
        assertEquals("5", PointUtil.getPointName(name));

        name = "A0";
        assertEquals("A", PointUtil.getPointGalleryName(name));
        assertEquals("0", PointUtil.getPointName(name));
    }

    @Test
    public void testGetMiddlePointFromString() {
        String name = "C3-C4@1.5";
        assertEquals("C3", PointUtil.getMiddleFromName(name));
        assertEquals("C4", PointUtil.getMiddleToName(name));
        assertEquals(1.5f, PointUtil.getMiddleLength(name), 0.001);
    }

    @Test
    public void testIsMiddlePoint() {
        assertFalse(PointUtil.isMiddlePoint("A1", "A2"));
        assertFalse(PointUtil.isMiddlePoint("B1", ""));
        assertTrue(PointUtil.isMiddlePoint("C3-C4@1.5", "C4"));
        assertTrue(PointUtil.isMiddlePoint("C3", "C3-C4@1.5"));
    }
}
