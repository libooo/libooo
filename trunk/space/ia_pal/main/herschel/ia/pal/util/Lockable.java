// -*-java-*-
//
// File:      Lockable.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Aug 30, 2007
// Usage:     -
// Info:      -

package herschel.ia.pal.util;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * This interface represents any object that is susceptible of being locked
 * by different processes.<p>
 * Symmetric calls to acquireLock and releaseLock are allowed. For instance:<p>
 * <pre>
 * Lockable lock = ...;
 * lock.acquireLock();  // acquires lock
 * lock.acquireLock();  // does nothing but increments lock counter
 * lock.releaseLock();  // does nothing but decrements lock counter
 * lock.releaseLock();  // releases lock
 * lock.releaseLock();  // does nothing
 * </pre>
 * <p>This way, these methods can be called safely within nested methods calls.
 * For example:
 * <pre>
 * private Lockable _lock;
 * 
 * void method1() throws SomeException {
 *     try {
 *         _lock.acquireLock();
 *         method2();
 *         // stuff A
 *     }
 *     finally {
 *         _lock.releaseLock();
 *     }
 * }
 * 
 * void method2() throws SomeException {
 *     try {
 *         _lock.acquireLock();
 *         // stuff B
 *     }
 *     finally {
 *         _lock.releaseLock(); // this will not release the lock here if this
 *     }                        // method has been called from method1, so
 * }                            // stuff A can be done with the lock acquired
 * </pre>
 */
public interface Lockable {

    /**
     * Acquires the lock; if the lock has been taken by another process,
     * waits until it is freed. Can be called safely more than once.
     */
    public void acquireLock() throws IOException, GeneralSecurityException;

    /**
     * Releases the lock if it is the equivalent call to the first acquireLock.
     * Can be called safely more than once.
     */
    public void releaseLock() throws IOException, GeneralSecurityException;

    /**
     * Informs whether the lock has been acquired by any process.
     */
    public boolean isLocked() throws IOException, GeneralSecurityException;

    /**
     * Informs whether the object has been locked by this process.
     */
    public boolean isLockAcquired();
}
