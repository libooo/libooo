package herschel.ia.pal.util;

import static herschel.ia.pal.query.StorageQuery.QUERY_ALL;
import herschel.ia.pal.Context;
import herschel.ia.pal.ListContext;
import herschel.ia.pal.ProductPool;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.managers.PoolCreatorFactory;
import herschel.share.util.Configuration;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class provides means for copying all the contents of a pool to another
 * pool, by preserving hierarchy and avoiding unnecessary duplication of leaf
 * products.<p>
 * For example, a pool with contents like:<p>
 * <pre>
 *    A   B
 *   / \ / \
 *  C   D   E
 * </pre>
 * is cloned by making a single copy of D. On the other hand, selecting all
 * products and cloning all of them in a loop would lead to something like:<p>
 * <pre>
 *    A      B
 *   / \    / \
 *  C   D  D   E
 * </pre>
 * thus making an unwanted copy of D.
 * <p>
 * Cloning all products in a pool may allow to "evolve" them, for instance if
 * some deprecated data is to be removed. If old products are still capable of
 * being read, they are automatically stored with the new format.<p>
 * This is the reason why this tool is called "schema evolver", although it just
 * clones products.
 */
public class PoolSchemaEvolver {

    private static final Logger log = Logger.getLogger(PoolSchemaEvolver.class.getName());
    
    private String sourceId;
    private String sourceType;
    private String targetId;
    private String targetType;
    private boolean copyAllVersions = true;

    /**
     * Executes the tool.
     * <pre>
     * Usage: evolvePool &lt;sourceId&gt; &lt;sourceType&gt; [&lt;targetId&gt; &lt;targetType&gt;]
     *   The sourceId and sourceType refer to the id and type of the source pool, resp.
     *   Allowed pool types are simple, db, and lstore.
     *   The target pool is optional. If you don't provide a target, the source pool
     *   will be updated with the evolved products, and the old products will be removed.
     * </pre>
     */
    public static void main(String[] args)
    throws IOException, GeneralSecurityException {
        
        if (args.length < 2 || args.length > 5) {
            System.err.println("Usage: evolvePool <sourceId> <sourceType> [<targetId> <targetType>] [<copyAllVersions>]");
            System.err.println("  The sourceId and sourceType refer to the id and type of the source pool, resp. ");
            System.err.println("  Allowed pool types are simple, db, and lstore.");
            System.err.println("  The target pool is optional. If you don't provide a target, the source pool");
            System.err.println("  will be updated with the evolved products, and the old products will be removed.");
            System.err.println("  The copyAllVersions flag can be true or false; it indicates whether to copy all");
            System.err.println("  versions of each root product, or just the last version of each root product.");
            System.err.println("  If omitted, the default is to copy all versions of each root product.");
            System.err.println("  A root product is any context or product not contained in other context.");
            System.exit(0);
        }

        boolean copyAllVersions = (args.length == 3)? Boolean.valueOf(args[2]) :
                                  (args.length == 5)? Boolean.valueOf(args[4]) :
                                  true;
        PoolSchemaEvolver evolver = (args.length < 4)?
                                    new PoolSchemaEvolver(args[0], args[1]) :
                                    new PoolSchemaEvolver(args[0], args[1],
                                                          args[2], args[3]);
        evolver.setCopyAllVersions(copyAllVersions);
        evolver.evolve();
    }

    /**
     * Constructor for evolving the products within the same pool.
     * Existing products will be evolved (cloned) and then removed.
     * @param sourceId Id of the pool, the one given by
     * <code>pool.getId()</code>
     * @param sourceType One of <code>simple</code>, <code>lstore</code> and
     * <code>db</code>
     */
    public PoolSchemaEvolver(String sourceId, String sourceType) {
        this(sourceId, sourceType, null, null);
    }

    /**
     * Constructor for evolving the products to another pool.
     * @param sourceId Id of the source pool, the one given by
     * <code>pool.getId()</code>
     * @param sourceType Type of the source pool,
     * one of <code>simple</code>, <code>lstore</code> and <code>db</code>
     * @param targetId Id of the target pool; the pool is created if not
     * existing before.
     * @param targetType Type of the target pool,
     * one of <code>simple</code>, <code>lstore</code> and <code>db</code>
     */
    public PoolSchemaEvolver(String sourceId, String sourceType,
                             String targetId, String targetType) {
        this.sourceId   = sourceId;
        this.sourceType = sourceType;
        this.targetId   = targetId;
        this.targetType = targetType;
    }

    /**
     * Returns whether the evolve method would copy all versions of each
     * product, or just the last versions.
     */
    public boolean isCopyAllVersions() {
	return copyAllVersions;
    }

    /**
     * Sets whether the evolve method shall copy all versions of each product
     * (<code>true</code>), or just the last versions (<code>false</code>).
     */
    public void setCopyAllVersions(boolean copyAllVersions) {
	this.copyAllVersions = copyAllVersions;
    }

