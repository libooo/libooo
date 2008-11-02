/* $Id: JythonStorageConfigReader.java,v 1.5 2008/04/30 16:38:40 jsaiz Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.managers.jython;

import herschel.ia.pal.PoolManager;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.managers.StorageConfigReader;

public class JythonStorageConfigReader extends JythonConfigReader implements StorageConfigReader {

    private static StorageConfigReader INSTANCE = new JythonStorageConfigReader();

    public static StorageConfigReader getInstance() {
        return INSTANCE;
    }

    public ProductStorage getStorage (String script) {
		
        ProductStorage storage = new ProductStorage();
        _interp.set ("store", storage);
        _interp.set ("pools", PoolManager.getInstance());
        _interp.execfile (script);
        return storage;
    }
}
