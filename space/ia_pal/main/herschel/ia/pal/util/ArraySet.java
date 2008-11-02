/* $Id: ArraySet.java,v 1.3 2008/03/31 15:59:17 jsaiz Exp $
 * Copyright (c) 2007 STFC
 */

package herschel.ia.pal.util;

import java.util.*;

/**
  * Variation on ArrayList to provide Set behaviour. This means that each element in the list can be
  * present only once. This allows the results returned from queries to be accessed from Jython in a 
  * natural way. It would probably be obsolete were Jython to start treating sets in the same way as
  * lists. It is also made possible to disable the set uniqueness checking for performance reasons. If
  * this is done, it must be externally ensured that duplicate elements are not inserted.
  *
  * @author   S.Guest       RAL/SPIRE
  * @version  April 2007    first version
  */
public class ArraySet<E> extends ArrayList<E> implements Set<E> {

  private static final long serialVersionUID = 1l;
  private boolean _check = true;

  /**
    * Create a set implementation backed by an array list.
    */
  public ArraySet() {
  }

  /**
    * Create a set implementation backed by an array list, with initial data.
    */
  public ArraySet(Collection<E> col) {
      super(col);
  }

  /**
    * Create a set implementation backed by an array list, with optional set uniqueness enforcement.
    * This allows the possibility of providing a Set interface without any performance penalty of
    * checking for uniqueness. It is then the responsibility of the user of this class to ensure that
    * elements are not duplicated.
    * @param  check  enforce set uniqueness if <code>true</code>
    */
  public ArraySet (boolean check) {
    _check = check;
  }

  @Override
  public boolean add (E o) {
    if (_check && contains (o)) return false;
    return super.add (o);
  }

  @Override
  public void add (int index, E o) {
    if (_check && contains (o)) throw new IllegalArgumentException ("Element is already in the set");
    super.add (index, o);
  }

  @Override
  public E set (int index, E o) {

    // Allow same element to replace itself, but put in a different place
    if (_check) {
        int i = indexOf (o);
        if (i >= 0 && i != index) throw new IllegalArgumentException (
           "Element is already in the set at a different location");
    } 

    return super.set (index, o);
  }
}