    /**
     * Evolves the source pool by cloning all its products.
     */
    public void evolve() throws IOException, GeneralSecurityException {
        ProductStorage sourceStorage = new ProductStorage();
        ProductStorage targetStorage = null;

        ProductPool sourcePool = getProductPool(sourceId, sourceType);
        sourceStorage.register(sourcePool);

        if (targetId != null) {
            targetStorage = new ProductStorage();
            ProductPool targetPool = getProductPool(targetId, targetType);
            targetStorage.register(targetPool);
            
            log.info("Evolving products in "+sourceType+" pool "+sourceId+
                     " and storing them in "+targetType+" pool "+targetId+".");
        }
        else {
            log.info("Evolving products in "+sourceType+" pool "+sourceId+".");
        }
        
        evolve(sourceStorage, targetStorage);
    }

    private void evolve(ProductStorage sourceStorage,
                        ProductStorage targetStorage)
    throws IOException, GeneralSecurityException {

        ProductPool targetPoolTmp = null;
        
        if (targetStorage == null) {
            String poolId = "tmp.PoolSchemaEvolver." + new Date().getTime();
            targetPoolTmp = PoolCreatorFactory.createPool(poolId, "lstore");
            targetStorage = new ProductStorage();
            targetStorage.register(targetPoolTmp);
        }
        
        Set<ProductRef> originalProducts = sourceStorage.select(QUERY_ALL);
        log.info("Store has " + originalProducts.size() + " products");

        Set<ProductRef> rootProducts = findRootProducts(originalProducts);
        
        ListContext superRoot = new ListContext();
        for (ProductRef ref : rootProducts) {
            if (copyAllVersions) {
        	try {
        	    List<ProductRef> refVersions = sourceStorage.getVersions(ref);
        	    superRoot.getRefs().addAll(refVersions);
        	} catch(NoSuchElementException e) { 
        	    log.warning("Product "+ref+" is corrupt in source pool -- not copying it.");
        	}
            } else {
        	superRoot.getRefs().add(ref);
            }
        }

        ProductRef superRootRef = targetStorage.save(superRoot);
        //log.info("Super root saved. Target now has "+targetStorage.select(queryAllProducts).size()+" products.");

        // If we've created a temporary target pool
        if (targetPoolTmp != null) {

            // Need to clear original source storage
            clearStorage(sourceStorage);

            // Need to copy new products back to source storage
            superRootRef = sourceStorage.save(superRootRef.getProduct());

            // Dropping super root product again
            sourceStorage.remove(superRootRef.getUrn());

            // Delete temporary storage
            // FIXME: done manually as it's a LocalStore.
            // If a method is added to ProductPool to completely remove a pool,
            // this code should be updated accordingly
            String basedir = Configuration.getFileProperty("hcss.ia.pal.pool.lstore.dir");
            String dirToRemove = basedir + File.separator + targetPoolTmp.getId();            
            FileUtil.deleteDirectory(new File(dirToRemove));
            log.info("Done. Counting products in final store... (You can Ctrl-C now if you don't care to know.)");
            log.info("Final now has "+sourceStorage.select(QUERY_ALL).size()+" products.");
        }
        else {
            // Just delete the super-root product
            targetStorage.remove(superRootRef.getUrn());
            log.info("Done. Counting products in target store... (You can Ctrl-C now if you don't care to know.)");
            log.info("Target now has "+targetStorage.select(QUERY_ALL).size()+" products.");
        }

    }
    
    /**
     * Return true if a ProductRef points to a Context object.
     * 
     * @param ref: ProductRef to evaluate
     * @return true if product referenced is an instance of a Context object.
     */
    private boolean isProductContext(ProductRef ref) {
        // This if-statement is the same as:
        //            if(p.getProduct() instanceof Context)
        // ...but without loading the product into memory.
        return Context.class.isAssignableFrom(ref.getType());
    }

    private ProductPool getProductPool(String name, String type) {
        
        if (!type.equals("simple") &&
            !type.equals("lstore") &&
            !type.equals("db")) {
            throw new IllegalArgumentException("Illegal pool type: " + type +
                                               " (not simple, lstore or db)." );
        }
        return PoolCreatorFactory.createPool(name, type);
    }

    private Set<ProductRef> findRootProducts(Set<ProductRef> allRefs)
    throws IOException, GeneralSecurityException {
        
        Set<ProductRef> rootProducts = new HashSet<ProductRef>(allRefs);
        
        for(ProductRef ref : allRefs) {
            if (isProductContext(ref)) {
                Context c = (Context) ref.getProduct();
                Set<ProductRef> childRefs = c.getAllRefs();
                for (ProductRef childRef : childRefs) {
                    if (rootProducts.contains(childRef)) {
                        rootProducts.remove(childRef);
                    }
                }
            }
        }
        
        log.info("Found " + rootProducts.size() + " root products.");
        return rootProducts;
    }

    /**
     * Delete all Products from a ProductStorage.
     */
    private void clearStorage(ProductStorage storage)
    throws IOException, GeneralSecurityException {
        
        Set<ProductRef> products = storage.select(QUERY_ALL);
        for(ProductRef ref : products) {
            storage.remove(ref.getUrn());
        }
            
    }
}
