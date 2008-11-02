// -*-java-*-
//
// File:      InputCheck.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Mar 5, 2008
// Usage:     -
// Info:      -

package herschel.ia.pal.util;

import herschel.ia.dataset.Product;

/**
 * This class provides miscellaneous utility methods.
 */
public class Util {

    /**
     * Throws an IllegalArgumentException if the provided input is null.
     */
    public static void checkNotNull(Object object, String name) {
	if (object == null) {
	    String message = "Cannot handle a reference to a null " + name;
	    throw new IllegalArgumentException(message);
	}
    }

    /**
     * Convenience method to convert a named class into a real class object.
     * @param className
     * @return the Class instance of specified className
     * @throws IllegalArgumentException
     *         if specified className can not represent a legal class.
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Product> asClass(String className) {
	try {
	    return (Class<? extends Product>) Class.forName(className);
	} catch (ClassNotFoundException e) {
	    String message = "Improper class name: '" + className + "': " + e;
	    throw new IllegalArgumentException(message, e);
	}
    }

    /**
     * Utility method that determines whether specified derived class belongs
     * to the family of specified base class.
     * @param base Base class
     * @param derived Class to be checked whether it derives from base
     * @return whether the derived class sub-classes the base class.
     */
    public static boolean isClass(Class<?> base, Class<?> derived) {
	if (base == null || derived == null) {
	    return (base == null && derived == null);
	}
	return base.isAssignableFrom(derived);
    }

    /**
     * Checks if the given class name is a valid Java class name.<p>
     * 
     * @param className the name to check
     * @return <code>true</code> if the given string is a valid Java class name,
     * <code>false</code> otherwise.
     */
    public static boolean isValidClassName(String className) {
	if (className == null || className.equals("")||className.endsWith(".")){
	    return false;
	}
	int length = className.length();
	boolean nodot = true;
	for (int i = 0; i < length; i++) {
	    char ch = className.charAt(i);
	    if (nodot) {
		if (ch == '.') {
		    return false;
		} else if (Character.isJavaIdentifierStart(ch)) {
		    nodot = false;
		} else {
		    return false;
		}
	    } else {
		if (ch == '.') {
		    nodot = true;
		} else if (Character.isJavaIdentifierPart(ch)) {
		    nodot = false;
		} else {
		    return false;
		}
	    }
	}
	return true;
    }
}
