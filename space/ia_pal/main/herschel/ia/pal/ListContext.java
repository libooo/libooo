/* $Id: ListContext.java,v 1.23 2008/07/22 11:03:42 pbalm Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import herschel.ia.pal.util.ArraySet;
import herschel.ia.dataset.Column;
import herschel.ia.dataset.Dataset;
import herschel.ia.dataset.TableDataset;
import herschel.ia.numeric.String1d;
import herschel.share.predicate.AbstractPredicate;
import herschel.share.predicate.Predicate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.AbstractSequentialList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Groups {@link herschel.ia.dataset.Product}s (or more strictly {@link ProductRef}s) in list-like structure.
 * <p>New entries can be added if they match the {@link #getAddingRule() rule} belonging
 * to this context.</p>
 * <p>An example:
 * <pre>
 *      Jython:
 *      # ImageProduct is a Java class that extends Product
 *      # SpectrumProduct is a Java class that extends Product
 *      product1=ImageProduct(description=&quot;hi&quot;)
 *      product2=ImageProduct(description=&quot;there&quot;)
 *      product3=SpectrumProduct(description=&quot;everyone&quot;)
 *      
 *      context=ListContext()
 *      context.refs.add(ProductRef(product1))
 *      context.refs.add(ProductRef(product2))
 *      context.refs.add(ProductRef(product3))
 *      print context.refs.size() # 3
 *      print context.refs.get(0).product.description # hi
 *      print context.refs.get(1).product.description # there
 *      print context.refs.get(2).product.description # everyone
 * </pre>
 * </p>
 * 
 * <p>
 * It is possible to insert a ProductRef at a specific position (or index) in the ListContext.
 * The same insertion behaviour is followed as for a java {@link java.util.List}.
 * The ProductRef currently at that position and subsequent products in the
 * sequences are shifted to the right (one is added to their indices):
 * 
 * <pre>
 *     product4=SpectrumProduct(description=&quot;everybody&quot;)
 *     context.refs.add(1, ProductRef(product4))
 *     print context.refs.get(0).product.description # hi
 *     print context.refs.get(1).product.description # everybody
 *     print context.refs.get(2).product.description # there
 *     print context.refs.get(2).product.description # everyone
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * It is possible to restrict what can be added to a ListContext by extending
 * ListContext and override the {#link #getAddingRule()} method. For example:</p>
 * <p>
 * <pre>
 *      // Java:
 *      // A ListContext that allows ImageProduct and SpectrumProduct products only 
 *      class ImageSpectrumProducts extends ListContext {
 *          private static Predicate&lt;ProductRef&gt; _RULE=
 *          new AbstractPredicate&lt;ProductRef&gt;() {
 *              public boolean evaluate(ProductRef ref) {
 *                  return ImageProduct.class.equals(ref.getType()) ||
 *                         SpectrumProduct.class.equals(ref.getType());
 *              }
 *          };
 *      
 *          protected Predicate&lt;ProductRef&gt; getAddingRule() {
 *              return _RULE;
 *          }
 *      }
 *      
 *      # jython:
 *      context=ImageSpectrumProducts()
 *      context.refs.add(ProductRef(ImageProduct()))
 *      context.refs.add(ProductRef(SpectrumProduct()))
 *      context.refs.add(ProductRef(Product())) # ERROR!
 * </pre>
 * </p>
 * 
 * <p>
 * Note that the <em>adding rules</em> are only applied when <em>adding</em>
 * an entry to the list!
 * 
 * <p>
 * <b>Be aware</b> that
 * 
 * <ol>
 * <li>the add() method of the list view may throw a ContextRuleException if
 * the data added to the context violates the rules applied to the context.</li>
 * <li>the add() method of the map view may throw a IllegalArgumentException if
 * either of the arguments to the put() method are null.</li>
 * </li>
 * </ol>
 * </p>
 * 
 * 
 * @author DP Development Team
 * 
 * @jhelp Groups products (or other Contexts that in turn group products) in a
 *        list-like structure. Products grouped in a ListContext can be
 *        subsequently retrieved by an index (with index=0 corresponding to the
 *        first product in the list), through the 'refs' method.
 * 
 * Note: users may be confused as to why there is a need to have to go though a
 * 'refs' method to add or access products from a ListContext. This is due to a
 * technical limitation in the design which will be addressed in due course.
 * 
 * @jcategory herschel.ia.pal;
 * 
 * @jref herschel.ia.pal.urm
 * 
 * @jexample Adding a product to a ListContext product = Product() ref =
 *           storage.save(product) # the ref is a ProductRef object listcontext =
 *           ListContext() listcontext.refs.add(ref)
 * 
 * @jexample Getting the first product from a ListContext ref_first =
 *           lcontext.refs.get(0) product = ref_first.product
 * 
 * @jexample Saving a ListContext to ProductStorage (same way as any other
 *           product) ref_context = storage.save(listcontext)
 * 
 */
