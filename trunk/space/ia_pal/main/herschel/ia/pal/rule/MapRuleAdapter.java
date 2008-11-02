/* $Id: MapRuleAdapter.java,v 1.1 2007/07/10 16:20:46 hsiddiqu Exp $
 * Copyright (c) 2007 STFC
 */

package herschel.ia.pal.rule;

import herschel.ia.pal.*;
import herschel.share.predicate.*;

import java.util.*;

/**
  * Wrapper class to apply a rule on a product inside a map. Note that the "key" part
  * of the map is never checked by this rule.
  *
  * @author   S.Guest        RAL/SPIRE
  * @version  July 2006      
  */
public class MapRuleAdapter extends AbstractPredicate<Map.Entry<String, ProductRef>> {

    private final Predicate<ProductRef> _rule;

    /**
      * Create a rule applying to a map from a rule applying to a product.
      * @param  rule  applying to a product
      */
    public MapRuleAdapter (Predicate<ProductRef> rule) {
      _rule = rule;
    }

    /**
     * Evaluate a map entry.
     * Note that key component of this entry is not considered in the evaluation.
     * @param entry the map entry to be evaluated.
     */
    public boolean evaluate (Map.Entry<String, ProductRef> entry) {
	return _rule.evaluate (entry.getValue());
    }

    /**
      * Get a string representation of this rule.
      * @return  a string expressing the rule
      */
    public String toString() {
      return _rule.toString();
    }
}
