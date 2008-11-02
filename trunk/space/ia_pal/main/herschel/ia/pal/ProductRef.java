/* $Id: ProductRef.java,v 1.33 2008/08/28 07:23:26 bli Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.ProductOwner;
import herschel.ia.io.serial.SerialArchive;
import herschel.ia.pal.util.HashCoder;
import herschel.ia.pal.util.SizeCalculator;
import herschel.ia.pal.util.UrnUtils;
import herschel.ia.pal.util.Util;
import herschel.share.util.Configuration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A lightweight reference to a product that is stored in a ProductPool or in
 * memory.
 * 
 * @jhelp A ProductRef provides a reference to a product that is held in a product storage or to a
 * product in memory.
 * 
 * Typically a ProductRef is returned by the load, save and select methods of a ProductStorage.
 * 
 * A Product reference is providing a mechanism to inspect the meta data of a stored product without
 * loading the complete product into memory.
 * 
 * @jexample Usage of a product reference p=Product(creator="me") ref=storage.save(p)
 * 
 * print ref.type # herschel.ia.dataset.Product print ref.urn #
 * urn:simple.default:herschel.ia.dataset.Product:23674 print ref.meta['creator'] # me print
 * ref.product.creator # me (product loaded into memory!)
 * 
 * @jcategory herschel.ia.pal;
 * @jref herschel.ia.pal.urm
 * 
 */
public class ProductRef implements Serializable, Comparable {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(ProductRef.class.getName());

    private static final long serialVersionUID = 1L;

    private static final int invalidNumber = -1;

    // Descriptor keys, to be used by pools
    public static final String TRACK_ID   = "trackId";
    public static final String VERSION    = "version";
    public static final String SIZE       = "size";
    public static final String TOTAL_SIZE = "totalSize";
    public static final String HASH       = "hash";

    /* BACKWARDS COMPATIBILITY. TODO: REMOVE */
    @Deprecated
    private transient boolean _tryWithMeta = true;
    /* END BACKWARDS COMPATIBILITY */

    private transient Product _product = null;
    private transient ProductStorage _storage = null;

    // For computing hash codes of products
    private static final HashCoder _hasher = new HashCoder();

    private Class<? extends Product> _type = null;

    private String _urn = null;

    private Map<String, Object> _descriptors = null;

    private void init(ProductStorage storage,
                      Class<? extends Product> type,
                      String urn,
                      Map<String, Object> descriptors) {
	Util.checkNotNull(type, "type");
	Util.checkNotNull(urn, "urn");
	_type = type;
	_urn = urn;
	_storage = storage;
	_descriptors = descriptors;
    }

    /**
     * Constructor to be used by ProductPools.
     */
    public ProductRef(Class<? extends Product> type,
                      String urn,
                      Map<String, Object> descriptors) {
	init(null, type, urn, descriptors);
    }

    /** Constructor to be used by ProductPools. */
    public ProductRef(String urn, Map<String, Object> descriptors) {
	init(null, UrnUtils.getClass(urn), urn, descriptors);
    }

    /** Package private constructor. */
    ProductRef(ProductStorage storage, String urn) {
	init(storage, UrnUtils.getClass(urn), urn, null);
    }

    /**
     * Creates a reference for a product in memory.
     * @throws IllegalArgumentException if the product is null.
     */
    public ProductRef(Product product) {
	Util.checkNotNull(product, "product");
	_product = product;
	_type = _product.getClass();
	_tryWithMeta = false;

	if (_product instanceof Context) {
	    _urn = ((Context)_product).getUrn();
	}
	ProductOwner owner = product.getOwner();
	if (owner instanceof ProductVersion) {
	    ProductVersion productVersion = (ProductVersion)owner;
	    setTrackId(productVersion.getTrackId());
	    setVersion(productVersion.getVersion());
	    setStorage(productVersion.getStorage());
	}

	// Set needed descriptors
	initDescriptors();
    }

    /**
     * Sets the descriptors manually.
     */
    void setDescriptors(Map<String, Object> descriptors) {
	_descriptors = descriptors;
	_tryWithMeta = false;
    }

