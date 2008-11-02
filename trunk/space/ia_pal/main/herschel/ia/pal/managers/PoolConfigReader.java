package herschel.ia.pal.managers;

import herschel.ia.pal.ProductPool;

/**
 * Generates a {@link ProductPool} instance from a configuration
 * file.
 * @author hsiddiqu
 *
 */
public interface PoolConfigReader {

	/**
	 * Generates a {@link ProductPool} instance from a configuration file
	 * @param filename name of configuration file
	 * @return
	 */
	public ProductPool getPool(String filename);
}
