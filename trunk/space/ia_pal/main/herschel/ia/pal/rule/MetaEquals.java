/* $Id: MetaEquals.java,v 1.2 2008/02/26 11:29:11 sguest Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.rule;

import herschel.ia.dataset.Parameter;
import herschel.ia.pal.ProductRef;

/**
 * Rule to check the metadata in a {@link ProductRef} contains specified (key,value) entry.
 * @author   S.Guest (RAL/SPIRE)
 */
public class MetaEquals extends MetaPresent {

    private Object _value;

    /**
      * Create a rule requiring a metadata item to be present <em>and</em> set to the given value.
      * @param  key   metadata name
      * @param  value metadata value
      */
    public MetaEquals (String key, Object value) {
	super (key);
	_value = value;
    }

    /**
      * Get the metadata value.
      * @return the metadata value
      */
    public Object getValue() {
	return _value;
    }

    public boolean evaluate (ProductRef toProduct) {
	if (!super.evaluate(toProduct)) return false;
	Parameter p=getMeta(toProduct).get(getKey());
	return p.hasValue() && p.getValue().equals(_value);
    }
    
    /**
      * Return information on this rule as a string.
      * @return a string representation
      */
    public String toString() {
	return "Metadata must contain (key,value)=("+getKey()+","+_value+")";
    }
}
