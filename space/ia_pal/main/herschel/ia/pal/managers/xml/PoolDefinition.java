package herschel.ia.pal.managers.xml;

import java.util.HashMap;
import java.util.Map;


public class PoolDefinition {

	private String _type;
	private Map<String, String> _params = new HashMap<String, String>();

	private PoolDefinition() {
		// do nothing
	}
	
	public PoolDefinition(String type, Map<String, String> params) {
		_type = type;
		_params = params;
	}
	

	public String getType() {
		return _type;
	}

	public  Map<String, String> getParams() {
		return _params;
	}

}
