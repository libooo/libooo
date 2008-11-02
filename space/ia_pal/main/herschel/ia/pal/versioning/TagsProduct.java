package herschel.ia.pal.versioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A product that stores the tag to urn mapping.
 */
public class TagsProduct extends AbstractVersionProduct {

    private static final long serialVersionUID = 1L;

    private static final String TAGS_DATASET = "tagsInfo";

    public TagsProduct() {
	set(TAGS_DATASET, new TagsDataset());
    }

    public TagsProduct(Map<String, String> tagMap) {
	set(TAGS_DATASET, new TagsDataset(tagMap));
    }

    public Map<String, String> getTagUrnMap() {
	return ((TagsDataset) get(TAGS_DATASET)).getTagMap();
    }

    public Map<String, List<String>> getUrnTagMap() {
	List<String> tags = null;
	Map<String, List<String>> out = new HashMap<String, List<String>>();
	Map<String, String> tagUrnMap = getTagUrnMap();
	for (Map.Entry<String, String> entry : tagUrnMap.entrySet()) {
	    String tag = entry.getKey();
	    String urn = entry.getValue();
	    if ((tags = out.get(urn)) == null) {
		tags = new ArrayList<String>();
		out.put(urn, tags);
	    }
	    tags.add(tag);
	}
	return out;
    }
}
