/* $Id: PerformancePalTestGroup.java,v 1.14 2008/04/29 08:46:36 pbalm Exp $
 * Copyright (c) 2006 NAOC
 */
package herschel.ia.pal;

import herschel.ia.dataset.Product;
import herschel.ia.pal.performance.Timer;
import herschel.ia.pal.query.AttribQuery;
import herschel.ia.pal.query.FullQuery;
import herschel.ia.pal.query.MetaQuery;
import herschel.ia.pal.query.Query;
import herschel.share.fltdyn.time.FineTime;
import herschel.share.util.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Before;

/**
 * 
 * PAL performance test group defines the common and basic testing cases for PAL
 * pool implementations. 
 * 
 * A integer configuration item "hcss.ia.pal.pool.performancetest.size" was used to control
 * the data volume in the performance test.
 * 
 * @author libo@bao.ac.cn
 * 
 */
public class PerformancePalTestGroup extends AbstractPalTestGroup {

    private static final int SIZE = Configuration.getInteger("hcss.ia.pal.pool.performancetest.size");

    private ProductStorage _storage;
    protected ProductRef[] pRefsArray; // cache of all ProductRefs in the current store
    
    private static final Logger _LOGGER = Logger
    .getLogger("PerformancePalTestGroup");

    private static final String EQUALS = "=="; // equals operator for PAL queries

    /**
     * To time the duration of operations
     */
    protected Timer timer = new Timer();


    /**
     * 
     * @param name
     * @param testcase
     */
    public PerformancePalTestGroup(String name, AbstractPalTestCase testcase) {
	super(name, testcase);
	_LOGGER.info("hcss.ia.pal.pool.performancetest.size: " + SIZE);

    }

    /**
     * Initialize a pool.
     * This method is run before each test.
     *
     */
    @Before public void initStorage() {
	
	ProductPool pool = getPool();
	ProductStorage storage = new ProductStorage();
	storage.register(pool);
	
	_storage=storage;

	// make the timer start immediately (no garbage collection + 1 sec sleep)
	timer.startImmediately = true;
    }

    /**
     * Set the pool to use during the different tests, when you run them manually.
     * Note that if you're running within the JUnit framework, this may be undone
     * by the initPool method, which may run automatically before each test.
     */
    public void setStorage(ProductStorage store) {
	this._storage = store;
	
	// Clear ProductRefs cache:
	pRefsArray = null;
    }
    
    public ProductStorage getStorage() {
	return this._storage;
    }

