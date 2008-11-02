/* $Id: FullQuery.java,v 1.5 2007/11/07 15:14:50 sguest Exp $
 * Copyright (c) 2007 ESA, NAOC, STFC
 */
package herschel.ia.pal.query;

import herschel.ia.dataset.Product;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author Jorgo Bakker <jbakker@rssd.esa.int>
 *
 * @jhelp A data mining query formulates a query the full interface of a
 * Product.
 * As such, one can query any aspect of a Product.
 * Typically this type of query is quite slow and should be used in combination
 * with query refinements.
 *
 * @jexample Example of a data mining query
 * q=FullQuery(MyProduct.class,"p",  "ANY(p['array'].data<2)")
 * @jcategory 
 *        herschel.ia.pal.query;
 * @jref  herschel.ia.pal.query.urm
 *
 */
public final class FullQuery extends AbstractQuery {
    private static final long serialVersionUID=1l;

    public FullQuery(Class<? extends Product> product, String variable, String where) {
	super(product,variable,where);
    }
    
    public FullQuery(Class<? extends Product> product, String variable, String where, boolean retrieveAllVersions) {
    	super(product, variable, where, retrieveAllVersions);
        }

    public void accept(StorageQueryVisitor visitor) throws IOException,
	GeneralSecurityException {
	visitor.visit(this);
    }
}
