package herschel.ia.pal;

import herschel.ia.dataset.Product;
import herschel.ia.pal.query.Query;
import herschel.ia.pal.util.PoolSchemaEvolver;
import herschel.share.util.Configuration;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A test of the PoolSchemaEvolver class.
 * Fill a pool with a certain set of Products that reference each other, runs the evolver,
 * and checks that the structure is still the same.
 * 
 * Does not check copying from one type of pool to another (evolving a LocalStore and storing
 * it into a DbPool).
 * 
 * @author pbalm
 */
public abstract class AbstractPoolSchemaEvolverTest extends AbstractPalTestCase {

    public final int SRCPOOL_INDEX = 0;
    public final int TGTPOOL_INDEX = 1;
    
    private static Logger log = Logger.getLogger("AbstractPoolSchemaEvolverTest");
    
    private AbstractPalTestCase poolFactory;

    /** Constructor */
    public AbstractPoolSchemaEvolverTest() {
	super(AbstractPoolSchemaEvolverTest.class.getName());
	String simplePoolRoot = Configuration.getProperty("var.hcss.workdir") +
	                        File.separator + "pal" + File.separator;
	Configuration.setProperty("hcss.ia.pal.pool.simple.dir",simplePoolRoot);
    }

    public void setPoolFactory(AbstractPalTestCase poolFactory) {
	this.poolFactory = poolFactory;
    }
    
    @Override
    protected void cleanPool(int index) {
	poolFactory.cleanPool(index);
    }

    @Override
    protected ProductPool createPool(int index) {
	return poolFactory.createPool(index);
    }

    @Override
    public String getPoolType() {
	return poolFactory.getPoolType();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanPools();
    }
    
    /**
     * Copy the evolved data to a new pool.
     */
    public void testA() throws Exception {
	
	ProductStorage store = prepareStorage();
	assertTrue(checkStore(store));

	PoolSchemaEvolver tool = new PoolSchemaEvolver(getPool(SRCPOOL_INDEX).getId(), getPoolType(),
		getPool(TGTPOOL_INDEX).getId(), getPoolType());
	
	tool.evolve();
	
	assertTrue(checkStore(store));
	
    }
    
    /**
     * Replace the existing data with evolved data.
     */
    public void testB() throws Exception {
	
	ProductStorage store = prepareStorage();
	assertTrue(checkStore(store));
	
	PoolSchemaEvolver tool = new PoolSchemaEvolver(getPool(SRCPOOL_INDEX).getId(), getPoolType());
	
	tool.evolve();
	
	assertTrue(checkStore(store));
	
    }


    /**
     * Clean pool 0 and fill it with the following structure of Contexts: 
     * Context A references to other Contexts B and C which each reference Product D.
     */
    public ProductStorage prepareStorage() throws IOException, GeneralSecurityException {
	
	cleanPool(SRCPOOL_INDEX);
	ProductPool pool = getPool(SRCPOOL_INDEX);
	ProductStorage store = new ProductStorage();
	store.register(pool);
	
	ListContext A = new ListContext();
	ListContext B = new ListContext();
	ListContext C = new ListContext();
	Product D = new Product();
	
	A.setDescription("A");
	B.setDescription("B");
	C.setDescription("C");
	D.setDescription("D");
	
	ProductRef refD = store.save(D);
	B.getRefs().add(refD);
	C.getRefs().add(refD);
	
	A.getRefs().add(store.save(B));
	A.getRefs().add(store.save(C));
	
	store.save(A);
	
	return store;
    }

    /**
     * Check that the store passed as the argument contains the structure set up in the prepareStore method.
     */
    public boolean checkStore(ProductStorage store) throws IOException, GeneralSecurityException {
	
	Query q = new Query(Product.class, "1");
	Set<ProductRef> prodSet = store.select(q);
	Iterator<ProductRef> iter = prodSet.iterator();
	
	if(prodSet.size()!=4) {
	    log.info("Pool has "+prodSet.size()+" products, expecting 4.");
	    
	    for(ProductRef ref : prodSet) {
		log.info("- "+ref.getUrn()+": "+ref.getProduct().getDescription());
	    }
	    
	    return false;
	}
	
	boolean testResult = true; // true = ok
	
	while(iter.hasNext() && testResult==true) {
	    Product p = iter.next().getProduct();
	    log.info("Checking product "+p.getDescription());

	    if("A".equals(p.getDescription())) {
		testResult = checkChildren(p, new String[]{"B","C"});
	    }
	    else if("B".equals(p.getDescription())) {
		testResult = checkChildren(p, new String[]{"D"});
	    }
	    else if("C".equals(p.getDescription())) {
		testResult = checkChildren(p, new String[]{"D"});
	    }
	    else if("D".equals(p.getDescription())) {
		testResult = checkChildren(p, null);
	    }
	    else {
		fail("unrecognized product");
	    }
	}
	
	if(testResult) {
	    log.info("Store checked: contents as expected.");
	}
	else {
	    log.warning("Store checked: contents not as expected!");
	}
	
	return testResult;
    }
    
    /**
     * Internal method.
     * Check that the children of the Product p have descriptions as passed in the String array.
     * @return true if as expected
     * @throws GeneralSecurityException 
     * @throws IOException 
     */
    private boolean checkChildren(Product p, String[] str) throws IOException, GeneralSecurityException {
	
	if(str==null) {
	    // no children or not a Context
	    if(! (p instanceof Context)) {
		return true; // ok
	    }
	    else {
		Context c = (Context) p;
		boolean test = (c.getAllRefs()==null || c.getAllRefs().size()==0);
		if(!test) {
		    log.info("Failing check because this Context has children (expected none).");
		}
		return test;
	    }
	}
	
	// Expect to have children; must be a Context
	if(! (p instanceof Context)) {
	    log.info("Failing check because this Product is not a Context.");
	    return false;
	}
	
	Set<ProductRef> children = ((Context) p).getAllRefs();
	
	if(children.size()!=str.length) {
	    log.info("Failing check because the number of children is incorrect (expected "+str.length+", has "+children.size()+").");
	    return false;
	}
	
	for(String descr : str) {
	    if(!hasProductWithDescription(children, descr)) {
		log.info("Did not find child with description: "+descr);
		return false;
	    }
	}
	
	return true;
    }
    
    /**
     * Internal method.
     * Check that the set of ProductRefs contains a Product with the descriptions as passed in the String argument.
     * @return true if as expected
     */
    private boolean hasProductWithDescription(Set<ProductRef> pRefSet, String descr) throws IOException, GeneralSecurityException {
	
	for(ProductRef ref : pRefSet) {
	    String pDescr = ref.getProduct().getDescription();
	    if(descr.equals(pDescr)) {
		return true;
	    }
	}
	
	return false;
	
    }
    
    

}