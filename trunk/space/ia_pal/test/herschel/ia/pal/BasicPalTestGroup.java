package herschel.ia.pal;

import herschel.ia.dataset.LongParameter;
import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Parameter;
import herschel.ia.dataset.Product;
import herschel.share.util.StackTrace;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.Assert;

public class BasicPalTestGroup extends AbstractPalTestGroup {

    public BasicPalTestGroup(String name, AbstractPalTestCase testcase) {
	super(name, testcase);
    }

    /**
     * Test different constuctors of ProductStorage.
     */
    public void testStorageCreation() {

	// Initialization
	String poolName1 = "pool1";
	String poolName2 = "pool2";
	String poolName3 = "pool3";
	ProductPool pool1 = getPool(1);
	ProductPool pool2 = getPool(2);
	ProductPool pool3 = getPool(3);
	Map<String, ProductPool> pools = PoolManager.getPoolMap();
	pools.clear();
	pools.put(poolName1, pool1);
	pools.put(poolName2, pool2);
	pools.put(poolName3, pool3);
	ProductStorage storage;

	// Default constructor and register method
	storage = new ProductStorage();
	assertTrue(storage.getPools().isEmpty());
	storage.register(pool1);
	assertEquals(1, storage.getPools().size());
	assertEquals(pool1, storage.getWritablePool());
	storage.register(pool2, pool3);
	assertEquals(3, storage.getPools().size());
	assertEquals(pool1, storage.getWritablePool());

	// Constructors with pools
	storage = new ProductStorage(pool1);
	assertEquals(1, storage.getPools().size());
	assertEquals(pool1, storage.getWritablePool());
	storage = new ProductStorage(pool1, pool2, pool3);
	assertEquals(3, storage.getPools().size());
	assertEquals(pool1, storage.getWritablePool());

	// Constructors with pool names
	storage = new ProductStorage(poolName1);
	assertEquals(1, storage.getPools().size());
	assertEquals(pool1, storage.getWritablePool());
	storage = new ProductStorage(poolName1, poolName2, poolName3);
	assertEquals(3, storage.getPools().size());
	assertEquals(pool1, storage.getWritablePool());
    }

    /**
         * Test whether a pool can be correctly created, and a product can be written to and read from
         * that pool.
         */
    public void testPoolCreation() {

	try {

	    ProductPool pool = getPool();

	    if (pool == null) {
		fail("Cannot create a simple pool instance");
	    }

	    /* Try adding a simple product to pool */
	    Product myproduct = new Product();
	    String urn = null;
	    urn = pool.save(myproduct);

	    /* Check that the product previously saved exists in the pool */
	    assertTrue(pool.exists(urn));

	} catch (IOException e) {
	    StackTrace.trace(e);
	    fail("Unexpected IOException found: " + e);
	} catch (GeneralSecurityException e) {
	    StackTrace.trace(e);
	    fail("Unexpected GeneralSecurityException found: " + e);
	}
    }

    /**
     * Tests the pool.load() method
     */
    public void testLoad() {

	final String creator = "testcreator";

	try {

	    ProductPool pool = getPool();

	    if (pool == null) {
		fail("Cannot create a pool instance");
	    }

	    String urn = null;
	    {
		/* Try adding a simple product to pool */
		Product myproduct = new Product();
		myproduct.setCreator(creator);
		urn = pool.save(myproduct);
	    }

	    /* Check that the product previously saved exists in the pool */
	    Assert.assertTrue(pool.exists(urn));

	    /* Load metadata for product from pool */
	    Product p = pool.loadProduct(urn);
	    assertTrue(p != null);
	    assertTrue(p.getCreator().equals(creator));

	    /* Try with a non-existing urn */
	    pool.remove(urn);
	    try {
		pool.loadProduct(urn);
		fail("An exception should be thrown");
	    } catch (NoSuchElementException e) { /* correct */ }

	} catch (IOException e) {
	    StackTrace.trace(e);
	    fail("Unexpected IOException found: " + e);
	} catch (GeneralSecurityException e) {
	    StackTrace.trace(e);
	    fail("Unexpected GeneralSecurityException: " + e);
	}
    }

    /**
     * Tests the pool.meta() method
     */
    public void testMetaAccess() {

	final String metakey = "mymeta";
	final LongParameter metavalue = new LongParameter(10);

	try {

	    ProductPool pool = getPool();

	    if (pool == null) {
		fail("Cannot create a pool instance");
	    }

	    String urn = null;
	    {
		/* Try adding a simple product to pool */
		Product myproduct = new Product();
		MetaData md = myproduct.getMeta();
		md.set(metakey, metavalue);
		urn = pool.save(myproduct);
	    }

	    /* Check that the product previously saved exists in the pool */
	    assertTrue(pool.exists(urn));

	    /* Load metadata for product from pool */
	    MetaData m = pool.meta(urn);
	    assertTrue(m != null);
	    assertTrue(m.containsKey(metakey));
	    Parameter d = m.get(metakey);
	    assertTrue(d instanceof LongParameter);
	    assertTrue(((LongParameter) d).equals(metavalue));

	    /* Try with a non-existing urn */
	    pool.remove(urn);
	    try {
		pool.meta(urn);
		fail("An exception should be thrown");
	    } catch (NoSuchElementException e) { /* correct */ }

	} catch (IOException e) {
	    StackTrace.trace(e);
	    fail("Unexpected IOException found: " + e);
	} catch (GeneralSecurityException e) {
	    StackTrace.trace(e);
	    fail("Unexpected GeneralSecurityException found: " + e);
	}
    }

}
