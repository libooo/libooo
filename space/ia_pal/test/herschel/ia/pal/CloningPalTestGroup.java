package herschel.ia.pal;

import herschel.ia.dataset.Product;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CloningPalTestGroup extends AbstractPalTestGroup {

    @SuppressWarnings("unused")
    private static final Logger _LOGGER = Logger.getLogger("CloningTest");

	public CloningPalTestGroup(String name, AbstractPalTestCase testcase) {
		super(name, testcase);
	}

    /**
     * <p>Clone a leaf product from a 'source' storage (storageOne)
     * to one of two 'target' storages (storageTwo and storageThree).</p>
     * <p>The contents of the storages is as follows:
     * <ol>
     * <li>storageOne contains a single pool poolOne. This contains the original leaf product</li>
     * <li>storageTwo contains a single pool poolTwo. Thus storageTwo does not initial contain
     * any data covered by the source storage storageOne.</li>
     * <li>storageThree contains a single pool poolOne, ie the same data as the original
     * source storage storageOne.</li>
     * </li>
     * </p>
     * 
     * <p>A cloning attempt between storageOne and storageTwo should result in a copy
     * of the leaf product to storageTwo.</p>
     * 
     * <p>A cloning attempt between storageOne and storageThree should result in no new copies
     * being generated.</p>
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     * 
     */
    public void testLeafProductClone() throws IOException, GeneralSecurityException {

        final String descr = "myproduct";

        ProductStorage storageOriginal;
        ProductStorage storageTwo;
        ProductStorage storageThree;

        ProductPool poolOne = getPool(1);
        ProductPool poolTwo = getPool(2);

        /* Initialise product storages */
        String urn;
        {
            storageOriginal = new ProductStorage();
            storageOriginal.register(poolOne);
            Product p = new Product(descr);
            urn = storageOriginal.save(p).getUrn();
        }

        /*
         * Create a storage containing a single pool that differs from that
         * pointed to by original storage
         */
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolTwo);
        }

        /*
         * Create a storage containing a single pool that is the same as that
         * pointed to by original storage
         */
        {
            storageThree = new ProductStorage();
            storageThree.register(poolOne);
        }

        {
            /*
             * Clone from storageOriginal to storageTwo. Result should be a new
             * copy of product in storageTwo.
             */
//          ProductRef tr = StorageCloner.cloneProduct(urn, storageOriginal,
//                  storageTwo);
            ProductRef tr = storageTwo.save(storageOriginal.load(urn).getProduct());

            assertFalse(urn.equals(tr.getUrn()));
            assertTrue(tr.getProduct().getDescription().equals(descr));
        }

        {
            /*
             * Clone from storageOriginal to storageThree. Operation should
             * produce no new products in storageTwo, as product in original
             * storage is also available in storageThree.
             */
//          ProductRef tr = StorageCloner.cloneProduct(urn, storageOriginal,
//                  storageThree);
            _LOGGER.info("urn of context to be cloned " + urn);
            ProductRef tr = storageThree.save(storageOriginal.load(urn).getProduct());
            _LOGGER.info("urn of context that is cloned " + tr.getUrn());

            assertTrue(urn.equals(tr.getUrn()));
            assertTrue(tr.getProduct().getDescription().equals(descr));
        }
    }

    /**
     * <p>
     * Clone a ListContext referencing data entirely in one pool, from one
     * storage to another.
     * </p>
     * 
     * <p>
     * There is one source storage (storageOne) and two target storages
     * (storageTwo and storageThree). The contents of the storages is as
     * follows:
     * <ol>
     * <li>storageOne contains a single pool poolOne. This contains the
     * original ListContext.</li>
     * <li>storageTwo contains a single pool poolTwo. Thus storageTwo does not
     * initial contain any data covered by the source storage storageOne.</li>
     * <li>storageThree contains a single pool poolOne, ie the same data as the
     * original source storage storageOne.</li>
     * </li>
     * </p>
     * 
     * <p>
     * A cloning attempt between storageOne and storageTwo should result in a
     * copy of the leaf product to storageTwo.
     * </p>
     * 
     * <p>
     * A cloning attempt between storageOne and storageThree should result in no
     * new copies being generated.
     * </p>
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     * 
     */
    public void testListContextSimpleClone() throws IOException,
            GeneralSecurityException {

        final String descr = "myproduct";
        final int product_id = 0;

        ProductStorage storageOriginal;
        ProductStorage storageTwo;
        ProductStorage storageThree;
        ProductStorage storageFour;

        ProductPool poolOne  = getPool(1);
        ProductPool poolTwo  = getPool(2);
        ProductPool poolFour = getPool(4);

        /* Initialise product storages */
        String c_urn;
        String p_urn;
        ListContext c;
        {
            storageOriginal = new ProductStorage();
            storageOriginal.register(poolOne);
            Product p = new Product(descr);
            ProductRef p_r = storageOriginal.save(p);
            p_urn = p_r.getUrn();
            c = new ListContext();
            c.getRefs().add(product_id, p_r);
            c_urn = storageOriginal.save(c).getUrn();
        }

        /*
         * Create a storage containing a single pool that differs from that
         * pointed to by original storage
         */
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolTwo);
        }

        /*
         * Create another storage containing a single pool that differs from that
         * pointed to by original storage
         */
        {
            storageFour = new ProductStorage();
            storageFour.register(poolFour);
        }

        /*
         * Create a storage containing a single pool that is the same as that
         * pointed to by original storage
         */
        {
            storageThree = new ProductStorage();
            storageThree.register(poolOne);
        }

        {
            /*
             * Clone from storageOriginal to storageTwo. Result should be a new
             * copy of product in storageTwo and a new copy of the parent
             * context.
             */
            _LOGGER.info("storageOriginal=" + storageOriginal + " storageTwo=" + storageTwo);
            ProductRef tr = storageTwo.save(storageOriginal.load(c_urn).getProduct());

            _LOGGER.info("urn of original:" + c_urn + " urn of cloned:" + tr.getUrn());
            assertFalse(c_urn.equals(tr.getUrn()));

            ListContext tc = (ListContext) tr.getProduct();

            ProductRef tp_r = tc.getRefs().get(product_id);
            assertFalse(tp_r.getUrn().equals(p_urn));

        }

        {
            /*
             * As above but instead of starting from a URN, use the original context instance.
             */
            _LOGGER.info("storageOriginal=" + storageOriginal + " storageFour=" + storageFour);
            ProductRef tr = storageFour.save(c);
            _LOGGER.info("urn of original:" + c_urn + " urn of cloned:" + tr.getUrn());
            assertFalse(c_urn.equals(tr.getUrn()));

            ListContext tc = (ListContext) tr.getProduct();

            ProductRef tp_r = tc.getRefs().get(product_id);
            assertFalse(tp_r.getUrn().equals(p_urn));

        }

        {
            /*
             * Clone from storageOriginal to storageThree. Operation should
             * produce no new products in storageTwo, as product in original
             * storage is also available in storageThree.
             */
            _LOGGER.info("storageOriginal=" + storageOriginal + " storageThree=" + storageThree);
            ProductRef tr = storageThree.save(storageOriginal.load(c_urn).getProduct());
            _LOGGER.info("urn of original:" + c_urn + " urn of cloned:" + tr.getUrn());

            assertTrue(c_urn.equals(tr.getUrn()));

            ListContext tc = (ListContext) tr.getProduct();

            ProductRef tp_r = tc.getRefs().get(product_id);
            assertTrue(tp_r.getUrn().equals(p_urn));
        }

    }

    /**
     * <p>
     * Clone a ListContext referencing data across two pools, from one storage
     * to another.
     * </p>
     * 
     * <p>
     * There is one source storage (storageOne) and two target storages
     * (storageTwo and storageThree). The contents of the storages is as
     * follows:
     * <ol>
     * <li>storageOne contains two pools poolOne and poolTwo. The ListContext
     * data is distributed across the two pools (one of its child leaf products
     * is in poolTwo, the rest including the ListContext itself in poolOne).</li>
     * <li>storageTwo contains two pools poolTwo and poolThree. Thus storageTwo
     * contains some but not all the data referenced by the ListContext.</li>
     * <li>storageThree contains the exact same pools source storage
     * storageOne.</li>
     * </li>
     * </p>
     * 
     * <p>
     * A cloning attempt between storageOne and storageTwo should result in a
     * copy of the data referenced by the ListContext in poolOne to poolTwo.
     * </p>
     * 
     * <p>
     * A cloning attempt between storageOne and storageThree should result in no
     * new copies being generated.
     * </p>
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     * 
     */
    public void testListContextComplexClone() throws IOException,
            GeneralSecurityException {

        final String desc_one = "myproduct one";
        final int product_id_one = 0;

        final String desc_two = "myproduct two";
        final int product_id_two = 1;

        ProductStorage storageOne;
        ProductStorage storageTwo;
        ProductStorage storageThree;
        ProductPool poolOne   = getPool(1);
        ProductPool poolTwo   = getPool(2);
        ProductPool poolThree = getPool(3);

        /* Initialise product storages */
        String c_urn;
        String p1_urn;
        String p2_urn;
        {
            ProductRef p2_r;
            {
                ProductStorage storageTemp = new ProductStorage();
                storageTemp.register(poolTwo);
                Product p2 = new Product(desc_two);
                p2_r = storageTemp.save(p2);
                p2_urn = p2_r.getUrn();
            }

            storageOne = new ProductStorage();
            storageOne.register(poolOne);
            storageOne.register(poolTwo);
            Product p = new Product(desc_one);
            ProductRef p1_r = storageOne.save(p);
            p1_urn = p1_r.getUrn();

            ListContext c = new ListContext();
            c.getRefs().add(product_id_one, p1_r);

            /* Now assign product stored in poolTwo to same list context */
            c.getRefs().add(product_id_two, p2_r);

            c_urn = storageOne.save(c).getUrn();
        }

        /*
         * Create a storage containing one pool that is also pointed to in the
         * original storage, and one pool that is not. The data in the original
         * storage that is in a pool not 'visible' to the target storage will be
         * cloned to poolThree.
         */
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolThree);
            storageTwo.register(poolOne);
        }

        /*
         * Create a storage containing the same pools as that pointed to by
         * original storage
         */
        {
            storageThree = new ProductStorage();
            storageThree.register(poolOne);
            storageThree.register(poolTwo);
        }

        {
            /*
             * Clone from storageOne to storageTwo. Result should be a new
             * copy of product in poolThree, that also results in the ListContext
             * also being cloned.
             */
            ProductRef tr = storageTwo.save(storageOne.load(c_urn).getProduct());

            _LOGGER.info("c_urn=" + c_urn + " tr.urn=" + tr.getUrn());
            assertFalse("original urn " + c_urn + " and final urn " +
        	        tr.getUrn() + " should differ",
        	        c_urn.equals(tr.getUrn()));

            ListContext tc = (ListContext) tr.getProduct();

            ProductRef tp1_r = tc.getRefs().get(product_id_one);
            assertTrue(tp1_r.getUrn().equals(p1_urn));

            ProductRef tp2_r = tc.getRefs().get(product_id_two);
            assertFalse(tp2_r.getUrn().equals(p2_urn));

        }

        /*
         * Clone from storageOriginal to storageThree. Operation should produce
         * no new products in storageTwo, as product in original storage is also
         * available in storageThree.
         */
        ProductRef tr = storageThree.save(storageOne.load(c_urn).getProduct());


        _LOGGER.info("Got urn: " + tr.getUrn() + ". Original= " + c_urn);
        assertTrue(c_urn.equals(tr.getUrn()));
        ListContext tc = (ListContext) tr.getProduct();

        ProductRef tp1_r = tc.getRefs().get(product_id_one);
        assertTrue(tp1_r.getUrn().equals(p1_urn));

        ProductRef tp2_r = tc.getRefs().get(product_id_two);
        assertTrue(tp2_r.getUrn().equals(p2_urn));

    }

    /**
     * Simple test of cloning ListContext that has multiple versions. A
     * ListContext with multiple versions residing in one pool is cloned to
     * another
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void testListContextVersion() throws IOException,
            GeneralSecurityException {

        final int numberOfVersions = 5;
        final String descr_root = "product";
        final int product_id = 0;

        /* Populate product descriptions */
        List<String> descr = new ArrayList<String>();
        for (int i = 0; i < numberOfVersions; ++i) {
            descr.add(descr_root + "_" + i);
        }

        ProductStorage storageOriginal;
        ProductStorage storageTwo;
        ProductPool poolOne = createPool(1);
        ProductPool poolTwo = createPool(2);

        String urn = null;

        /* Populate original data */
        {
            storageOriginal = new ProductStorage();
            storageOriginal.register(poolOne);

            /* Create an initial mapcontext with a single leaf product */
            ListContext l = new ListContext();
            {
                Product p = new Product(descr.get(0));
                ProductRef p_r = storageOriginal.save(p);
                l.getRefs().add(product_id, p_r);
                urn = storageOriginal.save(l).getUrn();
            }

            /*
             * Now repeatedly modify the listcontext by assigning different new
             * leaf product. This should create multiple versions of context.
             */
            for (int i = 1; i < numberOfVersions; ++i) {

                Product p = new Product(descr.get(i));
                ProductRef p_r = storageOriginal.save(p);
                l.getRefs().set(product_id, p_r);
                urn = storageOriginal.save(l).getUrn();
            }
        }

        /* Validate original data */
        {
            storageOriginal = new ProductStorage();
            storageOriginal.register(poolOne);

            ProductRef l_r = storageOriginal.load(urn);
            List<ProductRef> lcVersions = storageOriginal.getVersions(l_r);
            _LOGGER.info("Number of versions of original listcontext stored: "
                     + lcVersions.size());
            assertEquals(numberOfVersions, lcVersions.size());

            for (ProductRef lv_r : lcVersions) {
                ListContext lv = (ListContext) lv_r.getProduct();
                String desc = lv.getRefs().get(product_id).getProduct()
                        .getDescription();
                _LOGGER.info("Desc:" + desc);
                assertTrue(descr.contains(desc));
            }

        }

        /* Clone data from poolOne to poolTwo */
        String urnCloned;
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolTwo);

            _LOGGER.info("TEST-A : transfering urn " + urn + " to storage " + storageTwo);
            urnCloned = storageTwo.save(storageOriginal.load(urn).getProduct()).getUrn();
            _LOGGER.info("TEST-B");
        }

        /* Verify contents of clone */
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolTwo);

            _LOGGER.info("Loading versions of urn " + urnCloned + " from storage " + storageTwo);
            ProductRef l_r = storageTwo.load(urnCloned);
            List<ProductRef> lcVersions = storageOriginal.getVersions(l_r);
            // Behaviour of save changed: versions are not cloned
