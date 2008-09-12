package herschel.ia.pal.pool.lstore.test;

import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.StringParameter;
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
import java.util.Set;

public class StructureTest {

	public static void main(String[] args) throws IOException,
			GeneralSecurityException {
		Configuration.setProperty("hcss.ia.pal.pool.lstore.dir",
				"C:\\Documents and Settings\\Scott.Lee\\.hcss\\lstore");
		LocalStore store = LocalStoreFactory.getStore("spg_temp");
		ProductStorage storage = new ProductStorage();
		storage.register(store);

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
		int j=0;
		for (ProductRef ref : products) {
			Product p = ref.getProduct();
			MetaData meta = p.getMeta();
			if(meta.containsKey("obsid")){
				String obsid = meta.get("obsid").getValue().toString();
				System.out.println((j++)+": "+ obsid);
				meta.set("context", new StringParameter(obsid));
			}else{
				meta.set("context", new StringParameter(""+j));
			}
			System.out.println("context: "+p.getMeta().get("context"));
			ProductRef ref1 = storage.save(p);
			String urn = ref1.getUrn();
			ProductRef ref2 = storage.load(urn);
			System.out.println(ref2.getMeta().get("context"));
		}

//		Set<ProductRef> rs = storage.select(new Query("1"));
//		int i = 0;
//		for (ProductRef ref : rs) {
//			String ctx = null;
//			if (ref.getMeta().containsKey("context")) {
//				ctx = ref.getMeta().get("context").toString();
//			}
//			System.out.println((i++) + ": " + ctx);
//		}
	}
}
