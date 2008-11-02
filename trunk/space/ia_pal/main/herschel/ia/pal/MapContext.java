/* $Id: MapContext.java,v 1.26 2008/07/22 11:03:42 pbalm Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import herschel.ia.pal.util.ArraySet;
import herschel.ia.dataset.Column;
import herschel.ia.dataset.Dataset;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.TableDataset;
import herschel.ia.numeric.String1d;
import herschel.share.predicate.AbstractPredicate;
import herschel.share.predicate.Predicate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Groups {@link herschel.ia.dataset.Product}s into a {@link #getRefs() map}
 * of (String,ProductRef) pairs. 
 * <p>New entries can be added if they comply to the
 * {@link #getAddingRule() adding rules} of this context.
 * The default behaviour is to allow adding any (String,ProductRef).</p>
 * 
 * <p>An example:
 * <pre>
 *    # Jython:
 *    # (java class ImageProduct extends Product)
 *    # (java class SpectrumProduct extends Product)
 *    image     = ImageProduct(description=&quot;hi&quot;)
 *    spectrum  = SpectrumProduct(description=&quot;there&quot;)
 *    simple    = Product(description=&quot;everyone&quot)
 *    
 *    context=ProductMap()
 *    context.refs.put(&quot;x&quot;,ProductRef(image))
 *    context.refs.put(&quot;y&quot;,ProductRef(spectrum))
 *    context.refs.put(&quot;z&quot;,ProductRef(simple))
 *    print context.refs.size() # 3
 *    print context.refs.get('x').product.description # hi
 *    print context.refs.get('y').product.description # there
 *    print context.refs.get('z').product.description # everyone
 * </pre>
 * </p>
 * 
 * <p>
 * It is possible to insert a ProductRef at a specific key in the MapContext.
 * The same insertion behaviour is followed as for a java {@link java.util.Map},
 * in that if there is already an existing ProductRef for the given key, that ProductRef is replaced
 * with the new one:
 * 
 * <pre>
 *     product4=SpectrumProduct(description=&quot;everybody&quot;)
 *     context.refs.add("y", ProductRef(product4))
 *     product5=SpectrumProduct(description=&quot;here&quot;)
 *     context.refs.add("a", ProductRef(product5))
 *     
 *     print context.refs.get('x').product.description # hi
 *     print context.refs.get('y').product.description # everybody
 *     print context.refs.get('z').product.description # everyone
 *     print context.refs.get('a').product.description # here
 * </pre>

 * 
 * <p>
 * It is possible to restrict what can be added to a MapContext by extending
 * MapContext and override the {#link #getAddingRule()} method. For example:</p>
 * <p>
 * 
 * <pre>
 *      // Java:
 *      // A MapContext that allows ImageProduct and SpectrumProduct products only 
 *      class ImageSpectrumProducts extends MapContext {
 *        private static Predicate&lt;Map.Entry&lt;String,ProductRef&gt;&gt; _RULE=
 *        new AbstractPredicate&lt;Map.Entry&lt;String,ProductRef&gt;&gt;() {
 *            public boolean evaluate(Map.Entry&lt;String,ProductRef&gt; pair) {
 *                return ImageProduct.class.equals(pair.getValue().getType()) ||
 *                       SpectrumProduct.class.equals(pair.getValue().getType());
 *            }
 *        };
 *    
 *        protected Predicate&lt;Map.Entry&lt;String,ProductRef&gt;&gt; getAddingRule() {
 *            return _RULE;
 *        }
 *    }
 *    
 *    # jython:
 *    context=ImageSpectrumProducts()
 *    context.refs.put(&quot;x&quot;,ProductRef(ImageProduct()))
 *    context.refs.put(&quot;y&quot;,ProductRef(SpectrumProduct()))
 *    context.refs.put(&quot;z&quot;,ProductRef(Product())) # ERROR!
 * </pre>
 * 
 * <p>
 * Note that the <em>adding rules</em> are only applied when <em>putting</em>
 * an entry to the map!
 * 
 * <p>
 * <b>Be aware</b> that
 * 
 * <ol>
 * <li>the put() method of the map view may throw a ContextRuleException if the
 * data added to the context violates the rules applied to the context.</li>
 * </li>
 * <li>the put() method of the map view may throw a IllegalArgumentException if
 * either of the arguments to the put() method are null.</li>
 * </li>
 * </p>
 * 
 * @author DP Development Team
 * 
 * @jhelp Groups products (or other Contexts that in turn group products) in a
 *        map-like structure. Products grouped in a MapContext can be
 *        subsequently retrieved by key, through the 'refs' method.
 * 
 * Note: users may be confused as to why there is a need to have to go though a
 * 'refs' method to add or access products from a MapContext. This is due to a
 * technical limitation in the design which will be addressed in due course.
 * 
 * @jcategory herschel.ia.pal;
 * 
 * @jref herschel.ia.pal.urm
 * 
 * @jexample Adding a product to a MapContext 
 * product = Product()
 * ref = storage.save(product) # the ref is a ProductRef object
 * mapcontext = MapContext() 
 * mapcontext.refs.put("john",ref)
 * 
 * @jexample Getting a product from a MapContext
 * ref_john = mapcontext.refs.get("john")
 * product = ref_john.product
 * 
 * @jexample Saving a MapContext to ProductStorage (same way as any other product)
 * ref_context = storage.save(mapcontext)
 * 
 */
