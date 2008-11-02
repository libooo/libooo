package herschel.ia.pal.util;

import herschel.ia.dataset.ArrayDataset;
import herschel.ia.dataset.Dataset;
import herschel.ia.dataset.Product;
import herschel.ia.numeric.ArrayData;
import herschel.ia.numeric.Int1d;
import herschel.ia.pal.TestProductGenerator;
import herschel.ia.pal.util.SizeCalculator;
import junit.framework.TestCase;

import org.junit.Test;

public class SizeCalculatorTest extends TestCase {

	public final int MIN_SIZE = 1;
	public final int MAX_SIZE = 16;

	@Test
	public void testAProduct() throws Exception {
		Product p = TestProductGenerator.getProductOfSize(0.01);
		double productSize = SizeCalculator.sizeOf(p) / 1000;
		assertTrue(productSize > 8);
		assertTrue(productSize < 12);
	}

	@Test
	public void testTwoProduct() {
		Product p1 = TestProductGenerator.getProductOfSize(0.01);
		long productSize1 = SizeCalculator.sizeOf(p1);
		Product p2 = TestProductGenerator.getProductOfSize(0.01);
		ArrayData data = new Int1d((int) (100));
		Dataset dataSet = new ArrayDataset(data);
		p2.set("foo", dataSet);
		long productSize2 = SizeCalculator.sizeOf(p2);
		assertTrue(productSize2 > productSize1);
		Product p3 = TestProductGenerator.getProductOfSize(0.01);
		p3.setDescription(p3.getDescription()+"foo description");
		long productSize3 = SizeCalculator.sizeOf(p3);
		assertTrue(productSize3 > productSize1);
		
	}

}
