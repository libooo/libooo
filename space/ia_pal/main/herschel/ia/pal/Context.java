/* $Id: Context.java,v 1.23 2008/09/12 04:25:14 bli Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import herschel.ia.dataset.CompositeDataset;
import herschel.ia.dataset.Dataset;
import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.pal.util.ArraySet;
import herschel.ia.pal.util.Util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A Context is a special kind of Product that can hold references to other
 * Products.
 * <p>
 * This abstract product introduces the lazy loading and saving of 
 * references to Products or {@link ProductRef}s that it is holding. 
 * It remembers its state.
 * <p>
 * The ProductRefs are held in memory in a temporary {@link Dataset}. This dataset is not
 * accessible via the generic Product {@link #get(String)} interface.
 * Implementers should specify how to {@link #readDataset(Dataset) read} and
 * {@link #writeDataset(ProductStorage) write} that dataset to a persistent {@link ProductStorage}
 * instance.
 * <p>
 * The implementor of a specialization of this abstract class is
 * in control of marking this context as {@link #refsChanged() dirty} whenever there is a change
 * made in memory to this context or with child products (implementors may need to be aware that it is 
 * not possible presently to ascertain whether leaf products have been modified in memory, ie dirty. 
 * But at least, child contexts can be tested).
 * <p>
 * Note that this context is only tied to a {@link ProductStorage product storage} if it
 * is either loaded or saved to that storage.
 * 
 * @author DP Development Team
 */
public abstract class Context extends Product {

    @SuppressWarnings("unused")
    private static Logger _LOGGER = Logger.getLogger(Context.class.getName());

    private static final long serialVersionUID = 1L;

    /**
     * Key to the dataset holding the information of ProductRefs as specified by
     * specializations of this abstract class.
     * 
     * [HS 02-Jul-2007]: This name should be changed to 'refs', but cannot do that presently
     * as this involves a schema evolution.
     */
    private static final String _REF = "bridges";

    /**
     * Key to the dataset that is holding information used when reading/writing
     * context specific information from a Product Storage.
     * <p>
     * This marker is coupled to a CompositeDataset as follows:
     * <dl>
     * <dt><b>bridges</b>
     * <dd>Dataset holding ProductRefs to child products
     * <dt><b>version</b>
     * <dd>TableDataset(Column(String1d)) holding URNs of previous versions of
     * this context.
     * </dl>
     */
    private static final String _CONTEXT = "context";
    /**
     * True if a reference has been added or removed.
     */
    private transient boolean _refsChanged = false;

    /**
     * The URN of this context itself.
     */
    private transient String _urn;

    /**
     * A deep copy of the meta data as retrieved from a storage. If null, this
     * context was constructed in memory.
     * This is used for the isDirty() check.
     */
    private transient MetaData _metaFromStorage;

    /**
     * A copy of the description as retrieved from a storage.
     * This is used for the isDirty() check.
     */
    private transient String _descriptionFromStorage;

    /**
      * Create an context as a copy of the given one.
      * @param  context to copy
      */
    protected Context(Context context) {
	// FIXME: uncommenting this causes problems while
	// cloning. It is unclear why this presents a problem.
	// _urn = context._urn;

	_metaFromStorage = new MetaData(context._metaFromStorage);
	_descriptionFromStorage = context.getDescription();
    }

    /**
      * Create an empty context.
      */
    protected Context() {
    }

    /**
     * Yields true if specified class belongs to the family of contexts.
     * @return <code>true</code> if it does
     */
    public final static boolean isContext(Class clazz) {
	return Util.isClass(Context.class, clazz);
    }

    /**
     * Provides a mechanism to ensure whether it is valid to store this context
     * in its current state.
     * <p>
     * Implementers of sub-classes can override this method to provide specific
     * testing for validity.
     * The default implementation returns true.
     * <p>
     * 
     * @return whether this context is complete for persistency or not.
     */
    public boolean isValid() {
	return true;
    }
    
    public void accept(ProductVisitor v) {
	v.visit(this);
    }

    // ************************************************************************
    // METHODS AVAILABLE FOR SUB-CLASSES
    // ************************************************************************

    /**
     * Reads a dataset with information within this context that is normally not
     * accessible from the normal {@link Product} interface.
     * <p>
     * This method is called whenever the product is loaded from a
     * {@link ProductStorage} to allow sub-classes to do further initialisation
     * based on the information within specified table.
     * <p>
     * Note that implementations can safely assume that {@link #getStorage()}
     * will return the store related to this context.
     * @param  table  dataset to read
     */
    protected abstract void readDataset(ProductStorage storage, Dataset table)
    throws IOException, GeneralSecurityException;

    /**
     * Creates a dataset with information within this context that is normally
     * not accessible from the normal {@link Product} interface.
     * <p>
     * This method is called prior to saving this product to a
     * {@link ProductStorage} to allow sub-classes to store additional
     * information in a dataset.
     * <p>
     * Note that implementations should
     * use the argument storageTo to refer to the storage
     * the data is being written to.
     * 
     * @param storage the storage the data is being written to.
     */
    protected abstract Dataset writeDataset(ProductStorage storage)
    throws IOException, GeneralSecurityException;

