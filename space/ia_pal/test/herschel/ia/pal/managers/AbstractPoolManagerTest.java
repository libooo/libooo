/* $Id: AbstractPoolManagerTest.java,v 1.5 2008/10/20 15:41:48 bli Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal.managers;

import herschel.ia.pal.PoolManager;
import herschel.ia.pal.ProductPool;
import herschel.share.util.ConfigurationException;
import herschel.share.util.ConfiguredTestCase;

import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;

public abstract class AbstractPoolManagerTest extends ConfiguredTestCase {

	private static final Logger logger = Logger.getLogger (AbstractPoolManagerTest.class.getName());

	public AbstractPoolManagerTest(String arg0) {
		super(arg0);
	}

	/**
	 * This tests the ability of PoolManager to create pools using a configuration file.
	 */
	public abstract void testManager();

	/**
	 * This tests the ability of PoolManager to create pools using the correct
     * defaults when a configuration file is not specified.
	 */
	public abstract void testManagerDefaults();

    public ProductPool checkPool (String id) {
	ProductPool pool = null;
	pool = PoolManager.getPool (id);
	assertNotNull (id+" not null", pool);
	assertEquals (id+" equal", id, pool.getId());
	return pool;
    }

    public void testSpr3588() {
        logger.info("Running SPR-3588 test");
        checkFailure ("error1");
        checkFailure ("error2");
        checkFailure ("error3");
    }
  /*    //SPR-4539		public void testSPR4539(){			List<String> ids = PoolManager.getInstance().getIdList();			logger.info("ids: "+ids);			assertTrue(ids.contains("cal_conf"));	}	*/
	
    void checkFailure (String id) {
        try {
            ProductPool pool = PoolManager.getPool (id);
            fail ("Creation of "+id+" should have failed");
        }
        catch (ConfigurationException x) {
            logger.info ("Creation (correctly) failed with a "+x);
        }
    }
    
    protected void setUp() throws Exception {
	super.setUp();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }

    public static Test suite() {
	return ConfiguredTestCase.suite(AbstractPoolManagerTest.class);
    }    
}
