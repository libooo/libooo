package herschel.ia.pal.versioning;

import herschel.ia.pal.ProductPool;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.query.AttribQuery;
import herschel.ia.pal.util.UrnUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * Contains methods for retrieving product information from the product storage.
 * @author jsaiz
 */
public class TagsLoader {

    /**
     * Retrieves the URN of the last TagsProduct.
     */
    public static String loadTagsProductUrn(ProductPool pool)
    throws IOException, GeneralSecurityException {

        String urnForTagsProduct = null;
        AttribQuery query = new AttribQuery(TagsProduct.class, "p", "1", true);
        Set<ProductRef> refs = pool.select(query);
        for(ProductRef ref : refs) {
            String urn = ref.getUrn();
            urnForTagsProduct = (urnForTagsProduct == null)?
                                 urn : UrnUtils.getLater(urn,urnForTagsProduct);
        }
        return urnForTagsProduct;
    }

    /**
     * Retrieves the last TagsProduct.
     */
    public static TagsProduct loadTagsProduct(ProductPool pool)
    throws IOException, GeneralSecurityException {
        String urn = loadTagsProductUrn(pool);
        return (urn != null)? (TagsProduct)pool.loadProduct(urn) : null;
    }
}