    /**
     * Indicates that the references have been changed in memory, which marks
     * this context as dirty.
     */
    protected final void refsChanged() {
	_refsChanged = true;
    }

    // ************************************************************************
    // PACKAGE LOCAL STUFF: COOPERATION WITH PRODUCTREFS AND STORAGE
    // ************************************************************************

    /**
     * Returns the URN of this context, which may be null.
     * <p>
     * The URN is null whenever this context is constructed in memory but not
     * yet saved or this context is marked as dirty.
     * <p>
     * In principle only the ProductRef implementation requires access to this
     * method.
     * 
     * @return the URN coupled to this context.
     */
    final String getUrn() {
	return _urn;
    }

    /**
     * Does further initialization of a context.
     * <p>
     * This method is only accessed within the
     * {@link ProductStorage#loadProduct(String)} method prior to returning this
     * sub-classed Product to the client.
     * 
     * @param storage -
     *            an instance of a ProductStorage.
     * @param urn -
     *            an instance of a String holding a URN.
     * @throws IOException
     *             if something went wrong in IO operations.
     */
    final void load(ProductStorage storage, String urn)
    throws IOException, GeneralSecurityException {

	// keep a local copy, so that we can check the dirtiness of the metadata
	_metaFromStorage = new MetaData(getMeta());
	_descriptionFromStorage = getDescription();
	Dataset dataset = remove(_CONTEXT);
	if (dataset == null || !(dataset instanceof CompositeDataset))
	    throw new IOException("Corrupted context:" + urn);

	readComposite(storage, (CompositeDataset)dataset);
	_refsChanged = false;
	_urn = urn;
    }

    /**
     * Commits this context product to the store if it is dirty. A context knows
     * about its state, and therefore can remember its URN as well.
     * <p>
     * 
     * @param storage
     * @return true if the context references have been updated; false otherwise
     * @throws IOException
     *             if something went wrong storing this context.
     * @throws IllegalStateException
     *             if this context is not valid to be stored yet.
     */
    final void commitRefs(ProductStorage storage)
    throws IOException, GeneralSecurityException {
	if (!isValid()) {
	    throw new IllegalStateException("Trying to save an context of " + getClass()
		    + " that reports itself as invalid (yet).");
	}

	Dataset dataset = writeComposite(storage);
	set(_CONTEXT, dataset);
    }

    /**
     * Cleans-up internal status after commiting the context.
     */
    final void committed(String urn) {
	_urn = urn;
	_metaFromStorage = new MetaData(getMeta());
	_descriptionFromStorage = getDescription();
	_refsChanged = false;
	remove(_CONTEXT);
    }

    // ************************************************************************
    // PRIVATE STUFF
    // ************************************************************************

    final boolean isDirty(ProductStorage storage) throws IOException, GeneralSecurityException {

	if (_refsChanged) {
	    return true;
	}

	if (_urn == null) {
	    return true;
	}

	if (_metaFromStorage != null) {
	    if (!_metaFromStorage.equals(getMeta())) {
		return true;
	    }
	} else if (getMeta() != null) {
	    return true;
	}

	if (_descriptionFromStorage != null) {
	    if (!_descriptionFromStorage.equals(getDescription())) {
		return true;
	    }
	} else if (getDescription() != null) {
	    return true;
	}

	return hasDirtyReferences(storage);
    }

    /**
     * Returns a logical to specify whether this context has dirty references or not.
     * As a Context cannot be instantiated, this method returns 'false' always.
     * 
     * @return 'false' always.
     * @throws GeneralSecurityException 
     * @throws IOException 
     */
    @SuppressWarnings("unused")
    protected boolean hasDirtyReferences(ProductStorage storage) throws IOException,
	    GeneralSecurityException {
	return false;
    }

    /**
     * Initializes the internals of this context from the information found in
     * specified dataset.
     * <p>
     * This method calls the {@link #readDataset(Dataset)} method. Note that
     * this method is only called by the
     * {@link #load(ProductStorage, String) load} method.
     */
    private void readComposite(ProductStorage storage, CompositeDataset composite)
    throws GeneralSecurityException, IOException {

	// extract information about bridges specific to this context.
	Dataset dataset = composite.remove(_REF);
	if (dataset == null)
	    throw new IOException("Corrupted bridges entry in context:" + getUrn());
	readDataset(storage, dataset);
    }

    /**
     * Stores information about the internals of this context to a composite
     * dataset.
     * <p>
     * This method calls the {@link #writeDataset()} method. Note that this
     * method is only called by the {@link #commitRefs(ProductStorage) commitRefs}
     * method.
     */
    // only called if dirty()!
    private CompositeDataset writeComposite(ProductStorage storage) throws IOException,
	    GeneralSecurityException {
	CompositeDataset composite = new CompositeDataset();

	// Add bridges, as specified by sub-classes:
	Dataset bridges = writeDataset(storage);

	composite.set(_REF, bridges);

	return composite;
    }

    /**
     * Provides a set of the references stored in this Context.
     * 
     * @return a set of ProductRef with the referenced objects stored in this Context.
     */
    public abstract Set<ProductRef> getAllRefs();

    /**
     * 
     */
    
}
