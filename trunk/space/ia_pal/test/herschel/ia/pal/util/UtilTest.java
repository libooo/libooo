// -*-java-*-
//
// File:      UtilTest.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Mar 6, 2008
// Usage:     -
// Info:      -

package herschel.ia.pal.util;

import herschel.ia.dataset.Product;
import herschel.ia.dataset.ProductComparator;
import herschel.ia.pal.Context;
import herschel.ia.pal.ListContext;
import herschel.ia.pal.MapContext;
import junit.framework.TestCase;

/**
 * Test methods for Util class.
 */
public class UtilTest extends TestCase {

    /**
     * Test method for {@link Util#checkNotNull(Object, String)}.
     */
    public void testCheckNotNull() {
	try {
	    Util.checkNotNull(null, "null");
	    fail("An IllegalArgumentException should be thrown");
	} catch (IllegalArgumentException e) {}

	try {
	    Util.checkNotNull("hello", "string");
	} catch (IllegalArgumentException e) {
	    fail("An IllegalArgumentException should not be thrown");
	}
    }

    /**
     * Test method for {@link Util#asClass(String)}.
     */
    public void testAsClass() {
	assertEquals(Product.class,    Util.asClass(Product.class.getName()));
	assertEquals(Context.class,    Util.asClass(Context.class.getName()));
	assertEquals(MapContext.class, Util.asClass(MapContext.class.getName()));
	assertEquals(ListContext.class,Util.asClass(ListContext.class.getName()));

	try {
	    Util.asClass("some.non.existing.ClassName");
	    fail("An IllegalArgumentException should be thrown");
	} catch (IllegalArgumentException e) {}
    }

    /**
     * Test method for {@link Util#isClass(Class, Class)}.
     */
    public void testIsClass() {
	assertTrue(Util.isClass(ProductComparator.class, Product.class)); // interface
	assertTrue(Util.isClass(Product.class, Product.class));
	assertTrue(Util.isClass(Product.class, Context.class));
	assertTrue(Util.isClass(Product.class, MapContext.class));
	assertTrue(Util.isClass(Product.class, ListContext.class));
	assertTrue(Util.isClass(Context.class, MapContext.class));
	assertTrue(Util.isClass(Context.class, ListContext.class));
	assertTrue(Util.isClass(Number.class,  Double.class));
	assertTrue(Util.isClass(null, null));

	assertFalse(Util.isClass(Context.class, Product.class));
	assertFalse(Util.isClass(Context.class, String.class));
	assertFalse(Util.isClass(Context.class, null));
	assertFalse(Util.isClass(null, Context.class));
    }

    /**
     * Test method for {@link Util#isValidClassName(String)}.
     */
    public void testIsValidClassName() {
	assertTrue(Util.isValidClassName("one.two.three.ClassName"));
	assertTrue(Util.isValidClassName("one.two.ClassName"));
	assertTrue(Util.isValidClassName("one.ClassName"));
	assertTrue(Util.isValidClassName("ClassName"));
	assertTrue(Util.isValidClassName("some.package.p1.ClassName"));
	assertTrue(Util.isValidClassName("some.package.ClassName2"));
	assertTrue(Util.isValidClassName("some.package.ClassName$2"));
	assertTrue(Util.isValidClassName("some.package.ClassName$InnerClass"));

	assertFalse(Util.isValidClassName(".ClassName"));
	assertFalse(Util.isValidClassName("some.package..ClassName"));
	assertFalse(Util.isValidClassName("some.package.ClassName\n"));
	assertFalse(Util.isValidClassName("some.package.Class Name"));
	assertFalse(Util.isValidClassName("some. package.ClassName"));
	assertFalse(Util.isValidClassName("some.pac kage.ClassName"));
	assertFalse(Util.isValidClassName("some.package.ClassName."));
	assertFalse(Util.isValidClassName(" some.package.ClassName"));
	assertFalse(Util.isValidClassName("some.package.1.ClassName"));
    }
}
