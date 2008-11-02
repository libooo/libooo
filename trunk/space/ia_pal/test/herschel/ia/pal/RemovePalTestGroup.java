/* $Id: RemovePalTestGroup.java,v 1.9 2008/03/31 15:59:53 jsaiz Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import herschel.ia.dataset.Product;
import herschel.ia.dataset.LongParameter;
import herschel.ia.dataset.StringParameter;
import herschel.ia.pal.query.Query;
import herschel.ia.pal.query.MetaQuery;
import herschel.ia.pal.query.AttribQuery;
import herschel.share.fltdyn.time.FineTime;
import herschel.share.util.StackTrace;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

public class RemovePalTestGroup extends AbstractPalTestGroup{
    
    private static final Logger _LOGGER = Logger.getLogger("RemovePalTestGroup");

    protected static final int _NUM_ITEMS = 20;    
    protected static final long _lngTimeBase = 1000000000000000L;
    protected Query _query = new Query(Product.class, "instrument!='Unknown'");
    protected ProductPool _pool;
    
    public RemovePalTestGroup(String name, AbstractPalTestCase testCase){
        super(name, testCase);
    }
    
    
    protected String[] createProducts(int iNumItems)throws Exception{
        String strUrns[] = new String[iNumItems];
        for (int i = 0; i < iNumItems; i++){
            Product p = new Product("P_" + i);
            p.setCreationDate(new FineTime(_lngTimeBase + i));
            p.setStartDate(new FineTime(_lngTimeBase + (i+1)));
            p.setEndDate(new FineTime(_lngTimeBase + (i+2)));
            p.setInstrument("INSTRUMENT_"+i);
            p.setModelName("MODEL_"+i);
            p.setCreator("CREATOR_"+i);
            p.setType("TYPE_"+i);
            //String urn = _pool.save(p);
            //System.out.println(urn);
            strUrns[i] = _pool.save(p);
        }
        return strUrns;
    }
    
    protected int getNumberOfRecords() throws Exception{
        Set<ProductRef> refs = _pool.select(_query);
        return refs.size();
    }
    
    
    protected void deleteProduct(String urn)throws Exception{
        _pool.remove(urn);
    }

    
    protected void searchEachId(String strUrns[], int initId)throws Exception{
        for (int i = initId; i < strUrns.length; i++){
            if (!_pool.exists(strUrns[i])){
                throw new Exception("item: '" + strUrns[i] + "' does not exist.");
            }
        }        
    }
    
    
    protected void searchEachIdReverse(String strUrns[], int initId)throws Exception{
        for (int i = initId; i >= 0; i--){
            if (!_pool.exists(strUrns[i])){
                throw new Exception("item: '" + strUrns[i] + "' does not exist.");
            }
        }        
    }
    
    
    protected void dumpProducts() throws IOException, GeneralSecurityException{
        System.out.println("pool: " + _pool.getId());
        
        Set<ProductRef> refs = _pool.select(_query);        
        int iNumRecords = refs.size();
        
        System.out.println("Num items: " + iNumRecords);
        
        Product p;

        for (ProductRef ref : refs){
            String urn = ref.getUrn();
            p = _pool.loadProduct(urn);
            System.out.println("Product: " + urn);
            System.out.println(p);
        }
    }
    

    protected String[] runRemoveSomeItems(int iNumItems, int iItemsToRemove[]) 
    throws Exception{
        String strUrns[] = createProducts(iNumItems);
        //dumpProducts();
        //System.out.println("-----------------------------------");
        for (int i = 0; i < iItemsToRemove.length; i++){
            deleteProduct(strUrns[iItemsToRemove[i]]);
        }
        return strUrns;
    }
    
    
    protected String[] runRemoveAllItems(int iNumItems) 
    throws Exception{
        String strUrns[] = createProducts(iNumItems);
        //dumpProducts();
        //System.out.println("-----------------------------------");
        for (int i = 0; i < iNumItems; i++){
            //searchEachId(iNumItems, i);
            deleteProduct(strUrns[i]);
        }
        
        return strUrns;
    }
    
    
    protected String[] runReverseOrderRemoveAllItems(int iNumItems) 
    throws Exception{
        String strUrns[] = createProducts(iNumItems);
        //dumpProducts();
        //System.out.println("-----------------------------------");
        for (int i = iNumItems-1; i >= 0; i--){
            //searchEachIdReverse(i);
            deleteProduct(strUrns[i]);
        }

        return strUrns;
    }
    
    protected boolean verifyItems(int iNumItems, int iItemsToRemove[], String strUrns[])
    throws IOException, GeneralSecurityException{

        int iMask [] = new int[iNumItems];
        for (int i = 0; i < iItemsToRemove.length; i++){
            iMask[iItemsToRemove[i]] = 1;
        }
        
        for (int i = 0; i < iMask.length; i++){
            if (iMask[i]==0){
                //Item must exist
                if (!_pool.exists(strUrns[i])) return false;
            }else{
                //Item must not exist
                if (_pool.exists(strUrns[i])) return false;
            }
        }

        return true;
    }
    
    
    public void testRemoveFirstItem() throws IOException, GeneralSecurityException{
        _LOGGER.info("Removing first item.");
        _pool = getPool();
        int iItemsToRemove[] = new int[]{0};
        String strUrns[] = null;
        try{
            strUrns = runRemoveSomeItems(_NUM_ITEMS,iItemsToRemove);
        }catch(Exception e){
            fail("Removing first item (numItems = "+_NUM_ITEMS+").");
        }
        if(!verifyItems(_NUM_ITEMS,iItemsToRemove,strUrns)){
            fail("Wrong procedure removing first item (numItems = "+_NUM_ITEMS+")");
        }
    }
    
    public void testRemoveLastItem() throws IOException, GeneralSecurityException{
        _LOGGER.info("Removing last item.");
        _pool = getPool();
        int iItemsToRemove[] = new int[]{_NUM_ITEMS-1};
        String strUrns[] = null;
        try{
            strUrns = runRemoveSomeItems(_NUM_ITEMS,iItemsToRemove);
        }catch(Exception e){
            fail("Removing last item (numItems = "+_NUM_ITEMS+").");
        }
        if(!verifyItems(_NUM_ITEMS,iItemsToRemove,strUrns)){
            fail("Wrong procedure removing last item (numItems = "+_NUM_ITEMS+")");
        }
        
    }
    
    public void testRemoveMiddleItem() throws IOException, GeneralSecurityException{
        _LOGGER.info("Removing middle item.");
        _pool = getPool();
        int iItemsToRemove[] = new int[]{_NUM_ITEMS/2};
        String strUrns[] = null;
        try{
            strUrns = runRemoveSomeItems(_NUM_ITEMS,iItemsToRemove);
        }catch(Exception e){
            fail("Removing intermediate item (numItems = "+_NUM_ITEMS+").");
        }
        if(!verifyItems(_NUM_ITEMS,iItemsToRemove,strUrns)){
            fail("Wrong procedure removing intermediate item (numItems = "+_NUM_ITEMS+")");
        }
        
    }
    
    public void testRemoveLast3Items() throws IOException, GeneralSecurityException{
        _LOGGER.info("Removing last three items.");
        _pool = getPool();
        int iItemsToRemove[] = new int[]{0,1,2};
        String strUrns[] = null;
        try{
            strUrns = runRemoveSomeItems(_NUM_ITEMS,iItemsToRemove);
        }catch(Exception e){
            fail("Removing first 3 items (numItems = "+_NUM_ITEMS+").");
        }
        if(!verifyItems(_NUM_ITEMS,iItemsToRemove,strUrns)){
            fail("Wrong procedure removing first 3 items (numItems = "+_NUM_ITEMS+")");
        }
        
    }
    
    public void testRemoveFirst3Items() throws IOException, GeneralSecurityException{
        _LOGGER.info("Removing first three items.");
        _pool = getPool();
        int iItemsToRemove[] = new int[]{_NUM_ITEMS-3,_NUM_ITEMS-2,_NUM_ITEMS-1};
        String strUrns[] = null;
        try{
            strUrns = runRemoveSomeItems(_NUM_ITEMS,iItemsToRemove);
        }catch(Exception e){
            fail("Removing last 3 items (numItems = "+_NUM_ITEMS+").");
        }
        if(!verifyItems(_NUM_ITEMS,iItemsToRemove,strUrns)){
            fail("Wrong procedure removing last 3 items (numItems = "+_NUM_ITEMS+")");
        }
        
    }
    
    public void testRemoveMiddle3Items() throws IOException, GeneralSecurityException{
        _LOGGER.info("Removing middle three items.");
        _pool = getPool();
        int iItemsToRemove[] = new int[]{_NUM_ITEMS/2,(_NUM_ITEMS/2)+1,(_NUM_ITEMS/2)+2};
        String strUrns[] = null;
        try{
            strUrns = runRemoveSomeItems(_NUM_ITEMS,iItemsToRemove);
        }catch(Exception e){
            fail("Removing intermediate 3 items (numItems = "+_NUM_ITEMS+").");
        }        
        if(!verifyItems(_NUM_ITEMS,iItemsToRemove,strUrns)){
            fail("Wrong procedure removing itermediate 3 items (numItems = "+_NUM_ITEMS+")");
        }
        
    }
    
    public void testRemoveSomeItems() throws IOException, GeneralSecurityException{
        _LOGGER.info("Removing some items.");
        _pool = getPool();
        int iItems[] = new int[_NUM_ITEMS/2];
        for (int i = 0, iPos = 0; (i < _NUM_ITEMS && iPos < iItems.length); i++){
            if (i%2 == 0){
                iItems[iPos++]=i;
            }
        }
        String strUrns[] = null;
        try{
            strUrns = runRemoveSomeItems(_NUM_ITEMS,iItems);
        }catch(Exception e){
            fail("Removing even items (numItems = "+_NUM_ITEMS+").");
        }
        if(!verifyItems(_NUM_ITEMS,iItems,strUrns)){
            fail("Wrong procedure removing even items (numItems = "+_NUM_ITEMS+")");
        }
    }

    public void testRemoveAllElements() {
        _LOGGER.info("Removing all items.");
        _pool = getPool();
        try{
            runRemoveAllItems(_NUM_ITEMS);
        }catch(Exception e){
            fail("Removing all items (numItems = "+_NUM_ITEMS+").");
        }
        
        try{
            int iNumRec = getNumberOfRecords();
            if (iNumRec != 0){
                fail("Removing all items: Number of remaining records not zero: " + iNumRec);
            }
        }catch(Exception e){
            fail("Removing all items: " + e.getMessage());            
        }
        
    }
    
    public void testRemoveAllItemsReverse() {
        _LOGGER.info("Removing all items (reverse).");
        _pool = getPool();
        try{
            runReverseOrderRemoveAllItems(_NUM_ITEMS);
        }catch(Exception e){
            fail("Removing all items (reverse order, numItems = "+_NUM_ITEMS+").");
        }        

        try{
            int iNumRec = getNumberOfRecords();
            if (iNumRec != 0){
                fail("Removing all items (reverse order): Number of remaining records not zero: " + iNumRec);
            }
        }catch(Exception e){
            fail("Removing all items (reverse order): " + e.getMessage());            
        }
        
    }

     public void testRemoveMetaItems() throws IOException,
    GeneralSecurityException {
        _LOGGER.info("Removing Meta Items.");
        ProductStorage storage = new ProductStorage();
        ProductPool pool = getPool();
        storage.register(pool);

        Product p = new Product("p1_a");
        p.getMeta().set("Par1", new StringParameter("ValPar1"));
        ProductRef pRef_a = storage.save(p);
        String strUrnRef_a = pRef_a.getUrn();
        
        //Try to find the product.
        
        MetaQuery mq = new MetaQuery(Product.class,"p","p.meta.containsKey('Par1') and p.meta['Par1'].value=='ValPar1'");
        
        Set<ProductRef> pResults = storage.select(mq);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 1){
            fail ("Incorrect number of products: found: " + pResults.size());
        }
        
        ProductRef pRefLoad = pResults.iterator().next();        
        String strResultUrn = pRefLoad.getUrn();
        
        if (!strResultUrn.equals(strUrnRef_a)){
            fail ("URN Object not valid");
        }
        
        Product pLoad = pRefLoad.getProduct();
        
        if (pLoad.compare(p) != 0){
            fail ("Product Not equals");
        }
        
        //try to remove
        try{
            pool.remove(strUrnRef_a);
        }catch(Exception e){
            fail ("Error removing item: " + e.getMessage());
        }
        
        //try to find it again
        pResults = storage.select(mq);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 0){
            fail ("Removed product found.");
        }

    }

    public void testRemoveItemsMetaRefine() throws IOException,
    GeneralSecurityException {
        _LOGGER.info("Refine Removing Meta Items.");
        ProductStorage storage = new ProductStorage();
        ProductPool pool = getPool();
        storage.register(pool);

        Product p_a = new Product();
        p_a.getMeta().set("Par1", new LongParameter(1L));
        ProductRef pRef_a = storage.save(p_a);
        String strUrnRef_a = pRef_a.getUrn();
        
        Product p_b = new Product();
        p_b.getMeta().set("Par1", new LongParameter(2L));
        storage.save(p_b);
        
        //Try to find the product.
        
        MetaQuery mq1 = new MetaQuery(Product.class,"p","p.meta.containsKey('Par1') and p.meta['Par1'].value>0");
        
        Set<ProductRef> pResults = storage.select(mq1);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 2){
            fail ("Incorrect number of products: found: " + pResults.size());
        }
        
        //Refine the query.
        MetaQuery mq2 = new MetaQuery(Product.class,"p","p.meta.containsKey('Par1') and p.meta['Par1'].value==1");

        pResults = storage.select(mq2, pResults);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 1){
            fail ("Incorrect number of products: found: " + pResults.size());
        }
        
        ProductRef pRefLoad = pResults.iterator().next();        
        String strResultUrn = pRefLoad.getUrn();
        
        if (!strResultUrn.equals(strUrnRef_a)){
            fail ("URN Object not valid");
        }
        
        Product pLoad = pRefLoad.getProduct();
        
        if (pLoad.compare(p_a) != 0){
            fail ("Product Not equals");
        }
        
        //try to remove
        try{
            pool.remove(strUrnRef_a);
        }catch(Exception e){
            fail ("Error removing item: " + e.getMessage());
        }
        
        //try to find it again
        pResults = storage.select(mq1);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 1){
            fail ("Product not found.");
        }

        pResults = storage.select(mq2);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 0){
            fail ("Removed product found.");
        }
    }
    
    
    
    public void testRemoveAttribItems() throws IOException,
    GeneralSecurityException {
        _LOGGER.info("Removing Attrib Items.");
        ProductStorage storage = new ProductStorage();
        ProductPool pool = getPool();
        storage.register(pool);

        Product p = new Product("Desc1");
        p.setCreator("Type1");
        ProductRef pRef_a = storage.save(p);
        String strUrnRef_a = pRef_a.getUrn();
        
        //Try to find the product.
        
        AttribQuery mq = new AttribQuery(Product.class,"p","p.creator=='Type1'");
        
        Set<ProductRef> pResults = storage.select(mq);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 1){
            fail ("Incorrect number of products: found: " + pResults.size());
        }
        
        ProductRef pRefLoad = pResults.iterator().next();        
        String strResultUrn = pRefLoad.getUrn();
        
        if (!strResultUrn.equals(strUrnRef_a)){
            fail ("URN Object not valid");
        }
        
        Product pLoad = pRefLoad.getProduct();
        
        if (pLoad.compare(p) != 0){
            fail ("Product Not equals");
        }
        
        //try to remove
        try{
            pool.remove(strUrnRef_a);
        }catch(Exception e){
            fail ("Error removing item: " + e.getMessage());
        }
        
        //try to find it again
        pResults = storage.select(mq);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 0){
            fail ("Removed product found.");
        }
   }
   
    public void testRemoveItemsAttribRefine() throws IOException,
    GeneralSecurityException {        
        _LOGGER.info("Refine Removing Attrib Items.");
        ProductStorage storage = new ProductStorage();
        ProductPool pool = getPool();
        storage.register(pool);

        Product p_a = new Product();
        p_a.setCreator("1");
        ProductRef pRef_a = storage.save(p_a);
        String strUrnRef_a = pRef_a.getUrn();
        
        Product p_b = new Product();
        p_b.setCreator("2");
        storage.save(p_b);
        
        //Try to find the product.
        
        AttribQuery mq1 = new AttribQuery(Product.class,"p","p.creator!='0'");
        
        Set<ProductRef> pResults = storage.select(mq1);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 2){
            fail ("Incorrect number of products: found: " + pResults.size());
        }
        
        //Refine the query.
        AttribQuery mq2 = new AttribQuery(Product.class,"p","p.creator=='1'");

        pResults = storage.select(mq2, pResults);
        if (pResults == null) fail ("ProductRef empty.");
        
        if (pResults.size() != 1){
            fail ("Incorrect number of products: found: " + pResults.size());
        }
        
        ProductRef pRefLoad = pResults.iterator().next();        
        String strResultUrn = pRefLoad.getUrn();
        
        if (!strResultUrn.equals(strUrnRef_a)){
            fail ("URN Object not valid");
        }
        
        Product pLoad = pRefLoad.getProduct();
        
        if (pLoad.compare(p_a) != 0){
            fail ("Product Not equals");
        }
        
        //try to remove
        try{
            pool.remove(strUrnRef_a);
        }catch(Exception e){
            fail ("Error removing item: " + e.getMessage());
        }
        
        //try to find it again
        pResults = storage.select(mq1);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 1){
            fail ("Product not found.");
        }

        pResults = storage.select(mq2);
        if (pResults == null) fail ("ProductRef empty.");
        
        if(pResults.size() != 0){
            fail ("Removed product found.");
        }
    }
    
    
    public void testRemoveGeneral() throws IOException,
    GeneralSecurityException {
        _LOGGER.info("Removing General.");
        ProductStorage storage = new ProductStorage();
        ProductPool pool = getPool();
        storage.register(pool);

        Product p = new Product("p1_a");
        p.getMeta().set("Par1", new StringParameter("ValPar1"));
        ProductRef pRef_a = storage.save(p);
        String strUrnRef_a = pRef_a.getUrn();

        //try to remove
        try{
            pool.remove(strUrnRef_a);
        }catch(Exception e){
            fail ("Error removing item: " + e.getMessage());
        }

        try {
            storage.load(strUrnRef_a);
            fail("Deleted product of urn " + strUrnRef_a + " still exists");
        }
        catch (NoSuchElementException e){
            // Test succeeded. Do nothing
        }
    }

    public void testRemoveNonExistant() throws IOException, GeneralSecurityException {
        _LOGGER.info("Removing Non existant Items.");
        ProductStorage storage = new ProductStorage();
        ProductPool pool = getPool();
        storage.register(pool);

        Product p = new Product();
        ProductRef pRef = storage.save(p);
        String strUrnItems[] = pRef.getUrn().split("\\:");
        int iNumber = Integer.parseInt(strUrnItems[3]) + 100;
        String urnNonExistant = strUrnItems[0] + ":" + strUrnItems[1] + ":" + strUrnItems[2] + ":" + iNumber;

        //try to remove a non-existant product
        //String urnNonExistant = "urn:simple.test:Product:123";
        try{
            pool.remove(urnNonExistant);
            fail ("URN " + urnNonExistant + " has been successfully 'removed' even though it does not exist!");
        } catch (NoSuchElementException e){
            // Test successful. Do nothing.
        }
    } 
    
    public void testRemoveAndAdd() throws IOException, GeneralSecurityException{
        _LOGGER.info("Removing and Adding Items.");
        Query query = new Query(Product.class, "1"); // query all products
        ProductStorage storage = new ProductStorage();
        storage.register(getPool());
        
        ProductRef ref1 = storage.save(new Product());
        ProductRef ref2 = storage.save(new Product());
        ProductRef ref3 = storage.save(new Product());
        ProductRef ref4 = storage.save(new Product());
        assertEquals(4, storage.select(query).size());

        storage.remove(ref1.getUrn());
        assertEquals(3, storage.select(query).size());

        ProductRef ref5 = storage.save(new Product());
        assertEquals(4, storage.select(query).size());

        storage.remove(ref2.getUrn());
        storage.remove(ref3.getUrn());
        assertEquals(2, storage.select(query).size());

        ProductRef ref6 = storage.save(new Product());
        assertEquals(3, storage.select(query).size());

        storage.remove(ref4.getUrn());
        storage.remove(ref5.getUrn());
        assertEquals(1, storage.select(query).size());

        ProductRef ref7 = storage.save(new Product());
        assertEquals(2, storage.select(query).size());

        storage.remove(ref6.getUrn());
        assertEquals(1, storage.select(query).size());

        ProductRef ref8 = storage.save(new Product());
        assertEquals(2, storage.select(query).size());

        storage.remove(ref7.getUrn());
        storage.remove(ref8.getUrn());
        assertEquals(0, storage.select(query).size());
    }

    /*
    public void testRemoveProblem() {
        
        SimplePool pool = SimplePool.getInstance();

        try {
            String urn;
            Product p = new Product();
            urn = pool.save(p);
            pool.remove(urn);
            urn = pool.save(p);
            pool.remove(urn);
            urn = pool.save(p);
            pool.remove(urn);
            
        } catch (IOException e) {
            fail("Following exception was raised " + StackTrace.trace(e));
        } catch (GeneralSecurityException e) {
            fail("Following exception was raised " + StackTrace.trace(e));
        }
    }
    */

    public void testRemoveVersioning1() {
        ProductStorage storage = new ProductStorage();
        storage.register(getPool(0));
    
        try {    
            Product p = new Product();
            ProductRef pRef1 = storage.save(p);        
            ProductRef pRef2 = storage.save(p);
            
            storage.setTag("tag1", pRef1.getUrn());
            storage.setTag("tag2", pRef2.getUrn());
            storage.setTag("tag2b", pRef2.getUrn());
    
            storage.remove(pRef1.getUrn());
            storage.remove(pRef2.getUrn());

        } catch (IOException e) {
            fail("Following exception was raised " + StackTrace.trace(e));
        } catch (GeneralSecurityException e) {
            fail("Following exception was raised " + StackTrace.trace(e));
        } catch (Exception e){
            fail("Following exception was raised " + StackTrace.trace(e));
        }
    }

    /**
     * It is not clear this test should be here...
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void testRemoveVersioning2() {
    
        try {
            ProductStorage storageTmp = new ProductStorage();
            storageTmp.register(getPool(1));
            
            Product p2 = new Product();
            ProductRef pRef2 = storageTmp.save(p2);
            
            ProductStorage storage = new ProductStorage();
            storage.register(getPool(0));
            storage.register(getPool(1));
    
            Product p1 = new Product();
            ProductRef pRef1 = storage.save(p1);        
            
            storage.setTag("tag1", pRef1.getUrn());
            storage.setTag("tag2", pRef2.getUrn());
            storage.setTag("tag2b", pRef2.getUrn());

            try{
                storage.remove(pRef2.getUrn());
                fail("An urn from read-only pool has been deleted.");
            }catch(Exception ioe){
                //ok
            }
            
        } catch (IOException e) {
            fail("Following exception was raised " + StackTrace.trace(e));
        } catch (GeneralSecurityException e) {
            fail("Following exception was raised " + StackTrace.trace(e));
        } catch (Exception e){
            fail("Following exception was raised " + StackTrace.trace(e));
        }
    }
}
