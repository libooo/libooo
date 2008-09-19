package herschel.ia.pal.pool.hsa;

import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.StringParameter;
import herschel.ia.pal.Context;
import herschel.ia.pal.MapContext;
import herschel.ia.pal.ProductPool;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.pool.hsa.HsaReadPool;
import herschel.ia.pal.pool.lstore.LocalStore;
import herschel.ia.pal.pool.lstore.LocalStoreFactory;
import herschel.ia.pal.query.MetaQuery;
import herschel.ia.pal.query.StorageQuery;
import herschel.share.util.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.Set;

public class StructureTest {

	public static void main(String[] args) throws IOException,
			GeneralSecurityException {
		ProductStorage storage = getStorage();
		Product p = getTestContext();
		storage.save(buildContext(p,"root"));

		 testDownloadData(storage);

		// Set<ProductRef> rs = storage.select(new Query("1"));
		// int i = 0;
		// for (ProductRef ref : rs) {
		// String ctx = null;
		// if (ref.getMeta().containsKey("context")) {
		// ctx = ref.getMeta().get("context").toString();
		// }
		// System.out.println((i++) + ": " + ctx);
		// }
	}

	private static void testDownloadData(ProductStorage storage)
			throws IOException, GeneralSecurityException {
		String HSA_HAIO_HOST = "hsa.esac.esa.int";
		String HSA_HAIO_PORT = "8080";
		String urlHaio = "http://" + HSA_HAIO_HOST + ":" + HSA_HAIO_PORT
				+ "/aio/jsp/";
		ProductPool archive = new HsaReadPool(urlHaio + "metadata.jsp", urlHaio
				+ "product.jsp");
		ProductStorage hsaStore = new ProductStorage(archive);
		String queryStr = "p.type == \"OBS\" and p.creator==\"SPG v0.0\" and p.instrument==\"spire\"";
		StorageQuery query = new MetaQuery(
				herschel.ia.obs.ObservationContext.class, "p", queryStr);
		Set<ProductRef> products = hsaStore.select(query);
		System.out.println(products.size());
		int j = 0;
		for (ProductRef ref : products) {
			Product p = ref.getProduct();
			MetaData meta = p.getMeta();
			String obsid ="obsid";
			if (meta.containsKey("obsid")) {
				obsid = meta.get("obsid").getValue().toString();
				System.out.println((j++) + ": " + obsid);
				meta.set("context", new StringParameter(obsid));
			} else {
				meta.set("context", new StringParameter("" + j));
			}
			System.out.println("context: " + p.getMeta().get("context"));
			ProductRef ref1 = storage.save(buildContext(p, obsid));
			String urn = ref1.getUrn();
			ProductRef ref2 = storage.load(urn);
			System.out.println(ref2.getMeta().get("context"));
		}
	}

	public static ProductStorage getStorage() {
		Configuration.setProperty("hcss.ia.pal.pool.lstore.dir",
				"C:\\Documents and Settings\\Scott.Lee\\.hcss\\lstore");
		LocalStore store = LocalStoreFactory.getStore("spg_temp");
		ProductStorage storage = new ProductStorage();
		storage.register(store);
		return storage;
	}

	private static Product getTestContext() {
		MapContext root = new MapContext();
		Product a = new Product();
		a.getMeta().set("context", new StringParameter("a"));
		root.setProduct("a", a);
		Product b = new Product();
		root.setProduct("b", b);
		return root;
	}

	public static Product buildContext(Product p, String ctx) throws IOException,
			GeneralSecurityException {
		String context = null;
		if (p.getMeta().containsKey("context")) {
			System.out.println("context: "+ p.getMeta().get("context").getValue());
			context = ctx+"/"+ (String) p.getMeta().get("context").getValue();
			
		}else{
			context=ctx;
		}
		if (Context.isContext(p.getClass())) {
			Context c = (Context) p;
			Set<ProductRef> leaves = c.getAllRefs();
			Iterator ll = leaves.iterator();
			while (ll.hasNext()) {
				ProductRef ref = (ProductRef) ll.next();
				Product pp = ref.getProduct();
				pp=buildContext(pp, context);
			}
		}
		System.out.println("context: "+context);
		p.getMeta().set("context", new StringParameter(context));
		return p;
	}

}
