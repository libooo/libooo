package herschel.ia.pal.managers.xml;

import java.util.List;

public class StorageDefinition {

	private String _writablePoolName;

	private List<String> _poolNames;

	public StorageDefinition(String writablePoolName, List<String> poolNames) {
		
		_writablePoolName = writablePoolName;
		_poolNames = poolNames;
	}

	public List<String> getPoolNames() {
		return _poolNames;
	}

	public String getWritablePoolName() {
		return _writablePoolName;
	}

}
