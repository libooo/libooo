/* $Id: UrnUtils.java,v 1.13 2008/10/20 11:28:22 bli Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal.util;

import herschel.ia.pal.ProductPool;
import herschel.ia.dataset.Product;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides utility methods when working with product URNs in PAL
 * pools.<p>
 * It makes use of the current pattern for a urn, which current pattern is not
 * part of the specification, and is prone to change.
 * This pattern is: "urn:site:class:id"</p>
 */
public class UrnUtils {

    private UrnUtils() {}

    // urn="urn:site:class:id"
    private static Pattern URN = Pattern.compile("^urn:([\\S^:]+):([\\S^:]+):(\\d+)$");
    private static Pattern TAG = Pattern.compile("[^\r\n]+");

    /**
     * Get the class contained in a URN.
     * @param urn reference.
     * @return a class known to this system
     * @throws IllegalArgumentException if urn has illegal format
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Product> getClass(String urn) {
        String message;
        try {
            Class<?> productClass = Class.forName(getClassName(urn));
            if (!Product.class.isAssignableFrom(productClass)) {
                message = "Class in urn "+ urn +" must extend "+ Product.class;
        	throw new IllegalArgumentException(message);
            }
            return (Class<? extends Product>)productClass;
        } catch (ClassNotFoundException e) {
            message = "Improper class in urn " + urn + ": " + e.getMessage();
            throw new IllegalArgumentException(message, e);
        }
    }

    /**
     * Get the class name contained in a URN.
     * @param urn reference.
     * @return a class name
     * @throws IllegalArgumentException if urn has illegal format
     */
    public static String getClassName(String urn) {
        return getMatcher(urn).group(2);
    }

    /**
     * Returns the product id part of the URN, that is, the last token.
     * @param  urn  to examine
     * @return product id
     * @throws IllegalArgumentException if urn has illegal format
     */
    public static int getProductId(String urn) {
        return Integer.parseInt(getMatcher(urn).group(3));
    }

    /**
     * Returns the pool id part of the URN.
     * @param  urn  to examine
     * @return pool id
     * @throws IllegalArgumentException if urn has illegal format
     */
    public static String getPoolId(String urn) {
        return getMatcher(urn).group(1);
    }

    /**
     * Returns the pool corresponding to the pool id inside the given urn.
     * @param  urn  to examine
     * @param  pools to search for a match
     * @return the product pool
     * @throws NoSuchElementException if no pool is found with that id.
     */
    public static ProductPool getPool(String urn, Iterable<ProductPool> pools)
    throws IOException, GeneralSecurityException {
	if (urn == null) {
	    throw new IllegalArgumentException("Cannot get the pool of a null urn");
	}
        String poolId = getPoolId(urn);        
        if (poolId.startsWith("db_")) {
            // The pool id is not contained in the URN in a DbPool
            for (ProductPool pool : pools) {
                if (pool.exists(urn)) {
                    return pool;
                }
            }
        } else {
            // No need to access the pools: get the pool from the urn
            // TODO Remove the following line when SPR-3335 is fixed
            if (poolId.startsWith("simple.")) poolId = poolId.substring(7);        	
            for (ProductPool pool : pools) {
                if (pool.getId().equals(poolId)) {
                    return pool;
                }
            }
        }
        String msg = "No such urn in any pool of storage " + pools + ": " + urn;
        throw new NoSuchElementException(msg);
    }

    /**
     * Returns the later of two urns.
     * The later is simply based on the product number that is the last token of
     * each urn.
     * @param  urn1  first URN
     * @param  urn2  second URN
     * @return the later of the two URNs
     * @throws IllegalArgumentException if any urn has illegal format
     */
    public static String getLater(String urn1, String urn2) {

        Matcher m1 = getMatcher(urn1);
        Matcher m2 = getMatcher(urn2);

        if (!m1.group(1).equals(m2.group(1)) ||
            !m1.group(2).equals(m2.group(2))) {
            String msg = "urn " + urn1 + " has a different base value " +
                         "(ie everything except the final id) to urn " + urn2;
            throw new IllegalArgumentException(msg);
        }

        return getProductId(urn1) >
               getProductId(urn2) ? urn1 : urn2;
    }

    /**
     * Informs whether a URN belongs to the given pool.
     * @param pool ProductPool where we are looking for the URN
     * @param urn URN to be checked against the pool
     * @return <code>true</code> if the URN belongs to the pool
     */
    public static boolean containsUrn(ProductPool pool, String urn) {
	try {
	    return getPool(urn, Arrays.asList(pool)) == pool;
	}
	catch (Exception e) {
	    return false;
	}
    }

    /**
     * Informs whether the given identifier corresponds to a URN.
     * @param identifier which might be a URN
     * @return <code>true</code> if it is a well-built URN
     */
    public static boolean isUrn(String identifier) {
	if (identifier == null) return false;
        Matcher matcher = URN.matcher(identifier);
        return (matcher.matches() && Util.isValidClassName(matcher.group(2)));
    }

    
    /**
     * Throw an IllegalArgumentException if the identifier is not a legal URN.
     * @param identifier
     * @throws an IllegalArgumentException if the argument is not a valid URN.
     */
    public static void checkUrn(String identifier){
    	if(!isUrn(identifier)){
    		throw new IllegalArgumentException("Not a legal URN: " + identifier);
    	}
    }
    
    /**
     * Informs whether the given identifier corresponds to a tag.
     * @param identifier which might be a tag
     * @return <code>true</code> if it is a well-built tag
     */
    // Could belong to a TagUtils class, but it can stay here also,
    // so we avoid creating a new class with only one static method.
    public static boolean isTag(String identifier) {
	if (identifier == null) return false;
        Matcher matcher = TAG.matcher(identifier);
        return (matcher.matches() && !identifier.startsWith("urn:"));
    }

    /**
     * Returns a matcher for a given URN.
     * @param  urn  to examine
     * @return a matcher object
     * @throws an IllegalArgumentException if the argument is not a valid URN.
     */
    static Matcher getMatcher(String urn) {
        Matcher matcher = URN.matcher(urn);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a legal URN: " + urn);
        }
        return matcher;
    }

    /**
     * Returns the urn assembled by class name and id. *
     */
    public static String getUrn(String poolId, String type, int id) {
	StringBuffer b = new StringBuffer("urn:");
	b.append(poolId).append(':').append(type).append(':').append(id);
	String urn = b.toString();
	if (!isUrn(urn)) {
	    throw new IllegalArgumentException("Not a legal URN to create: " + urn);
	}
	return urn;
    }

    /**
     * Extracts product IDs from a set of urns.
     */
    public static int[] extractRecordIDs(Set<String> urns) {
	int[] ids = new int[urns.size()];
	int current = 0;
	for (String urn : urns) {
	    if (isUrn(urn)) {
		ids[current++] = getProductId(urn);
	    }
	}
	return ids;
    }
}
