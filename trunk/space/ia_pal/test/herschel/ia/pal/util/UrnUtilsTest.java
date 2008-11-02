package herschel.ia.pal.util;

import herschel.ia.dataset.Product;
import herschel.ia.pal.MapContext;
import junit.framework.TestCase;

public class UrnUtilsTest extends TestCase {

    private static String urn0 = "urn:poolId:herschel.ia.dataset.Product:0";
    private static String urn1 = "urn:poolId:herschel.ia.dataset.Product:1";
    private static String urn2 = "urn:poolId:herschel.ia.pal.MapContext:2";
    private static String fakeUrn = "urn:fakePool:herschel.ia.NonExisting:3";

    public void testClass() {
	assertEquals(UrnUtils.getClass(urn0), Product.class);
	assertEquals(UrnUtils.getClass(urn1), Product.class);
	assertEquals(UrnUtils.getClass(urn2), MapContext.class);
	try {
	    UrnUtils.getClass(fakeUrn);
	    fail("An IllegalArgumentException should be raised");
	} catch (IllegalArgumentException e) { /* correct */ }
    }

    public void testClassName() {
	assertEquals(UrnUtils.getClassName(urn0), Product.class.getName());
	assertEquals(UrnUtils.getClassName(urn1), Product.class.getName());
	assertEquals(UrnUtils.getClassName(urn2), MapContext.class.getName());
	assertEquals(UrnUtils.getClassName(fakeUrn), "herschel.ia.NonExisting");
    }

    public void testProductId() {
	assertEquals(UrnUtils.getProductId(urn0), 0);
	assertEquals(UrnUtils.getProductId(urn1), 1);
	assertEquals(UrnUtils.getProductId(urn2), 2);
	assertEquals(UrnUtils.getProductId(fakeUrn), 3);
    }

    public void testGetLater() {
	assertEquals(UrnUtils.getLater(urn0, urn1), urn1);
	try {
	    UrnUtils.getLater(urn0, urn2);    // different classes
	    fail("An IllegalArgumentException should be raised");
	} catch (IllegalArgumentException e) { /* correct */ }
	try {
	    UrnUtils.getLater(urn0, fakeUrn); // different pools
	    fail("An IllegalArgumentException should be raised");
	} catch (IllegalArgumentException e) { /* correct */ }
    }

    public void testIsUrn() {

	// Good URNs
	assertTrue(UrnUtils.isUrn("urn:hsa:herschel.ia.dataset.Product:31"));
	assertTrue(UrnUtils.isUrn(urn0));
	assertTrue(UrnUtils.isUrn(urn1));
	assertTrue(UrnUtils.isUrn(urn2));

	// Good URN with newline == Bad URN
	assertFalse(UrnUtils.isUrn("urn:hsa:herschel.ia.dataset.Product:31\n"));

	// These are actually good URNs!
	assertTrue(UrnUtils.isUrn("urn:hsa:herschel.ia.dataset:3120"));
	assertTrue(UrnUtils.isUrn("urn:a:b:0"));
	assertTrue(UrnUtils.isUrn(fakeUrn));

	// Blanks are not allowed
	assertFalse(UrnUtils.isUrn("urn:a\nb\tc\0d:e:0"));

	// Some more tests
	assertFalse(UrnUtils.isUrn("urn:abcde"));
	assertFalse(UrnUtils.isUrn(""));

	// Null is accepted but returns false
	assertFalse(UrnUtils.isUrn(null));
    }

    public void testIsTag() {

	// Good tag
	assertTrue(UrnUtils.isTag("someTag"));

	// Good tag with new line == Bad tag
	assertFalse(UrnUtils.isTag("someTag\n"));

	// Some valid tags
	assertTrue(UrnUtils.isTag("tag-abcde"));
	assertTrue(UrnUtils.isTag("some:tag:with:colons"));

	// Some not valid tags
	assertFalse(UrnUtils.isTag("urn:one:failed:tag")); // starts with urn:
	assertFalse(UrnUtils.isTag("a\nb\tc\0d"));         // new line
	assertTrue (UrnUtils.isTag("some blank spaces"));  // blanks are allowed
	assertFalse(UrnUtils.isTag(""));                   // empty string

	// Null is accepted but returns false
	assertFalse(UrnUtils.isTag(null));
    }
}
