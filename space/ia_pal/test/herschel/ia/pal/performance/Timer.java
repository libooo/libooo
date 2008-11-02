/**
 * 
 */
package herschel.ia.pal.performance;

import java.util.Date;
import java.util.logging.Logger;

/**
 * A helper class to measure the elapsed time during a benchmark.
 * Helps to measure the time between start and end.
 * 
 * @author pbalm
 */
public class Timer {

    private Date startTime;
    private Date stopTime;

    private static Logger log = Logger.getLogger("Timer");
    
    /**
     * When start() is called, garbage collection is requested, and 1 sec sleep is built-in.
     * Only after that, the timer starts, unless startImmediately is set.
     */
    public boolean startImmediately = false;

    /**
     * Start the timer.
     * Before actually starting, request garbage collection and wait 1 second for
     * garbage collection to complete.
     */
    public void start() {
	if(!startImmediately) {
	    System.gc();
	    try {
		Thread.sleep(1000);
	    }
	    catch(InterruptedException e) {
		log.severe(e.getMessage());
	    }
	}

	stopTime = null;
	startTime = new Date();
    }

    /**
     * Stop timer.
     */
    public void stop() {
	stopTime = new Date();
    }

    /**
     * Get the elapsed time between start and stop. Note: No checking that Timer was actually started
     * and stopped.
     * 
     * @return The elapsed time in milliseconds.
     */
    public long read() {
	if(startTime==null) {
	    throw new IllegalStateException("Timer not started.");
	}
	if(stopTime==null) {
	    throw new IllegalStateException("Timer not stopped.");
	}
	
	return stopTime.getTime() - startTime.getTime();
    }
}