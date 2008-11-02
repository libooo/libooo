package herschel.ia.pal.managers.xml;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PoolHandler extends DefaultHandler {

	private static Logger _LOGGER = Logger.getLogger(PoolHandler.class
			.getName());
	
	/* XML Element Values */
	private final static String _POOL_ELEMENT = "pool";

	private final static String _TYPE_ELEMENT = "pool-type";

	private final static String _PARAM_ELEMENT = "param";

	private final static String _PARAM_NAME_ELEMENT = "param-name";

	private final static String _PARAM_VALUE_ELEMENT = "param-value";

	private final static String _CACHEDPOOL_WRAPPED_ELEMENT = "wrapped-pool";

    public final static String _WRAPPED_POOL_PARAM = "wrapped-pool-type";

    public final static String _WRAPPED_TYPE = "cache";

	/* Members to hold data incrementally constructed during parsing process */

	private String _currentElementValue;

	private String _poolType;

	private Map<String, String> _params;

	private String _paramName;

	private String _paramValue;

	private PoolDefinition _poolDefinition;

	public void startElement(String namespaceURI, String sName, String qName,
			Attributes attrs) throws SAXException {

		String eName = sName;
		if ("".equals(eName)) {
			eName = qName;
		}

		if (_POOL_ELEMENT.equals(eName)) {
			_poolType = null;
			_params = new HashMap<String, String>();
		}

		if (_PARAM_ELEMENT.equals(eName)) {
			_paramName = null;
			_paramValue = null;
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
		
		// Trim whitespace from element value
		_currentElementValue = _currentElementValue.trim();

		if (_POOL_ELEMENT.equals(eName)) {
			
			_poolDefinition = new PoolDefinition(_poolType, _params);

			_LOGGER.fine("Found pool definition for pool type " + _poolDefinition.getType() + ".");
			
			/* Clear variables for next pool definition read */
			_poolType = null;
			_params = null;
			
			return;
		}

		if (_CACHEDPOOL_WRAPPED_ELEMENT.equals(eName)){
			
			/* The pool type currently read in corresponds to the wrapped pool type */
			_params.put(_WRAPPED_POOL_PARAM, _poolType);
			
			/* Reassign poolType to 'cachedpool'*/
			_LOGGER.fine("Reassigned pool type to cachedpool");
			_poolType = _WRAPPED_TYPE;

			return;

		}
		
		if (_TYPE_ELEMENT.equals(eName)) {
			_poolType = _currentElementValue;
			_LOGGER.fine("Found pool type " + _poolType);

			return;

		}

		if (_PARAM_ELEMENT.equals(eName)) {
			_params.put(_paramName, _paramValue);
			
			/* Clear variables for next param read */
			_paramName = null;
			_paramValue = null;

			return;

		}

		if (_PARAM_NAME_ELEMENT.equals(eName)) {
			_paramName = _currentElementValue;
			_LOGGER.fine("Found param name " + _paramName);

			return;

		}

		if (_PARAM_VALUE_ELEMENT.equals(eName)) {
			_paramValue = _currentElementValue;
			_LOGGER.fine("Found param name " + _paramValue);

			return;

		}

		_LOGGER.warning("Element [" + eName + "] is not supported.");
	}

	/**
	 * Handle the characters encountered from the given element
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String s = new String(ch, start, length);
		_currentElementValue = s;
	}

	public PoolDefinition getPoolDefinition() {
		return _poolDefinition;
	}

}
