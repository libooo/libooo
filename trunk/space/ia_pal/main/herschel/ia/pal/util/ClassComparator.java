// -*-java-*-
//
// File:      ClassComparator.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Mar 7, 2008
// Usage:     -
// Info:      -

package herschel.ia.pal.util;

import java.util.Comparator;

/**
 * This class compares classes by their class name.
 */
public class ClassComparator implements Comparator<Class> {

    /** Returns the result of comparing the corresponding class names. */
    public int compare(Class c1, Class c2) {
	return c1.getName().compareTo(c2.getName());
    }

}
