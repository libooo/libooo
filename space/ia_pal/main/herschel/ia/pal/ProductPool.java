/* $Id: ProductPool.java,v 1.14 2008/09/12 04:25:15 bli Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import herschel.ia.dataset.Product;
import herschel.ia.dataset.MetaData;
import herschel.ia.pal.query.StorageQuery;
import herschel.ia.pal.util.Lockable;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.security.GeneralSecurityException;

/**
 * ProductPool is the interface for implementations that can store and
 * retrieve Products.
 * <p>
 * A product pool should <b>not</b> be used directly by users.
 * The general user should access data in a ProductPool through a {@link ProductStorage} instance.</p>
 * 
 * <p>When implementing a ProductPool, the following rules need to be applied:
 * <ol>
 * <li>Pools must guarantee that a Product saved via the pool {@link #save(Product)} method is stored persistently,
 * and that method returns a unique identifier (URN). If it is not possible to save a Product, an {@link IOException}
 * shall be raised.</li>
 * <li>A saved Product can be retrieved using the {@link #loadProduct(String)} method, using as the argument
 * the same URN that assigned to that Product in the earlier {@link #save(Product)} call. 
 * No other Product shall be retrievable by that same URN. If this is not possible, an {@link IOException} is raised.</li>
 * <li>Pools should not implement functionality currently implemented in the core ia.pal package.
 * Specifically, it should not address functionality provided in the {@link Context} abstract class,
 * and it should not implement versioning/cloning support.</li>
 * </ol>
 * </p>
 * <p>
 * In addition, the implementor should also provide an implementation of the {@link herschel.ia.pal.PoolCreator PoolCreator}, and
 * a property that enables the PoolManager to find that implementation from the short name of the pool type that is specified
 * in a XML pool configuration file. Look at the javadoc for {@link herschel.ia.pal.PoolCreator PoolCreator} for further information.
 * </p>
 * 
 * @author DP Development Team
 *
 */
public interface ProductPool extends Versionable, Taggable, Lockable {

    /**
      * Test if the pool is capable of responding to commands.
      * @return <code>true</code> if it is, <code>false</code> otherwise
      */
    public boolean isAlive();
    
    /**
     * Get the identifier of this pool.
     * @return a string identifying this Product Pool
     */
    public String getId();
    
    /**
     * Yields all Product classes found in this pool.
     * @return a collection of Product classes.
     */
    public Set<Class<? extends Product>> getProductClasses() throws IOException, GeneralSecurityException;
    
    /**
     * Determines the existence of a product with specified URN.
     * @param urn
     * @return whether a product with specified URN exists.
     * @throws IOException if an IO operation has failed,
     * eg due to the internet connection being broken,
     * or due to a corrupted file system
     * @throws GeneralSecurityException if the URN specified
     * references a product that is not permitted by the user
     * to be accessed.
     */
    public boolean exists(String urn) throws IOException, GeneralSecurityException;

    /**
     * Loads the meta-data belonging to the product of specified URN.
     * Get the meta data for a given product.
     * @param urn
     * @return the meta-data of a product with specified URN
     * @throws IllegalArgumentException if urn argument is null
     * @throws NoSuchElementException if the urn does not belong to this pool
     * @throws IOException if an IO operation has failed, eg due to the
     * internet connection being broken, or due to a corrupted file system
     * @throws GeneralSecurityException if the URN specified
     * references a product that is not permitted by the user
     */
    public MetaData meta(String urn) throws IOException, GeneralSecurityException;


    /**
     * Loads a Product belonging to specified URN.
     * @param urn
     * @return a product as specified by the URN
     * @throws IllegalArgumentException if urn argument is null
     * @throws NoSuchElementException if the urn does not belong to this pool
     * @throws IOException if an IO operation has failed, eg due to the
     * internet connection being broken, or due to a corrupted file system
     * @throws GeneralSecurityException if the URN specified references
     * a product that is not permitted by the user to be accessed.
     */
    public Product loadProduct(String urn) throws IOException, GeneralSecurityException;

    /**
     * Loads the descriptors belonging to specified URN.
     * @param urn
     * @return descriptors associated to the URN
     * @throws IllegalArgumentException if urn argument is null
     * @throws NoSuchElementException if the urn does not belong to this pool
     * @throws IOException if an IO operation has failed, eg due to the
     * internet connection being broken, or due to a corrupted file system
     * @throws GeneralSecurityException if the URN specified references
     * a product that is not permitted by the user to be accessed.
     */
    public Map<String, Object> loadDescriptors(String urn) throws IOException, GeneralSecurityException;

    /**
     * Saves specified product and returns the designated URN.
     * @param product - product to be saved.
     * @return the URN belonging to the stored product.
     * @throws IllegalArgumentException if product argument is null.
     * @throws IOException if an IO operation has failed, eg due to the
     * internet connection being broken, or due to a corrupted file system
     * @throws GeneralSecurityException if it is not permissible
     * for the user to write products to this pool.
     */
    public String save(Product product) throws IOException, GeneralSecurityException;

    /**
     * Saves specified product and returns the designated URN.
     * @param ref - reference to the product to be saved.
     * @return the URN belonging to the stored product.
     * @throws IllegalArgumentException if product argument is null.
     * @throws IOException if an IO operation has failed, eg due to the
     * internet connection being broken, or due to a corrupted file system
     * @throws GeneralSecurityException if it is not permissible
     * for the user to write products to this pool.
     */
    public String save(ProductRef ref) throws IOException, GeneralSecurityException;

    /**
     * Returns a list of references to products that match the specified query.
     * @param query the query object
     * @return the set of references to products matching the supplied query. 
     * In future implementations a set of ProductRefs
     * would be returned instead of URNs.
     * @throws IllegalArgumentException if the query argument is null
     * or in the wrong format.
     * @throws IOException if an IO operation has failed, eg due to the
     * internet connection being broken, or due to a corrupted file system
     * @throws GeneralSecurityException if it is not permissible
     * for the user to query this pool.
     */
    public Set<ProductRef> select(StorageQuery query)
    throws IOException, GeneralSecurityException;

    /**
     * Refines a previous query, given the refined query and result of the
     * previous query.
     * 
     * @param query refined query
     * @param results the results from the previous query
     * @return the set of references to products matching
     * the refined query. 
     * In future implementations a set of ProductBridges
     * would be returned instead of URNs.
     * @throws IllegalArgumentException if the query argument is null
     * or in the wrong format.
     * @throws IOException if an IO operation has failed, eg due to the
     * internet connection being broken, or due to a corrupted file system
     * @throws GeneralSecurityException if it is not permissible
     * for the user to query this pool.
     */
    public Set<ProductRef> select(StorageQuery query, Set<ProductRef> results)
    throws IOException, GeneralSecurityException;

    /**
     * Removes a Product belonging to specified URN.
     * @param urn
     * @return true if the product, specified by the URN, is removed.
     * @throws IllegalArgumentException if urn argument is null
     * @throws NoSuchElementException if the urn does not belong to this pool
     * @throws IOException if an IO operation has failed, eg due to the
     * internet connection being broken, or due to a corrupted file system
     * @throws GeneralSecurityException if the URN specified references
     * a product that is not permitted by the user to be accessed.
     */
    public void remove(String urn) throws IOException, GeneralSecurityException;
}
