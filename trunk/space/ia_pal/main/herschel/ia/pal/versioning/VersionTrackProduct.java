package herschel.ia.pal.versioning;

import herschel.ia.dataset.Column;
import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.StringParameter;
import herschel.ia.dataset.TableDataset;
import herschel.ia.numeric.Int1d;
import herschel.ia.numeric.String1d;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This product contains information about the versions of a Product, which is
 * called a "edition" or a version track.
 * This information is stored in a TableDataset, that links each version of the
 * product with its corresponding urn.
 * 
 * @deprecated To be removed along with AbstractVersionProduct
 */
public class VersionTrackProduct extends AbstractVersionProduct {

    private static final long serialVersionUID = 1L;

    static final String VERSION_INFO  = "versionInfo";
    static final String VERSION       = "version";
    static final String URN           = "urn";
    static final String TRACK_ID      = "trackName";
    static final String PRODUCT_TYPE  = "productType";
    static final String PRODUCT_CLASS = "productClass";

    private transient String urn;

    /** Default constructor, for the serialization mechanism. */
    public VersionTrackProduct() {}

    /** Constructor. */
    public VersionTrackProduct(String trackId,
                               SortedMap<Integer, String> versionMap,
                               ProductCombinedType type) {

	// Create dataset for the versions
        TableDataset versionsDataset = new TableDataset();
        versionsDataset.addColumn(VERSION, new Column(new Int1d()));
        versionsDataset.addColumn(URN,     new Column(new String1d()));
        for (Map.Entry<Integer, String> entry : versionMap.entrySet()) {
            Integer version   = entry.getKey();
            String urnProduct = entry.getValue();
            versionsDataset.addRow(new Object[] { version, urnProduct });
        }
        set(VERSION_INFO, versionsDataset);

        // Add the meta data about the counter, and the product type and class
        MetaData meta = getMeta();
        meta.set(TRACK_ID     , new StringParameter(trackId));
        meta.set(PRODUCT_TYPE , new StringParameter(type.getProductType()));
        meta.set(PRODUCT_CLASS, new StringParameter(type.getProductClass()
                                                        .getName()));
    }

    /**
     * Returns the map corresponding to the versions of each version track
     * stored.
     */
    public SortedMap<Integer, String> getVersionMap() {

        SortedMap<Integer, String> out = new TreeMap<Integer, String>();
        TableDataset versionsDataset = (TableDataset)get(VERSION_INFO);
        int  nVersions = versionsDataset.getRowCount();
        Int1d versions = (Int1d)versionsDataset.getColumn(VERSION).getData();
        String1d  urns = (String1d)versionsDataset.getColumn(URN).getData();

        for (int i = 0; i < nVersions; i++) {
            out.put(versions.get(i), urns.get(i));
        }
        return out;
    }

    /**
     * Returns the combined type.
     */
    public ProductCombinedType getCombinedType() {
        String productType = (String)getMeta().get(PRODUCT_TYPE ).getValue();
        String className   = (String)getMeta().get(PRODUCT_CLASS).getValue();
        Class productClass;
        try {
            productClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            String message = "Cannot find class corresponding to " + className;
            throw new IllegalStateException(message, e);
        }
        return new ProductCombinedType(productClass, productType);
    }

    public String getTrackId() {
        return (String)getMeta().get(TRACK_ID).getValue();
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }
}
