package com.astoev.cave.survey.test.util;

import com.astoev.cave.survey.util.GalleryUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 2/28/13
 * Time: 12:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class GalleryUtilTest {

    @Test
    public void testFirstName() {
        assertEquals("A", GalleryUtil.getFirstGalleryName());
    }

    @Test
    public void testNextName() {
        assertEquals("B", GalleryUtil.generateNextGalleryName("A"));
        assertEquals("C", GalleryUtil.generateNextGalleryName("B"));
        assertEquals("Z", GalleryUtil.generateNextGalleryName("Y"));
        assertEquals("AA", GalleryUtil.generateNextGalleryName("Z"));
        assertEquals("AB", GalleryUtil.generateNextGalleryName("AA"));
        assertEquals("AZ", GalleryUtil.generateNextGalleryName("AY"));
        assertEquals("BA", GalleryUtil.generateNextGalleryName("AZ"));
        assertEquals("CA", GalleryUtil.generateNextGalleryName("BZ"));
        assertEquals("DA", GalleryUtil.generateNextGalleryName("CZ"));
        assertEquals("DB", GalleryUtil.generateNextGalleryName("DA"));
        assertEquals("AAA", GalleryUtil.generateNextGalleryName("ZZ"));
        assertEquals("AAB", GalleryUtil.generateNextGalleryName("AAA"));
        assertEquals("ABA", GalleryUtil.generateNextGalleryName("AAZ"));
        assertEquals("BAA", GalleryUtil.generateNextGalleryName("AZZ"));
        assertEquals("BAB", GalleryUtil.generateNextGalleryName("BAA"));
        assertEquals("BBA", GalleryUtil.generateNextGalleryName("BAZ"));
        assertEquals("CAA", GalleryUtil.generateNextGalleryName("BZZ"));
    }

}