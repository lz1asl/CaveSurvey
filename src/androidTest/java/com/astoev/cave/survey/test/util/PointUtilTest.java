package com.astoev.cave.survey.test.util;

import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.util.PointUtil;

import junit.framework.TestCase;

import org.junit.Test;

public class PointUtilTest extends TestCase {
    public void testCreateFirstPoint() {
        Point p = PointUtil.createFirstPoint();
        assertEquals("0", p.getName());
    }

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

        name = "A2-A3_at_8.6";
        assertEquals("A", PointUtil.getPointGalleryName(name));
        assertEquals("3", PointUtil.getPointName(name));

    }
}
