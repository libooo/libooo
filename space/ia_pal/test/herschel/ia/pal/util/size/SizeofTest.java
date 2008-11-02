package herschel.ia.pal.util.size;

import junit.framework.TestCase;

import org.junit.Test;

public class SizeofTest extends TestCase {

    public final int MIN_SIZE = 1;
    public final int MAX_SIZE = 16;

    @Test
    public void testAProduct() throws Exception {
	
	Sizeof.intSize(); // this is to make debugging easier -- after this, Sizeof static members are initialized
	
	double productSize = Sizeof.calculate(TenKBProduct.class, null)/1000.;
	
	// On my JVM, the TestProductGenerator appears to produce around 12 kB products, when
	// asked for a product of 10 kB.
	
	assertTrue(productSize>8);
	assertTrue(productSize<20);
    }

    
    @Test
    public void testObjectShellSize() {
	int size = Sizeof.objectShellSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

    @Test
    public void testObjectRefSize() {
	int size = Sizeof.objectRefSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

    @Test
    public void testLongSize() {
	int size = Sizeof.longSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

    @Test
    public void testIntSize() {
	int size = Sizeof.intSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

    @Test
    public void testShortSize() {
	int size = Sizeof.shortSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

    @Test
    public void testCharSize() {
	int size = Sizeof.charSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

    @Test
    public void testByteSize() {
	int size = Sizeof.byteSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

    @Test
    public void testBooleanSize() {
	int size = Sizeof.booleanSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

    @Test
    public void testDoubleSize() {
	int size = Sizeof.doubleSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

    @Test
    public void testFloatSize() {
	int size = Sizeof.floatSize();
	assertTrue(size >= MIN_SIZE && size <= MAX_SIZE);
    }

}
