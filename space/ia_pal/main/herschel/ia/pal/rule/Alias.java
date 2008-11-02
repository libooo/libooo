/* $Id: Alias.java,v 1.4 2008/02/26 11:29:10 sguest Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.rule;

import herschel.ia.pal.ProductRef;
import herschel.ia.dataset.Product;
import herschel.share.predicate.AbstractPredicate;
import herschel.share.predicate.Predicate;

import java.util.Map.Entry;

/**
 * The Alias rule is a map rule imposing a fixed (key,product) combination. Only keys allowed
 * by the rule may be inserted, and products assigned to them must of the specified type. Typically
 * alias rules are combined in multiples so that more than one type can be inserted. The
 * {@link herschel.share.predicate.AnyPredicate} class comes in particularly useful for this.
 * <p>
 * Example usage:
 * <pre>
 * // Java:
 * public class Foo extends Product {}
 * public class Bar extends Foo {}
 *  
 * public class MyMap extends MapContext {
 *    // ...rule=new Alias("x",Foo.class,true) ... strict alias
 *    // ...rule=new Alias("y",Foo.class)      ... relaxed alias
 * }
 * 
 * # Jython:
 * x=Foo()     # see above
 * y=Bar()     # see above
 * z=Product() # default product
 * 
 * context=MyMap()
 * context.map.put("x",ProductRef(x)) # OK
 * context.map.put("x",ProductRef(y)) # ERROR, value(y) is not allowed
 * context.map.put("y",ProductRef(x)) # OK
 * context.map.put("y",ProductRef(y)) # OK
 * context.map.put("y",ProductRef(z)) # ERROR, value(z) is not allowed
 * context.map.put("z",ProductRef(z)) # ERROR, key("y") is not allowed
 * </pre>
 * @author jbakker
 */
public final class Alias extends AbstractPredicate<Entry<String, ProductRef>> {
    private String _alias;
    private Predicate<ProductRef> _value;
    private Class<? extends Product> _clazz;

    /**
      * Create an alias rule.
      * @param alias the map key
      * @param product type that can be assigned to the key, it can also be a subclass of it.
      */
    public Alias(String alias, Class<? extends Product> product) {
	this(alias, product, false);
    }

    /**
      * Create an alias rule with optional subclass strictness.
      * @param alias the map key
      * @param product type that can be assigned to the key
      * @param isStrict if <code>true</code> a product must be exactly the same class as specified,
      *        otherwise it may also be a subclass.
      */
    public Alias(String alias, Class<? extends Product> product, boolean isStrict) {
	if (alias == null)
	    throw new IllegalArgumentException("alias argument must be defined");
	if (product == null)
	    throw new IllegalArgumentException("product argument must be defined");
	_alias = alias;
	_clazz = product;
	_value = isStrict ? new StrictInstanceOf(_clazz) : new InstanceOf(_clazz);
    }

    /* package local accessor */
    String getKey() {
	return _alias;
    }
    
    public boolean evaluate(Entry<String, ProductRef> entry) {
	return _alias.equals(entry.getKey()) && _value.evaluate(entry.getValue());
    }

    /**
      * Return information on this rule as a string.
      * @return a string representation
      */
    public String toString() {
	return Alias.class.getName() + "(\"" + _alias + "\"," + _clazz.getName() + ")";
    }
}
