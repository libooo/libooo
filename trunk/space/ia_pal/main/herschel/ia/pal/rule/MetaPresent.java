/* $Id: MetaPresent.java,v 1.2 2008/02/26 11:29:11 sguest Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.rule;

import herschel.ia.pal.ProductRef;

/**
 * Rule to check a metadata item in a {@link ProductRef} is present.
 * @author   S.Guest (RAL/SPIRE)
 */
public class MetaPresent extends AbstractMetaPredicate {

    private String _key;

    /**
      * Create a rule requiring a metadata item to be present.
      * @param  key   metadata name
      */
    public MetaPresent (String key) {
	_key = key;
    }

    /**
      * Get the metadata name aka the key.
      * @return the metadata name
      */
    public String getKey() {
	return _key;
    }

    public boolean evaluate(ProductRef toProduct) {
	return getMeta(toProduct).containsKey(getKey());
    }
    
    /**
      * Return information on this rule as a string.
      * @return a string representation
      */
    public String toString() {
	return "Metadata must contain key \""+getKey()+"\"";
    }
}
