package test;

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

    public void test1() {
        String name = Gallery.nextName("A");
        assertEquals("B", name);
    }

    public void test2() {
     String   name = Gallery.nextName("AA");
        assertEquals("AB", name);
    }

    public void test3() {
        String  name = Gallery.nextName("Z");
        assertEquals("AZ", name);
    }

//    public void test4() {
//        String    name = Gallery.nextName("ABCZZ");
//        assertEquals("ABDZZ", name);
//    }

    public void test5() {
        String  name = Gallery.nextName("ZZZZ");
        assertEquals("AZZZZ", name);
    }
}
