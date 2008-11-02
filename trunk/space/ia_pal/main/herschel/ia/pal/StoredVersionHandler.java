/*
 * $Id: StoredVersionHandler.java,v 1.3 2008/06/24 15:28:56 pbalm Exp $
 */
package herschel.ia.pal;

import herschel.ia.dataset.Product;
import herschel.ia.pal.query.AttribQuery;
import herschel.ia.pal.versioning.AbstractVersionProduct;
import herschel.ia.pal.versioning.VersionLoader;
import herschel.ia.pal.versioning.VersionTrackProduct;
import herschel.share.util.StackTrace;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Manages versions by storing them in a persistent manner.
 */
class StoredVersionHandler extends VersionHandler {

    @SuppressWarnings("unused")
    private static Logger LOG = Logger.getLogger(StoredVersionHandler.class.getName());

    private interface VersionsFinder {
	List<ProductRef> find(ProductPool pool, ProductRef ref)
	throws IOException, GeneralSecurityException;
    };

    private VersionsFinder _lastVersionFinder = new VersionsFinder() {
	public List<ProductRef> find(ProductPool pool, ProductRef ref)
	throws IOException, GeneralSecurityException {
	    // ProductRef last = pool.getLastVersion(ref);
	    /* BACKWARDS COMPATIBILITY. TODO: remove */
	    ProductRef last = null;
	    try {
		last = pool.getLastVersion(ref);
	    } catch (NoSuchElementException e) {
		SortedMap<Integer, String> versions = getOldTrack(pool, ref.getTrackId());
		String urn = versions.get(versions.lastKey());
		last = new ProductRef(getStorage(), urn);
	    }
	    /* END BACKWARDS COMPATIBILITY */
	    return Arrays.asList(last);
	}
    };

    private VersionsFinder _allVersionsFinder = new VersionsFinder() {
	public List<ProductRef> find(ProductPool pool, ProductRef ref)
	throws IOException, GeneralSecurityException {
	    // List<ProductRef> refs = pool.getVersions(ref);
	    /* BACKWARDS COMPATIBILITY. TODO: remove */
	    List<ProductRef> refs = null;
	    try {
		refs = pool.getVersions(ref);
	    } catch (NoSuchElementException e) {
		SortedMap<Integer, String> versions = getOldTrack(pool, ref.getTrackId());
		refs = new ArrayList<ProductRef>(versions.size());
		for (String urn : versions.values()) {
		    refs.add(new ProductRef(getStorage(), urn));
		}
	    }
	    /* END BACKWARDS COMPATIBILITY */
	    return refs;
	}
    };

    /* BACKWARDS COMPATIBILITY. TODO: remove*/
    private Map<ProductPool, Map<String, SortedMap<Integer, String>>> _tracks =
    new HashMap<ProductPool, Map<String, SortedMap<Integer, String>>>();
    /* END BACKWARDS COMPATIBILITY */

    public StoredVersionHandler(ProductStorage storage) {
	super(storage);
    }

    /**
     * Saves the product with a new version.
     * If the product didn't belong to any track, it associates
     * the product a trackId of the form <productType>_<uniqueNumber>.
     */
    public String save(ProductRef ref)
    throws IOException, GeneralSecurityException {
	return save(ref, true);
    }

    /**
     * Saves the product with a new version.
     * If the product didn't belong to any track, it associates
     * the product a trackId of the form <productType>_<uniqueNumber>.
     */
    public String save(ProductRef ref, boolean findVersion)
    throws IOException, GeneralSecurityException {

	// Auxiliary variables
	ProductPool pool = getStorage().getWritablePool();
	Product product = ref.getProduct();
	String productType = product.getType();

	// Update the versioning info
	if (!ref.hasTrack()) {
	    ref.setTrackId(productType + "_" + System.nanoTime());
	    ref.setVersion(0);
	} else {
	    String allowedType = ref.getTrackId().replaceAll("_[^_]+$", "");
	    if (!allowedType.equals(productType)) {
		throw new IOException("Type " + productType + " not allowed " +
		                      "for version track of product with type "+
		                      allowedType);
	    }
	    int version = 0;
	    if (findVersion) {
		try{version = getLastVersion(ref).getVersion() + 1;}
		catch (NoSuchElementException e) {} // track is not in the pool
	    }
	    ref.setVersion(version);
	}

	// Compute other descriptors if not already done
	ref.initDescriptors();

	// Save the product
	String urn = pool.save(ref);
	product.setOwner(new ProductVersion(getStorage(),
	                                    product,
	                                    ref.getTrackId(),
	                                    ref.getVersion()));
	return urn;
    }