    /**
     * 
     * Test method for PAL saves()
     * 
     * @throws GeneralSecurityException
     */
    public void testSave() throws GeneralSecurityException {
	_LOGGER.info("testSave");
	try {
	    for (int i = 0; i < SIZE; i++) {
		Product product = TestProductGenerator.get1MProduct();
		ProductRef ref = _storage.save(product);
		assertNotNull("ProductRef can not be null",ref);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(" "+e.getMessage());
	}

    }

    /**
     * Load benchmark: Measure time it takes to load 10 products from the given pool.
     * 
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void testLoad() throws IOException, GeneralSecurityException {
	_LOGGER.info("testLoad");

	checkLoad(10);
    }
    
    public ProductRef[]  getAllProductRefs() throws IOException, GeneralSecurityException {
	
	if(pRefsArray==null) {
	    _LOGGER.info("Rebuilding list of available URNs in pool.");
	    Query q = new Query(Product.class,"1");
	    Set<ProductRef> pRefs = _storage.select(q);
	    pRefsArray = new ProductRef[pRefs.size()];
	    pRefs.toArray(pRefsArray);
	}
	
	return pRefsArray;
    }
        
    /**
     * 
     * Test method for PAL save() and load()
     * 
     * @throws GeneralSecurityException
     */
    public void testSaveAndLoad() throws GeneralSecurityException {
	try {
	    Product product = TestProductGenerator.getSimpleProduct();
	    ProductRef pRef = _storage.save(product);
	    //Product productLoaded = 
		pRef.getProduct();
//	    assertTrue(product.equals(productLoaded));
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(" ");
	}
    }

    /**
     * 
     * Test method for PAL FullQuery
     * 
     * @throws GeneralSecurityException
     */
    public void testFullQuery() throws GeneralSecurityException {
	FullQuery query = new FullQuery(Product.class, "p", "p.creator=='Scott'");
	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * 
     * Test method for updating a Product: load a Random product and save it on top as a new version
     * 
     * @throws GeneralSecurityException
     */
    public void testUpdate() throws GeneralSecurityException, IOException {
	_LOGGER.info("Update benchmark starting.");

	int nTotal = getAllProductRefs().length; 

	float random = new Random().nextFloat();
	int randomIndex = Math.round(random*(nTotal-1));

	// Get the ProductRef of the Product to update:
	ProductRef pRef = getAllProductRefs()[randomIndex];

	//_LOGGER.info("Loading "+refsToLoad.size()+" products...");

	{
	    timer.start();
	    Product p = pRef.getProduct();
	    pRef.unload();
	    _storage.save(p);
	    timer.stop();
	}	
	
    }

    /**
     * 
     * Test method for updating a Product *twice*: load a Random product and save it on top as a new version.
     * The load the new version again and save that.
     * 
     * @throws GeneralSecurityException
     */
    public void testUpdateTwice() throws GeneralSecurityException, IOException {
	_LOGGER.info("UpdateTwice benchmark starting.");

	int nTotal = getAllProductRefs().length; 

	float random = new Random().nextFloat();
	int randomIndex = Math.round(random*(nTotal-1));

	// Get the ProductRef of the Product to update:
	ProductRef pRef = getAllProductRefs()[randomIndex];

	//_LOGGER.info("Loading "+refsToLoad.size()+" products...");

	{
	    timer.start();
	    ProductRef pRefNew = _storage.save(pRef.getProduct());
	    pRef.unload();
	    _storage.save(pRefNew.getProduct());
	    pRefNew.unload();
	    timer.stop();
	}	
	
    }

    /**
     * s Test method for PAL AttribQuery
     * 
     * @throws GeneralSecurityException
     */
    public void testAttribQuery() throws GeneralSecurityException {
	AttribQuery query = new AttribQuery(Product.class, "p","p.creator=='Scott'");
	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(" ");
	}
    }

    /**
     * Test method for PAL MetaQuery
     * 
     * @throws GeneralSecurityException
     */
    public void testMetaQuery() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p","p.creator=='Scott'");
	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(" ");
	}
    }

    /**
     * Test method for PAL MetaQuery on an attribute with many different values.
     * 
     * @throws GeneralSecurityException
     */
    public void testMetaQueryWhen() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.creationDate < "+new FineTime(new Date()).microsecondsSince1958()+"L");


	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(" ");
	}
    }

    /**
     * Test method for PAL MetaQuery on many attributes
     * 
     * @throws GeneralSecurityException
     */
    public void testMetaQueryMany() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.creationDate < "+new FineTime(new Date()).microsecondsSince1958()+"L and " +
		"p.creator == 'Scott' and p.instrument == 'Spire' and " +
	"p.modelName == 'Spire' and p.type == 'Spire' and p.meta['version'].value == '1.0'");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT1_1() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.instrument "+EQUALS+" \"Spire\"");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT1_2() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.instrument "+EQUALS+" \"Spire\" and "+
		"p.startDate < "+new FineTime(new Date()).microsecondsSince1958()+"L and " +
		"p.endDate < "+new FineTime(new Date()).microsecondsSince1958()+"L");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT2_1() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.creator "+EQUALS+" \"Scott\"");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT2_2() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.type "+EQUALS+" \"Spire\" and "+
		"p.creationDate < "+new FineTime(new Date()).microsecondsSince1958()+"L and " +
	 "p.meta['version'].value "+EQUALS+" \"1.0\"");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT2_3() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.modelName "+EQUALS+" \"Spire\" and "+
		"p.type "+EQUALS+" \"Spire\" and "+
		"p.creationDate < "+new FineTime(new Date()).microsecondsSince1958()+"L and " +
	 "p.meta['version'].value "+EQUALS+" '1.0' and p.creator "+EQUALS+" \"Scott\"");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT1_1Spec() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.instrument "+EQUALS+" \"XXX\"");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT1_2Spec() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.instrument "+EQUALS+" \"XXX\" and "+
		"p.startDate < "+new FineTime(new Date()).microsecondsSince1958()+"L and " +
		"p.endDate < "+new FineTime(new Date()).microsecondsSince1958()+"L");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT2_1Spec() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.creator "+EQUALS+" \"XXX\"");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT2_2Spec() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.type "+EQUALS+" \"XXX\" and "+
		"p.creationDate < "+new FineTime(new Date()).microsecondsSince1958()+"L and " +
	 "p.meta['version'].value "+EQUALS+" \"1.0\"");

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT2_3Spec() throws GeneralSecurityException {
	MetaQuery query = new MetaQuery(Product.class, "p",
		"p.modelName "+EQUALS+" \"SPIRE\" and "+
		"p.type "+EQUALS+" \"XXX\" and "+
		"p.creationDate < "+new FineTime(new Date()).microsecondsSince1958()+"L and " +
	 "p.meta['version'].value "+EQUALS+" \"1.0\" and p.creator "+EQUALS+" \"Scott\"");
	

	try {
	    timer.start();
	    Set<ProductRef> set = _storage.select(query);
	    timer.stop();

	    _LOGGER.info("Selected "+set.size()+" products");
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT3_1() throws IOException, GeneralSecurityException {
	_LOGGER.info("Test HAIO T3.1");
	
	checkLoad(1);
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT3_2() throws IOException, GeneralSecurityException {
	_LOGGER.info("Test HAIO T3.1");
	
	checkLoad(10);
    }
    
    /**
     * These are testcases from the PAL/HAIO Performance Test
     * For test plan see:
     * http://www.rssd.esa.int/llink/livelink/open/2814705
     * 
     * @throws GeneralSecurityException
     */
    public void testHaioT3_3() throws IOException, GeneralSecurityException {
	_LOGGER.info("Test HAIO T3.1");
	
	checkLoad(100);
    }
    
    public void checkLoad(int n) throws IOException, GeneralSecurityException {
	_LOGGER.info("Retrieving "+n+" products and timing the operation.");

	int nTotal = getAllProductRefs().length; 

	if(nTotal<n) {
	    throw new RuntimeException("The number of Products to load ("+n+") is higher than the number of Products in the pool ("+nTotal+").");
	}

	Set<Integer> indicesToLoad = new HashSet<Integer>();
	while(indicesToLoad.size()<n) {
	    float random = new Random().nextFloat();
	    int randomIndex = Math.round(random*(nTotal-1));
	    ProductRef pRef = getAllProductRefs()[randomIndex];
	    if(pRef!=null) {
		if(getStorage().exists(pRef.getUrn()) ) {
		    // ProductRefs that were used before are set to null, so if that happens, don't add it.
		    // And case we used quick'n'dirty way to generate list of product refs, it may not exist...
		    indicesToLoad.add(randomIndex);
		}
		else {
		    _LOGGER.info("Product with URN "+pRef.getUrn()+" does not exist.");
		}

	    }
	}
	

	//_LOGGER.info("Loading "+refsToLoad.size()+" products...");
	{
	    ProductRef[] allRefs = getAllProductRefs();
	    
	    timer.start();
	    for(Integer index : indicesToLoad) {
		//_LOGGER.info("Loading ["+index+"]: urn "+allRefs[index].getUrn());
		allRefs[index].getProduct(); // actual load
		
		try {
		    allRefs[index].unload();
		}
		catch(NoSuchMethodError e) {
		    _LOGGER.warning("Running with version of PAL that doesn't support ProductRef.unload().");
		}

		//System.gc();
	    }
	    timer.stop();
	    
	}
	
    }
    
    public void testLoadListContext() throws IOException, GeneralSecurityException {
	testLoadContext(ListContext.class);
    }
    public void testLoadMapContext() throws IOException, GeneralSecurityException {
	testLoadContext(MapContext.class);
    }
    
    private void testLoadContext(Class<? extends Context> cls) throws IOException, GeneralSecurityException {
	
	    AttribQuery q = new AttribQuery(cls,"p", "1");
	    List<ProductRef> pRefs = new ArrayList<ProductRef>();
	    pRefs.addAll(_storage.select(q));
	    
	    _LOGGER.info("Selected "+pRefs.size()+" products of class "+cls.getSimpleName());
	    
	    int n = 10;
	    int nTotal = pRefs.size();
	    
	    // First create a set with the refs to load
	    Set<Integer> indicesToLoad = new HashSet<Integer>();
	    while(indicesToLoad.size()<n) {
		float random = new Random().nextFloat();
		int randomIndex = Math.round(random*(nTotal-1));
		ProductRef pRef = pRefs.get(randomIndex);
		if(pRef!=null) {
		    if(getStorage().exists(pRef.getUrn()) ) {
			// ProductRefs that were used before are set to null, so if that happens, don't add it.
			// And case we used quick'n'dirty way to generate list of product refs, it may not exist...
			indicesToLoad.add(randomIndex);
		    }
		    else {
			_LOGGER.info("Product with URN "+pRef.getUrn()+" does not exist.");
		    }

		}
	    }

	    // Perform test
	    timer.start();
	    for(Integer i : indicesToLoad) {
		pRefs.get(i).getProduct();
	    }
	    timer.stop();
    }
    
}
