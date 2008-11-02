/* $Id: StorageQueryVisitor.java,v 1.2 2007/11/07 15:14:50 sguest Exp $
 * Copyright (c) 2007 ESA, NAOC, STFC
 */
package herschel.ia.pal.query;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
  * Complement of {@link herschel.ia.pal.query.StorageQuery}, required by the
  * visitor pattern.
  *
  * @see StorageQuery
  */
public interface StorageQueryVisitor {

    /**
      * Required method for visitor pattern
      * @param  query  to be executed
      */
    void visit(FullQuery query) throws IOException,GeneralSecurityException;

    /**
      * Required method for visitor pattern
      * @param  query  to be executed
      */
    void visit(MetaQuery query) throws IOException,GeneralSecurityException;

    /**
      * Required method for visitor pattern
      * @param  query  to be executed
      */
    void visit(AttribQuery query) throws IOException,GeneralSecurityException;
}
