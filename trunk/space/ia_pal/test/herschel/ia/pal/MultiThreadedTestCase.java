package herschel.ia.pal;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * 
 * A multi-threaded JUnit test case. 
 * 
 * To perform test cases that spin off threads to do tests: <p>
 * <UL>
 * <LI>Extend <code>MultiThreadedTestCase</code>
 * <LI>Write your tests cases as normal except for when you want to spin off threads.
 * <LI>When you want to spin off threads:
 * <UL>
 * <LI>Instead of implementing <code>Runnable</code> extend <code>MultiThreadedTestCase.TestCaseRunnable</code>.
 * <LI>Define <code>runTestCase ()</code> to do your test, you may call <code>fail (), assert ()</code> etc. and throw
 * exceptions with impunity.
 * <LI>Handle thread interrupts by finishing.
 * </UL>
 * <LI>Instantiate all the runnables (one for each thread you wish to spawn) and pass an array of them
 * to <code>runTestCaseRunnables ()</code>.
 * </UL>
 * That's it. An example is below:
 * <PRE>
 * public class MTTest extends MultiThreadedTestCase
 * {
 *   MTTest (String s) { super (s); }
 *   public class CounterThread extends TestCaseRunnable
 *   {
 *     public void runTestCase () throws Throwable
 *     {
 *       for (int i = 0; i < 1000; i++)
 *       {
 *         System.out.println ("Counter Thread: " + Thread.currentThread () + " : " + i);
 *         // Do some testing...
 *         if (Thread.currentThread ().isInterrupted ()) {
 *           return;
 *         }
 *       }
 *     }
 *   }
 *
 *   public void test1 ()
 *   {
 *     TestCaseRunnable tct [] = new TestCaseRunnable [5];
 *     for (int i = 0; i < 5; i++) 
 *     {
 *       tct[i] = new CounterThread ();
 *      }
 *     runTestCaseRunnables (tct);
 *   }
 * }
 * </PRE> 
 * 
 * This class was got from the article - http://www.javaworld.com/jw-12-2000/jw-1221-junit.html?page=5.
 * 
*/
public class MultiThreadedTestCase extends TestCase {
    /** 
     * The threads that are executing.
     */
    private Thread threads[] = null;
    /**
     * The tests TestResult.*/
    private TestResult testResult = null;
    /**
     * Simple constructor. 
     */
    
    public MultiThreadedTestCase(String s) {
        super(s);
    }
    /**
     * Interrupt the running threads.
     */
    
    public void interruptThreads() {
        if(threads != null) {
            for(int i = 0;i < threads.length;i++) {
                threads[i].interrupt();
            }
        }
    }
    /**
     * Override run so we can squirrel away the test result.*/
    
    public void run(final TestResult result) {
        testResult = result;
        super.run(result);
        testResult = null;
    }
    /**
     * Run the test case threads.*/
    
    protected void runTestCaseRunnables (final TestCaseRunnable[] runnables) {
        if(runnables == null) {
            throw new IllegalArgumentException("runnables is null");
        }
        threads = new Thread[runnables.length];
        for(int i = 0;i < threads.length;i++) {
            threads[i] = new Thread(runnables[i]);
        }
        for(int i = 0;i < threads.length;i++) {
            threads[i].start();
        }
        try {
            for(int i = 0;i < threads.length;i++) {
                threads[i].join();
            }
        }
        catch(InterruptedException ignore) {
            System.out.println("Thread join interrupted.");
        }
        threads = null;
    }
    /**
     * Handle an exception. Since multiple threads won't have their
     * exceptions caught the threads must manually catch them and call
     * <code>handleException ()</code>.
     * @param t Exception to handle.*/
    
    private void handleException(final Throwable t) {
        synchronized(testResult) {
            if(t instanceof AssertionFailedError) {
                testResult.addFailure(this, (AssertionFailedError)t);
            }
            else {
                testResult.addError(this, t);
            }
        }
    }
    /**
     * A test case thread. Override runTestCase () and define
     * behaviour of test in there.*/
    protected abstract class TestCaseRunnable implements Runnable {
        /**
         * Override this to define the test*/
        
        public abstract void runTestCase()
                              throws Throwable;
        /**
         * Run the test in an environment where
         * we can handle the exceptions generated by the test method.*/
        
        public void run() {
            try {
                runTestCase();
            }
            catch(Throwable t) /* Any other exception we handle and then we interrupt the other threads.*/ {
                handleException(t);
                interruptThreads();
            }
        }
    }
}