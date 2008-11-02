/*
 * $Id: MemoryTagsHandler.java,v 1.1 2008/05/30 13:30:25 jsaiz Exp $
 */
package herschel.ia.pal;

import herschel.ia.pal.util.*;

import java.util.*;

/**
 * Tagging implementation that is entirely transient in memory. Tags are not saved.
 */
class MemoryTagsHandler extends TagsHandler {

    private Map<String, String> _map = new HashMap<String, String>();

    public void setTag(String tag, String urn) {
	_map.put(tag, urn);
    }

    public void removeUrn(String urn) {
	Iterator<String> iterator = _map.keySet().iterator();
	while (iterator.hasNext()) {
	    if (urn.equals(_map.get(iterator.next()))) {
		iterator.remove();
	    }
	}
    }

    public void removeTag(String tag) {
	_map.remove(tag);
    }

    public Set<String> getTags() {
	return _map.keySet();
    }

    public Set<String> getTags(String urn) {
	Set<String> result = new ArraySet<String>(false);

	for (Map.Entry<String, String> entry : _map.entrySet()) {
	    if (entry.getValue().equals(urn))
		result.add(entry.getKey());
	}

	return result;
    }

    public String getUrn(String tag) {
	return _map.get(tag);
    }

    public boolean tagExists(String tag) {
	return _map.containsKey(tag);
    }
}
