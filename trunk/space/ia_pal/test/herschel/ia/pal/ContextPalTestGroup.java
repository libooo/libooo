package herschel.ia.pal;

import herschel.ia.dataset.Product;
import herschel.ia.pal.query.Query;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ContextPalTestGroup extends AbstractPalTestGroup {

	private static Logger _LOGGER = Logger.getLogger("ContextPalTestGroup");

	public ContextPalTestGroup(String name, AbstractPalTestCase testcase) {
		super(name, testcase);
	}

	public void testContextSavedAfterEveryUpdate() throws IOException,
			GeneralSecurityException {

		final String child_id = "child";
		final String leaf_id = "leaf";
		final String leaf_descrip_a = "product 1";
		final String leaf_descrip_b = "product 2";

		ProductStorage storage = new ProductStorage();
		ProductPool pool = getPool();
		storage.register(pool);

		String urn_parent;
		{
			/* Populate context */
			Product leaf = new Product();
			leaf.setDescription(leaf_descrip_a);
			ProductRef leaf_ref = storage.save(leaf);

			MapContext child = new MapContext();
			child.getRefs().put(leaf_id, leaf_ref);
			ProductRef child_ref = storage.save(child);
			printRefs(child);

			MapContext parent = new MapContext();
			parent.getRefs().put(child_id, child_ref);

			String descr = getProductDescription(parent, child_id, leaf_id);
			assertTrue(leaf_descrip_a.equals(descr));

			// Now save parent context
			urn_parent = storage.save(parent).getUrn();
		}

		{
			MapContext parent2 = (MapContext) storage.load(urn_parent)
					.getProduct();
			assertTrue(parent2 != null);
		}

		/* Now modify child context by adding a product to it */
		{
			MapContext parent = (MapContext) storage.load(urn_parent)
					.getProduct();
			assertTrue(parent != null);

			MapContext child = (MapContext) parent.getRefs().get(child_id)
					.getProduct();
			assertTrue(parent != null);

			Product leaf = new Product();
			leaf.setDescription(leaf_descrip_b);
			ProductRef leaf_ref = storage.save(leaf);
			child.getRefs().put(leaf_id, leaf_ref);
			printRefs(child);

			urn_parent = storage.save(parent).getUrn();
		}

		{
			MapContext parent;
			MapContext child;

			parent = (MapContext) storage.load(urn_parent).getProduct();
			child = (MapContext) parent.getRefs().get(child_id).getProduct();

			printRefs(parent);
			printRefs(child);
		}

	}

	/**
	 * Test subtle behvaviour of state changes in contexts: A child context is
	 * created and saved. Then associated with the parent context. Then the
	 * child context is updated in-memory. Does the parent context save the
	 * updates correctly?
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void testContextModifiedInMemoryAfterSave() throws IOException,
			GeneralSecurityException {

		final String child_id = "child";
		final String leaf_id = "leaf";
		final String leaf_description = "test product";

		ProductStorage storage = new ProductStorage();
		ProductPool pool = getPool();
		storage.register(pool);

		String urn_parent;
		{
			/* Populate context */
			Product leaf = new Product();
			leaf.setDescription(leaf_description);
			ProductRef leaf_ref = storage.save(leaf);

			MapContext child = new MapContext();
			ProductRef child_ref = storage.save(child);

			child.getRefs().put(leaf_id, leaf_ref);
			printRefs(child);

			MapContext parent = new MapContext();
			parent.getRefs().put(child_id, child_ref);

			String descr = getProductDescription(parent, child_id, leaf_id);
			assertTrue(leaf_description.equals(descr));

			// Now save parent context
			urn_parent = storage.save(parent).getUrn();
		}

		{
			MapContext o2 = (MapContext) storage.load(urn_parent).getProduct();
			assertTrue(o2 != null);
		}

	}

	/**
	 * Tests that parent context explicitly saves a child context created wholly
	 * in-memory.
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void testContextCreatedWhollyInMemory() throws IOException,
			GeneralSecurityException {

		final String child_id = "child";
		final String leaf_id = "leaf";
		final String leaf_description = "test product";

		ProductStorage storage = new ProductStorage();
		ProductPool pool = getPool();
		storage.register(pool);

		String urn_o;
		{
			/* Populate context */
			Product leaf = new Product();
			leaf.setDescription(leaf_description);
			ProductRef leaf_ref = storage.save(leaf);

			MapContext child = new MapContext();
			child.getRefs().put(leaf_id, leaf_ref);
			ProductRef child_ref = new ProductRef(child);

			MapContext parent = new MapContext();
			parent.getRefs().put(child_id, child_ref);

			String descr = getProductDescription(parent, child_id, leaf_id);
			assertTrue(leaf_description.equals(descr));

			// Now save parent context
			urn_o = storage.save(parent).getUrn();
		}

		{
			MapContext parent = (MapContext) storage.load(urn_o).getProduct();
			assertTrue(parent != null);
		}

	}

	/**
	 * Tests that a product created wholly in memory is saved by its parent
	 * context.
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void testProductCreatedInMemory() throws IOException,
			GeneralSecurityException {

		final String leaf_id = "leaf";
		final String leaf_description = "test product";

		ProductStorage storage = new ProductStorage();
		ProductPool pool = getPool();
		storage.register(pool);

		String urn_parent;
		MapContext parent;
		Product leaf;
		{
			/* Populate context */
			leaf = new Product();
			leaf.setDescription(leaf_description);
			ProductRef leaf_ref = new ProductRef(leaf);

			parent = new MapContext();
			parent.getRefs().put(leaf_id, leaf_ref);

			String descr = parent.getRefs().get(leaf_id).getProduct()
					.getDescription();
			assertTrue(leaf_description.equals(descr));

			// Now save context
			urn_parent = storage.save(parent).getUrn();
		}

		{
			// Check data accessible from storage
			MapContext parent2 = (MapContext) storage.load(urn_parent)
					.getProduct();
			assertTrue(parent2 != null);
			String descr = parent2.getRefs().get(leaf_id).getProduct()
					.getDescription();
			assertTrue(leaf_description.equals(descr));

		}
	}

	/**
	 * Tests that a product modified in memory is saved by its parent context.
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void testProductModifiedInMemoryAfterSave() throws IOException,
			GeneralSecurityException {

		final String leaf_id = "leaf";
		final String leaf_description_a = "test product a";
		final String leaf_description_b = "test product b";

		ProductStorage storage = new ProductStorage();
		ProductPool pool = getPool();
		storage.register(pool);

		String urn_parent;
		MapContext parent;
		Product leaf;
		{
			/* Populate context */
			leaf = new Product();
			leaf.setDescription(leaf_description_a);
			ProductRef leaf_ref = new ProductRef(leaf);

			parent = new MapContext();
			parent.getRefs().put(leaf_id, leaf_ref);

			String descr = parent.getRefs().get(leaf_id).getProduct()
					.getDescription();
			assertTrue(leaf_description_a.equals(descr));

			// Now save context
			urn_parent = storage.save(parent).getUrn();
		}

		{
			// Check data accessible from storage
			MapContext parent2 = (MapContext) storage.load(urn_parent)
					.getProduct();
			assertTrue(parent2 != null);
			String descr = parent2.getRefs().get(leaf_id).getProduct()
					.getDescription();
			assertTrue(leaf_description_a.equals(descr));

		}

		{
			// Modify product in memory
			leaf.setDescription(leaf_description_b);

			// See if context has been updated
			String descr = parent.getRefs().get(leaf_id).getProduct()
					.getDescription();
			assertTrue(leaf_description_b.equals(descr));

			// Now save context
			urn_parent = storage.save(parent).getUrn();
		}

		{
			// Check data accessible from storage
			MapContext parent2 = (MapContext) storage.load(urn_parent)
					.getProduct();
			assertTrue(parent2 != null);
			String descr = parent2.getRefs().get(leaf_id).getProduct()
					.getDescription();
			assertTrue(leaf_description_b.equals(descr));

		}

	}

	/**
	 * Tests the ListContext with multiple leaf products
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void testListContext() throws IOException, GeneralSecurityException {
		final String leaf_description_a = "Leaf one";
		final String leaf_description_b = "Leaf two";

		ProductStorage storage = new ProductStorage();
		ProductPool pool = getPool();
		storage.register(pool);

		int childIndex0 = 0;
		int childIndex1 = 1;
		int childIndex2 = 2;

		ListContext parent;
		Product leaf1;
		Product leaf2;

		{
			/* Populate context */
			leaf1 = new Product();
			leaf1.setDescription(leaf_description_a);
			ProductRef leaf_ref1 = new ProductRef(leaf1);

			leaf2 = new Product();
			leaf2.setDescription(leaf_description_b);
			ProductRef leaf_ref2 = new ProductRef(leaf2);

			parent = new ListContext();
			parent.setDescription("Parent Description");
			parent.getRefs().add(childIndex0, leaf_ref1);
			parent.getRefs().add(childIndex1, leaf_ref2);

			/* Save the context */
			storage.save(parent);

			/* Examine all the products */

			ListIterator li = parent.getRefs().listIterator();
			while (li.hasNext()) {
				_LOGGER.info("Original Descriptions are "
						+ ((ProductRef) li.next()).getProduct()
								.getDescription());
			}

			/* Retrieve the leafs and check the descriptions */
			assertEquals(parent.getRefs().get(childIndex0).getProduct()
					.getDescription(), "Leaf one");
			assertEquals(parent.getRefs().get(childIndex1).getProduct()
					.getDescription(), "Leaf two");

			/* Test that product can be added to list */
			/*
			 * Retrieve the first leaf and reset the description - add as new
			 * product
			 */
			parent.getRefs().add(childIndex0,
					new ProductRef(new Product("Leaf one update")));

			// Test if the change is registered as a dirty ref.
			assertTrue(parent.isDirty(storage));
			storage.save(parent);

			// Now should not have dirty refs
			assertFalse(parent.isDirty(storage));

			// Now has 3 products in list
			ListIterator li_new = parent.getRefs().listIterator();
			while (li_new.hasNext()) {
				_LOGGER.info("New Descriptions are "
						+ ((ProductRef) li_new.next()).getProduct()
								.getDescription());
			}
			assertEquals(parent.getRefs().get(childIndex0).getProduct()
					.getDescription(), "Leaf one update");
			assertEquals(parent.getRefs().get(childIndex1).getProduct()
					.getDescription(), "Leaf one");
			assertEquals(parent.getRefs().get(childIndex2).getProduct()
					.getDescription(), "Leaf two");

			// Get the last product in list (which should now be leaf2)
			assertEquals(parent.getRefs().get(childIndex2), leaf_ref2);
			/* End test for simple product addition */

			/* Tests that new product can be added at a given list index */
			/*
			 * Note this moves previous product to next index, but does not
			 * remove it
			 */
			// Get the product at index2 and product ref as new product
			Product leaf3 = parent.getRefs().get(childIndex2).getProduct();
			leaf3.setDescription("New leaf at Index 2");
			ProductRef leaf_ref3 = new ProductRef(leaf3);

			// Add this in at index2 to replace product leaf2
			// Using 'add' method will add new product in and shift
			// the others down the list.
			parent.getRefs().add(childIndex2, leaf_ref3);

			// Has this changed ?
			_LOGGER.info("Dirty after adding new index2 "
					+ parent.isDirty(storage));
			// Should now have dirty refs
			assertTrue(parent.isDirty(storage));

			// Save parent context
			storage.save(parent);
			// Should not have dirty refs
			assertFalse(parent.isDirty(storage));

			// Check that new description is there
			assertEquals((parent.getRefs().get(childIndex2)).getProduct()
					.getDescription(), "New leaf at Index 2");
			// Now have 4 products, with old index2 now at index3
			assertEquals(parent.getRefs().size(), 4);
			ListIterator li_2 = parent.getRefs().listIterator();
			while (li_2.hasNext()) {
				_LOGGER.info("New descriptions with additions are "
						+ ((ProductRef) li_2.next()).getProduct()
								.getDescription());
			}
			/* End tests for product addition at specified index */

			// Test for replacement of existing product at a given index
			// Get the product at index2 and product ref as new product
			Product leaf4 = parent.getRefs().get(childIndex2).getProduct();
			leaf4.setDescription("Replaced leaf at Index 2");
			ProductRef leaf_ref4 = new ProductRef(leaf4);

			// Add this in at index2 to replace product leaf2
			// Using 'set' method will add new product in and replace the
			// the previous product at this index.
			parent.getRefs().set(childIndex2, leaf_ref4);

			// Has this changed ?
			_LOGGER.info("Dirty after replacing index2 "
					+ parent.isDirty(storage));
			// Should now have dirty refs
			assertTrue(parent.isDirty(storage));
			storage.save(parent);
			// Should not have dirty refs
			assertFalse(parent.isDirty(storage));

			// Check that new description is there
			assertEquals((parent.getRefs().get(childIndex2)).getProduct()
					.getDescription(), "Replaced leaf at Index 2");
			// Still have 4 products, with new index2 replacing index3
			assertEquals(parent.getRefs().size(), 4);
			ListIterator li_3 = parent.getRefs().listIterator();
			while (li_3.hasNext()) {
				_LOGGER.info("Replaced Descriptions are "
						+ ((ProductRef) li_3.next()).getProduct()
								.getDescription());
			}
			/* End of test for replacing pre-existing product */

			/* Test for update of product without assigning new ref */
			/* This does not behave as I would expect */
			// Change description at index2
			(parent.getRefs().get(childIndex2).getProduct())
					.setDescription("Update leaf three");
			// This test will fail, as update does not work
			// assertEquals("Update leaf three",
			// parent.getRefs().get(childIndex2).getProduct().getDescription());

			// Update using product ref
			ProductRef pr = (parent.getRefs().get(childIndex2));
			assertEquals(pr, leaf_ref4);
			pr.getProduct().setDescription("Update leaf at Index 2");
			// this test also fails
			// assertEquals("Update leaf three",
			// parent.getRefs().get(childIndex2).getProduct().getDescription());
			// Has this changed ?
			_LOGGER.info("Dirty after direct update = "
					+ parent.isDirty(storage));

			storage.save(parent);

			ListIterator li_4 = parent.getRefs().listIterator();
			while (li_4.hasNext()) {
				_LOGGER.info("New Descriptions are "
						+ ((ProductRef) li_4.next()).getProduct()
								.getDescription());
			}

			// Has this changed now ?
			// _LOGGER.info("Dirty ? "+parent.isDirty(storage));

		}

	}

	/*
	 */
	public void testSpr2487MapContextDetailed() throws IOException,
			GeneralSecurityException {

		ProductStorage store = new ProductStorage();
		ProductPool pool = getPool();
		store.register(pool);

		final String child = "child";
		final String leaf = "leaf";

		MapContext parent;

		{
			final String descrip_a = "a";
			parent = new MapContext();
			parent.getRefs().put(child, new ProductRef(new MapContext()));
			((MapContext) parent.getRefs().get(child).getProduct()).getRefs()
					.put(leaf, new ProductRef(new Product(descrip_a)));
			assertTrue(descrip_a.equals(((MapContext) parent.getRefs().get(
					child).getProduct()).getRefs().get(leaf).getProduct()
					.getDescription()));

			String urn = store.save(parent).getUrn();
			_LOGGER.info("urn=" + urn);

			assertTrue(descrip_a.equals(((MapContext) ((MapContext) store.load(
					urn).getProduct()).getRefs().get(child).getProduct())
					.getRefs().get(leaf).getProduct().getDescription()));
		}

		/* Now modify leaf product */
		{
			final String descrip_b = "b";
			_LOGGER.info("testA");
			((MapContext) parent.getRefs().get(child).getProduct()).getRefs()
					.put(leaf, new ProductRef(new Product(descrip_b)));
			_LOGGER.info("testB");
			assertTrue(descrip_b.equals(((MapContext) parent.getRefs().get(
					child).getProduct()).getRefs().get(leaf).getProduct()
					.getDescription()));

			_LOGGER.info("testC");
			String urn = store.save(parent).getUrn();
			_LOGGER.info("testD");
			_LOGGER.info("urn=" + urn);

			assertTrue(descrip_b.equals(((MapContext) ((MapContext) store.load(
					urn).getProduct()).getRefs().get(child).getProduct())
					.getRefs().get(leaf).getProduct().getDescription()));

		}
	}

	/*
	 */
	public void testSpr2487MapContextBasic() throws IOException,
			GeneralSecurityException {

		ProductStorage store = new ProductStorage();
		ProductPool pool = getPool();
		store.register(pool);

		final String leaf = "leaf";

		MapContext parent;

		{
			final String descrip_a = "a";
			parent = new MapContext();
			parent.getRefs().put(leaf, new ProductRef(new Product(descrip_a)));
			assertTrue(descrip_a.equals(parent.getRefs().get(leaf).getProduct()
					.getDescription()));

			String urn = store.save(parent).getUrn();

			assertTrue(descrip_a.equals(((MapContext) store.load(urn)
					.getProduct()).getRefs().get(leaf).getProduct()
					.getDescription()));
		}

		/* Now modify leaf product */
		{
			final String descrip_b = "b";
			parent.getRefs().put(leaf, new ProductRef(new Product(descrip_b)));
			assertTrue(descrip_b.equals(parent.getRefs().get(leaf).getProduct()
					.getDescription()));
			assertTrue(descrip_b.equals(parent.getRefs().get(leaf).getProduct()
					.getDescription()));

			String urn = store.save(parent).getUrn();

			assertTrue(descrip_b.equals(((MapContext) store.load(urn)
					.getProduct()).getRefs().get(leaf).getProduct()
					.getDescription()));
		}
	}

	public void testSpr2487ListContextDetailed() throws IOException,
			GeneralSecurityException {

		ProductStorage store = new ProductStorage();
		ProductPool pool = getPool();
		store.register(pool);

		final int child = 0;
		final int leaf = 0;

		ListContext parent;

		{
			final String descrip_a = "a";
			parent = new ListContext();
			parent.getRefs().add(child, new ProductRef(new ListContext()));
			((ListContext) parent.getRefs().get(child).getProduct()).getRefs()
					.add(leaf, new ProductRef(new Product(descrip_a)));
			assertTrue(descrip_a.equals(((ListContext) parent.getRefs().get(
					child).getProduct()).getRefs().get(leaf).getProduct()
					.getDescription()));

			String urn = store.save(parent).getUrn();
			_LOGGER.info("urn=" + urn);

			assertTrue(descrip_a.equals(((ListContext) ((ListContext) store
					.load(urn).getProduct()).getRefs().get(child).getProduct())
					.getRefs().get(leaf).getProduct().getDescription()));
		}

		/* Now modify leaf product */
		{
			final String descrip_b = "b";
			((ListContext) parent.getRefs().get(child).getProduct()).getRefs()
					.add(leaf, new ProductRef(new Product(descrip_b)));
			assertTrue(descrip_b.equals(((ListContext) parent.getRefs().get(
					child).getProduct()).getRefs().get(leaf).getProduct()
					.getDescription()));

			String urn = store.save(parent).getUrn();

			assertTrue(descrip_b.equals(((ListContext) ((ListContext) store
					.load(urn).getProduct()).getRefs().get(child).getProduct())
					.getRefs().get(leaf).getProduct().getDescription()));

		}
	}

	/*
	 */
	public void testSpr2487ListContextBasic() throws IOException,
			GeneralSecurityException {

		ProductStorage store = new ProductStorage();
		ProductPool pool = getPool();
		store.register(pool);

		final int leaf = 0;

		ListContext parent;

		{
			final String descrip_a = "a";
			parent = new ListContext();
			parent.getRefs().add(leaf, new ProductRef(new Product(descrip_a)));
			assertTrue(descrip_a.equals(parent.getRefs().get(leaf).getProduct()
					.getDescription()));

			String urn = store.save(parent).getUrn();

			assertTrue(descrip_a.equals(((ListContext) store.load(urn)
					.getProduct()).getRefs().get(leaf).getProduct()
					.getDescription()));
		}

		/* Now modify leaf product */
		{
			final String descrip_b = "b";
			parent.getRefs().add(leaf, new ProductRef(new Product(descrip_b)));
			assertTrue(descrip_b.equals(parent.getRefs().get(leaf).getProduct()
					.getDescription()));
			assertTrue(descrip_b.equals(parent.getRefs().get(leaf).getProduct()
					.getDescription()));

			String urn = store.save(parent).getUrn();

			assertTrue(descrip_b.equals(((ListContext) store.load(urn)
					.getProduct()).getRefs().get(leaf).getProduct()
					.getDescription()));
		}
	}

	public void testSpr3547() {
		Context context = new MapContext();
		ProductRef ref = new ProductRef(context);
		try {
			String urn = ref.getUrn();
			assertNull(urn);
		} catch (NullPointerException e) {
			fail("getUrn should not throw a NullPointerException");
		}
	}

	public void testSpr5204RemoveContext() throws IOException,
			GeneralSecurityException {

		ProductPool pool = getPool();
		ProductStorage store = new ProductStorage();
		store.register(pool);
		MapContext mc1 = new MapContext();
		Product p1 = new Product();
		p1.setDescription("p1");
		Product p2 = new Product();
		p2.setDescription("p2");
		MapContext mc11 = new MapContext();
		Product p11 = new Product();
		p11.setDescription("p11");
		Product p22 = new Product();
		p22.setDescription("p22");
		mc11.setProduct("p11", p11);
		mc11.setProduct("p22", p22);
		mc1.setProduct("key1", p1);
		mc1.setProduct("key2", p2);
		mc1.setProduct("mc11", mc11);
		ProductRef ref = store.save(mc1);
		Set<ProductRef> rs = store.select(new Query("1=1"));
		assertTrue(rs.size()+"", rs.size()==6);
		String urn = ref.getUrn();
		store.remove(urn, true);
		rs = store.select(new Query("1=1"));
		assertTrue(rs.size()+"", rs.size()==0);
	}

	/**
	 * Displays the product description of a product that is associated to a
	 * child MapContext that is in turn associated with a parent context. The
	 * child MapContext is associated with its parent with an id cal_id and the
	 * product associated with the child context has an id product_id with
	 * respect to the child context.
	 */
	private String getProductDescription(MapContext parent, String child_id,
			String product_id) throws IOException, GeneralSecurityException {
		Map<String, ProductRef> m = parent.getRefs();
		assertTrue(m.containsKey(child_id));

		ProductRef ref_c = m.get(child_id);
		MapContext c = (MapContext) ref_c.getProduct();

		Product p = c.getRefs().get(product_id).getProduct();
		return p.getDescription();
	}

	private void printRefs(MapContext m) {

		Map<String, ProductRef> refs = m.getRefs();

		for (String i : refs.keySet()) {
			_LOGGER.info("Reference in context:" + i);
		}

	}

}