public class MapContext extends Context {

    @SuppressWarnings("unused")
    private static Logger _LOGGER = Logger.getLogger(MapContext.class.getName());

    /**
     * Private implementation of a map where Rules of this Context are applied.
     * <p>
     * <em>Strictly speaking, this class should be private. However, due to a bug
     * in jython, it must be publicly visible</em>
     * 
     * @author jbakker
     */
    public static final class Internal extends AbstractMap<String, ProductRef> {
	private Map<String, ProductRef> map;

	private MapContext parent;

	/**
	 * Creates an map implementation that is coupled to the MapContext in
	 * question.
	 * 
	 * @param context
	 */
	Internal(MapContext context) {
	    map = new HashMap<String, ProductRef>();
	    parent = context;
	}

	/**
	 * Re-implemented for internal reasons, respecting the general contract
	 * as specified by the method in the Map interface.
	 */
	@Override
	public Set<Entry<String, ProductRef>> entrySet() {
	    return map.entrySet();
	}

	/**
	 * Re-implemented for internal reasons, respecting the general contract
	 * as specified by the method in the Map interface and marking the
	 * parent as dirty if this key was found.
	 */
	public ProductRef remove(String key) {
	    if (key == null)
		throw new IllegalArgumentException("key argument must be defined");

	    if (!map.containsKey(key))
		return null;

	    ProductRef ref = map.remove(key);
	    if (ref != null)
		parent.refsChanged();
	    return ref;
	}

	private static class MyEntry implements Map.Entry<String, ProductRef> {
	    private String _key;

	    private ProductRef _value;

	    public MyEntry(String key, ProductRef value) {
		_key = key;
		_value = value;
	    }

	    public String getKey() {
		return _key;
	    }

	    public ProductRef setValue(ProductRef value) {
		ProductRef old = _value;
		_value = value;
		return old;
	    }

	    public ProductRef getValue() {
		return _value;
	    }
	}

