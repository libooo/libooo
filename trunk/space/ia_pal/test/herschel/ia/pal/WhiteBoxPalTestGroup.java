// -*-java-*-
//
// File:      WhiteBoxPalTest.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Aug 13, 2007
// Usage:     -
// Info:      -

package herschel.ia.pal;

import herschel.ia.dataset.ArrayDataset;
import herschel.ia.dataset.Dataset;
import herschel.ia.dataset.Product;
import herschel.ia.numeric.Double3d;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ListIterator;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class contains some specific white box test cases.
 */
public class WhiteBoxPalTestGroup extends AbstractPalTestGroup {

    /** Constructor. */
    public WhiteBoxPalTestGroup(String name, AbstractPalTestCase testcase) {
        super(name, testcase);
    }

    public void testSpr3458() throws IOException, GeneralSecurityException {

        ProductRef ref = new ProductRef(new Product());

        // Test MapContext
        {
            ProductStorage storage = new ProductStorage();
            storage.register(getPool(1));

            MapContext map = new MapContext();
            map.getRefs().put("one", ref);
            ProductRef rmap = storage.save(map);
            assertFalse(rmap.isDirty(storage));
            map.getRefs().put("one", ref);      // should not set the dirty flag
            assertFalse(rmap.isDirty(storage));
        }

        // Test ListContext
        {
            ProductStorage storage = new ProductStorage();
            storage.register(getPool(2));

            ListIterator<ProductRef> iterator;
            ListContext list = new ListContext();
            list.getRefs().add(ref);
            ProductRef rlist = storage.save(list);
            assertFalse(rlist.isDirty(storage));
            iterator = list.getRefs().listIterator();
            iterator.next();
            iterator.set(ref);                  // should not set the dirty flag
            assertFalse(rlist.isDirty(storage));
        }
    }

    
    public void testSpr4590() throws IOException, GeneralSecurityException{
    	ProductStorage storage = new ProductStorage();
        storage.register(getPool(1));
        Dataset d= new ArrayDataset(new Double3d(3,2,2));
        Product p = TestProductGenerator.getSimpleProduct();
        p.set("foo", d);
        ProductRef ref = storage.save(p);
        System.out.println("****"+ref.isLoaded());
        assertTrue(ref.isLoaded()==false);
        
    }
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(WhiteBoxPalTestGroup.class);
        return suite;
    }
}
