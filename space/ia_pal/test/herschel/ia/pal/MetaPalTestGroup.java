package herschel.ia.pal;

import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.StringParameter;
import herschel.ia.pal.query.MetaQuery;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.python.core.PyException;

public class MetaPalTestGroup extends AbstractPalTestGroup {

	@SuppressWarnings("unused")
	private static Logger _LOGGER = Logger.getLogger("MetaPalTestGroup");

	public MetaPalTestGroup(String name, AbstractPalTestCase testcase) {
		super(name, testcase);
	}

	/**
	 * Simple meta query test involving one product.
	 */
	public void testSingleMetaQuery() {

		try {

			ProductPool pool = getPool();
			ProductStorage storage = new ProductStorage();
			storage.register(pool);

			/*
			 * Note placed in inner brackets to ensure variable p is
			 * subsequently not in scope
			 */
			{
				Product p = new Product();
				MetaData md = p.getMeta();
				md.set("flag", new StringParameter("up"));
				storage.save(p);
			}

			MetaQuery mq = new MetaQuery(Product.class, "p",
					"p.meta.containsKey('flag') and p.meta['flag'].value == 'up'");

			Set<ProductRef> selection = storage.select(mq);

			Assert.assertTrue(selection.size() == 1);

			for (ProductRef ref : selection) {
				Product product;
				product = ref.getProduct();
				Assert.assertTrue(product.getMeta().get("flag").getValue()
						.equals("up"));

			}

		} catch (IOException e) {
			fail("Unexpected IOException found. Details:" + e);
		} catch (GeneralSecurityException e) {
			fail("Unexpected GeneralSecurityException found. Details:" + e);
		}

	}

	/**
	 * Check that Jython exceptions are not raised during a query.
	 */
	public void testSpr2290() {

		final String queryString = "akrgjwejktwegkJB";

		try {

			ProductPool pool = getPool();
			ProductStorage storage = new ProductStorage();
			storage.register(pool);

			/*
			 * Note placed in inner brackets to ensure variable p is
			 * subsequently not in scope
			 */
			{
				Product p = new Product();
				storage.save(p);
			}

			// MetaQuery mq = new MetaQuery(Product.class, "p",
			// "p.meta['flag'].value == 'up'");
			MetaQuery mq = new MetaQuery(Product.class, "p", queryString);

			try {
				storage.select(mq);
			} catch (IllegalArgumentException e) {
				// success
				// _LOGGER.info("Trapped the following exception correctly" +
				// e);
			} catch (PyException e) {
				fail("Following PyException encountered which is not as expected "
						+ e);
			}

		} catch (IOException e) {
			fail("Unexpected IOException found. Details:" + e);
		} catch (GeneralSecurityException e) {
			fail("Unexpected GeneralSecurityException found. Details:" + e);
		}

	}

	/**
	 * Check if pool.meta(urn) returns a NoSuchElementException when an urn does
	 * not exist
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void testSpr5207() {
		try {
			ProductPool pool = getPool();
			pool.save(new Product());
			// a non-existing urn
			String _urn = "urn:" + pool.getId()
					+ ":herschel.spire.ia.whatever:0";
			try {
				pool.meta(_urn);
				fail("An NoSuchElementException should be thrown");
			} catch (NoSuchElementException e) { /* correct */
			}
		} catch (IOException e) {
			fail("Unexpected IOException found. Details:" + e);
		} catch (GeneralSecurityException e) {
			fail("Unexpected GeneralSecurityException found. Details:" + e);
		}
	}

}
