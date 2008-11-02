package herschel.ia.pal;

import herschel.ia.dataset.Column;
import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.StringParameter;
import herschel.ia.dataset.TableDataset;
import herschel.ia.numeric.Double1d;
import herschel.ia.pal.query.FullQuery;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

public class FullQueryPalTestGroup extends AbstractPalTestGroup {

	public FullQueryPalTestGroup(String name,  AbstractPalTestCase testcase) {
		super(name, testcase);
	}

	/**
	 * A Full query test involving three products.
	 */
	public void testFullQuery() {

		try {

			ProductPool pool = getPool();

			ProductStorage storage = new ProductStorage();
			storage.register(pool);

			String urn1;
			{
				Double1d data1 = new Double1d(new double[] { 23.4, 16.9, 11.2,
						28.4 });
				TableDataset table1 = new TableDataset("This is tabledata1");
				table1.addColumn("Numbers", new Column(data1));

				Product p1 = new Product("QueryFullP1");
				MetaData md = p1.getMeta();
				md.set("creator", new StringParameter("me"));
				p1.set("table", table1);
				ProductRef ref1 = storage.save(p1);
				urn1 = ref1.getUrn();
			}

			String urn2;
			{
				Double1d data2 = new Double1d(new double[] { 35.4, 46.9, 18.2,
						47.4 });

				TableDataset table2 = new TableDataset("This is tabledata2");
				table2.addColumn("Numbers", new herschel.ia.dataset.Column(
						data2));

				Product p2 = new Product("QueryFullP2");
				MetaData md = p2.getMeta();
				md.set("creator", new StringParameter("me"));
				p2.set("table", table2);
				ProductRef ref2 = storage.save(p2);
				urn2 = ref2.getUrn();
			}

			{
				Double1d data3 = new Double1d(new double[] { 85.4, 56.9, 78.2,
						67.4 });

				TableDataset table3 = new TableDataset("This is tabledata2");
				table3.addColumn("Numbers", new herschel.ia.dataset.Column(
						data3));

				Product p3 = new Product("QueryFullP3");
				MetaData md = p3.getMeta();
				md.set("creator", new StringParameter("someone-else"));
				p3.set("table", table3);
				storage.save(p3);
			}

			// Test for Attribute query that should return several products
			FullQuery fq = new FullQuery(Product.class, "p",
					"p.creator == 'me' and (ANY(p['table']['Numbers'].data < 50.0))");

			Set<ProductRef> results = null;
			results = storage.select(fq);

			// Should get two matches
			assertTrue(results.size() == 2);

			// Longwinded check, better done with maps if there
			// are more matches
			boolean urn1Found = false;
			boolean urn2Found = false;
			for (ProductRef ref : results) {

				if (ref.getUrn().equals(urn1)) {
					urn1Found = true;
				}
				if (ref.getUrn().equals(urn2)) {
					urn2Found = true;
				}
			}

			assertTrue(urn1Found && urn2Found);

		} catch (IOException e) {
			fail("Unexpected IOException found. Details:" + e);
		} catch (GeneralSecurityException e) {
			fail("Unexpected GeneralSecurityException found. Details:" + e);
		}

	}


}
