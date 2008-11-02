/*
 * $Id: IgnoreVersionHandler.java,v 1.1 2008/05/30 13:30:25 jsaiz Exp $
 */
package herschel.ia.pal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * Minimal version handler implementation that does essentially nothing other
 * than delegating essential operations to the writable pool.
 */
class IgnoreVersionHandler extends VersionHandler {

    IgnoreVersionHandler(ProductStorage storage) {
	super(storage);
    }

    public void removeUrn(String urn)
    throws IOException, GeneralSecurityException {
	// does nothing
    }

    public String save(ProductRef ref)
    throws IOException, GeneralSecurityException {
	return getStorage().getWritablePool().save(ref);
    }

    public String save(ProductRef ref, boolean findVersion)
    throws IOException, GeneralSecurityException {
	return save(ref);
    }

    public List<ProductRef> getVersions(ProductRef ref) {
	return Arrays.asList(ref); // the only known version is this one
    }

    /**
     * Returns the latest version of the given {@link ProductRef}.
     * @return the ref of the latest version
     */
    public ProductRef getLastVersion(ProductRef ref) {
	return ref; // return the same reference
    }

    @Override
    public String toString() {
	return "No versioning is active";
    }
}
