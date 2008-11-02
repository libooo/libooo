package herschel.ia.pal.managers.xml;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StorageHandler extends DefaultHandler {

	private static Logger _LOGGER = Logger.getLogger(StorageHandler.class
			.getName());
	
	/* XML Element Values */
	private final static String _STORAGE_ELEMENT = "storage";

	private final static String _POOL_ELEMENT = "pool";

	private final static String _WRITEPOOL_ELEMENT = "write-pool";


	/* Members to hold data incrementally constructed during parsing process */

	private String _currentElementValue;

	private List<String> _pools;
	private String _writePoolName;

	private StorageDefinition _storageDefinition;

	public StorageDefinition getStorageDefinition() {
		return _storageDefinition;
	}

	public void startElement(String namespaceURI, String sName, String qName,
			Attributes attrs) throws SAXException {

		String eName = sName;
		if ("".equals(eName)) {
			eName = qName;
		}

		if (_STORAGE_ELEMENT.equals(eName)) {
			
			_pools = new ArrayList<String>();
		}

	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {

		String eName = sName;
		// element name
		if ("".equals(eName)) {
			eName = qName;
		}
		
		_LOGGER.fine("endElement called for element [" + eName + "]");
		
		// trim whitespace from element value
		_currentElementValue = _currentElementValue.trim();

		if (_STORAGE_ELEMENT.equals(eName)) {
			
			_LOGGER.fine("found storage.");
			
			_storageDefinition = new StorageDefinition(_writePoolName, _pools);
			
			/* Clear variables for next pool definition read */
			_writePoolName = null;
			_pools = null;
			
			return;
		}

		if (_WRITEPOOL_ELEMENT.equals(eName)) {
			_writePoolName = _currentElementValue;
			_LOGGER.fine("Found writable pool name " + _writePoolName);

			return;
		}

		if (_POOL_ELEMENT.equals(eName)) {
			_LOGGER.fine("Added pool " + _currentElementValue + " to list");
			_pools.add(_currentElementValue);
			
			return;
		}

	}

	/**
	 * Handle the characters encountered from the given element
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String s = new String(ch, start, length);
		_currentElementValue = s;
	}

}
