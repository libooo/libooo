/* $Id: StorageConfigReader.java,v 1.6 2008/04/30 16:38:39 jsaiz Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.managers;

import herschel.ia.pal.ProductStorage;

/**
 * Reads in a configuration file containing a {@link ProductStorage} definition.
 * 
 * @author hsiddiqu
 *
 */
public interface StorageConfigReader {

	/**
	 * Creates a {@link ProductStorage} instance from a configuration file
	 * @param filename name of configuration file
	 * @return
	 */
	public ProductStorage getStorage(String filename);
}
