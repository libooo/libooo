/* $Id: AbstractStorageManagerTest.java,v 1.4 2008/10/20 15:41:48 bli Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.managers;

import herschel.ia.pal.ProductPool;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.StorageManager;
import herschel.share.util.ConfiguredTestCase;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.Test;

public abstract class AbstractStorageManagerTest extends ConfiguredTestCase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("StorageManagerTest");

	public AbstractStorageManagerTest(String arg0) {
		super(arg0);
	}

	public abstract void testManagerConfig();

	public void testManagerList() {
		logger.info ("Running list test");
        // Setting the property this way does not work if the StorageManager
        // was used previously
        //Configuration.setProperty ("hcss.ia.pal.store.test.xyz", "{xxx, yyy, zzz}");
		ProductStorage storage = StorageManager.getStorage ("xyz");

		Set<ProductPool> pools = storage.getPools();
		assertEquals ("3 pools", 3, pools.size());

		int i = 0;
		for (ProductPool pool : pools) {
			assertNotNull ("not null", pool);
			assertEquals ("length 3", 3, pool.getId().length());
			if (i++ == 0) assertEquals ("xxx writable", "xxx", pool.getId());
		}
	}

	/*
    //SPR-4539	
	
	public void testSPR4539(){
			List<String> ids = StorageManager.getInstance().getIdList();
			logger.info("ids: "+ids);
			assertTrue(ids.contains("standard"));
	
	}
	*/
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static Test suite() {
		return ConfiguredTestCase.suite(AbstractStorageManagerTest.class);
	}

}