    /**
     * Returns the descriptors associated to the product.
     * To be used only by ProductPools.
     * @return a copy of the descriptors map, so any external change does not
     * affect internal data.
     */
    public Map<String, Object> getDescriptors() {
	if (_descriptors == null) {
	    _descriptors = loadDescriptors();
	}
	return _descriptors;
    }

    private Map<String, Object> loadDescriptors() {
	try {
	    return (_storage != null && _urn != null)?
	            _storage.loadDescriptors(_urn):new HashMap<String,Object>();
	} catch (IOException e) {
	    String message = "Error when loading descriptors of " + _urn;
	    logger.severe(message + ": " + e);
	    throw new IllegalStateException(message, e);
	} catch (GeneralSecurityException e) {
	    String message = "Error when loading descriptors of " + _urn;
	    logger.severe(message + ": " + e);
	    throw new IllegalStateException(message, e);
	}
    }

    /**
     * Sets the product manually.
     */
    void setProduct(Product product) {
	_product = product;
    }

    /**
     * Get the product that this reference points to.<p>
     * If the product is a Context, it is kept internally, so further accesses
     * don't need to ask the storage for loading it again.<br/>
     * Otherwise, the product is returned but the internal reference remains
     * null, so every call to this method involves a request to the storage.<p>
     * This way, heavy products are not kept in memory after calling this
     * method, thus maintaining the ProductRef a lighweight reference to the
     * target product.<p>
     * In case of a Context, if it is wanted to free the reference,
     * call {@link #unload()}.
     * @return the product
     * @see #unload()
     * @see #isLoaded()
     * @throws IOException
     * if an IO operation has failed, eg due to the internet connection being
     * broken, or due to a corrupted file system
     * @throws GeneralSecurityException
     * if it is not permissable for that user to remove a product of that urn
     * from the pool.
     * @jhelp Returns the Product class to which this Product reference is pointing to.
     */
    public Product getProduct() throws IOException, GeneralSecurityException {

	if (_product != null) {
	    return _product;
	}
	Product product = loadProduct();
	if (product instanceof Context) {
	    _product = product;
	}
	return product;
    }

    private Product loadProduct() throws IOException, GeneralSecurityException {
	checkConnected();
	return _storage.loadProduct(this);
    }

    /**
     * Get the metadata of the product pointed to by this reference.
     * @return the metadata
     */
    public MetaData getMeta() throws IOException, GeneralSecurityException {
	MetaData meta = (_product != null)? _product.getMeta() : loadMeta();
	/* BACKWARDS COMPATIBILITY. TODO: REMOVE */
	if (meta.containsKey("versionTrack")) {
	    String trackId = (String)meta.remove("versionTrack").getValue();
	    int version = ((Long)meta.remove("versionNumber").getValue()).intValue();
	    setTrackId(trackId);
	    setVersion(version);
	}
	/* END BACKWARDS COMPATIBILITY */
	return meta;
    }

    private MetaData loadMeta() throws IOException, GeneralSecurityException {
	checkConnected();
	return _storage.loadMeta(_urn);
    }

    /**
     * Returns the product storage associated.
     * @throws NullPointerException if there is no storage connected.
     */
    public ProductStorage getStorage() {
//        if (_storage == null)
//            throw new NullPointerException("no storage connected!");
        return _storage;
    }

    void setStorage(ProductStorage storage) {
	_storage = storage;
    }

    /**
     * Returns the Uniform Resource Name (URN) of the product.<p>
     * It may be <code>null</code> if the product does not come from a storage,
     * e.g. a newly created product.
     */
    public String getUrn() {
	return _urn;
    }

    /**
     * Sets the Uniform Resource Name (URN) of this proxy.
     * Package private operation.
     */
    void setUrn(String urn) {
	_urn = urn;
    }

    /**
     * Specifies the Product class to which this Product reference is pointing to.
     * 
     * @jhelp Returns the Product class to which this Product reference is pointing to.
     * @jalias type
     */
    public Class<? extends Product> getType() {
        return _type;
    }