    /**
     * Returns all the versions of the given {@link ProductRef}.
     */
    public List<ProductRef> getVersions(ProductRef ref) throws IOException, GeneralSecurityException {
	return findVersions(ref, _allVersionsFinder);
    }

    public ProductRef getLastVersion(ProductRef ref) throws IOException, GeneralSecurityException {
	return findVersions(ref, _lastVersionFinder).get(0);
    }

    public void removeUrn(String urn) throws IOException, GeneralSecurityException {
	// does nothing, because versions are not cached for the time being
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        try {
            // Collect versions information
            AttribQuery query = new AttribQuery(Product.class, "p", "1", true);
            Map<String, SortedSet<Integer>> tracks = new HashMap<String, SortedSet<Integer>>();
            Set<ProductRef> refs = getStorage().select(query);
            for (ProductRef ref : refs) {
        	// TODO replace by ref.getType().equals(TagsProduct.class)
            	if (AbstractVersionProduct.class.isAssignableFrom(ref.getType())) {
        	    continue;
        	}
        	String trackId = ref.getTrackId();
        	int version = ref.getVersion();
        	SortedSet<Integer> versions = tracks.get(trackId);
        	if (versions == null) {
        	    versions = new TreeSet<Integer>();
        	    tracks.put(trackId, versions);
        	}
        	versions.add(version);
            }

            // Return information
            StringBuffer b = new StringBuffer("\nVersions:\n--------\n");
            for (Map.Entry<String, SortedSet<Integer>> entry : tracks.entrySet()) {
        	String trackId = entry.getKey();
        	SortedSet<Integer> versions = entry.getValue();
        	b.append(trackId).append(" -> ").append(versions).append("\n");
            }
            return b.toString();
        } catch (Exception e) {
            return "***Couldn't get versions info***:\n"+ StackTrace.trace(e);
        }
    }

    /* BACKWARDS COMPATIBILITY. TODO: remove*/
    private SortedMap<Integer, String> getOldTrack(ProductPool pool, String trackId)
    throws IOException, GeneralSecurityException {
	Map<String, SortedMap<Integer, String>> track = _tracks.get(pool);
	if (track == null) {
	    track = new HashMap<String, SortedMap<Integer,String>>();
	    _tracks.put(pool, track);
	}
	SortedMap<Integer, String> versions = track.get(trackId);
	if (versions == null) {
	    VersionTrackProduct vtp = VersionLoader.loadVersionTrack(pool, trackId);
	    if (vtp == null) {
		throw new NoSuchElementException("Track " + trackId + " not found in pool " + pool);
	    }
	    versions = vtp.getVersionMap();
	    track.put(trackId, versions);
	}
	return versions;
    }
    /* END BACKWARDS COMPATIBILITY */

    private List<ProductRef> findVersions(ProductRef ref, VersionsFinder finder)
    throws IOException, GeneralSecurityException {
	String trackId = ref.getTrackId();
	if (trackId == null) {
	    return Collections.emptyList();
	}
	List<ProductRef> refs = null;
	for (ProductPool pool : getStorage().getPools()) {
	    try {
		refs = finder.find(pool, ref);
		break;
	    } catch (NoSuchElementException e) { /* try with the following */ }
	}
	if (refs == null) {
	    String msg = "Track "+ trackId +" not found in storage " + getStorage();
	    throw new NoSuchElementException(msg);
	}
	for (ProductRef r : refs) {
	    r.setStorage(getStorage());
	}
	return refs;
    }
}
