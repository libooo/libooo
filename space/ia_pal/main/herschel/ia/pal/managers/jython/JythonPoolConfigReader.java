/* $Id: JythonPoolConfigReader.java,v 1.6 2008/04/30 16:38:40 jsaiz Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.managers.jython;

import herschel.ia.pal.ProductPool;
import herschel.ia.pal.managers.PoolConfigReader;

import java.io.*;

public class JythonPoolConfigReader extends JythonConfigReader implements PoolConfigReader {

    private static final int MAX_ARGS = 10;
    private static JythonPoolConfigReader INSTANCE = new JythonPoolConfigReader();

    public static JythonPoolConfigReader getInstance() {
        return INSTANCE;
    }

    public ProductPool getPool (InputStream script) {

        // The contract of the script is to create a "pool" variable
        _interp.execfile (script);
        ProductPool pool = (ProductPool) _interp.get ("pool", ProductPool.class);

        return pool;
    }

    public ProductPool getPool (String script) {
        InputStream is = null;

        // First look for a resource with the given name
        is = JythonPoolConfigReader.class.getResourceAsStream (script);
        if (is == null) {
            try {
              is = new FileInputStream (script);
            }
            catch (FileNotFoundException x) {
              throw new IllegalArgumentException ("Resource or file does not exist: "+script, x);
            }
        }

        return getPool (is);
    }

    public ProductPool getPool (String script, Object... args) {
        //System.out.println ("getPool on "+script+" with "+args.length+" args");

        if (args.length > MAX_ARGS)
           throw new IllegalArgumentException ("Too many arguments to getPool: "+args.length);

        _interp.set ("_0", script);

        // Set the first n variables to the given args
        for (int i = 0; i < args.length; i++) {
            String varname = "_"+(i+1);
            _interp.set (varname, args[i]);
            //System.out.println (varname+" = "+args[i]);
        }

        // Clear any unused ones, just in case
        for (int i = args.length; i < MAX_ARGS; i++) {
            String varname = "_"+(i+1);
            _interp.set (varname, null);
        }

        return getPool (script);
    }
}
