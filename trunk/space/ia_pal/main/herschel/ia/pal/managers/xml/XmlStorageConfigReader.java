/* $Id: XmlStorageConfigReader.java,v 1.7 2008/04/30 16:38:40 jsaiz Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.managers.xml;

import herschel.ia.pal.PoolManager;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.managers.StorageConfigReader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class XmlStorageConfigReader implements StorageConfigReader {

	private static Logger LOGGER = Logger
			.getLogger(XmlStorageConfigReader.class.getName());

	private StorageDefinition getStorageDefinition (String storageDefinitionsFile) {

		try {
			StorageHandler handler = new StorageHandler();

			// Use the default (non-validating) parser
			SAXParserFactory factory = SAXParserFactory.newInstance();

			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(storageDefinitionsFile), handler);

			return handler.getStorageDefinition();

		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Could not parse definitions file." + storageDefinitionsFile, e);
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException(
					"Could not parse definitions file." + storageDefinitionsFile, e);
		} catch (SAXException e) {
			throw new IllegalArgumentException(
					"Could not parse definitions file." + storageDefinitionsFile, e);
		}
	}

	public ProductStorage getStorage(String script) {
		StorageDefinition d = getStorageDefinition(script);
		List<String> poolNames = d.getPoolNames();

		ProductStorage storage = new ProductStorage();

		String writablePool = d.getWritablePoolName();

		// This check allows it to work if the writable pool tag is not present
		if (writablePool != null) {
			LOGGER.fine("Registering writable pool " + writablePool
					+ " to storage " + storage);
			storage.register (PoolManager.getPool(writablePool));
		}

		for (String poolName : poolNames) {
			if (writablePool != null && poolName.equals (writablePool)) continue;

			// SG: remove, PoolManager has defaults now
			//if (!PoolManager.hasPool(poolName)) {
			//	throw new IllegalArgumentException("The storage config file "+ script + " Specifies a pool of name "
			//	+ poolName + ". This is not available from the pool manager.");
			//}

			LOGGER.fine ("Registering pool " + poolName + " to storage "+ storage);
			storage.register (PoolManager.getPool(poolName));
		}

		return storage;
	}
}