	/**
	 * Re-implemented to allow for testing against the rules of the parent's
	 * context and marking the parent as dirty if the rules are matched.
	 * 
	 * @throws ContextRuleException
	 *             when the arguments do not map any rule.
	 */
	public ProductRef put(String key, ProductRef ref) {
	    if (key == null)
		throw new IllegalArgumentException(
			"The key value is null. Please specify a non-null key.");
	    if (ref == null)
		throw new IllegalArgumentException(
			"The ProductRef supplied is null. Please specify a non-null ProductRef");

	    Predicate<Map.Entry<String, ProductRef>> rule = parent.getAddingRule();

	    if (rule.evaluate(new MyEntry(key, ref))) {
		if (!ref.equals(map.put(key, ref))) {
		    parent.refsChanged();
		}
		return ref;
	    }
	    throw new ContextRuleException("The key/ProductRef combination of [key= " + key + ", ref=\'"
		    + ref + "\'] is not compliant with the rules for this context.");
	}
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Holds information about its children. Should never be accessed directly
     * but via the {@link #getRefs()} method.
     */
    private transient Map<String, ProductRef> _map;

    // ************************************************************************
    // INTERFACES AVAILABLE FOR SUB-CLASSES
    // ************************************************************************

    /**
     * Convenience rule that allows any key,value pair combination.
     */
    protected static Predicate<Map.Entry<String, ProductRef>> ALLOW_ANYTHING = new AbstractPredicate<Map.Entry<String, ProductRef>>() {
	public boolean evaluate(Map.Entry<String, ProductRef> pair) {
	    return true;
	}

	public String toString() {
	    return MapContext.class.getName() + ".ALLOW_ANYTHING";
	}
    };

    /**
     * Convenience rule that allows no key,value pair combination whatsoever.
     */
    protected static Predicate<Map.Entry<String, ProductRef>> ALLOW_NOTHING = new AbstractPredicate<Map.Entry<String, ProductRef>>() {
	public boolean evaluate(Map.Entry<String, ProductRef> pair) {
	    return false;
	}

	public String toString() {
	    return MapContext.class.getName() + ".ALLOW_NOTHING";
	}
    };

    // ************************************************************************
    // PUBLIC METHODS
    // ************************************************************************

    /** Default constructor */
    public MapContext() {
	super();
	getRefs(); // initialize
    }

    /** Copy constructor */
    public MapContext(MapContext copy) {
	super(copy);
	_map = new Internal(this);
	_map.putAll(copy._map);
    }

    /**
     * Convenience method for getRefs().get(key).getProduct(). Note the general
     * remarks on get methods.
     */
    public final Product getProduct(String key) throws IOException, GeneralSecurityException {
	/*
	 * if (!getMap().containsKey(key)) { throw SomeException; }
	 */
	ProductRef ref = getRefs().get(key);
	return (ref == null) ? null : ref.getProduct();
    }

    /**
     * Convenience method for getRefs().put(key,new ProductRef(product)); Note
     * the general remarks on <em>adding rules</em>.
     */
    public final void setProduct(String key, Product product) {
	if (product == null)
	    throw new IllegalArgumentException("product argument must be defined");
	getRefs().put(key, new ProductRef(product));
    }

    /**
     * Returns a modifiable map on which {@link #getAddingRule() rule}s are
     * imposed specific to this context.
     * 
     * <p>
     * <b>Note</b> that
     * 
     * <ol>
     * <li>the put() method of the map view may throw a ContextRuleException if
     * the data added to the context violates the rules applied to the context.</li>
     * </li>
     * <li>the put() method of the map view may throw a
     * IllegalArgumentException if either of the arguments to the put() method
     * are null.</li>
     * </li>
     * </p>
     * 
     * @return a map view of references to children products.
     * 
     * 
     * @jhelp get the 'map' of ProductRefs stored. From this 'map', you can put
     *        products into the MapContext, or retrieve products by key.
     * 
     * @jexample Putting a product into the the MapContext 
     * ref = storage.save(product) # the ref is a ProductRef object
     * storage.save(product) # the ref is a ProductRef object
     * mapcontext.refs.put("john", ref)
     * 
     * @jexample Getting the product from the MapContext with key "john"
     * ref_john = lcontext.refs.get("john")
     * 
     * @jreturn A map of product refs.
     * 
     * 
     */
    public final Map<String, ProductRef> getRefs() {
	if (_map == null)
	    _map = new Internal(this);
	return _map;
    }

    /**
     * Returns an string represetnation of this MapContext
     * 
     * @return an string representation of this MapContext
     */
    public String toString() {
	StringBuffer b = new StringBuffer();
	b.append('{');
	b.append(contentsToString()); // from base class
	b.append(", refs=[");
	if (!getRefs().isEmpty()) {
	    boolean leadComma = false;
	    for (String key : getRefs().keySet()) {
		if (leadComma)
		    b.append(',');
		b.append(key);
		leadComma = true;
	    }
	}
	b.append(']');
	b.append('}');
	return b.toString();
    }

    // ************************************************************************
    // METHODS AVAILABLE FOR SUB-CLASSES
    // ************************************************************************

    /**
     * Returns the rule imposed on this MapContext when putting new (String,
     * ProductRef) pairs to this map.
     * <p>
     * The default rule of this implementation is to allow any combination of
     * (String,ProductRef) pairs.
     * <p>
     * Implementers can override the default behavior by implementing their own
     * rule. Such rule can be a simple one or a composite of rules.
     * 
     * @return a rule that is non-null;
     */
    protected Predicate<Map.Entry<String, ProductRef>> getAddingRule() {
	return ALLOW_ANYTHING;
    }

    // ************************************************************************
    // IMPLEMENTATION
    // ************************************************************************

    /**
     * Reads the internals of this context from a dataset.
     */
    @Override
    protected final void readDataset(ProductStorage storage, Dataset dataset)
    throws IOException, GeneralSecurityException {
	if (dataset instanceof TableDataset)
	    readTable(storage, (TableDataset)dataset);
	else
	    throw new IOException("Dataset holding product refs is corrupted!");
    }

    /**
     * Writes the internals of this context to a dataset.
     */
    @Override
    protected final Dataset writeDataset(ProductStorage storage)
    throws IOException, GeneralSecurityException {
	Map<String, String> cloningInfo = storage.getCloningInfo();
	final int n = getRefs().size();
	String1d name = new String1d(n);
	String1d urn = new String1d(n);
	String1d clazz = new String1d(n);

	// FIXME: this algorithm would create more than one copy of a product
	// belonging to more that one parent, if the original hierarchy was
	// not previously saved (their urns are null). Not a problem when
	// cloning an already stored context.
	int i = 0;
	for (Map.Entry<String, ProductRef> e : getRefs().entrySet()) {
	    ProductRef ref = e.getValue();
	    String oldUrn = ref.getUrn();
	    String newUrn = cloningInfo.get(oldUrn);
	    if (newUrn == null) {
		storage.commit(ref);
		newUrn = ref.getUrn();
		if (oldUrn != null) {
		    cloningInfo.put(oldUrn, newUrn);
		}
	    } else {
		ref.setUrn(newUrn);
		ref.setStorage(storage);
	    }
	    name.set(i, e.getKey());
	    urn.set(i, newUrn);
	    clazz.set(i, ref.getType().getName());
	    i++;
	}

	TableDataset table = new TableDataset();
	table.addColumn("name", new Column(name));
	table.addColumn("urn", new Column(urn));
//	table.addColumn("class", new Column(clazz));

	return table;
    }

    /**
     * Reads the internals of this context from a table dataset.
     */
    private final void readTable(ProductStorage storage, TableDataset table) {
	String1d names = (String1d) table.getColumn("name").getData();
	String1d urns = (String1d) table.getColumn("urn").getData();
	//String1d classNames = (String1d) table.getColumn("class").getData();

	for (int i = 0, n = names.length(); i < n; i++) {
	    String urn = urns.get(i);
	    if (urn == null) continue;

	    String key = names.get(i);
	    ProductRef val = new ProductRef(storage, urn);
	    getRefs().put(key, val);
	}
    }

    /**
     * Returns a logical to specify whether this MapContext has dirty references or not.
     * @return 'true' if one or more references are dirty.
     */
    protected boolean hasDirtyReferences(ProductStorage storage)
    throws IOException, GeneralSecurityException {
	for (ProductRef ref : getRefs().values()) {
	    if (ref.isDirty(storage)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Provides a set of the references stored in this MapContext.
     * @return a set of ProductRef with the references to the objects held by this MapContext.
     */
    @Override
    public Set<ProductRef> getAllRefs() {
	Set<ProductRef> out = new ArraySet<ProductRef>();

	for (ProductRef ref : getRefs().values()) {
	    out.add(ref);
	}
	return out;
    }
    
    public void accept(ProductVisitor v) {
	v.visit(this);
    }

}
