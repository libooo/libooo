/* $Id: ImmutableAlias.java,v 1.2 2008/09/17 08:23:52 bli Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.rule;

import herschel.ia.pal.MapContext;
import herschel.ia.pal.ProductRef;
import herschel.share.predicate.AbstractPredicate;
import herschel.share.predicate.Predicate;

import java.util.Map.Entry;

/**
 * This rule is a combination of the {@link Alias} and {@link ImmutableMapKey} rules.
 * <p>
 * Example usage:
 * <pre>
 * // Java:
 * public class Foo extends Product {}
 * 
 * public class MyMap extends MapContext {
 *    // ...new ImmutableAlias(new Alias("x",Foo.class),this) ... added
 *    // ...new Alias("y",Foo.class) ... added
 * }
 * 
 * # Jython:
 * x=Foo(description="Hello")     # see above
 * y=Foo(description="Hi")        # see above
 * z=Product()
 * 
 * context=MyMap()
 * context.map.put("x",ProductRef(z)) # ERROR, wrong Product class
 * context.map.put("x",ProductRef(x)) # OK
 * context.map.put("x",ProductRef(y)) # ERROR, already exists
 * context.map.put("y",ProductRef(x)) # OK
 * context.map.put("y",ProductRef(y)) # OK, overwritten by new value
 * </pre>
 * @see Alias
 * @see ImmutableMapKey
 * @author jbakker
 */
public final class ImmutableAlias extends AbstractPredicate<Entry<String, ProductRef>> {

    private Predicate<Entry<String, ProductRef>> _rule;

    /**
      * Create a rule for the given context.
      * @param  alias    rule to apply
      * @param  context  to make key immutable
      */
    public ImmutableAlias (Alias alias, MapContext context) {
	if (alias == null)
	    throw new IllegalArgumentException("alias argument must be defined");
	if (context == null)
	    throw new IllegalArgumentException("context argument must be defined");
	
	_rule = alias.and (new ImmutableMapKey(context));
	
    }

    public boolean evaluate(Entry<String, ProductRef> entry) {
	return _rule.evaluate(entry);
    }

    /**
      * Return information on this rule as a string.
      * @return a string representation
      */
    public String toString() {
	return ImmutableAlias.class.getName() + " for " + _rule;
    }
}
