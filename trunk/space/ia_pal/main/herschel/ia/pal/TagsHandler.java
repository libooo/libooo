// -*-java-*-
//
// File:      TagsHandler.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Mar 4, 2008
// Usage:     -
// Info:      -

package herschel.ia.pal;

import herschel.ia.pal.util.UrnUtils;
import herschel.share.util.StackTrace;

/**
 * Manages tags of all pools in the storage.<p>
 * Implementation note: it does not hold any tags map in memory, in order to
 * avoid duplicating information that probably is already hold in the pools.
 */
abstract class TagsHandler implements Taggable {

    public String toString() {
        try {
            StringBuffer b = new StringBuffer("\nTags:\n----\n");
            for (String tag : getTags()) {
        	b.append(tag).append(" -> ");
        	b.append(getUrn(tag)).append("\n");
            }
            return b.toString();
        } catch (Exception e) {
            return "***Couldn't get tags info***:\n"+ StackTrace.trace(e);
        }
    }

    protected void checkTag(String tag) {
	if (!UrnUtils.isTag(tag)) {
	    throw new IllegalArgumentException("Not a valid tag: " + tag);
	}
    }

    protected void checkUrn(String urn) {
	if (!UrnUtils.isUrn(urn)) {
	    throw new IllegalArgumentException("Not a valid urn: " + urn);
	}
    }
}
