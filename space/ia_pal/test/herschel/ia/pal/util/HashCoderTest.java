// -*-java-*-
//
// File:      HashCoderTest.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Feb 25, 2008
// Usage:     -
// Info:      -

package herschel.ia.pal.util;

import java.util.Date;
import java.util.logging.Logger;

import junit.framework.TestCase;
import herschel.ia.dataset.Product;
import herschel.share.fltdyn.time.FineTime;

/**
 * This class ...
 */
public class HashCoderTest extends TestCase {

    public void testComparison() {
	HashCoder coder1 = new HashCoder();
	HashCoder coder2 = new HashCoder();
	String one = "one";
	String two = "two";
	String oneAgain = "one";

	assertTrue (coder1.getHash(one).equals(coder1.getHash(oneAgain)));
	assertFalse(coder1.getHash(one).equals(coder1.getHash(two)));
	assertTrue (coder1.getHash(one).equals(coder2.getHash(one)));
	assertTrue (coder1.getHash(one).equals(coder2.getHash(oneAgain)));

	FineTime now = new FineTime(new Date());
	Product p1A = new Product("one");
	Product p1B = new Product("one");
	Product p2  = new Product("two");
	p1A.setCreationDate(now);
	p1B.setCreationDate(now);
	p1A.setStartDate(now);
	p1B.setStartDate(now);
	p1A.setEndDate(now);
	p1B.setEndDate(now);

	assertTrue (coder1.getHash(p1A).equals(coder1.getHash(p1B)));
	assertFalse(coder1.getHash(p1A).equals(coder1.getHash(p2)));
	assertTrue (coder1.getHash(p1A).equals(coder2.getHash(p1A)));
	assertTrue (coder1.getHash(p1A).equals(coder2.getHash(p1B)));
    }

    public void testTime() {
	HashCoder coder = new HashCoder();
	Product product = new Product();

	long ini = System.currentTimeMillis();
	for (int i = 0; i < 1000; i++) {
	    coder.getHash(product);
	}
	long end = System.currentTimeMillis();
	Logger.getLogger(getClass().getName()).info((end - ini) + " ms.");

//	assertTrue((end - ini) < 1000); // < 1 second for 1000 products
    }
}
