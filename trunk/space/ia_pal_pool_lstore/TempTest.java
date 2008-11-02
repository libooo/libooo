package herschel.ia.pal.pool.lstore.test;

import herschel.ia.dataset.Product;
import herschel.ia.pal.MapContext;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.ingest.FitsProductIngester;
import herschel.ia.pal.ingest.FitsProductIngesterParam;
import herschel.ia.pal.pool.lstore.LocalStore;
import herschel.ia.pal.pool.lstore.LocalStoreContext;
import herschel.ia.pal.pool.lstore.LocalStoreFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class TempTest {
	public static void main(String[] args) throws IOException,
			GeneralSecurityException {
		testRemoveContext();
		testIngest();
	}

	public static void testIngest() {
		LocalStoreContext context = new LocalStoreContext("mypool2",
				"e:/lstore/level0");
		LocalStore pool = LocalStoreFactory.getStore(context);
		LStoreTestUtil.cleanPool(pool);
		ProductStorage store = new ProductStorage();
		store.register(pool);
		store.ingest(new FitsProductIngester(), new FitsProductIngesterParam(
				new File("e:/lstore/level0/mypool")));
	}

	public static void testRemoveContext() throws IOException,
			GeneralSecurityException {
		LocalStoreContext context = new LocalStoreContext("mypool",
				"e:/lstore/level0");
		LocalStore pool = LocalStoreFactory.getStore(context);
		LStoreTestUtil.cleanPool(pool);
		ProductStorage store = new ProductStorage();
		store.register(pool);
		MapContext mc1 = new MapContext();
		Product p1 = new Product();
		p1.setDescription("p1");
		Product p2 = new Product();
		p2.setDescription("p2");
		MapContext mc11 = new MapContext();
		Product p11 = new Product();
		p11.setDescription("p11");
		Product p22 = new Product();
		p22.setDescription("p22");
		mc11.setProduct("p11", p11);
		mc11.setProduct("p22", p22);
		mc1.setProduct("key1", p1);
		mc1.setProduct("key2", p2);
		mc1.setProduct("mc11", mc11);
		ProductRef ref = store.save(mc1);

		String urn = ref.getUrn();

		System.out.println("ref: " + urn);
//		store.remove(urn, true);
	}
}
