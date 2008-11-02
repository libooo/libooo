/* $Id: AttribQuery.java,v 1.7 2008/01/09 10:31:02 jsaiz Exp $
 * Copyright (c) 2007 ESA, NAOC, STFC
 */
package herschel.ia.pal.query;

import herschel.ia.dataset.Product;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * A Query on fixed attributes.
 * Fixed attributes are:
 * <table>
 * <tr><th>attribute<th>type
 * <tr><td>creator<td>String
 * <tr><td>creationDate<td>Date
 * <tr><td>instrument<td>String
 * <tr><td>startDate<td>Date
 * <tr><td>endDate<td>Date
 * <tr><td>modelName<td>String
 * <tr><td>type<td>String
 * </table>
 * <pre>
 * date = SimpleTimeFormat().parse("2008-10-31T12:00:00 TAI").microsecondsSince1958()
 * q = AttribQuery(Product, "p", "p.creationDate < " + str(date) + "L and p.creator = 'Me'")
 * </pre>
 * @author Jorgo Bakker <jbakker@rssd.esa.int>
 *
 * @jhelp Attribute Query formulates a query on the attributes of a Product.
 * In principle this can be the fastest query on the Product Access Layer.
 * 
 * Known attributes are:
 * <ul>
 * <li> type        : String</li>
 * <li> creator     : String</li>
 * <li> creationDate: FineTime</li>
 * <li> startDate   : FineTime</li>
 * <li> endDate     : FineTime</li>
 * <li> modelName   : String</li>
 * <li> instrument  : String</li>
 * <li> description : String</li>
 * </ul>
 *
 * @jexample Example of a query on attributes
 * date = SimpleTimeFormat().parse("2008-10-31T12:00:00 TAI").microsecondsSince1958()
 * q = AttribQuery(Product, "p", "p.creationDate < " + str(date) + "L and p.creator = 'Me'")
 * @jcategory
 *        herschel.ia.pal.query;
 * @jref  herschel.ia.pal.query.urm
 *
 */
public final class AttribQuery extends AbstractQuery {
    private static final long serialVersionUID=1l;

    public AttribQuery(Class<? extends Product> product, String variable, String where) {
    	super(product, variable, where);
        }

    public AttribQuery(Class<? extends Product> product, String variable, String where, boolean retrieveAllVersions) {
    	super(product, variable, where, retrieveAllVersions);
        }

    public void accept(StorageQueryVisitor visitor) throws IOException,GeneralSecurityException {
	visitor.visit(this);
    }
}
