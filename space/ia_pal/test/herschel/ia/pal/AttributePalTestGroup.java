package herschel.ia.pal;

import herschel.ia.dataset.Product;
import herschel.ia.pal.query.AttribQuery;
import herschel.share.fltdyn.time.FineTime;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.logging.Logger;

public class AttributePalTestGroup extends AbstractPalTestGroup {

    private Logger _LOGGER = Logger.getLogger("AttributePalTestGroup");

    public AttributePalTestGroup(String name, AbstractPalTestCase testcase) {
	super(name, testcase);
    }

    /**
     * Test whether a product can be stored in a pool and a simple query assigned to that product
     * would yield that product.
     */
    public void testAttQuerySimple() {

	try {

	    ProductPool pool1 = getPool();

	    ProductStorage storage = new ProductStorage();
	    storage.register(pool1);

	    {
		Product p1 = new Product("QueryAttP1");
		p1.setInstrument("PACS");
		p1.setModelName("mod1");
		p1.setCreator("me");
		p1.setType("TestType");
		storage.save(p1);
	    }

	    // Test for Attribute query that returns single product
	    AttribQuery aq = new AttribQuery(Product.class, "p", "p.creator == 'me'");

	    Set<ProductRef> results = null;
	    results = storage.select(aq);
	    for (ProductRef ref : results) {
		Product product = ref.getProduct();
		assertTrue(product.getCreator().equals("me"));
	    }

	} catch (IOException e) {
	    fail("Unexpected IOException found. Details:" + e);
	} catch (GeneralSecurityException e) {
	    fail("Unexpected GeneralSecurityException found. Details:" + e);
	}

    }

    /**
     * More advanced query test involving three products of different attribute data.
     */
    public void testAttQueryComplex() {

	try {

	    ProductPool pool1 = getPool();
	    ProductStorage storage = new ProductStorage();
	    storage.register(pool1);

	    {
		Product p1 = new Product("QueryAttP1");
		p1.setInstrument("PACS");
		p1.setModelName("mod1");
		p1.setCreator("me");
		p1.setType("TestType");
		storage.save(p1);

		Product p2 = new Product("QueryAttP2");
		p2.setInstrument("HIFI");
		p2.setModelName("mod2");
		p2.setCreator("me");
		p2.setType("TestType");
		storage.save(p2);

		Product p3 = new Product("QueryAttP3");
		p3.setInstrument("SPIRE");
		p3.setModelName("mod3");
		p3.setCreator("me");
		p3.setType("TestType");
		storage.save(p3);
	    }

	    {
		// Test for Attribute query that should return several products
		AttribQuery aq = new AttribQuery(Product.class, "p", "p.creator == 'me'");

		Set<ProductRef> results = null;
		results = storage.select(aq);
		for (ProductRef ref : results) {
		    Product product;
		    product = ref.getProduct();
		    assertTrue("me".equals(product.getCreator()));
		}
	    }

	    {
		// Test for attributes returning multiple products
		AttribQuery aq1 = new AttribQuery(Product.class, "p", "p.modelName == 'mod1'");

		Set<ProductRef> results = null;
		results = storage.select(aq1);
		assertTrue(results.size() == 1);

		for (ProductRef ref : results) {
		    Product product;
		    product = ref.getProduct();
		    assertTrue("QueryAttP1".equals(product.getDescription()));
		}
	    }

	} catch (IOException e) {
	    fail("Unexpected IOException found. Details:" + e);
	} catch (GeneralSecurityException e) {
	    fail("Unexpected GeneralSecurityException found. Details:" + e);
	}

    }

    /**
         * Attribute query involving a single date.
         */
    public void testAttDateSingleQuery() {

	Calendar cal = new GregorianCalendar(2000, Calendar.JANUARY, 1, 0, 0, 0);
	FineTime startTime = new FineTime(cal.getTime());
	long startTimeMicrosec = startTime.microsecondsSince1958();

	try {

	    ProductPool pool1 = getPool();

	    ProductStorage storage = new ProductStorage();
	    storage.register(pool1);

	    {
		Product p1 = new Product();
		p1.setCreator("me");
		p1.setStartDate(startTime);
		storage.save(p1);
	    }

	    // Test for Attribute query that returns single product
	    String where = "p.startDate.microsecondsSince1958() == " + startTimeMicrosec;
	    AttribQuery aq = new AttribQuery(Product.class, "p", where);

	    Set<ProductRef> results = null;
	    results = storage.select(aq);
	    for (ProductRef ref : results) {
		Product product = ref.getProduct();
		assertTrue(product.getCreator().equals("me"));
	    }

	} catch (IOException e) {
	    fail("Unexpected IOException found. Details:" + e);
	} catch (GeneralSecurityException e) {
	    fail("Unexpected GeneralSecurityException found. Details:" + e);
	}

    }

    /**
         * Attribute query involving multiple dates.
         */
    public void testAttDateComplexQuery() {

	Calendar cal = new GregorianCalendar(2000, Calendar.JANUARY, 1, 0, 0, 0);
	FineTime startTimeOne = new FineTime(cal.getTime());
	long startTimeOneMicrosec = startTimeOne.microsecondsSince1958();
	long startTimeTwoMicrosec = startTimeOneMicrosec + 1000000;
	long startTimeThreeMicrosec = startTimeOneMicrosec + 2000000;
	FineTime startTimeTwo = new FineTime(startTimeTwoMicrosec);
	FineTime startTimeThree = new FineTime(startTimeThreeMicrosec);

	try {

	    ProductPool pool1 = getPool();
	    ProductStorage storage = new ProductStorage();
	    storage.register(pool1);

	    {
		Product p1 = new Product();
		p1.setModelName("mod1");
		p1.setStartDate(startTimeOne);
		storage.save(p1);

		Product p2 = new Product();
		p2.setModelName("mod2");
		p1.setStartDate(startTimeTwo);
		storage.save(p2);

		Product p3 = new Product();
		p3.setModelName("mod3");
		p1.setStartDate(startTimeThree);
		storage.save(p3);
	    }

	    {
		// Test for Attribute query that should return p2
		String where = "p.startDate.microsecondsSince1958() > " + startTimeOneMicrosec
		        + " and p.startDate.microsecondsSince1958() < " + startTimeThreeMicrosec;
		_LOGGER .info("query=[" + where + "]");
		AttribQuery aq = new AttribQuery(Product.class, "p", where);

		Set<ProductRef> results = null;
		results = storage.select(aq);
		for (ProductRef ref : results) {
		    Product product;
		    product = ref.getProduct();
		    assertTrue("mod2".equals(product.getModelName()));
		}
	    }

	} catch (IOException e) {
	    fail("Unexpected IOException found. Details:" + e);
	} catch (GeneralSecurityException e) {
	    fail("Unexpected GeneralSecurityException found. Details:" + e);
	}

    }

}
