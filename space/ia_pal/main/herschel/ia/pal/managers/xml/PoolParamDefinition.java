package herschel.ia.pal.managers.xml;

public class PoolParamDefinition {

	private String _name;
	private String _value;
	
	private PoolParamDefinition() {
		// do nothing
	}

	PoolParamDefinition(String name, String value){
		_name = name;
		_value = value;
	}

	public String getName() {
		return _name;
	}

	public String getValue() {
		return _value;
	}


}
