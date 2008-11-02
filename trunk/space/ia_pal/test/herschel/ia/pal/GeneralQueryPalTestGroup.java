package herschel.ia.pal;

import herschel.ia.dataset.Column;
import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.StringParameter;
import herschel.ia.dataset.TableDataset;
import herschel.ia.numeric.Double1d;
import herschel.ia.pal.query.AttribQuery;
import herschel.ia.pal.query.FullQuery;
import herschel.ia.pal.query.MetaQuery;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

public class GeneralQueryPalTestGroup extends AbstractPalTestGroup {

	public GeneralQueryPalTestGroup(String name,  AbstractPalTestCase testcase) {
		super(name, testcase);
	}

	/**
	 * Testing the refining-a-query function.
	 * 
	 */
	public void testRefineQuery() {
		try {
			ProductPool pool = getPool();
			ProductStorage storage = new ProductStorage();
			storage.register(pool);
			{
				Double1d data1 = new Double1d(new double[] { 13.4, 66.9, 53.2,
						38.4 });
				TableDataset table1 = new TableDataset("This is tabledata1");
				table1.addColumn("Numbers", new Column(data1));
				Product p1 = new Product("QueryFullP1");
				MetaData md = p1.getMeta();
				md.set("creator", new StringParameter("me"));
				md.set("check", new StringParameter("red"));
				p1.set("table", table1);
				storage.save(p1);
			}
			{
				Double1d data2 = new Double1d(new double[] { 35.4, 46.9, 18.2,
						47.4 });
				TableDataset table2 = new TableDataset("This is tabledata2");
				table2.addColumn("Numbers", new herschel.ia.dataset.Column(
						data2));
				Product p2 = new Product("QueryFullP2");
				MetaData md = p2.getMeta();
				md.set("creator", new StringParameter("me"));
				md.set("check", new StringParameter("blue"));
				p2.set("table", table2);
				storage.save(p2);
			}
			String urn3;
			{
				Double1d data3 = new Double1d(new double[] { 102.4, 512.9,
						345.2, 325.8 });
				TableDataset table3 = new TableDataset("This is tabledata3");
				table3.addColumn("Numbers", new herschel.ia.dataset.Column(
						data3));
				Product p3 = new Product("QueryFullP3");
				MetaData md = p3.getMeta();
				md.set("check", new StringParameter("red"));
				md.set("creator", new StringParameter("me"));
				p3.set("table", table3);
				ProductRef ref3 = storage.save(p3);
				urn3 = ref3.getUrn();
			}
			{
				Double1d data4 = new Double1d(new double[] { 45.34, 56.9, 23.8,
						63.0 });
				TableDataset table4 = new TableDataset("This is tabledata2");
				table4.addColumn("Numbers", new herschel.ia.dataset.Column(
						data4));
				Product p4 = new Product("QueryFullP4");
				MetaData md = p4.getMeta();
				md.set("check", new StringParameter("blue"));
				md.set("creator", new StringParameter("me"));
				p4.set("table", table4);
				storage.save(p4);
			}
			// Test for Attribute query that should return four products.
			AttribQuery aq = new AttribQuery(Product.class, "p",
					"p.creator == 'me'");
			Set<ProductRef> resultsAtt = null;
			resultsAtt = storage.select(aq);
			assertTrue(resultsAtt.size() == 4);
			for (ProductRef ref : resultsAtt) {
				Product product;
				product = ref.getProduct();
				assertTrue(product.getCreator().equals("me"));
			}
			MetaQuery mq = new MetaQuery(Product.class, "p",
					"p.meta.containsKey('check') and p.meta['check'].value == 'red'");
			Set<ProductRef> resultsMeta = null;
			resultsMeta = storage.select(mq, resultsAtt);
			assertTrue(resultsMeta.size() == 2);
			for (ProductRef ref : resultsMeta) {
				Product product;
				product = ref.getProduct();
				assertTrue(product.getMeta().get("check").getValue().equals(
						"red"));
			}
			FullQuery fq = new FullQuery(Product.class, "p",
					"p.creator == 'me' and (ALL(p['table']['Numbers'].data > 100.0))");
			Set<ProductRef> resultsFull = null;
			resultsFull = storage.select(fq, resultsMeta);
			// Should get one match
			assertTrue(resultsFull.size() == 1);
			// Longwinded check, better done with maps if there
			// are more matches
			boolean urn3Found = false;
			for (ProductRef ref : resultsFull) {
				if (ref.getUrn().equals(urn3)) {
					urn3Found = true;
				}
				Product product;
				product = ref.getProduct();
				assertTrue(product.getDescription().equals("QueryFullP3"));
			}
			assertTrue(urn3Found);
		} catch (IOException e) {
			fail("Unexpected IOException found. Details:" + e);
		} catch (GeneralSecurityException e) {
			fail("Unexpected GeneralSecurityException found. Details:" + e);
		}
	}

	/**
	 * SPR-2370: Tests that queries involving values with underscores are parsed
	 * correctly.
	 */
	public void testSpr2370() {
		checkWithModelName("HIFI_ILT_005");
		checkWithModelName("HIFI_005");
		checkWithModelName("_HIFI005_");
	}

