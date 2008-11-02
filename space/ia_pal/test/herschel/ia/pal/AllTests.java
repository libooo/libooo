package herschel.ia.pal;

import herschel.ia.pal.util.SizeCalculatorTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test suite for JUnit.
 *
 * @author Jorgo Bakker
 */
public class AllTests {

    /**
     * Test suite holding all tests for this sub-system.
     */
    public static Test suite( ) {
        TestSuite suite = new TestSuite("All tests");

        suite.addTest(herschel.ia.pal.managers.AllTests.suite());
        suite.addTest(herschel.ia.pal.query.AllTests.suite());
        suite.addTest(herschel.ia.pal.rule.AllTests.suite());
        suite.addTest(herschel.ia.pal.util.AllTests.suite());
        suite.addTest(herschel.ia.pal.versioning.AllTests.suite());
        
//        suite.addTestSuite(herschel.ia.pal.util.size.SizeofTest.class);
//        suite.addTestSuite(herschel.ia.pal.util.size.ObjectProfilerTest.class);
        suite.addTest(new TestSuite(SizeCalculatorTest.class));
        
        return suite;
    }

    /**
     * Allows you to run all tests as an application.
     */
    public static void main(String[] arguments ) {
	TestRunner.run(suite());
    }

}
