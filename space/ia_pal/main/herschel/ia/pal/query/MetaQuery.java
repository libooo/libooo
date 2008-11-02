/* $Id: MetaQuery.java,v 1.5 2007/11/07 15:14:50 sguest Exp $
 * Copyright (c) 2007 ESA, NAOC, STFC
 */
package herschel.ia.pal.query;

import herschel.ia.dataset.Product;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author Jorgo Bakker <jbakker@rssd.esa.int>
 * 
 * @jhelp Meta data query formulates a query on the meta data of a Product.
 *        Typically this type of query is slower than an Attribute Query, but
 *        faster than a full query on the Product Access Layer.
 * 
 * @jexample Example of an query on meta data q=MetaQuery(MyProduct.class,"p",
 *           "p.meta['creator'].value == 'Me'")
 * @jcategory herschel.ia.pal.query;
 * @jref herschel.ia.pal.query.urm
 * 
 */
public final class MetaQuery extends AbstractQuery {
	private static final long serialVersionUID = 1l;

	public MetaQuery(Class<? extends Product> product, String variable, String where) {
		super(product, variable, where);
	}

	public MetaQuery(Class<? extends Product> product, String variable, String where,
			boolean retrieveAllVersions) {
		super(product, variable, where, retrieveAllVersions);
	}

	public void accept(StorageQueryVisitor visitor) throws IOException,
			GeneralSecurityException {
		visitor.visit(this);
	}
}
