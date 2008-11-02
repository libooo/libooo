package herschel.ia.pal.query;

import herschel.share.testharness.PyTestModule;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test suite for JUnit.
 *
 * @author Jorgo Bakker
 */
public class AllTests {

	private static Test py(String module) { return new PyTestModule(module); }

    /**
     * Test suite holding all tests for this sub-system.
     */
    public static Test suite( ) {
        TestSuite suite = new TestSuite("All tests");
        suite.addTest(QueryTest.suite());
//		suite.addTest(py("herschel.ia.pal.query.PalApiTest"));
        return suite;
    }

    /**
     * Allows you to run all tests as an application.
     */
    public static void main(String[] arguments ) {
	TestRunner.run(suite());
    }

}
