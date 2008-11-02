/* $Id: BasicStorageQueryVisitor.java,v 1.4 2008/06/06 13:40:17 jsaiz Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

import herschel.ia.pal.util.ArraySet;
import herschel.ia.pal.query.AttribQuery;
import herschel.ia.pal.query.FullQuery;
import herschel.ia.pal.query.MetaQuery;
import herschel.ia.pal.query.StorageQuery;
import herschel.ia.pal.query.StorageQueryVisitor;

class BasicStorageQueryVisitor implements StorageQueryVisitor {

    private transient Set<ProductRef> _results;

    private transient Set<ProductRef> _previous;

    private transient ProductStorage _storage;

    public BasicStorageQueryVisitor(ProductStorage storage) {
	_storage = storage;
    }

    public void visit(AttribQuery query) throws IOException, GeneralSecurityException {
	handleSelect(query);
    }

    public void visit(MetaQuery query) throws IOException, GeneralSecurityException {
	handleSelect(query);
    }

    public void visit(FullQuery query) throws IOException, GeneralSecurityException {
	handleSelect(query);
    }

    // This dispatches the query to each pool in turn and merges the results
    private void handleSelect(StorageQuery query)
    throws IOException, GeneralSecurityException {
	_results = new ArraySet<ProductRef>(false);
	Set<ProductRef> partialResult;
	for(ProductPool pool : _storage.getPools()) {
	    partialResult = (_previous == null? pool.select(query) :
		                                pool.select(query, _previous));
	    if (_results.isEmpty()) {
		_results = partialResult; // avoid unnecessary copying
	    } else {
		_results.addAll(partialResult);
	    }
	}
    }

    void resetResults() {
	_results = null;
    }

    void resetPrevious() {
	_previous = null;
    }

    Set<ProductRef> getResults() {
	return _results;
    }

    void setPrevious(Set<ProductRef> previous) {
	_previous = previous;
    }

}
