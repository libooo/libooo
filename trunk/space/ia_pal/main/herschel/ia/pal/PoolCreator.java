package herschel.ia.pal;

import java.util.Map;


/**
 * Defines how a pool is created from an XML pool configuration file.
 * <p>Developers implementing new {@link ProductPool} implementations,
 * must also provide an implementation for this interface, and in addition
 * provide a property of the form hcss.ia.pal.poolfactory.&lt;type&gt;, where &lt;type&gt; 
 * is the short name of the pool type that is specified in the pool-type field of the XML configuration
 * file, that specifies the class name of the PoolCreator implementation. 
 * This property is included in the ia_pal module property file.</p>
 * <p>For example, in the case of the SimplePool, a property is defined as follows:
 * <pre>
 * hcss.ia.pal.poolfactory.simple=herschel.ia.pal.pool.simple.SimplePoolFactory
 * </pre>
 * In this case, the short name of the pool type is 'simple', and the PoolCreator implementation
 * is situated in the class herschel.ia.pool.simple.SimplePoolFactory.
 * </p>
 * <p>During the initialization of {@link PoolManager}, if a XML configuration file is encountered,
 * the PoolManager reads in the short name of the pool type, looks up the appropriate poolfactory property,
 * and reads in the PoolCreator implementation from the value of that property. 
 * It then calls that class, passing it the key-value parameters taken from the XML file.
 * The PoolCreator implementation must be able to handle those key-value parameters correctly.</p>
 * 
 * @author Juan Carlos Segovia 
 *
 */
public interface PoolCreator {
	
	/**
	 * Creates a pool from the name-value parameters of an XML pool configuration file.
	 * 
	 * @param strPoolType the short name of the pool type
	 * @param params the name-value parameters from the XML pool configuration file.
	 */
    public ProductPool createPool(String strPoolType, Map<String, String> params);
}
