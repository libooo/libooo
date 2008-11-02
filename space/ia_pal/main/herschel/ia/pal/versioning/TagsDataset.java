package herschel.ia.pal.versioning;

import herschel.ia.dataset.Column;
import herschel.ia.dataset.TableDataset;
import herschel.ia.numeric.String1d;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
* A dataset that stores the tag to urn mapping.
* <p>
* This dataset contains two columns:
* <ol>
* <li>The tags (String1d)</li>
* <li>The corresponding urns (String1d)</li>
* </ol>
* </p>
*/ 
public class TagsDataset extends TableDataset {

	@SuppressWarnings("unused")
	private static final Logger _LOGGER =
                         Logger.getLogger(TagsDataset.class.getName());

	private static final long serialVersionUID = 1L;

	private static final String _TAGS_COLUMN = "tags";
	private static final String _URNS_COLUMN = "urns";
	
	public TagsDataset() {
		addColumn(_TAGS_COLUMN, new Column(new String1d()));
		addColumn(_URNS_COLUMN, new Column(new String1d()));
	}
	
	public TagsDataset(Map<String, String> tagMap){
		int n = tagMap.size();
		
		String1d tags = new String1d(n);
		String1d urns = new String1d(n);

		int i = 0;
		for (Map.Entry<String, String> e : tagMap.entrySet()) {
			tags.set(i, e.getKey());
			urns.set(i, e.getValue());
			i++;
		}

		addColumn(_TAGS_COLUMN, new Column(tags));
		addColumn(_URNS_COLUMN, new Column(urns));

	}
		
	public Map<String, String> getTagMap() {
		
		Map<String, String> tagMap = new HashMap<String, String>();
		
		String1d tags = (String1d) getColumn(_TAGS_COLUMN).getData();
		String1d urns = (String1d) getColumn(_URNS_COLUMN).getData();

		for (int i = 0, n = tags.length(); i < n; i++) {
			String tag = tags.get(i);
			String urn = urns.get(i);
			
			tagMap.put(tag, urn);
		}

		return tagMap;
	}

}
