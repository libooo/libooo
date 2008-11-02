package herschel.ia.pal.performance;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.pal.AbstractProductPool;
import herschel.ia.pal.ProductPool;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.query.StorageQuery;


public class LoggerPool extends AbstractProductPool {
    
    private ProductPool _pool;
    
    public LoggerPool(ProductPool pool) {
	_pool = pool;
    }

    public boolean exists(String urn) throws IOException, GeneralSecurityException {
	log("exists");
	return _pool.exists(urn);
    }

    public String getId() {
	return _pool.getId();
    }

    public Set<Class<? extends Product>> getProductClasses()
    throws IOException, GeneralSecurityException {
	log("getProductClasses");
	return _pool.getProductClasses();
    }

    public boolean isAlive() {
	log("isAlive");
	return _pool.isAlive();
    }

    public Product loadProduct(String urn) throws IOException, GeneralSecurityException {
	log("loadProduct");
	return _pool.loadProduct(urn);
    }

    /** @see herschel.ia.pal.ProductPool#loadDescriptors(java.lang.String) */
    public Map<String, Object> loadDescriptors(String urn) throws IOException, GeneralSecurityException {
	log("loadDescriptors");
	return _pool.loadDescriptors(urn);
    }

    public MetaData meta(String urn) throws IOException, GeneralSecurityException {
	log("meta");
	return _pool.meta(urn);
    }

    public void remove(String urn) throws IOException, GeneralSecurityException {
	log("remove");
	_pool.remove(urn);
    }

    public String save(ProductRef ref) throws IOException, GeneralSecurityException {
	log("save");
	return _pool.save(ref);
    }

    public Set<ProductRef> select(StorageQuery query)
    throws IOException, GeneralSecurityException {
	log("select");
	return _pool.select(query);
    }

    public Set<ProductRef> select(StorageQuery query, Set<ProductRef> results)
    throws IOException, GeneralSecurityException {
	log("select");
	return _pool.select(query, results);
    }

    public void acquireLock() throws IOException, GeneralSecurityException {
	log("acquireLock");
	_pool.acquireLock();
    }

    public boolean isLockAcquired() {
	log("isLockAcquired");
	return _pool.isLockAcquired();
    }

    public boolean isLocked() throws IOException, GeneralSecurityException {
	log("isLocked");
	return _pool.isLocked();
    }

    public void releaseLock() throws IOException, GeneralSecurityException {
	log("releaseLock");
	_pool.releaseLock();
    }

    public ProductRef getLastVersion(ProductRef ref) throws IOException, GeneralSecurityException {
	log("getLastVersion");
	return _pool.getLastVersion(ref);
    }

    public List<ProductRef> getVersions(ProductRef ref) throws IOException, GeneralSecurityException {
	log("getVersions");
	return _pool.getVersions(ref);
    }

    private void log(String text) {
	String date = new SimpleDateFormat("dd-MMM-yy HH:mm:ss.SSS").format(new Date());
	System.out.println(date + " " + text);
    }
}
