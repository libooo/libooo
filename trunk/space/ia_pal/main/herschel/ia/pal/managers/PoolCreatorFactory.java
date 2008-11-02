/* $Id: PoolCreatorFactory.java,v 1.12 2008/04/30 16:38:39 jsaiz Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.managers;

import herschel.ia.pal.PoolCreator;
import herschel.ia.pal.ProductPool;
import herschel.ia.pal.managers.jython.JythonPoolConfigReader;
import herschel.ia.pal.managers.xml.*;
import herschel.share.util.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * Reads in a set of {@link PoolCreator} instances available in the system, and
 * provide access to those instances by way of key.
 * <p>The key is taken from the last token of the name of the propery holding the PoolCreator instance.
 * For example if the property name is hcss.ia.pal.poolfactory.simple, the key is 'simple'.</p>
 * @author Juan Carlos Segovia, S.Guest
 *
 */
public class PoolCreatorFactory {

    private static Logger LOG = Logger.getLogger (PoolCreatorFactory.class.getName());

    static final String FACTORY_ROOT = "hcss.ia.pal.poolfactory.";
    static final String PROP_ROOT = "hcss.ia.pal.pool.";
    static final String PROP_TYPE = PROP_ROOT+"type.";

    /** Default type of pool. */
    public static final String DEFAULT_POOLTYPE = Configuration.getProperty ("hcss.ia.pal.defaulttype");

    // Cache for pool creation factories, used with XML format.
    private static HashMap<String,PoolCreator> _factories = new HashMap<String,PoolCreator>();

    /**
     * Create a pool with the given identifying name.
     * @param  id   pool name
     * @return a product pool
     */
    public static ProductPool createPool (String id) {

       // This is the one that is called by PoolManager.
       // First look for a property with the right name.
       ProductPool pool = null;
       String propValue = Configuration.getProperty (PROP_ROOT+id, null);

       if (propValue == null) {
           // No property, so initialize with defaults
           pool = createPool (id, Configuration.getProperty (PROP_TYPE+id, DEFAULT_POOLTYPE));
       }
       else {
          // The creation of the pool is defined by the property.
          LOG.info ("Creating pool "+id+" from "+propValue);
          if (propValue.endsWith(".py")) {
              pool = getFromJython (propValue, id);   // id not used here
          }
          else if (propValue.endsWith(".xml")) {
              pool = getFromXml (propValue);
          }
          else {
              throw new ConfigurationException (propValue+" is not a Jython or XML file");
          }
       }
       return pool;
    }

    /**
     * Create a pool with the given identifying name and type.
     * @param  id   pool name
     * @param  type of pool
     * @return a product pool
     */
    public static ProductPool createPool (String id, String type) {

       // Create a pool from the defaults using the default Jython scripts.
       LOG.info ("Creating pool "+id+" of type "+type+" from defaults");
       return getFromJython (type+".py", id);
    }

    /**
     * Create a pool of the given type with the given parameters.
     * 
     * @param type the pool type (eg simple, db, etc). See the <a
     *  href="../../../../../ia/pal/doc/guide/html/pal-guide.html">Developers Guide</a>
     *  for more information as to the various allowed pool types for a default HCSS build.
     * @param params set of parameters from XML configuration file
     */
    public static ProductPool createPool (String type, Map<String, String> params) {
      return getPoolCreator(type).createPool (type, params);
    }

    static ProductPool getFromJython (String script, String id) {

        ProductPool pool = null;
        try {
            pool = JythonPoolConfigReader.getInstance().getPool (script, id);
        }
        catch (IllegalArgumentException x) {
            throw new ConfigurationException ("Jython script "+script+" not properly configured", x);
        }
        return pool;
    }

    static ProductPool getFromXml (String file) {

        ProductPool pool = null;
        try {
            PoolDefinition pd = XmlPoolConfigReader.getPoolDefinition (file);
            String type = pd.getType();

            pool = getPoolCreator(type).createPool (type, pd.getParams());
        }
        catch (IllegalArgumentException x) {
            throw new ConfigurationException ("XML file "+file+" not properly configured", x);
        }
        return pool;
    }

    // This is used for the XML initialisation.
    static PoolCreator getPoolCreator (String type) {
        LOG.info ("Getting a pool creator for type "+type);

        // Get an instance of the creator factory to make it. Cache existing ones.
        PoolCreator creator = _factories.get (type);
        if (creator != null) return creator;

        String className = Configuration.getProperty (FACTORY_ROOT+type);
        if (className == null) throw new ConfigurationException ("No factory defined for type "+type);

        PoolCreator pc = null;
        LOG.info ("Loading pool factory "+className);
        try {
            Class c = Class.forName (className);
            pc = (PoolCreator) c.newInstance();
        }
        catch (ClassNotFoundException x) {
            throw new ConfigurationException ("Can't find "+className, x);
        }
        catch (InstantiationException x) {
            throw new ConfigurationException ("Can't create instance of "+className, x);
        }
        catch (IllegalAccessException x) {
            throw new ConfigurationException ("Can't create instance of "+className, x);
        }
        _factories.put (type, pc);
        return pc;
    }
}
