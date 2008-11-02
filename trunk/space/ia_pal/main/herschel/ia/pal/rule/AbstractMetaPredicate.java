/* $Id: AbstractMetaPredicate.java,v 1.2 2008/02/26 11:29:10 sguest Exp $
 * Copyright (c) 2008 ESA, STFC
 */

package herschel.ia.pal.rule;
import herschel.ia.pal.ContextRuleException;
import herschel.ia.pal.ProductRef;
import herschel.ia.dataset.MetaData;
import herschel.share.predicate.AbstractPredicate;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * An abstract ProductRef Predicate that provides access to the meta data.
 * @author   S.Guest (RAL/SPIRE)
 */
public abstract class AbstractMetaPredicate extends AbstractPredicate<ProductRef> {

    /**
     * Default constructor.
     */
    protected AbstractMetaPredicate() {
        super();
    }

    public abstract boolean evaluate(ProductRef toProduct);
   
    /**
     * Convenience method to save rule implementations the bother of doing this
     * all the time.
     */
    protected MetaData getMeta (ProductRef ref) {
	try {
	    return ref.getMeta();
	}
	catch (IOException x) {
	    throw new ContextRuleException ("IOException in rule "+this+
					    " while accessing product ref", x);
	}
	catch (GeneralSecurityException x) {
	    throw new ContextRuleException ("Security exception in rule "+this+
					    " while accessing product ref", x);
	}
    }
}