//            assertEquals(numberOfVersions, lcVersions.size());

            for (ProductRef lv_r : lcVersions) {
                _LOGGER.info("version's urn:" + lv_r.getUrn());
                ListContext lv = (ListContext) lv_r.getProduct();
                String desc = lv.getRefs().get(product_id).getProduct()
                        .getDescription();
                _LOGGER.info("Desc of cloned product:" + desc);
                assertTrue(descr.contains(desc));
            }
        }

    }

    /**
     * <p>
     * Clone a MapContext referencing data entirely in one pool, from one
     * storage to another.
     * </p>
     * 
     * <p>
     * There is one source storage (storageOne) and two target storages
     * (storageTwo and storageThree). The contents of the storages is as
     * follows:
     * <ol>
     * <li>storageOne contains a single pool poolOne. This contains the
     * original MapContext.</li>
     * <li>storageTwo contains a single pool poolTwo. Thus storageTwo does not
     * initial contain any data covered by the source storage storageOne.</li>
     * <li>storageThree contains a single pool poolOne, ie the same data as the
     * original source storage storageOne.</li>
     * </li>
     * </p>
     * 
     * <p>
     * A cloning attempt between storageOne and storageTwo should result in a
     * copy of the leaf product to storageTwo.
     * </p>
     * 
     * <p>
     * A cloning attempt between storageOne and storageThree should result in no
     * new copies being generated.
     * </p>
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     * 
     */
    public void testMapContextSimpleClone() throws IOException,
            GeneralSecurityException {

        final String descr = "myproduct";
        final String product_id = "product";

        ProductStorage storageOriginal;
        ProductStorage storageTwo;
        ProductStorage storageThree;
        ProductStorage storageFour;

        ProductPool poolOne  = getPool(1);
        ProductPool poolTwo  = getPool(2);
        ProductPool poolFour = getPool(4);

        /* Initialise product storages */
        String c_urn;
        String p_urn;
        MapContext c;
        {
            storageOriginal = new ProductStorage();
            storageOriginal.register(poolOne);
            Product p = new Product(descr);
            ProductRef p_r = storageOriginal.save(p);
            p_urn = p_r.getUrn();
            c = new MapContext();
            c.getRefs().put(product_id, p_r);
            c_urn = storageOriginal.save(c).getUrn();
        }

        /*
         * Create a storage containing a single pool that differs from that
         * pointed to by original storage
         */
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolTwo);
        }

        /*
         * Create another storage containing a single pool that differs from that
         * pointed to by original storage
         */
        {
            storageFour = new ProductStorage();
            storageFour.register(poolFour);
        }

        /*
         * Create a storage containing a single pool that is the same as that
         * pointed to by original storage
         */
        {
            storageThree = new ProductStorage();
            storageThree.register(poolOne);
        }

        {
            /*
             * Clone from storageOriginal to storageTwo. Result should be a new
             * copy of product in storageTwo and a new copy of the parent
             * context.
             */
            ProductRef tr = storageTwo.save(storageOriginal.load(c_urn).getProduct());

            assertFalse(c_urn.equals(tr.getUrn()));

            MapContext tc = (MapContext) tr.getProduct();

            ProductRef tp_r = tc.getRefs().get(product_id);
            assertFalse(tp_r.getUrn().equals(p_urn));

        }

        {
            /*
             * As above, but use the original MapContext instance.
             */
            ProductRef tr = storageTwo.save(c);

            assertFalse(c_urn.equals(tr.getUrn()));

            MapContext tc = (MapContext) tr.getProduct();

            ProductRef tp_r = tc.getRefs().get(product_id);
            assertFalse(tp_r.getUrn().equals(p_urn));

        }

        {
            /*
             * Clone from storageOriginal to storageThree. Operation should
             * produce no new products in storageTwo, as product in original
             * storage is also available in storageThree.
             */
            ProductRef tr = storageThree.save(storageOriginal.load(c_urn).getProduct());

            assertTrue(c_urn.equals(tr.getUrn()));

            MapContext tc = (MapContext) tr.getProduct();

            ProductRef tp_r = tc.getRefs().get(product_id);
            assertTrue(tp_r.getUrn().equals(p_urn));
        }

    }

    /**
     * <p>
     * Clone a MapContext referencing data across two pools, from one storage to
     * another.
     * </p>
     * 
     * <p>
     * There is one source storage (storageOne) and two target storages
     * (storageTwo and storageThree). The contents of the storages is as
     * follows:
     * <ol>
     * <li>storageOne contains two pools poolOne and poolTwo. The MapContext
     * data is distributed across the two pools (one of its child leaf products
     * is in poolTwo, the rest including the MapContext itself in poolOne).</li>
     * <li>storageTwo contains two pools poolTwo and poolThree. Thus storageTwo
     * contains some but not all the data referenced by the MapContext.</li>
     * <li>storageThree contains the exact same pools source storage
     * storageOne.</li>
     * </li>
     * </p>
     * 
     * <p>
     * A cloning attempt between storageOne and storageTwo should result in a
     * copy of the data referenced by the MapContext in poolOne to poolTwo.
     * </p>
     * 
     * <p>
     * A cloning attempt between storageOne and storageThree should result in no
     * new copies being generated.
     * </p>
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     * 
     */
    public void testMapContextComplexClone() throws IOException,
            GeneralSecurityException {

        final String desc_one = "myproduct one";
        final String product_id_one = "product_1";

        final String desc_two = "myproduct two";
        final String product_id_two = "product_2";

        ProductStorage storageOriginal;
        ProductStorage storageTwo;
        ProductStorage storageThree;

        ProductPool poolOne   = getPool(1);
        ProductPool poolTwo   = getPool(2);
        ProductPool poolThree = getPool(3);

        /* Initialise product storages */
        String c_urn;
        String p1_urn;
        String p2_urn;
        {
            ProductRef p2_r;
            {
                ProductStorage storageTemp = new ProductStorage();
                storageTemp.register(poolTwo);
                Product p2 = new Product(desc_two);
                p2_r = storageTemp.save(p2);
                p2_urn = p2_r.getUrn();
            }

            storageOriginal = new ProductStorage();
            storageOriginal.register(poolOne);
            storageOriginal.register(poolTwo);
            Product p = new Product(desc_one);
            ProductRef p1_r = storageOriginal.save(p);
            p1_urn = p1_r.getUrn();

            MapContext c = new MapContext();
            c.getRefs().put(product_id_one, p1_r);

            /* Now assign product stored in poolTwo to same map context */
            c.getRefs().put(product_id_two, p2_r);

            c_urn = storageOriginal.save(c).getUrn();
        }

        /*
         * Create a storage containing one pool that is also pointed to in the
         * original storage, and one pool that is not. The data in the original
         * storage that is in a pool not 'visible' to the target storage will be
         * cloned to poolThree.
         */
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolThree);
            storageTwo.register(poolOne);
        }

        /*
         * Create a storage containing the same pools as that pointed to by
         * original storage
         */
        {
            storageThree = new ProductStorage();
            storageThree.register(poolOne);
            storageThree.register(poolTwo);
        }

        {
            /*
             * Clone from storageOriginal to storageTwo. Result should be a new
             * copy of product in poolTwo, that also results in the MapContext
             * also being cloned.
             */
            ProductRef tr = storageTwo.save(storageOriginal.load(c_urn).getProduct());

            assertFalse(c_urn.equals(tr.getUrn()));

            MapContext tc = (MapContext) tr.getProduct();

            ProductRef tp1_r = tc.getRefs().get(product_id_one);
            assertTrue(tp1_r.getUrn().equals(p1_urn));

            ProductRef tp2_r = tc.getRefs().get(product_id_two);
            assertFalse(tp2_r.getUrn().equals(p2_urn));

        }

        /*
         * Clone from storageOriginal to storageThree. Operation should produce
         * no new products in storageTwo, as product in original storage is also
         * available in storageThree.
         */
        ProductRef tr = storageThree.save(storageOriginal.load(c_urn).getProduct());

        assertTrue(c_urn.equals(tr.getUrn()));
        MapContext tc = (MapContext) tr.getProduct();

        ProductRef tp1_r = tc.getRefs().get(product_id_one);
        assertTrue(tp1_r.getUrn().equals(p1_urn));

        ProductRef tp2_r = tc.getRefs().get(product_id_two);
        assertTrue(tp2_r.getUrn().equals(p2_urn));

    }

    /**
     * <p>
     * Clone a Context referencing data across two pools, from one storage to
     * another.
     * </p>
     * 
     * <p>
     * The context contains a 3-level hierarchy of:
     * <ol>
     * <li>a MapContext that contains:
     * <ol>
     * <li>a LeafProduct</li>
     * <li>a ListContext that contains:
     * <ol>
     * <li>a leaf product</li>
     * </ol>
     * </li>
     * </ol>
     * </li>
     * </ol>
     * </p>
     * 
     * <p>
     * The MapContext and child leaf product live on one pool while the
     * ListContext and its child leaf product lives on the other pool.
     * </p>
     * 
     * <p>
     * There are two target storages. The first target storage contains two
     * pools - one being a wholly different pool to the original and one pool
     * that is also referenced in the original storage.
     * </p>
     * 
     * <p>
     * The other target storage contains the exact same pools as the original.
     * </p>
     * 
     * <p>
     * Cloning to the former target should result in a new cloned product, while
     * cloning to the latter target should result in no new copies made.<?p>
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     * 
     */
    public void testMapAndListContextComplexClone() throws IOException,
            GeneralSecurityException {

        final String desc_one = "leafproduct of mapcontext";
        final String mapcontext_product_id_one = "product_1";
        final String mapcontext_product_id_two = "product_2";

        final String desc_two = "leafproduct of listcontext";
        final int listcontext_product_id = 0;

        ProductStorage storageOriginal;
        ProductStorage storageTwo;
        ProductStorage storageThree;
        ProductPool poolOne   = getPool(1);
        ProductPool poolTwo   = getPool(2);
        ProductPool poolThree = getPool(3);

        /* Initialise product storages */
        String m_urn;
        String l_urn;
        String p1_urn;
        String p2_urn;
        {
            ProductRef p2_r;
            ProductRef l_r;
            {
                ProductStorage storageTemp = new ProductStorage();
                storageTemp.register(poolTwo);
                Product p2 = new Product(desc_two);
                p2_r = storageTemp.save(p2);
                p2_urn = p2_r.getUrn();
                ListContext l = new ListContext();
                l.getRefs().add(listcontext_product_id, p2_r);
                l_r = storageTemp.save(l);
                l_urn = l_r.getUrn();
            }

            storageOriginal = new ProductStorage();
            storageOriginal.register(poolOne);
            storageOriginal.register(poolTwo);
            Product p = new Product(desc_one);
            ProductRef p1_r = storageOriginal.save(p);
            p1_urn = p1_r.getUrn();

            MapContext c = new MapContext();
            c.getRefs().put(mapcontext_product_id_one, p1_r);

            /* Now assign listcontext stored in poolTwo to same map context */
            c.getRefs().put(mapcontext_product_id_two, l_r);

            m_urn = storageOriginal.save(c).getUrn();
        }

        /*
         * Create a storage containing one pool that is also pointed to in the
         * original storage, and one pool that is not. The data in the original
         * storage that is in a pool not 'visible' to the target storage will be
         * cloned to poolThree.
         */
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolThree);
            storageTwo.register(poolOne);
        }

        /*
         * Create a storage containing the same pools as that pointed to by
         * original storage
         */
        {
            storageThree = new ProductStorage();
            storageThree.register(poolOne);
            storageThree.register(poolTwo);
        }

        {
            /*
             * Clone from storageOriginal to storageTwo. Result should be a new
             * copy of listcontext and leaf product in poolTwo, that also
             * results in the MapContext also being cloned.
             */
            ProductRef tr = storageTwo.save(storageOriginal.load(m_urn).getProduct());

            assertFalse(m_urn.equals(tr.getUrn()));

            MapContext tm = (MapContext) tr.getProduct();

            ProductRef tp1_r = tm.getRefs().get(mapcontext_product_id_one);
            assertTrue(tp1_r.getUrn().equals(p1_urn));

            ProductRef tl_r = tm.getRefs().get(mapcontext_product_id_two);
            assertFalse(tl_r.getUrn().equals(l_urn));

            ListContext tl = (ListContext) tl_r.getProduct();
            ProductRef tp2_r = tl.getRefs().get(listcontext_product_id);
            assertFalse(tp2_r.getUrn().equals(p2_urn));

        }

        /*
         * Clone from storageOriginal to storageThree. Operation should produce
         * no new products in storageTwo, as product in original storage is also
         * available in storageThree.
         */
        ProductRef tr = storageThree.save(storageOriginal.load(m_urn).getProduct());

        assertTrue(m_urn.equals(tr.getUrn()));

        MapContext tm = (MapContext) tr.getProduct();

        ProductRef tp1_r = tm.getRefs().get(mapcontext_product_id_one);
        assertTrue(tp1_r.getUrn().equals(p1_urn));

        ProductRef tl_r = tm.getRefs().get(mapcontext_product_id_two);
        assertTrue(tl_r.getUrn().equals(l_urn));

        ListContext tl = (ListContext) tl_r.getProduct();
        ProductRef tp2_r = tl.getRefs().get(listcontext_product_id);
        assertTrue(tp2_r.getUrn().equals(p2_urn));

    }

    /**
     * Simple test of cloning MapContext that has multiple versions. A
     * MapContext with multiple versions residing in one pool is cloned to
     * another
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     */
//  public void testBasicVersioning() throws IOException,
//          GeneralSecurityException {
//
//      final int numberOfVersions = 5;
//      final String descr_root = "product";
//      final String product_id = "leafproduct";
//
//      /* Populate product descriptions */
//      List<String> descr = new ArrayList<String>();
//      for (int i = 0; i < numberOfVersions; ++i) {
//          descr.add(descr_root + "_" + i);
//      }
//
//      ProductStorage storageOriginal;
//      ProductStorage storageTwo;
//      ProductPool poolOne = createPool(_POOLID_ONE);
//      ProductPool poolTwo = createPool(_POOLID_TWO);
//
//      String urn = null;
//
//      /* Populate original data */
//      {
//          storageOriginal = new ProductStorage();
//          storageOriginal.register(poolOne);
//
//          /* Create an initial mapcontext with a leaf product */
//          MapContext m = new MapContext();
//          {
//              Product p = new Product(descr.get(0));
//              ProductRef p_r = storageOriginal.save(p);
//              m.getRefs().put(product_id, p_r);
//              urn = storageOriginal.save(m).getUrn();
//          }
//
//          /* Now repeatedly modify the mapcontext by 
//           * assigning different new leaf product.
//           * This should create multiple versions of context. */
//          for (int i = 1; i < numberOfVersions; ++i) {
//
//              Product p = new Product(descr.get(i));
//              ProductRef p_r = storageOriginal.save(p);
//              m.getRefs().put(product_id, p_r);
//              urn = storageOriginal.save(m).getUrn();
//          }
//      }
//
//      /* Validate original data */
//      {
//          storageOriginal = new ProductStorage();
//          storageOriginal.register(poolOne);
//
//          ProductRef m_r = storageOriginal.load(urn);
//          MapContext m = (MapContext) m_r.getProduct();
//
//          _LOGGER.info("Number of versions of original mapcontext stored: " + m.getVersions().size());
//          assertTrue(m.getVersions().size() == numberOfVersions);
//
//          for (ProductRef mv_r : m.getVersions()) {
//              MapContext mv = (MapContext) mv_r.getProduct();
//              String desc = mv.getRefs().get(product_id).getProduct()
//                      .getDescription();
//              _LOGGER.info("Desc:" + desc);
//              assertTrue(descr.contains(desc));
//          }
//
//          _LOGGER.info("original's versions: " + m.getVersions());
//
//      }
//
//      /* Clone data from poolOne to poolTwo */
//      String urnCloned;
//      {
//          storageTwo = new ProductStorage();
//          storageTwo.register(poolTwo);
//
//          urnCloned = StorageCloner.cloneProduct(urn, storageOriginal, storageTwo).getUrn();
//      }
//
//      /* Verify contents of clone */
//      {
//          storageTwo = new ProductStorage();
//          storageTwo.register(poolTwo);
//
//          _LOGGER.info("Loading clone of data at " + urnCloned);
//          ProductRef m_r = storageTwo.load(urnCloned);
//          MapContext m = (MapContext) m_r.getProduct();
//          _LOGGER.info("clone's versions: " + m.getVersions());
//          assertTrue(m.getVersions().size() == numberOfVersions);
//          for (ProductRef mv_r : m.getVersions()) {
//              MapContext mv = (MapContext) mv_r.getProduct();
//              String desc = mv.getRefs().get(product_id).getProduct()
//                      .getDescription();
//              _LOGGER.info("Desc of cloned product:" + desc);
//              assertTrue(descr.contains(desc));
//          }
//
//      }
//
//      /* Clone data back from poolTwo to poolOne and verify */
//      {
//          String urnReCloned = StorageCloner.cloneProduct(urnCloned, storageTwo, storageOriginal).getUrn();
//
//          _LOGGER.info("Loading clone of data at " + urnReCloned);
//          ProductRef m_r = storageOriginal.load(urnReCloned);
//          MapContext m = (MapContext) m_r.getProduct();
//
//          assertTrue(m.getVersions().size() == numberOfVersions);
//          for (ProductRef mv_r : m.getVersions()) {
//              MapContext mv = (MapContext) mv_r.getProduct();
//              String desc = mv.getRefs().get(product_id).getProduct()
//                      .getDescription();
//              _LOGGER.info("Desc of cloned product:" + desc);
//              assertTrue(descr.contains(desc));
//          }
//
//      }
//
//  }

    /**
     * Simple test of cloning MapContext that has multiple versions. A
     * MapContext with multiple versions residing in one pool is cloned to
     * another
     */
    public void testMapContextVersion() throws IOException,
            GeneralSecurityException {

        final int nVersions = 5;
        final String descPrefix = "product";
        final String productId = "leafproduct";

        /* Populate product descriptions */
        List<String> descriptions = new ArrayList<String>();
        for (int i = 0; i < nVersions; ++i) {
            descriptions.add(descPrefix + "_" + i);
        }

        ProductStorage storageOne;
        ProductStorage storageTwo;
        ProductPool poolOne = getPool(1);
        ProductPool poolTwo = getPool(2);

        String desc, urn = null;

        /* Populate original data */
        {
            storageOne = new ProductStorage();
            storageOne.register(poolOne);

            /* Create an initial mapcontext with a leaf product */
            MapContext mc = new MapContext();
            {
                Product p = new Product(descriptions.get(0));
                ProductRef p_r = storageOne.save(p);
                mc.getRefs().put(productId, p_r);
                urn = storageOne.save(mc).getUrn();
            }

            /* Now repeatedly modify the mapcontext by 
             * assigning different new leaf product.
             * This should create multiple versions of context. */
            for (int i = 1; i < nVersions; ++i) {

                Product p = new Product(descriptions.get(i));
                ProductRef p_r = storageOne.save(p);
                mc.getRefs().put(productId, p_r);
                urn = storageOne.save(mc).getUrn();
            }
        }

        /* Validate original data */
        {
            storageOne = new ProductStorage();
            storageOne.register(poolOne);

            ProductRef mc_r = storageOne.load(urn);
            List<ProductRef> mcVersions = storageOne.getVersions(mc_r);

            _LOGGER.info("Number of versions of original mapcontext stored: " + mcVersions.size());
            assertEquals(nVersions, mcVersions.size());

            for (ProductRef mv_r : mcVersions) {
                MapContext mv = (MapContext) mv_r.getProduct();
                desc = mv.getRefs().get(productId).getProduct().getDescription();
                _LOGGER.info("Desc: " + desc);
                assertTrue(descriptions.contains(desc));
            }

            _LOGGER.info("original's versions: " + mcVersions);

        }

        /* Clone data from poolOne to poolTwo */
        String urnCloned;
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolTwo);

            urnCloned = storageTwo.save(storageOne.load(urn).getProduct()).getUrn();
        }

        /* Verify contents of clone */
        {
            storageTwo = new ProductStorage();
            storageTwo.register(poolTwo);

            _LOGGER.info("Loading clone of data at " + urnCloned);
            ProductRef m_r = storageTwo.load(urnCloned);
            List<ProductRef> mcVersions = storageTwo.getVersions(m_r);
            _LOGGER.info("clone's versions: " + mcVersions);
            // Behaviour of save changed: versions are not cloned
//            assertEquals(nVersions, mcVersions.size());
            for (ProductRef mv_r : mcVersions) {
                MapContext mv = (MapContext) mv_r.getProduct();
                desc = mv.getRefs().get(productId).getProduct().getDescription();
                _LOGGER.info("Desc of cloned product:" + desc);
                assertTrue(descriptions.contains(desc));
            }

        }

        /* Clone data back from poolTwo to poolOne and verify */
        {
            String urnReCloned = storageOne.save(storageTwo.load(urnCloned).getProduct()).getUrn();

            _LOGGER.info("Loading clone of data at " + urnReCloned);
            ProductRef m_r = storageOne.load(urnReCloned);
            List<ProductRef> mcVersions = storageTwo.getVersions(m_r);

            // [HS 30-Jul-2007: Change in behaviour of versioning:
            // There are numberOfVersions versions of product in poolA
            // Cloning data back from poolTwo to poolOne results in a
            // numberOfVersions *further* versions being added to poolA.
            // Therefore total number of versions in original is
            // 2 * _numberOfVersions.
            // Behaviour of save changed: versions are not cloned
//            int nExpectedVersions = nVersions + nVersions;
//            assertEquals("Expected " + nExpectedVersions + " versions. Got " + mcVersions.size(),
//                         nExpectedVersions, mcVersions.size());
            for (ProductRef mv_r : mcVersions) {
                MapContext mv = (MapContext) mv_r.getProduct();
                desc = mv.getRefs().get(productId).getProduct().getDescription();
                _LOGGER.info("Desc of cloned product:" + desc);
                assertTrue(descriptions.contains(desc));
            }

        }
    }
}
