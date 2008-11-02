package herschel.ia.pal.ingest;

import herschel.ia.pal.ProductStorage;

public interface ProductIngester {
	public void ingest(ProductStorage store, ProductIngesterParam param);
}
