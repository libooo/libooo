/* $Id: Taggable.java,v 1.2 2008/03/31 15:59:15 jsaiz Exp $
 * Copyright (c) 2007 ESA, STFC
 */
package herschel.ia.pal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
  * Definition of services provided by a product storage supporting tagging.
  * @author   S.Guest  RAL/SPIRE
  * @version  March 2008
  */
public interface Taggable {

  /**
    * Sets the specified tag to the given URN.
    * @param  tag to set
    * @param  urn to set it to
    */
  void setTag (String tag, String urn) throws IOException, GeneralSecurityException;

  /**
    * Remove the given tag.
    * @param  tag to remove
    */
  void removeTag (String tag) throws IOException, GeneralSecurityException;

  /**
    * Remove the given urn from the tag-urn map.
    * @param  urn to remove
    */
  void removeUrn (String urn) throws IOException, GeneralSecurityException;

  /**
    * Get all known tags.
    * @return set of tags
    */
  Set<String> getTags () throws IOException, GeneralSecurityException;
 
  /**
    * Get all the tags that map to a given URN.
    * @param  urn to look for
    * @return set of tags
    */
  Set<String> getTags (String urn) throws IOException, GeneralSecurityException;

  /**
    * Tests if a tag exists.
    * @param  tag to check
    * @return <code>true</code> if it does
    */
  boolean tagExists (String tag) throws IOException, GeneralSecurityException;

  /**
    * Get the URN corresponding to the given tag.
    * @param  tag to look for
    * @return the URN
    * @throws NoSuchElementException if the tag is not found.
    */
  String getUrn (String tag) throws IOException, GeneralSecurityException;
}