    /**
     * Returns a code number for the product; actually its MD5 signature.
     * This allows checking whether a product already exists in a pool or not.
     */
    public String getHash() {
	return (String)getDescriptors().get(HASH);
    }

    void setHash(String hash) {
	getDescriptors().put(HASH, hash);
    }

    /**
     * Returns the estimated size of the product in memory.
     * Useful for providing this information for a user that wants to download
     * the product from a remote site.
     * @throws NullPointerException if the size is not defined yet.
     */
    public long getSize() {
	Object size = getDescriptors().get(SIZE);
	if (size == null) {
	    throw new NullPointerException("Size of " + _urn + " is unknown");
	}
	return (Long)size;
    }

    void setSize(long size) {
	getDescriptors().put(SIZE, size);
    }

    /**
     * Returns the total estimated size of the product in memory, including
     * its children if the product derives from {@link Context}.
     * @throws NullPointerException if the total size is not defined yet.
     */
    public long getTotalSize() {
	Object totalSize = getDescriptors().get(TOTAL_SIZE);
	if (totalSize == null) {
	    throw new NullPointerException("Total size of " + _urn + " is unknown");
	}
	return (Long)totalSize;
    }

    void setTotalSize(long totalSize) {
	getDescriptors().put(TOTAL_SIZE, totalSize);
    }

    /**
     * Returns the identifier of the track to which this product belongs.
     * It may be <code>null</code> if the product does not come from a storage,
     * e.g. a newly created product.
     */
    public String getTrackId() {
	/* BACKWARDS COMPATIBILITY. TODO: REMOVE */
	if (!getDescriptors().containsKey(TRACK_ID) && _tryWithMeta) {
	    try {
		getMeta();  // initializes versioning descriptors
	    } catch (Exception e) {}
	    _tryWithMeta = false;
	}
	/* END BACKWARDS COMPATIBILITY */
	return (String)getDescriptors().get(TRACK_ID);
    }

    void setTrackId(String trackId) {
	getDescriptors().put(TRACK_ID, trackId);
    }

    /**
     * Returns the product version within the track, or -1 if it is not defined.
     */
    public int getVersion() {
	/* BACKWARDS COMPATIBILITY. TODO: REMOVE */
	if (!getDescriptors().containsKey(VERSION) && _tryWithMeta) {
	    try {
		getMeta();  // initializes versioning descriptors
	    } catch (Exception e) {}
	    _tryWithMeta = false;
	}
	/* END BACKWARDS COMPATIBILITY */
	Object version = getDescriptors().get(VERSION);
	if (version == null) {
	    if (_urn!= null) {
		logger.warning("Unknown version for " + _urn);
	    }
	    return invalidNumber;
	}
	return (Integer)version;
    }

    void setVersion(int version) {
	getDescriptors().put(VERSION, version);
    }

    /**
     * Determines whether this ref is dirty with respect to the given storage.
     * <p>Note that the storage could differ from that which the ProductRef
     * is associated with.</p>
     * <p>This method may be of use for classes extending {@link Context}.</p>
     */
    boolean isDirty(ProductStorage storage) throws IOException, GeneralSecurityException {

	if (_product != null && _product instanceof Context) {
	    Context c = (Context) _product;
	    return c.isDirty(storage);
	}

	String urn = getUrn();

	if (urn == null) {
	    return true;
	}

	return (!storage.exists(urn));
    }

    /**
     * Returns whether the referenced product has a track or not.
     */
    public boolean hasTrack() {
	return getTrackId() != null;
    }

    /**
     * Re-implemented for internal reasons.
     */
    public int hashCode() {
	int id = 17; // arbitrary start value
	id = 37 * id + _type.hashCode();
	if (_urn != null)
	    id = 37 * id + _urn.hashCode();
	if (_product != null)
	    id = 37 * id + _product.hashCode();
	return id;
    }

