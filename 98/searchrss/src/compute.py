
import logging
import random
from searchrss.src import storage, feeder

def compute(what=None):
    """
    Updates internal states.
    Does one change in time to fit inside appengine timeout.
    """
    if not what:
        if random.randint(0, 1) == 0:
            what = "feeds"
        else:
            what = "loads"

    if what == "feeds":
        _updateOldestFeeds()
    elif what == "loads":
        _updateOutdatedFeedLoads()
    else:
        raise ValueError("Unknown what: %s" % what)

def _updateOldestFeeds():
    batchSize = 1
    for feed in storage.getOldestFeeds(batchSize):
        feeder.updateFeed(feed)

def _updateOutdatedFeedLoads():
    batchSize = 5
    feedLoads = storage.getOutdatedFeedLoads(batchSize)
    for feedLoad in feedLoads:
        storage.updateFeedLoad(feedLoad.feed)
    logging.debug("Updated %s feed loads", len(feedLoads))

