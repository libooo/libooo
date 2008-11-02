/* $Id: AllTests.java,v 1.4 2008/09/26 17:55:13 bli Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.managers;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test suite for JUnit.
 * 
 * @author hsiddiqu
 */
public class AllTests {

	/**
	 * Test suite holding all tests for this sub-system.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("All tests");
//		suite.addTest(herschel.ia.pal.managers.PoolManagerTest.suite());
//		suite.addTest(herschel.ia.pal.managers.StorageManagerTest.suite());
		return suite;
	}

	/**
	 * Allows you to run all tests as an application.
	 */
	public static void main(String[] arguments) {
		TestRunner.run(suite());
	}

}
