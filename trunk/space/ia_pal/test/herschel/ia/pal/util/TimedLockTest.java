package herschel.ia.pal.util;

import java.util.logging.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;

import herschel.share.util.ConfiguredTestCase;

public class TimedLockTest extends ConfiguredTestCase {

    /** Logger for this class */
    private static final Logger logger = Logger.getLogger(TimedLockTest.class.getName());

    public TimedLockTest(String name) {
	super(name);
    }

    public void testLock() throws IOException, GeneralSecurityException, InterruptedException {
	TimedLock lock = new TimedLock(new BasicLock("test"), 500);

	lock.acquireLock();
	Thread.sleep(1000);
	logger.info(" " + lock.isLockAcquired());
	assertFalse("Timeout, the lock should be released.", lock.isLockAcquired());

	lock.acquireLock();
	lock.stop();
	Thread.sleep(1000);
	logger.info(" " + lock.isLockAcquired());
	assertTrue("Lock timer stopped, the lock should not be released.", lock.isLockAcquired());

	lock.start();
	Thread.sleep(1000);
	logger.info(" " + lock.isLockAcquired());
	assertFalse("Lock timer started, the lock should be released.", lock.isLockAcquired());

    }
}
