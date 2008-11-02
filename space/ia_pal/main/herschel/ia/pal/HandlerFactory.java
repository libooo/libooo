/*
 * $Id: HandlerFactory.java,v 1.1 2008/05/30 13:30:26 jsaiz Exp $
 */
package herschel.ia.pal;

import herschel.share.util.Configuration;
import java.util.logging.Logger;

/**
 * Factory class for creating internal auxiliary handlers.
 */

class HandlerFactory {

    private final static String PROPERTY = "hcss.ia.pal.version";
    private final static String STRATEGY = Configuration.getProperty(PROPERTY);
    private static Logger LOG = Logger.getLogger(HandlerFactory.class.getName());

    static {
	LOG.info("Version strategy is " + STRATEGY);
    }

    private HandlerFactory() {
    }

    static VersionHandler getVersionHandler(ProductStorage storage) {
	if (STRATEGY.equals("none"))  return new IgnoreVersionHandler(storage);
	if (STRATEGY.equals("track")) return new StoredVersionHandler(storage);
	throw new IllegalStateException("Invalid value of " + PROPERTY + ": " + STRATEGY);
    }

    static TagsHandler getTagsHandler(ProductStorage storage) {
	if (STRATEGY.equals("none"))  return new MemoryTagsHandler();
	if (STRATEGY.equals("track")) return new StoredTagsHandler(storage);
	throw new IllegalStateException("Invalid value of " + PROPERTY + ": " + STRATEGY);
    }
}
