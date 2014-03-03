package com.astoev.cave.survey.test.util;

import com.astoev.cave.survey.util.StringUtils;
import junit.framework.TestCase;

import junit.framework.Assert;

public class StringUtilsTest extends TestCase {

    public void testIsEmpty() {
        String nullString = null;
        assertTrue(StringUtils.isEmpty(nullString));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty("text"));
    }

    public void testIsNotEmpty() {
        String nullString = null;
        assertFalse(StringUtils.isNotEmpty(nullString));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty("text"));
    }

    public void testCompare() {
        // same strings
        assertEquals(0, StringUtils.compare("", ""));
        assertEquals(0, StringUtils.compare("1", "1"));
        assertEquals(0, StringUtils.compare("ab", "ab"));
        assertEquals(0, StringUtils.compare("ab-1", "ab-1"));

        // building up
        assertEquals(-1, StringUtils.compare("1", "2"));
        assertEquals(-1, StringUtils.compare("1-1", "2"));
        assertEquals(-1, StringUtils.compare("1", "1-1"));
    }
}