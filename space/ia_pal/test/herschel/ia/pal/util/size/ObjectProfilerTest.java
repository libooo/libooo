package herschel.ia.pal.util.size;

import junit.framework.TestCase;

import org.junit.Test;

public class ObjectProfilerTest extends TestCase {

    @Test
    public void testSizeof() {
	
	TenKBProduct product = new TenKBProduct();
	
	int productSize = ObjectProfiler.sizeof(product)/1000;
	
	assertTrue(productSize>8);
	assertTrue(productSize<20);
    }


}
