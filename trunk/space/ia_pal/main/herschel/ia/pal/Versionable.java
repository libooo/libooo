/* $Id: Versionable.java,v 1.2 2008/03/31 15:59:16 jsaiz Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Definition of services provided by a product storage supporting versioning.
 * @author S.Guest RAL/SPIRE
 * @version November 2007 Original
 */
public interface Versionable {

    /**
     * Remove the versioning information of the given URN.
     * @param urn URN to be removed
     */
    void removeUrn(String urn) throws IOException, GeneralSecurityException;

    /**
     * Save the product referenced.
     * @param ref reference to the product to save
     * @return the URN
     */
    String save(ProductRef ref) throws IOException, GeneralSecurityException;

    /**
     * Returns all the versions of the given {@link ProductRef}.
     * @param ref product reference return collection of all versions as URNs
     * @throws NoSuchElementException if ref is not found in the storage.
     */
    List<ProductRef> getVersions(ProductRef ref) throws IOException, GeneralSecurityException;

    /**
     * Returns the latest version of the given {@link ProductRef}.
     * @return the URN of the latest version
     * @throws NoSuchElementException if ref is not found in the storage.
     */
    ProductRef getLastVersion(ProductRef ref) throws IOException, GeneralSecurityException;
}