/* $Id: ImmutableMapKey.java,v 1.1 2008/02/26 11:29:10 sguest Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.rule;

import herschel.ia.pal.ProductRef;
import herschel.ia.pal.MapContext;
import herschel.share.predicate.AbstractPredicate;
import java.util.Map.Entry;

/**
 * This rule enforces immutability of entries in a {@link herschel.ia.pal.MapContext}. This means
 * that once an entry has been created, it cannot be overridden with the same key name.
 * @author   S.Guest (RAL/SPIRE)
 */
public class ImmutableMapKey extends AbstractPredicate<Entry<String,ProductRef>> {
    private MapContext context;

    /**
      * Create a rule for the given context.
      * @param  context  to make key immutable
      */
    public ImmutableMapKey (MapContext context) {
	this.context = context;
    }
    
    public boolean evaluate(Entry<String,ProductRef> entry) {
	// Check whether there is already an entry
	return !context.getRefs().containsKey(entry.getKey());
    }
     
    /**
      * Return information on this rule as a string.
      * @return a string representation
      */
    public String toString() {
	return "Keys are immutable in "+context;
    }
}
