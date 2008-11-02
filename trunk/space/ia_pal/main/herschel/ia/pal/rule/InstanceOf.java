/* $Id: InstanceOf.java,v 1.4 2008/02/26 11:29:11 sguest Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.rule;

import herschel.ia.dataset.Product;
import herschel.ia.pal.ProductRef;
import herschel.share.predicate.AbstractPredicate;

/**
 * The "instance of" rule is a list rule that ensures that items inserted into the context are
 * a particular type of product, or a subclass of it. 
 *
 * <pre>
 * class Foo extends Product {
 * }
 * class Bar extends Foo {
 * }
 * 
 * Product x = new Product();
 * Product y = new Foo();
 * Product z = new Bar();
 * Predicate&lt;ProductRef&gt; predicate = new InstanceOf(Foo.class);
 * 
 * predicate.evaluate(new ProductRef(x)); // false
 * predicate.evaluate(new ProductRef(y)); // true
 * predicate.evaluate(new ProductRef(z)); // true
 * </pre>
 *
 * @see StrictInstanceOf
 * @author S.Guest (RAL/SPIRE)
 */
public class InstanceOf extends AbstractPredicate<ProductRef> {
    private Class<? extends Product> _clazz;

    /**
      * Create a rule that entries must be of a given type.
      * @param clazz  the class of products that may be entered
      */
    public InstanceOf (Class<? extends Product> clazz) {
	if (clazz == null) throw new IllegalArgumentException("clazz argument must be defined");
	_clazz = clazz;
    }

    public boolean evaluate(ProductRef ref) {
	return _clazz.isAssignableFrom(ref.getType());
    }

    /**
      * Return information on this rule as a string.
      * @return a string representation
      */
    public String toString() {
	return "Class or subclass of " + _clazz.getName();
    }
}
