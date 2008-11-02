// -*-java-*-
//
// File:      QueryUtil.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Aug 30, 2007
// Usage:     -
// Info:      -

package herschel.ia.pal.util;

import herschel.ia.pal.query.StorageQuery;

/**
 * This class provides utility methods regarding queries.
 * TODO Maybe it should be put in another place.
 */
public class QueryUtil {

    public static final String SELECT_ALL = "1"; // means select all products

    /**
     * Informs whether the passed query corresponds to the "select all" query.
     */
    public static boolean isSelectAll(StorageQuery query) {
        String where = query.getWhere();

        // Remove initial and final brackets, if present
        if (where.charAt(0) == '(' && where.charAt(where.length() - 1) == ')') {
            where = where.substring(1, where.length() - 1).trim();
        }
        return where.equals(SELECT_ALL);
    }
}
