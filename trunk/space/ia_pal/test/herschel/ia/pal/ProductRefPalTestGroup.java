// -*-java-*-
//
// File:      WhiteBoxPalTest.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Aug 13, 2007
// Usage:     -
// Info:      -

package herschel.ia.pal;

import herschel.ia.dataset.ArrayDataset;
import herschel.ia.dataset.Product;
import herschel.ia.numeric.Int1d;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class contains some specific test cases for descriptors.
 */
public class ProductRefPalTestGroup extends AbstractPalTestGroup {

    private static Logger log = Logger.getLogger(ProductRefPalTestGroup.class.getName());
    
    /** Constructor. */
    public ProductRefPalTestGroup(String name, AbstractPalTestCase testcase) {
        super(name, testcase);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ProductRefPalTestGroup.class);
        return suite;
    }

    public void testGetProduct() throws IOException, GeneralSecurityException {

	// Auxiliary variables
	String descriptionA = "descriptionA";
	String descriptionB = "descriptionB";
	String urnp, urnc;
	ProductRef refp, refc;
	Product p;
	Context c;

	// Initialize the storage
	ProductStorage storage = new ProductStorage();
	storage.register(getPool());

	// Save a Product and a Context
	{
	    p = new Product(descriptionA);
	    c = new ListContext();
	    c.setDescription(descriptionA);
	    refp = storage.save(p);
	    refc = storage.save(c);
	    urnp = refp.getUrn();
	    urnc = refc.getUrn();
	}

	// Check the context
	{
	    refc = storage.load(urnc);
	    assertFalse(refc.isLoaded());
	    c = (Context)refc.getProduct();
	    c.setDescription(descriptionB);
	    assertTrue(refc.isLoaded());
	    assertTrue(c.equals(refc.getProduct()));
	    assertSame(c, refc.getProduct());
	    refc.unload();
	    assertFalse(refc.isLoaded());
	    assertFalse(c.equals(refc.getProduct()));
	    assertNotSame(c, refc.getProduct());
	}

	// Check the product
	{
	    refp = storage.load(urnp);
	    assertFalse(refp.isLoaded());
	    p = refp.getProduct();
	    p.setDescription(descriptionB);
	    assertFalse(refp.isLoaded());
	    assertFalse(p.equals(refp.getProduct()));
	    assertNotSame(p, refp.getProduct());
	}
    }

    public void testGetTotalSize() throws IOException, GeneralSecurityException{
	
	double[] sizes = {0.1, 1, 2, 10}; // MB
	double sum = 0;
	
	ListContext listA = new ListContext();
	ListContext listB = new ListContext();
	MapContext  mapA  = new MapContext();
	MapContext  mapB  = new MapContext();
	
	ProductStorage storage = new ProductStorage();
	storage.register(getPool());
	
	for (double size : sizes) {
	    
	    log.info("Testing size: "+size+" MB");
	    
	    Product p = TestProductGenerator.getProductOfSize(size);
	    
	    ProductRef ref = new ProductRef(p);
	    checkTotalSize(ref, size);
	    
	    ProductRef refSaved = storage.save(p);
	    checkTotalSize(refSaved, size);
	    
	    listA.getRefs().add(ref);
	    listB.getRefs().add(refSaved);
	    mapA.getRefs().put("ref"+size, ref);
	    mapB.getRefs().put("ref"+size, refSaved);
	    
	    sum += size;
	    
	}
	
	// Check sizes of the Contexts by using constructor ProductRef(Product)
	ProductRef refListA = new ProductRef(listA);
	ProductRef refListB = new ProductRef(listB);
	ProductRef refMapA  = new ProductRef(mapA);
	ProductRef refMapB  = new ProductRef(mapB);
	
	//log.info("Context listA has # prods: "+listA.getRefs().size());
	
	checkTotalSize(refListA, sum);
	checkTotalSize(refListB, sum);
	checkTotalSize(refMapA, sum);
	checkTotalSize(refMapB, sum);

	// Now check sizes of the Contexts, obtaining ProductRef from store.save
	refListA = storage.save(listA);
	refListB = storage.save(listB);
	refMapA  = storage.save(mapA);
	refMapB  = storage.save(mapB);
	
	checkTotalSize(refListA, sum);
	checkTotalSize(refListB, sum);
	checkTotalSize(refMapA, sum);
	checkTotalSize(refMapB, sum);
    }

    /**
     * @param ref
     * @param expectedSize: in MB
     */
    private void checkTotalSize(ProductRef ref, double expectedSize) {
	
	double threshold = 0.1; // 10% accuracy required
	
	double totalSize = ref.getTotalSize();
	double totalSizeMB = totalSize / (1024*1024);
	double relDiff = (Math.abs(totalSizeMB-expectedSize)/expectedSize); 
	
	log.finer("Relative size difference is: " + relDiff);
	
	if (relDiff > threshold) {
	    fail("Reported total size is " + totalSizeMB +
	         " MB, whereas " + expectedSize + " MB is expected (diff. of " +
	         (100*relDiff) + "% -- " + (threshold*100) + "% allowed).");
	}
	
    }

    public void testDescriptorsSave() throws IOException, GeneralSecurityException {

	ProductPool pool = getPool();
	ProductStorage storage = new ProductStorage();
	storage.register(pool);

	Product p1 = new Product("p1");
	Product p2 = new Product("p2");
	p2.set("p2data", new ArrayDataset(Int1d.range(1000)));

	ProductRef ref1 = storage.save(p1);
	ProductRef ref2 = storage.save(p2);
	ProductRef ref3 = storage.save(p2);
	checkRefs(ref1, ref2, ref3);
    }

    public void testDescriptorsLoad() throws IOException, GeneralSecurityException {

	ProductPool pool = getPool();
	ProductStorage storage = new ProductStorage();
	storage.register(pool);
	String urn1, urn2, urn3;

	{
	    Product p1 = new Product("p1");
	    Product p2 = new Product("p2");
	    p2.set("p2data", new ArrayDataset(Int1d.range(1000)));

	    urn1 = storage.save(p1).getUrn();
	    urn2 = storage.save(p2).getUrn();
	    urn3 = storage.save(p2).getUrn();
	}

	ProductRef ref1 = storage.load(urn1);
	ProductRef ref2 = storage.load(urn2);
	ProductRef ref3 = storage.load(urn3);
	checkRefs(ref1, ref2, ref3);
    }

    private void checkRefs(ProductRef ref1, ProductRef ref2, ProductRef ref3) {
    	assertFalse (ref1.equals(ref2));
    	assertFalse (ref3.equals(ref2));
    	assertTrue  (ref1.hasTrack());
    	assertTrue  (ref2.hasTrack());
    	assertTrue  (ref3.hasTrack());
    	assertFalse (ref1.getTrackId().equals(ref2.getTrackId()));
    	assertTrue  (ref3.getTrackId().equals(ref2.getTrackId()));
    	assertEquals(ref1.getVersion(), 0);
    	assertEquals(ref2.getVersion(), 0);
    	assertEquals(ref3.getVersion(), 1);
    	assertTrue  (ref1.getSize() > 0);
    	assertTrue  (ref2.getSize() > 0);
    	assertTrue  (ref1.getSize() < ref2.getSize());
    	assertEquals(ref1.getSize(), ref1.getTotalSize());
    	assertEquals(ref2.getSize(), ref2.getTotalSize());
    	assertFalse (ref1.getHash().equals(ref2.getHash()));
    //	assertTrue  (ref3.getHash().equals(ref2.getHash()));

    	Map<String, Object> desc1 = ref1.getDescriptors();
    	Map<String, Object> desc2 = ref2.getDescriptors();
    	assertEquals(desc1.get(ProductRef.TRACK_ID)  , ref1.getTrackId());
    	assertEquals(desc2.get(ProductRef.TRACK_ID)  , ref2.getTrackId());
    	assertEquals(desc1.get(ProductRef.VERSION)   , ref1.getVersion());
    	assertEquals(desc2.get(ProductRef.VERSION)   , ref2.getVersion());
    	assertEquals(desc1.get(ProductRef.SIZE)      , ref1.getSize());
    	assertEquals(desc2.get(ProductRef.SIZE)      , ref2.getSize());
    	assertEquals(desc1.get(ProductRef.TOTAL_SIZE), ref1.getTotalSize());
    	assertEquals(desc2.get(ProductRef.TOTAL_SIZE), ref2.getTotalSize());
    	assertEquals(desc1.get(ProductRef.HASH)      , ref1.getHash());
    	assertEquals(desc2.get(ProductRef.HASH)      , ref2.getHash());
    }
}
