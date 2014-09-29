package com.astoev.cave.survey.test.util;

import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.model.Point;
import junit.framework.TestCase;

import junit.framework.Assert;

public class PointUtilTest extends TestCase {
    public void testCreateFirstPoint() {
        Point p = PointUtil.createFirstPoint();
        assertEquals("0", p.getName());
    }

    public void testCreateSecondPoint() {
        Point p = PointUtil.createSecondPoint();
        assertEquals("1", p.getName());
    }
}
