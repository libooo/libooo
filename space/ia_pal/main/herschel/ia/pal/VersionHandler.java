/* $Id: VersionHandler.java,v 1.5 2008/05/30 13:30:25 jsaiz Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Manages versions for a given product storage.
 */
abstract class VersionHandler implements Versionable {

    private ProductStorage _storage;

    VersionHandler(ProductStorage storage) {
	_storage = storage;
    }

    ProductStorage getStorage() {
	return _storage;
    }

    /** Save a product, and look for the version number if requested */
    abstract String save(ProductRef ref, boolean findVersion)
    throws IOException, GeneralSecurityException;
}
