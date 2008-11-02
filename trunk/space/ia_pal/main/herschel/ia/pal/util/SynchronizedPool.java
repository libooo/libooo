package herschel.ia.pal.util;

import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.pal.ProductPool;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.query.StorageQuery;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A synchronized wrapper for {@link ProductPool}. It simply adds java "synchronized" modifier to each
 * public method of a ProductPool. It could be used to ensure that pool's methods are not be executed by
 * two threads simultaneously.
 * <p>
 * An example:
 * 
 * <pre>
 * 		Java:
 * 		LocalStore _pool1 = new ThreadSafeLocalStore(LocalStoreFactory.getStore("test"));
 * 		_pool1.save(product)
 * </pre>
 * 
 * @author libo@bao.ac.cn
 * 
 */
public class SynchronizedPool implements ProductPool {

    private final ProductPool _pool;

    /**
     * Creates a synchronized wrapper for the specified ProductPool.
     * 
     * @param pool
     */
    public SynchronizedPool(ProductPool pool) {
	_pool = pool;
    }

    /**
     * @see herschel.ia.pal.ProductPool#exists(String urn)
     */
    public synchronized boolean exists(String urn)
    throws IOException, GeneralSecurityException {
	return _pool.exists(urn);
    }

    /**
     * @see herschel.ia.pal.ProductPool#getId()
     */
    public synchronized String getId() {
	return _pool.getId();
    }

    /**
     * @see herschel.ia.pal.ProductPool#getProductClasses()
     */
    public synchronized Set<Class<? extends Product>> getProductClasses()
    throws IOException, GeneralSecurityException {
	return _pool.getProductClasses();
    }

    /**
     * @see herschel.ia.pal.ProductPool#isAlive()
     */
    public synchronized boolean isAlive() {
	return _pool.isAlive();
    }
    
    /**
     * @see herschel.ia.pal.ProductPool#meta(String urn)
     */
    public synchronized MetaData meta(String urn)
    throws IOException, GeneralSecurityException {
	return _pool.meta(urn);
    }

    /**
     * @see herschel.ia.pal.ProductPool#remove(String urn)
     */
    public synchronized void remove(String urn)
    throws IOException, GeneralSecurityException {
	_pool.remove(urn);
    }

    /**
     * @see herschel.ia.pal.ProductPool#save(Product product)
     */
    public synchronized String save(Product product)
    throws IOException, GeneralSecurityException {
	return _pool.save(product);
    }

    /**
     * @see herschel.ia.pal.ProductPool#select(StorageQuery query)
     */
    public synchronized Set<ProductRef> select(StorageQuery query)
    throws IOException, GeneralSecurityException {
	return _pool.select(query);
    }

    /**
     * @see herschel.ia.pal.ProductPool#select(StorageQuery query, Set<String> results)
     */
    public synchronized Set<ProductRef> select(StorageQuery query, Set<ProductRef> results)
    throws IOException, GeneralSecurityException {
	return _pool.select(query, results);
    }

    /**
     * @see herschel.ia.pal.ProductPool#acquireLock()
     */
    public synchronized void acquireLock()
    throws IOException, GeneralSecurityException {
	_pool.acquireLock();
    }

    /**
     * @see herschel.ia.pal.ProductPool#isLockAcquired()
     */
    public synchronized boolean isLockAcquired() {
	return _pool.isLockAcquired();
    }

    /**
     * @see herschel.ia.pal.ProductPool#isLocked()
     */
    public synchronized boolean isLocked()
    throws IOException, GeneralSecurityException {
	return _pool.isLocked();
    }

    /**
     * @see herschel.ia.pal.ProductPool#releaseLock()
     */
    public synchronized void releaseLock()
    throws IOException, GeneralSecurityException {
	_pool.releaseLock();
    }

    /**
     * @see herschel.ia.pal.ProductPool#loadProduct(String urn)
     */
    public synchronized Product loadProduct(String urn)
    throws IOException, GeneralSecurityException {
	return _pool.loadProduct(urn);
    }

    /**
     * @see herschel.ia.pal.ProductPool#loadDescriptors(java.lang.String)
     */
    public Map<String, Object> loadDescriptors(String urn)
    throws IOException, GeneralSecurityException {
	return _pool.loadDescriptors(urn);
    }

    /**
     * @see herschel.ia.pal.ProductPool#save(ProductRef ref)
     */
    public synchronized String save(ProductRef ref)
    throws IOException, GeneralSecurityException {
	return _pool.save(ref);
    }

    /**
     * @see herschel.ia.pal.ProductPool#getLastVersion(ProductRef ref)
     */
    public synchronized ProductRef getLastVersion(ProductRef ref)
    throws IOException, GeneralSecurityException {
	return _pool.getLastVersion(ref);
    }

    /**
     * @see herschel.ia.pal.ProductPool#getVersions(ProductRef ref)
     */
    public synchronized List<ProductRef> getVersions(ProductRef ref)
    throws IOException, GeneralSecurityException {
	return _pool.getVersions(ref);
    }

    /**
     * @see herschel.ia.pal.ProductPool#removeUrn(String urn)
     */
    public synchronized void removeUrn(String urn)
    throws IOException, GeneralSecurityException {
	_pool.removeUrn(urn);
    }

    /**
     * @see herschel.ia.pal.ProductPool#getTags()
     */
    public synchronized Set<String> getTags()
    throws IOException, GeneralSecurityException {
	return _pool.getTags();
    }

    /**
     * @see herschel.ia.pal.ProductPool#getTags(String urn)
     */
    public synchronized Set<String> getTags(String urn)
    throws IOException, GeneralSecurityException {
	return _pool.getTags();
    }

    /**
     * @see herschel.ia.pal.ProductPool#getUrn(String tag)
     */
    public synchronized String getUrn(String tag)
    throws IOException, GeneralSecurityException {
	return _pool.getUrn(tag);
    }

    /**
     * @see herschel.ia.pal.ProductPool#removeTag(String tag)
     */
    public synchronized void removeTag(String tag)
    throws IOException, GeneralSecurityException {
	_pool.removeTag(tag);

    }

    /**
     * @see herschel.ia.pal.ProductPool#setTag(String tag, String urn)
     */
    public synchronized void setTag(String tag, String urn)
    throws IOException, GeneralSecurityException {
	_pool.setTag(tag, urn);
    }

    /**
     * @see herschel.ia.pal.ProductPool#tagExists(String tag)
     */
    public synchronized boolean tagExists(String tag)
    throws IOException, GeneralSecurityException {
	return _pool.tagExists(tag);
    }

}
