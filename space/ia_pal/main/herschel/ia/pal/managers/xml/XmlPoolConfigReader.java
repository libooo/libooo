/* $Id: XmlPoolConfigReader.java,v 1.5 2008/03/20 11:28:01 sguest Exp $
 * Copyright (c) 2008 ESA, STFC
 */
package herschel.ia.pal.managers.xml;

import herschel.ia.pal.ProductPool;
import herschel.ia.pal.managers.PoolConfigReader;
import herschel.ia.pal.managers.PoolCreatorFactory;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class XmlPoolConfigReader implements PoolConfigReader {

	@SuppressWarnings("unused")
	private static Logger _LOGGER = Logger.getLogger(XmlPoolConfigReader.class
			.getName());

	// FIXME: this creates a circular reference with PoolCreatorFactory
	public ProductPool getPool(String script) {
		PoolDefinition d = getPoolDefinition(script);
        	return PoolCreatorFactory.createPool(d.getType(),d.getParams());
	}


	public static PoolDefinition getPoolDefinition(String poolDefinitionsFile) {

		try {
			PoolHandler handler = new PoolHandler();

			// Use the default (non-validating) parser
			SAXParserFactory factory = SAXParserFactory.newInstance();

			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(poolDefinitionsFile), handler);

			PoolDefinition poolDefinition = handler.getPoolDefinition();
			return poolDefinition;
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Could not parse definitions file " + poolDefinitionsFile, e);
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException(
					"Could not parse definitions file " + poolDefinitionsFile, e);
		} catch (SAXException e) {
			throw new IllegalArgumentException(
					"Could not parse definitions file " + poolDefinitionsFile, e);
		}

	}

}