    /**
     * Re-implemented for internal reasons.
     * @return <code>true</code> if o is a non-null ProductRef, with the same Product type than
     * this one, and:<br>
     * <ul>
     * <li>urns and products are null in both refs, or</li>
     * <li>urns are null in both refs, and their products are equal, or</li>
     * <li>urns and products are equal in both refs</li>
     * </ul>
     */
    public boolean equals(Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null) {
	    return false;
	}
	if (!(o instanceof ProductRef)) {
	    return false;
	}
	ProductRef rhs = (ProductRef) o;
	if (!_type.equals(rhs._type)) {
	    return false;
	}
	if (_urn == null && rhs._urn == null) {
	    return (_product == null && rhs._product == null)
		   || (_product != null && _product.equals(rhs._product));
	}
	return (_urn != null && _urn.equals(rhs._urn));
    }

    /**
     * Provide information on this object as a string.
     * The exact format of this string is subject to change without notification.
     * @return information as a string
     */
    public String toString() {
	return (_urn == null? "[no urn]" : _urn);
    }

    /**
     * Frees internal reference to the product, so it can be garbage
     * collected.
     */
    public void unload() {
	_product = null;
    }

    /** Informs whether the pointed product is already loaded. */
    public boolean isLoaded() {
	return _product != null;
    }

    /** Informs whether the descriptors have been already loaded. */
    boolean hasLoadedDescriptors() {
	return _descriptors != null;
    }

    /** Initializes descriptors other than versioning, if not already done. */
    void initDescriptors() {

	boolean hashComputed = (getDescriptors().get(HASH) != null);
	boolean sizeComputed = (getDescriptors().get(SIZE) != null);
	String msg;

	if (hashComputed && sizeComputed) {
	    return;
	}

	Product product = _product;
	if (product == null) {
	    try {
		product = loadProduct();
	    } catch (Exception e) {
		msg = "Could not update descriptors for " + _urn + ": " + e;
		logger.warning(msg);
		return;
	    }
	}

	if (!hashComputed) {
	    setHash(_hasher.getHash(product));
	}

	if (!sizeComputed) {

	    // Size of the product, without children
//	    long mySize = ObjectProfiler.sizeof(product);		long mySize = SizeCalculator.sizeOf(product);
	    setSize(mySize);

	    // For Contexts, total size includes the (total) sizes of children
	    long totalSize = mySize;
	    if (_product instanceof Context) {
		for (ProductRef childRef : ((Context)_product).getAllRefs()) {
//		    // If it's loaded, it was already counted by the ObjectProfiler
//			new SizeCalculator does not count the childRefs, no matter they are loaded or not
//		    if (!childRef.isLoaded()) {
			try {
			    totalSize += childRef.getTotalSize();
			} catch (NullPointerException e) {
			    msg = "While computing total size of Context, "
				+ "found referenced product with total size "
				+ "not set: "+e+"\nContext: " + _urn
				+ "\nReferenced product: "
				+ childRef.getUrn();
			    boolean shouldWran = Configuration
				.getBoolean("hcss.ia.pal.sizecalculation.depresswarning");
			    if(shouldWran){
			    	logger.warning(msg);
			    }else{
			    	logger.fine(msg);
			    }
			}
//		    }
		}
	    }
	    setTotalSize(totalSize);
	}
    }

    public int compareTo(Object o) {
	if (o == null) {
	    throw new NullPointerException();
	}

	if (!(o instanceof ProductRef)) {
	    throw new ClassCastException();
	}

	ProductRef ref = (ProductRef) o;
	if (ref.equals(this)) {
	    return 0;
	}

	if (ref._urn == null) {
	    if (this._urn != null) {
		return +1;
	    }

	    return this._product.compare(ref._product);
	}

	if (this._urn == null) {
	    return -1; // ref._urn is not null
	}

	return this._urn.compareTo(ref._urn);
    }

    private void checkConnected() {
	if (_storage == null) {
	    throw new NullPointerException("no storage connected!");
	}
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();
	SerialArchive.createOutputStream(out).writeObject(_product);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();
	_product = (Product)SerialArchive.createInputStream(in).readObject();
    }
}
