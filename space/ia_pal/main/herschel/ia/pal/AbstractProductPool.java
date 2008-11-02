// -*-java-*-
//
// File:      AbstractProductPool.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Feb 29, 2008
// Usage:     -
// Info:      -

package herschel.ia.pal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import herschel.ia.dataset.Product;
import herschel.ia.pal.query.AttribQuery;
import herschel.ia.pal.util.UrnUtils;
import herschel.ia.pal.versioning.TagsProduct;

/**
 * This class provides default implementations for the product pools.
 */
public abstract class AbstractProductPool implements ProductPool {

    private static Logger LOGGER = Logger.getLogger(AbstractProductPool.class.getName());

    private Map<String, String> _tags = null;

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
    public String save(Product product) throws IOException, GeneralSecurityException {
	return save(new ProductRef(product));
    }

    /**
     * Returns a reference to a product belonging to specified URN.
     * The descriptors are initialized, so there will be no need for further
     * accesses for loading them.
     * @param urn
     * @return a product as specified by the URN
     * @throws IllegalArgumentException if urn argument is null
     * @throws NoSuchElementException if the urn does not belong to this pool
     * @throws IOException if an IO operation has failed, eg due to the
     * internet connection being broken, or due to a corrupted file system
     * @throws GeneralSecurityException if the URN specified references
     * a product that is not permitted by the user to be accessed.
     */
    public ProductRef load(String urn) throws IOException, GeneralSecurityException {
	
    	if (!exists(urn)) {
			throw new NoSuchElementException(
					"Requesting access to non existing item: " + urn);
		}
    	return new ProductRef(urn, loadDescriptors(urn));
    }

    /**
     * Get all the tags defined in the pool.
     * @return set of tags
     */
    public Set<String> getTags() throws IOException, GeneralSecurityException {
	checkTags();
	return new HashSet<String>(_tags.keySet()); // HashMap$KeySet is not serializable
    }

    /**
     * Get all the tags that map to a given URN.
     * @param  urn to look for
     * @return set of tags
     */
    public Set<String> getTags(String urn) throws IOException, GeneralSecurityException {
	checkTags();
	Set<String> tags = new HashSet<String>();
	for (Map.Entry<String, String> entry : _tags.entrySet()) {
	    if (entry.getValue().equals(urn)) {
		tags.add(entry.getKey());
	    }
	}
	return tags;
    }

    /**
     * Get the URN corresponding to the given tag.
     * @param  tag to look for
     * @return the URN
     * @throws NoSuchElementException if the tag is not found
     */
    public String getUrn(String tag) throws IOException, GeneralSecurityException {
	LOGGER.fine("getUrn " + tag);
	checkTags(tag);
	String urn = _tags.get(tag);
	if (urn == null) {
	    String message = "tag '" + tag + "' not found in pool " + getId();
	    throw new NoSuchElementException(message);
	}
	return urn;
    }

    /**
     * Remove the given tag.
     * @param  tag to remove
     */
    public void removeTag(String tag) throws IOException, GeneralSecurityException {
	checkTags(tag);
	if (_tags.remove(tag) != null) {
	    saveTags(_tags);
	}
    }

    /**
     * Remove the given urn from the tag-urn map.
     * @param  urn to remove
     */
    public void removeUrn(String urn) throws IOException, GeneralSecurityException {
	checkTags();
	Set<String> tagsToBeRemoved = new HashSet<String>();
	for (Map.Entry<String, String> entry : _tags.entrySet()) {
	    if (urn.equals(entry.getValue())) {
		tagsToBeRemoved.add(entry.getKey());
	    }
	}
	for (String tag : tagsToBeRemoved) {
	    _tags.remove(tag);
	}
	if (!tagsToBeRemoved.isEmpty()) {
	    saveTags(_tags);
	}
    }

    /**
     * Sets the specified tag to the given URN.
     * @param tag to set
     * @param urn to set it to
     */
    public void setTag(String tag, String urn) throws IOException, GeneralSecurityException {
	checkTags();
	_tags.put(tag, urn);
	saveTags(_tags);
    }

    /**
     * Tests if a tag exists.
     * @param  tag to check
     * @return <code>true</code> if it does
     */
    public boolean tagExists(String tag) throws IOException, GeneralSecurityException {
	checkTags();
	return _tags.containsKey(tag);
    }

    /** Loads the tags from a TagsProduct. Can be overridden if needed. */
    protected String saveTags(Map<String, String> tags)
    throws IOException, GeneralSecurityException {
        TagsProduct tagsProduct = new TagsProduct(tags);
        return save(tagsProduct);
    }

    /** Saves the tags in a TagsProduct. Can be overridden if needed. */
    protected Map<String, String> loadTags()
    throws IOException, GeneralSecurityException {

	// Get the urn of the TagsProduct
	String urn = null;
        AttribQuery query = new AttribQuery(TagsProduct.class, "p", "1", true);
        Set<ProductRef> refs = select(query);
        for (ProductRef ref : refs) {
            String newUrn = ref.getUrn();
            urn = (urn == null)? newUrn : UrnUtils.getLater(urn, newUrn);
        }
        if (urn == null) {
            return new HashMap<String, String>();
        }

        // Load the TagsProduct and return the information from it
        TagsProduct tagsProduct = (TagsProduct)loadProduct(urn);
        return tagsProduct.getTagUrnMap();
    }

    /** Loads the tags info if needed */
    private void checkTags() throws IOException, GeneralSecurityException {
	if (_tags == null || ProductStorage.isSharedMode()) {
	    _tags = loadTags();
	}
    }

    /** Loads the tags info if needed, and checks that the given tag exists */
    private void checkTags(String tag) throws IOException, GeneralSecurityException {
	checkTags();
	if (!_tags.containsKey(tag)) {
	    String message = "Tag not found in pool " + getId() + ": " + tag;
	    throw new NoSuchElementException(message);
	}
    }
}
