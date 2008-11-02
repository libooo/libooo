// -*-java-*-
//
// File:      ProductVersion.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Feb 25, 2008
// Usage:     -
// Info:      -

package herschel.ia.pal;

import herschel.ia.dataset.Product;
import herschel.ia.dataset.ProductOwner;

/**
 * This class provides versioning information for a product.
 */
class ProductVersion implements ProductOwner {

    private ProductStorage _storage;
    private Product _product;
    private String _trackId;
    private int _version;

    /** Constructor. */
    ProductVersion(ProductStorage storage,
                   Product product,
                   String trackId,
                   int version) {
	_storage = storage;
	_product = product;
	_trackId = trackId;
	_version = version;
    }

    /** Returns the storage from which the product comes. */
    public ProductStorage getStorage() {
	return _storage;
    }

    /** Returns the referred product. */
    public Product getProduct() {
	return _product;
    }

    /** Returns the version track of the associated product. */
    public String getTrackId() {
        return _trackId;
    }

    /** Returns the product's version within the track. */
    public int getVersion() {
        return _version;
    }
}