	private void checkWithModelName(String model) {
		try {

			ProductPool pool1 = getPool();

			ProductStorage storage = new ProductStorage();
			storage.register(pool1);

			{
				Product p1 = new Product();
				p1.setModelName(model);
				storage.save(p1);
			}

			// Test for Attribute query that returns single product
			AttribQuery aq = new AttribQuery(Product.class, "p",
					"p.modelName == \'" + model + "\'");

			Set<ProductRef> results = null;
			results = storage.select(aq);
			assertTrue(results.size() == 1);

			for (ProductRef ref : results) {
				Product product;
				product = ref.getProduct();
				assertTrue(product.getModelName().equals(model));
			}

		} catch (IOException e) {
			fail("Unexpected IOException found. Details:" + e);
		} catch (GeneralSecurityException e) {
			fail("Unexpected GeneralSecurityException found. Details:" + e);
		}

	}

	/**
	 * Test whether different handle names
	 * can be used for an attribute query.
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public void testSpr2356ProductHandle() throws IOException,
			GeneralSecurityException {

		final String creator = "me";
		final String defaultHandle = "p";
		final String altHandleOne = "rp";
		final String altHandleTwo = "product";

		ProductPool pool1 = getPool();

		ProductStorage storage = new ProductStorage();
		storage.register(pool1);

		{
			Product p1 = new Product();
			p1.setCreator(creator);
			storage.save(p1);
		}

		// Test using the 'default' handle string
		runHandleQuery(storage, defaultHandle, creator);

		// Test using the alternative handle string
		runHandleQuery(storage, altHandleOne, creator);

		// Test using the alternative handle string
		runHandleQuery(storage, altHandleTwo, creator);

	}

	private void runHandleQuery(ProductStorage storage, String handle, String creator)
			throws IOException, GeneralSecurityException {

		Set<ProductRef> results = null;

		String queryString = "" + handle + ".creator ==\'" + creator + "\'";

		AttribQuery aq = new AttribQuery(Product.class, handle, queryString);

		results = storage.select(aq);
		for (ProductRef ref : results) {
			Product product;
			product = ref.getProduct();
			assertTrue(product.getCreator().equals(creator));
		}

	}

	/**
	 * Test whether and attribute query involving brackets
	 * is correctly translated to a vql query.
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public void testSpr2356BracketsQuery() throws IOException,
			GeneralSecurityException {

		final String creator = "me";
		final String defaultHandle = "p";
		final String altHandleOne = "rp";
		final String altHandleTwo = "product";

		ProductPool pool1 = getPool();

		ProductStorage storage = new ProductStorage();
		storage.register(pool1);

		{
			Product p1 = new Product();
			p1.setCreator(creator);
			storage.save(p1);
		}

		// Test using the 'default' handle string
		runBracketsQuery(storage, defaultHandle, creator);

		// Test using the alternative handle string
		runBracketsQuery(storage, altHandleOne, creator);

		// Test using the alternative handle string
		runBracketsQuery(storage, altHandleTwo, creator);

	}

	private void runBracketsQuery(ProductStorage storage, String handle, String creator)
			throws IOException, GeneralSecurityException {

		Set<ProductRef> results = null;

		String queryString = "(" + handle + ".creator ==\'" + creator + "\')";

		AttribQuery aq = new AttribQuery(Product.class, handle, queryString);

		results = storage.select(aq);
		for (ProductRef ref : results) {
			Product product;
			product = ref.getProduct();
			assertTrue(product.getCreator().equals(creator));
		}

	}

	/**
	 * Check whether single or double quoting of attribute value yields the same
	 * result.
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public void test2357Quotes() throws IOException,
			GeneralSecurityException {

		final String _SINGLEQUOTE = "\'";

		final String _DOUBLEQUOTE = "\"";

		final String creator = "me";

		ProductPool pool1 = getPool();

		ProductStorage storage = new ProductStorage();
		storage.register(pool1);

		{
			Product p1 = new Product();
			p1.setCreator(creator);
			storage.save(p1);
		}

		runQuoteQuery(storage, _SINGLEQUOTE, creator);

		runQuoteQuery(storage, _DOUBLEQUOTE, creator);

	}

	private void runQuoteQuery(ProductStorage storage, String quote, String creator)
			throws IOException, GeneralSecurityException {

		final String handle = "p";

		Set<ProductRef> results = null;

		String queryString = "" + handle + ".creator ==" + quote + creator
				+ quote;

		AttribQuery aq = new AttribQuery(Product.class, handle, queryString);

		results = storage.select(aq);
		for (ProductRef ref : results) {
			Product product;
			product = ref.getProduct();
			assertTrue(product.getCreator().equals(creator));
		}

	}

	/**
	 * Check whether and attribute with an embedded quote works as expected.
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public void testSpr2357EmbeddedQuotes() throws IOException,
			GeneralSecurityException {

		final String handle = "p";

		final String embeddedQuoteValue = "embedded\\\"quote";
		ProductPool pool1 = getPool();

		ProductStorage storage = new ProductStorage();
		storage.register(pool1);

		{
			Product p1 = new Product();
			p1.setCreator(embeddedQuoteValue);
			storage.save(p1);
		}

		AttribQuery query = new AttribQuery(Product.class, handle, "" + handle
				+ ".creator==\"" + embeddedQuoteValue + "\"");

		Set<ProductRef> results = storage.select(query);
		for (ProductRef ref : results) {
			Product product;
			product = ref.getProduct();
			assertTrue(product.getCreator().equals(embeddedQuoteValue));
		}

	}


	
}
