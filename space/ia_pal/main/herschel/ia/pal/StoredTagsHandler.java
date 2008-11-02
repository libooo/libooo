/*
 * $Id: StoredTagsHandler.java,v 1.1 2008/05/30 13:30:25 jsaiz Exp $
 */
package herschel.ia.pal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Manages tags by storing them in a persistent manner.<p>
 * Implementation note: it does not hold any tags map in memory, in order to
 * avoid duplicating information that probably is already hold in the pools.
 */
class StoredTagsHandler extends TagsHandler {

    private ProductStorage _storage;

    StoredTagsHandler(ProductStorage storage) {
	_storage = storage;
    }

    public Set<String> getTags() throws IOException, GeneralSecurityException {
        Set<String> tags = new HashSet<String>();
	for (ProductPool pool : _storage.getPools()) {
	    tags.addAll(pool.getTags());
	}
	return tags;
    }

    public Set<String> getTags(String urn) throws IOException, GeneralSecurityException {
	checkUrn(urn);
	Set<String> tags = new HashSet<String>();
	for (ProductPool pool : _storage.getPools()) {
	    tags.addAll(pool.getTags(urn));
	}
	return tags;
    }

    public String getUrn(String tag) throws IOException, GeneralSecurityException {
	checkTag(tag);
	for (ProductPool pool : _storage.getPools()) {
	    try { return pool.getUrn(tag); }
	    catch (NoSuchElementException e) {} // try the following one
	}
	String message = "tag '" + tag + "' not found in storage " + _storage;
	throw new NoSuchElementException(message);
    }

    public void removeUrn(String urn) throws IOException, GeneralSecurityException {
	checkUrn(urn);
	_storage.getWritablePool().removeUrn(urn);
    }

    public void removeTag(String tag) throws IOException, GeneralSecurityException {
	checkTag(tag);
	_storage.getWritablePool().removeTag(tag);
    }

    public void setTag(String tag, String urn) throws IOException, GeneralSecurityException {
	checkTag(tag);
	checkUrn(urn);
	_storage.getWritablePool().setTag(tag, urn);
    }

    public boolean tagExists(String tag) throws IOException, GeneralSecurityException {
	checkTag(tag);
	for (ProductPool pool : _storage.getPools()) {
	    if (pool.tagExists(tag)) return true;
	}
	return false;
    }
}
