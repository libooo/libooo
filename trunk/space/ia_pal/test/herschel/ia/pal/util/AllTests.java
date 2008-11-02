package herschel.ia.pal.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test suite for herschel.ia.pal.util package.
 */
public class AllTests {

    /**
     * Test suite holding all tests for this sub-system.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");
        suite.addTest(new TestSuite(HashCoderTest.class));
        suite.addTest(new TestSuite(TimedLockTest.class));
        suite.addTest(new TestSuite(UrnUtilsTest.class));
        suite.addTest(new TestSuite(UtilTest.class));
        return suite;
    }

    /**
     * Allows you to run all tests as an application.
     */
    public static void main(String[] arguments) {
	TestRunner.run(suite());
    }

}
