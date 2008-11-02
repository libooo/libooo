package herschel.ia.pal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.logging.Logger;

import herschel.ia.dataset.Product;
import herschel.ia.pal.query.AttribQuery;
import herschel.share.util.ConfiguredTestCase;

public abstract class AbstractPalTestCase extends ConfiguredTestCase {

    private static Logger _LOGGER = Logger.getLogger("AbstractPalTestCase");
    protected static int MAX_POOLS = 5; 
    // (pbalm increased from 5 to 50 to be useful for benchmarks.
    // Note: Keep in sync with JavaDoc on getPool method.) 

    private ProductPool[] _pools = new ProductPool[MAX_POOLS];

    /** Constructor */
    public AbstractPalTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        _LOGGER.info("=======================================================");
        _LOGGER.info(getTestName());
        _LOGGER.info("=======================================================");
        //checkEmptyPool();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        _LOGGER.info("End of " + getTestName());
    }

    /**
     * Gets a pool for a given test harness.
     * @param index of the pool. Must not be greater than 50.
     * @throws IndexOutOfBoundsException If index > 50.
     */
    protected ProductPool getPool(int index) {
        if (index > MAX_POOLS) {
            throw new IndexOutOfBoundsException("No pool for index " + index+". Maximum allowed index is "+MAX_POOLS+".");
        }
        ProductPool pool = _pools[index];
        if (pool == null) {
            pool = _pools[index] = createPool(index);
        }
        return pool;
    }

    /**
     * Gets a default pool.
     * It returns <code>getPool(0)</code>.
     */
    protected final ProductPool getPool() {
        return getPool(0);
    }

    /**
     * Creates a new pool for a given test harness.
     * This method needs to be overridden, but not to be used directly.
     * getPool(index) must be used instead for getting a pool.
     */
    protected abstract ProductPool createPool(int index);

    /**
     * Cleans the created pools.
     */
    protected final void cleanPools() {
        for (int i = 0; i < _pools.length; i++) {
            ProductPool pool = _pools[i];
            if (pool != null) {
                cleanPool(i);
                _pools[i] = null;
            }
        }
    }

    /**
     * Cleans a pool.
     * @param index Index of the pool to be cleaned.
     */
    protected abstract void cleanPool(int index);

    /**
     * Returns the name of the current test.
     */
    protected final String getTestName() {
        return getClass().getSimpleName() + "." + getName();
    }

    /** Checks that the default pool is empty. */
    @SuppressWarnings("unused")
    private void checkEmptyPool() throws IOException, GeneralSecurityException {
        ProductPool     pool  = getPool();
        AttribQuery     query = new AttribQuery(Product.class, "p", "1");
        Set<ProductRef> refs  = pool.select(query);
        int nProducts = refs.size();
        if (nProducts > 0) {
            String message = "Pool " + pool.getId() +
                             " not empty: " + nProducts + " products";
            _LOGGER.severe(message);
            fail(message);
        }
    }
    
    /**
     * Return the pooltype implemented by the concrete class: db, simple, lstore...
     */
    public abstract String getPoolType();

}
