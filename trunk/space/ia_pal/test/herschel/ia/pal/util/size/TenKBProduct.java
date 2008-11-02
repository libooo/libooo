package herschel.ia.pal.util.size;

import herschel.ia.dataset.Product;
import herschel.ia.pal.TestProductGenerator;

public class TenKBProduct extends Product {
	@SuppressWarnings("unused")
	private Product p;

	@SuppressWarnings("unused")
	public TenKBProduct() {
	    p = TestProductGenerator.getProductOfSize(0.01);
	}
}
