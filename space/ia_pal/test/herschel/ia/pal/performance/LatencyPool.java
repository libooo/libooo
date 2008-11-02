package herschel.ia.pal.performance;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.pal.AbstractProductPool;
import herschel.ia.pal.ProductPool;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.query.StorageQuery;


public class LatencyPool extends AbstractProductPool {
    
    private ProductPool p;

    private final long DELAY = 500; // [ms]
    
    public LatencyPool(ProductPool pool) {
	p = pool;
    }
    
    private void sleep() {
	try {
	    Thread.sleep(DELAY);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    public boolean exists(String urn) throws IOException, GeneralSecurityException {
	sleep();
	return p.exists(urn);
    }

    public String getId() {
	return "latency-"+p.getId();
    }

    public Set<Class<? extends Product>> getProductClasses()
    throws IOException, GeneralSecurityException {
	sleep();
	return p.getProductClasses();
    }

    public boolean isAlive() {
	sleep();
	return p.isAlive();
    }

    public Product loadProduct(String urn) throws IOException, GeneralSecurityException {
	sleep();
	return p.loadProduct(urn);
    }

    /** @see herschel.ia.pal.ProductPool#loadDescriptors(java.lang.String) */
    public Map<String, Object> loadDescriptors(String urn) throws IOException, GeneralSecurityException {
	sleep();
	return p.loadDescriptors(urn);
    }

//    public ProductRef load(String urn) throws IOException, GeneralSecurityException {
//	sleep();
//	return p.load(urn);
//    }

    public MetaData meta(String urn) throws IOException, GeneralSecurityException {
	sleep();
	return p.meta(urn);
    }

    public void remove(String urn) throws IOException, GeneralSecurityException {
	sleep();
	p.remove(urn);
    }

    public String save(ProductRef ref) throws IOException, GeneralSecurityException {
	sleep();
	return p.save(ref);
    }

    public Set<ProductRef> select(StorageQuery query)
    throws IOException, GeneralSecurityException {
	sleep();
	return p.select(query);
    }

    public Set<ProductRef> select(StorageQuery query, Set<ProductRef> results)
    throws IOException, GeneralSecurityException {
	sleep();
	return p.select(query, results);
    }

    public void acquireLock() throws IOException, GeneralSecurityException {
	sleep();
	p.acquireLock();
    }

    public boolean isLockAcquired() {
	sleep();
	return p.isLockAcquired();
    }

    public boolean isLocked() throws IOException, GeneralSecurityException {
	sleep();
	return p.isLocked();
    }

    public void releaseLock() throws IOException, GeneralSecurityException {
	sleep();
	p.releaseLock();
    }

    public ProductRef getLastVersion(ProductRef ref) throws IOException, GeneralSecurityException {
	sleep();
	return p.getLastVersion(ref);
    }

    public List<ProductRef> getVersions(ProductRef ref) throws IOException, GeneralSecurityException {
	sleep();
	return p.getVersions(ref);
    }

}
