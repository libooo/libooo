/* $Id: PoolManager.java,v 1.16 2008/10/07 12:11:53 bli Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import herschel.ia.pal.managers.PoolCreatorFactory;
import herschel.share.util.Configuration;
import herschel.share.util.ConfigurationException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides the means to reference
 * {@link herschel.ia.pal.ProductPool} objects without having to hard-code the
 * <em>type</em> of pool. For example, it could be desired to easily switch
 * from one pool type to another.
 * <p>
 * This is done by calling the {@link #getPool(String)} method, which will
 * return an existing pool or create a new one if necessary. This operation can
 * be configured in the following ways:
 * <ol>
 * <li> The default pool type is defined by the value of the property
 * <code>hcss.ia.pal.defaulttype</code>. This can be set to "simple",
 * "lstore" etc. See the PAL guide for full details of valid values. </li>
 * <li> The default pool type can be overridden for individual pools by
 * specifying a property of the type
 * <code>hcss.ia.pal.pool.type.&lt;id&gt;</code>. These properties have the
 * same valid values as the <code>hcss.ia.pal.defaulttype</code> property. To
 * avoid get confused with other type of configuration properties, the
 * <code>&lt;id&gt</code> should not contains '.'. </li>
 * <li> For maximum flexibility and control, set a property of the type
 * <code>hcss.ia.pal.pool.&lt;id&gt;</code> to point to a configuration file
 * that will be used to initialise that pool. These files can either be in a
 * Jython or an XML format. See the PAL guide for full details how how to do
 * this. </li>
 * </ol>
 * 
 * The property hcss.ia.pal.defaultpool is used to specify the default
 * {@link herschel.ia.pal.ProductPool}
 * 
 * </p>
 * 
 * @see StorageManager
 * @see herschel.share.util.Configuration
 * @author hsiddiqu
 * 
 */
public class PoolManager {

	private static PoolManager INSTANCE;
	private static final String PROP_ROOT = "hcss.ia.pal.pool.";
	private List<String> _idList = new ArrayList<String>();
	private Map<String, ProductPool> _pools = new HashMap<String, ProductPool>();

	/**
	 * Jython only. Returns the ProductPool corresponding to the supplied key.
	 */
	public ProductPool __getitem__(String key) {
		return get(key);
	}

	private PoolManager() {

	}

	/**
	 * Returns the ProductPool corresponding to the supplied key. This method
	 * will create the pool if it does not already exist.
	 * 
	 * @param key
	 *            name of the pool
	 * @return the corresponding product pool
	 * @throws ConfigurationException
	 *             if the system is badly configured for the pool name
	 */
	public static ProductPool getPool(String key) {
		return getInstance().get(key);
	}

	/**
	 * Return the default pool as defined by the property
	 * <code>hcss.ia.pal.defaultpool</code>, or <code>null</code> if that
	 * property is undefined.
	 * 
	 * 
	 * @deprecated // SCR-4311
	 */
	public static ProductPool getDefault() {
		return getPool(Configuration.getProperty("hcss.ia.pal.defaultpool"));
	}

	/**
	 * Check if this PoolManager contains a pool of a given name
	 * 
	 * @param poolName
	 *            name of pool
	 * @return true if a pool is present corresponding to that name
	 */
	public static boolean hasPool(String poolName) {
		return getInstance()._pools.containsKey(poolName);
	}

	/**
	 * Returns a map of all pools contained in this PoolManager.
	 */
	public static Map<String, ProductPool> getPoolMap() {
		return getInstance()._pools;
	}

	private ProductPool get(String name) {

		// SG: if the pool doesn't exist, create one
		ProductPool pool = _pools.get(name);

		if (pool == null) {
			// Create a pool of the right name of the default type
			pool = PoolCreatorFactory.createPool(name);

			// Don't forget to add it to the map
			_pools.put(name, pool);
		}
		return pool;
	}

	/**
	 * Retuns an instance of a PoolManager.
	 */
	public static PoolManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PoolManager();
			INSTANCE.init();
		}
		return INSTANCE;
	}

	/**
	 * read and load all configured pool
	 */
	private void init() {

		// XXX: SPR-4539
		Enumeration<Object> keys = Configuration.getProperties().keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.startsWith(PROP_ROOT)) {
				String id = key.substring(PROP_ROOT.length());

				// FIXME: filter out properties like "performancetest.size,
				// simple.dir, cache.dir"

				if (!id.contains(".")) {
					_idList.add(id);
				}
				// getStorage(id);
			}
		}
		
	}

	/**
	 * Returns all the string representation of the configured stores without
	 * initializing any of them
	 * 
	 * Added for SPR-4539
	 * 
	 * @return all the string representation of the configured stores
	 * @deprecated
	 * 
	 */
	public List<String> getIDList() {
		return getIdList();
	}
	
	/**
	 * Returns all the string representation of the configured stores without
	 * initializing any of them
	 * 
	 * Added for SPR-4539
	 * 
	 * @return all the string representation of the configured stores
	 */
	public List<String> getIdList() {
		return _idList;
	}
}
