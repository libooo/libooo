// -*-java-*-
//
// File:      MetaDataTest.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Aug 7, 2007
// Usage:     -
// Info:      -

package herschel.ia.pal.versioning;

import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.pal.MapContext;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This class checks changing meta data values through convenience methods.
 * These tests do not use pools, so this is put as a specific test.
 */
public class MetaDataTest extends TestCase {

    public MetaDataTest(String name) {
        super(name);
    }

    public void testNoModifyProduct(){

        Product p1 = new Product();
        MetaData m1 = new MetaData(p1.getMeta());
        assertTrue(p1.getMeta().equals(m1));
    }

    public void testModifyProductCreator(){

        Product p1 = new MapContext();
        MetaData m1 = new MetaData(p1.getMeta());
        p1.setCreator("abc");
        assertFalse(p1.getMeta().equals(m1));
    }

    public void testModifyProductType(){

        Product p1 = new MapContext();
        MetaData m1 = new MetaData(p1.getMeta());
        p1.setType("abc");
        assertFalse(p1.getMeta().equals(m1));
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(MetaDataTest.class);
        return suite;
    }
}