public class ListContext extends Context {

    @SuppressWarnings("unused")
    private static Logger _LOGGER = Logger.getLogger(ListContext.class.getName());

    private static final long serialVersionUID = 1L;

    /**
     * Holds information about its children. Should never be accessed directly
     * but via the {@link #getRefs()} method.
     */
    private transient List<ProductRef> _refs;

    /**
     * Simple rule to allow anything. This is the default rule for this context.
     */
    private static Predicate<ProductRef> ALLOW_ANYTHING = new AbstractPredicate<ProductRef>() {
	/**
	 * Simply accept anything.
	 */
	public boolean evaluate(ProductRef toProduct) {
	    return true;
	}

	/**
	 * Pretty printing.
	 */
	public String toString() {
	    return ListContext.class.getName() + ".ALLOW_ANYTHING";
	}
    };

    /**
     * Private implementation of an iterator that notifies the parent's context
     * on changes made to the iterator. This implementation is required by the
     * required by MyList class.
     * 
     * @author jbakker
     */
    private final static class MyListIterator implements ListIterator<ProductRef> {

	private ListIterator<ProductRef> iterator;
	private ListContext parent;
	private ProductRef lastReturned;

	public MyListIterator(ListContext context, ListIterator<ProductRef> original) {
	    parent = context;
	    iterator = original;
	    lastReturned = null;
	}

	public boolean hasNext() {
	    return iterator.hasNext();
	}

	public ProductRef next() {
	    return (lastReturned = iterator.next());
	}

	public boolean hasPrevious() {
	    return iterator.hasPrevious();
	}

	public ProductRef previous() {
	    return (lastReturned = iterator.previous());
	}

	public int nextIndex() {
	    return iterator.nextIndex();
	}

	public int previousIndex() {
	    return iterator.previousIndex();
	}

	public void remove() {
	    iterator.remove();
	    parent.refsChanged();
	}

	public void set(ProductRef o) {
	    validate(parent.getAddingRule(), o);
	    iterator.set(o);
	    if ((lastReturned == null) || !lastReturned.equals(o)) {
		parent.refsChanged();
	    }
	}

	public void add(ProductRef o) {
	    validate(parent.getAddingRule(), o);
	    iterator.add(o);
	    parent.refsChanged();
	}

	private void validate(Predicate<ProductRef> rule, ProductRef ref) {
	    if (rule == null) {
		throw new NullPointerException(
			"The rule for this context is not defined. Please define it first.");
	    }
	    if (ref == null) {
		throw new IllegalArgumentException(
			"The ProductRef is null. Please provide a non-null ProductRef");
	    }
	    if (!rule.evaluate(ref)) {
		throw new ContextRuleException("The ProductRef \'" + ref
			+ "\' is not compliant with the rules set out for this context.");
	    }
	}
    }

    /**
     * Private implementation of a list to ensure that this context is aware of
     * additions,replacements and removals.
     * 
     * @author jbakker
     */
    private static final class MyList extends AbstractSequentialList<ProductRef> {
	private List<ProductRef> list;

	private ListContext parent;

	public MyList(ListContext context) {
	    list = new LinkedList<ProductRef>();
	    parent = context;
	}

	@Override
	public ListIterator<ProductRef> listIterator(int index) {
	    return new MyListIterator(parent, list.listIterator(index));
	}

	@Override
	public int size() {
	    return list.size();
	}

    };

    /**
     * Default constructor for this context.
     */
    public ListContext() {
	super();
    }

    /**
     * Copy constructor for this context.
     */
    public ListContext(ListContext copy) {
	super(copy);
	_refs = new MyList(this);
	_refs.addAll(copy._refs);
    }

