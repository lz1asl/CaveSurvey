package com.astoev.cave.survey.test;

import com.astoev.cave.survey.model.Gallery;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 2/28/13
 * Time: 12:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class GalleryTest extends TestCase {

    public void testNextName() {
        assertEquals("B", Gallery.nextName("A"));
        assertEquals("C", Gallery.nextName("B"));
        assertEquals("AZ", Gallery.nextName("Z"));
        assertEquals("AB", Gallery.nextName("AA"));
        assertEquals("AZZZZ", Gallery.nextName("ZZZZ"));
    }
}
