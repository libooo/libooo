package herschel.ia.pal.versioning;

import herschel.ia.pal.ProductPool;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.query.MetaQuery;
import herschel.ia.pal.query.Query;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains methods for retrieving product information from the product storage.
 * @deprecated To be removed along with the old versioning mechanism. Only still
 * provided for backwards copatibility.
 */
public class VersionLoader {

    /**
     * Returns the last urn corresponding to the given version track,
     * or null if no VersionTrackProduct is found with that trackId.
     */
    public static String loadVersionTrackUrn(ProductPool pool, String trackId)
    throws IOException, GeneralSecurityException {

        String metaKey = VersionTrackProduct.TRACK_ID;
        String where = "p.meta['" + metaKey + "'].value == '" + trackId + "'";
        MetaQuery query = new MetaQuery(VersionTrackProduct.class, "p", where);
        Set<ProductRef> refs = pool.select(query);
        return refs.isEmpty()? null : refs.iterator().next().getUrn();
    }

    /**
     * Returns the VersionTrackProduct of a given version track,
     * or null if none is found with that trackId.
     */
    public
    static VersionTrackProduct loadVersionTrack(ProductPool pool,String trackId)
    throws IOException, GeneralSecurityException {
        String urn = loadVersionTrackUrn(pool, trackId);
        return(urn == null)? null : loadVersionTrack(urn, pool);
    }

    /**
     * Returns the VersionTrackProduct of a given urn.
     */
    static VersionTrackProduct loadVersionTrack(String urn, ProductPool pool)
    throws IOException, GeneralSecurityException {
	VersionTrackProduct vtp = (VersionTrackProduct)pool.loadProduct(urn);
	vtp.setUrn(urn);
	return vtp;
    }

    /**
     * Return the set of version track products that exist in a given pool.
     */
    public static
    Set<VersionTrackProduct> loadVersionTracks(ProductPool pool)
    throws IOException, GeneralSecurityException {
        Set<VersionTrackProduct> tracks = new HashSet<VersionTrackProduct>();
        Query query = new Query(VersionTrackProduct.class, "1");
        Set<ProductRef> refs = pool.select(query);
        for(ProductRef ref : refs) {
            try {
                tracks.add(loadVersionTrack(ref.getUrn(), pool));
            }
            catch (IOException e) {
                // Ignore the exception in shared mode: it can be a track
                // product that has been removed by other process after the
                // previous query, and before it is read here.
                // TODO Use the locking mechanism implemented by Bo Li,
                //      instead of ignoring this exception.
                if (!ProductStorage.isSharedMode()) {
                    throw e;
                }
            }
        }
        return tracks;
    }
}
