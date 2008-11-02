// -*-java-*-
//
// File:      BasicLock.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Aug 30, 2007
// Usage:     -
// Info:      -

package herschel.ia.pal.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import herschel.share.util.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>
 * This class wraps a lockable, and release the lock after specified time interval. Client could
 * stop/start the timer it used. Each time the start called, the time interval will be reset(i.e. not
 * resumed).
 * <p>
 * <b>Note:</b> <em>Class to be used only by developers of PAL.</em>
 */
public class TimedLock implements Lockable {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(TimedLock.class.getName());

    private Timer _timer;
    private Lockable _lock;
    private long _interval;

    private final static long DEFAULT_INTERVAL =
	Long.parseLong(Configuration.getProperty("ia.pal.lock.interval","2000"));

    /**
     * Constructor for a wrapped lock
     */
    public TimedLock(Lockable lock) {
	this(lock, DEFAULT_INTERVAL);
    }

    /**
     * Constructor for a wrapped lock and a specific timeout for automatically releasing the lock.
     */
    public TimedLock(Lockable lock, long interval) {
	_lock = lock;
	_interval = interval;
    }

    /** @see herschel.ia.pal.util.Lockable#acquireLock() */
    public void acquireLock() throws IOException, GeneralSecurityException {

	if (logger.isLoggable(Level.INFO)) {
	    logger.info("acquireLock start"); //$NON-NLS-1$
	}

	start();
	_lock.acquireLock();

	if (logger.isLoggable(Level.INFO)) {
	    logger.info("acquireLock end"); //$NON-NLS-1$
	}
    }

    /** @see herschel.ia.pal.util.Lockable#releaseLock() */
    public void releaseLock() throws IOException, GeneralSecurityException {
	if (logger.isLoggable(Level.INFO)) {
	    logger.info("releaseLock start"); //$NON-NLS-1$
	}

	_lock.releaseLock();
	if (!_lock.isLocked() && _timer != null) {
	    _timer.cancel();
	    _timer = null;
	}

	if (logger.isLoggable(Level.INFO)) {
	    logger.info("releaseLock end"); //$NON-NLS-1$
	}
    }

    /** @see herschel.ia.pal.util.Lockable#isLockAcquired() */
    public boolean isLockAcquired() {
	if (logger.isLoggable(Level.INFO)) {
	    logger.info("isLockAcquired start"); //$NON-NLS-1$
	}

	boolean returnboolean = _lock.isLockAcquired();
	if (logger.isLoggable(Level.INFO)) {
	    logger.info("isLockAcquired end"); //$NON-NLS-1$
	}
	return returnboolean;
    }

    /** @see herschel.ia.pal.util.Lockable#isLocked() */
    public boolean isLocked() throws IOException, GeneralSecurityException {
	if (logger.isLoggable(Level.INFO)) {
	    logger.info("isLocked start"); //$NON-NLS-1$
	}

	boolean returnboolean = _lock.isLocked();
	if (logger.isLoggable(Level.INFO)) {
	    logger.info("isLocked end"); //$NON-NLS-1$
	}
	return returnboolean;
    }

    /**
     * Stop the timer.
     */
    public void stop() {
	logger.info("timer stop...");
	if (_timer != null) {
	    _timer.cancel();
	    _timer = null;
	}
    }

    /**
     * Start the timer.
     */
    public void start() {
	logger.info("timer start...");
	if (_timer == null) {
	    _timer = new Timer();
	    _timer.schedule(new ReleaseLockTask(), _interval);
	}
    }

    class ReleaseLockTask extends TimerTask {
	public void run() {
	    if (logger.isLoggable(Level.INFO)) {
		logger.info("run start"); //$NON-NLS-1$
	    }

	    try {
		while (_lock.isLocked()) {
		    _lock.releaseLock();
		    _timer = null;
		}
	    } catch (IOException e) {
		logger.severe("exception: " + e); //$NON-NLS-1$
		e.printStackTrace();
	    } catch (GeneralSecurityException e) {
		logger.severe("exception: " + e); //$NON-NLS-1$
		e.printStackTrace();
	    }

	    if (logger.isLoggable(Level.INFO)) {
		logger.info("run end"); //$NON-NLS-1$
	    }
	}
    }
}
