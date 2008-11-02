/* $Id: StorageQuery.java,v 1.4 2008/04/02 10:02:37 jsaiz Exp $
 * Copyright (c) 2007 ESA, NAOC, STFC
 */
package herschel.ia.pal.query;

import herschel.ia.dataset.Product;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;

/**
  * Specification of a query on a {@link herschel.ia.pal.ProductStorage}.
  */
public interface StorageQuery extends Serializable {

    public static final StorageQuery QUERY_ALL = new AttribQuery(Product.class, "p", "1");

    /**
      * Get the query expression to be evaluated.
      * @return the query expression
      */
    String getWhere();

    /**
      * Get the variable name used in the query expression, eg "p".
      * @return the variable name
      */
    String getVariable();

    /**
      * Get the class used in the query. This is of type Product or a subclass of it.
      * @return  class used to refine the query
      */
    Class<? extends Product> getType();

    /**
      * Are all versions to be retrieved, or just the latest?
      * @return <code>true</code> if all versions, <code>false</code? otherwise
      */
    boolean retrieveAllVersions();

    /**
      * Required method of the visitor pattern.
      * @param visitor  to the query
      */
    void accept(StorageQueryVisitor visitor) throws IOException, GeneralSecurityException;
}