    /**
     * Returns a List view of the product refs held in this context.
     * 
     * <p>
     * <b>Note</b> that
     * 
     * <ol>
     * <li>the add() method of the list view may throw a ContextRuleException
     * if the data added to the context violates the rules applied to the
     * context.</li>
     * <li>the add() method of the list view may throw an
     * IllegalArgumentException if the argument violates the rules applied to
     * the context.</li>
     * </ol>
     * </p>
     * 
     * @return a list of product refs.
     * 
     * @jhelp get the list of ProductRefs stored. From this list, you can put
     *        products into the ListContext, or retrieve products by index.
     * 
     * @jexample Putting a product into the the ListContext ref =
     *           storage.save(product) # the ref is a ProductRef object
     *           listcontext.refs.add(ref)
     * 
     * @jexample Getting the first product from the ListContext ref_first =
     *           lcontext.refs.get(0)
     * 
     * @jreturn A list of product refs.
     * 
     */
    public final List<ProductRef> getRefs() {
	if (_refs == null)
	    _refs = new MyList(this);
	return _refs;
    }

    /**
     * Returns an string represetnation of this ListContext
     * 
     * @return an string representation of this ListContext
     */
    public String toString() {
	StringBuffer b = new StringBuffer();
	b.append('{');
	b.append(contentsToString()); // from base class
	b.append(", # refs=").append(getRefs().size());
	b.append('}');
	return b.toString();
    }

    /**
     * Reads the internals of this context from a dataset.
     */
    @Override
    protected final void readDataset(ProductStorage storage, Dataset dataset)
    throws IOException, GeneralSecurityException {
	if (dataset instanceof TableDataset)
	    readTable(storage, (TableDataset)dataset);
	else
	    throw new IOException("Dataset holding product references is corrupted! "
		    + "Expected table dataset, got " + dataset.getClass());
    }

    /**
     * Writes the internals of this context to a dataset.
     */
    @Override
    protected final Dataset writeDataset(ProductStorage storage)
    throws IOException, GeneralSecurityException {
	Map<String, String> cloningInfo = storage.getCloningInfo();

	// convert the list of refs that we want to save into ArrayData
	List<ProductRef> refs = getRefs();
	final int n = refs.size();

	String1d urn = new String1d(n);
	String1d clazz = new String1d(n);
	int i = 0;
	for (ProductRef ref : refs) {
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
	    urn.set(i, newUrn);
	    clazz.set(i, ref.getType().getName());
	    i++;
	}

	// create the table, with appropriate restiction information:
	TableDataset table = new TableDataset();
	table.addColumn("urn", new Column(urn));
//	table.addColumn("class", new Column(clazz));
	return table;
    }

    /**
     * Reads the internals from specified table.
     * 
     * @param table
     * @throws GeneralSecurityException 
     * @throws IOException 
     */
    private final void readTable(ProductStorage storage, TableDataset table) {
	// read the refs:
	String1d urns = (String1d) table.getColumn("urn").getData();
//	String1d classNames = (String1d) table.getColumn("class").getData();

	List<ProductRef> refs = getRefs();
	for (int i = 0, n = urns.length(); i < n; i++) {
	    String urn = urns.get(i);
	    if (urn == null) continue;
	    
	    ProductRef ref = new ProductRef(storage, urn);
	    refs.add(ref);
	}
    }

    /**
     * Returns a logical to specify whether this ListContext has dirty
     * references or not.
     * 
     * @return 'true' if one or more references are dirty.
     * @throws GeneralSecurityException 
     * @throws IOException 
     */
    protected boolean hasDirtyReferences(ProductStorage storage)
    throws IOException, GeneralSecurityException {
	for (ProductRef ref : getRefs()) {
	    if (ref.isDirty(storage)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Provides a set of the references stored in this ListContext.
     * 
     * @return a set of ProductRef with the references to the objects held by
     *         this ListContext.
     */
    @Override
    public Set<ProductRef> getAllRefs() {
	Set<ProductRef> out = new ArraySet<ProductRef>();

	for (ProductRef ref : getRefs()) {
	    out.add(ref);
	}
	return out;
    }

    /**
     * Returns the rule imposed on this Context when putting new ProductRef
     * pairs to this map.
     * <p>
     * The default rule of this implementation is to allow any ProductRef.
     * <p>
     * Implementers can override the default behavior by implementing their own
     * rule. Such rule can be a simple one or a composite of rules.
     * 
     * @return a rule that is non-null;
     */
    protected Predicate<ProductRef> getAddingRule() {
        return ALLOW_ANYTHING;
    }
    
    public void accept(ProductVisitor v) {
	v.visit(this);
    }

}
