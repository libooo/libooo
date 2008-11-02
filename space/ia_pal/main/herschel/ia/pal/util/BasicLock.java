// -*-java-*-
//
// File:      BasicLock.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Aug 30, 2007
// Usage:     -
// Info:      -

package herschel.ia.pal.util;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

/**
 * This class implements a basic locking mechanism, by creating a lock file.<p>
 * <p><b>Note:</b> <em>Class to be used only by developers of PAL.</em>
 */
public class BasicLock implements Lockable {

    @SuppressWarnings("unused")
    private static Logger _LOGGER = Logger.getLogger(BasicLock.class.getName());
    private static final long LOCK_WAIT = 250;    // milliseconds

    private int _lockCounter;   // for allowing simmetric lock/unlock
    private File _file;         // file to implement the lock

    /**
     * Constructor.
     * @param name Name to identify the lock.
     */
    public BasicLock(String name) {
        String dirname = System.getProperty("java.io.tmpdir");
        _file = new File(dirname + "/.basicLock_" + name + ".lck");
        _lockCounter = 0;
    }

    /** @see herschel.ia.pal.util.Lockable#acquireLock() */
    public void acquireLock() throws IOException, GeneralSecurityException {

        // System.err.println("BasicLock.acquireLock " + (_lockCounter + 1));
        if (_lockCounter++ == 0) { // first compare, then increment
            while (!_file.createNewFile()) {
                // System.err.println("BasicLock.acquireLock: wait");
                try { Thread.sleep(LOCK_WAIT); }
                catch (InterruptedException e){}
            }
        }
    }

    /** @see herschel.ia.pal.util.Lockable#releaseLock() */
    public void releaseLock() throws IOException, GeneralSecurityException {
        if (_lockCounter > 0) {
            // System.err.println("BasicLock.releaseLock " + _lockCounter);
            _lockCounter--;
            if (_lockCounter == 0) {
                // System.err.println("BasicLock.releaseLock: delete");
                _file.delete();
            }
        }
    }

    /** @see herschel.ia.pal.util.Lockable#isLockAcquired() */
    public boolean isLockAcquired() {
        return _lockCounter > 0;
    }

    /** @see herschel.ia.pal.util.Lockable#isLocked() */
    public boolean isLocked() throws IOException, GeneralSecurityException {
        return _file.exists();
    }
}
