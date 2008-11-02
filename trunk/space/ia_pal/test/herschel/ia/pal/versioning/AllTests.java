package herschel.ia.pal.versioning;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test suite for JUnit.
 * 
 * @author Hassan Siddiqui <hsiddiqu@rssd.esa.int>
 */
public class AllTests {

	/**
	 * Test suite holding all tests for this sub-system.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("All tests");
        suite.addTest(MetaDataTest.suite());
 		
// Do not use - under development.		
//		suite.addTest(VersionTrackReuseTest.suite());
		return suite;
	}

	/**
	 * Allows you to run all tests as an application.
	 */
	public static void main(String[] arguments) {
		TestRunner.run(suite());
	}

}
