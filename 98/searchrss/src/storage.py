
import math
import time
import google.appengine.ext.db as db

from searchrss.src.model import Feed, FeedEntry, FeedLoad

def getFeed(name):
    """ Returns feed or None.
    """
    return Feed.get_by_key_name(name)

def getNewestFeeds(limit=1000):
    q = Feed.gql("order by updated desc")
    return q.fetch(limit=limit)

def getOldestFeeds(limit=1000):
    q = Feed.gql("order by updated asc")
    return q.fetch(limit=limit)

def getOutdatedFeedLoads(limit=1000):
    q = FeedLoad.gql("order by lastUpdateTimestamp asc")
    return q.fetch(limit=limit)

def getFeedEntries(feed, limit=1000):
    """ Returns N latest feed entries.
    """
    q = FeedEntry.gql("where ancestor is :1 order by index desc", feed)
    return q.fetch(limit=limit)

def updateFeedLoad(feed, increment=0):
    """
    Updates feed load value.
    The load drops with time:
    L(t) = 1 + L(t_last) * e**(-(t - t_last)/T)
    T ... after 3T the load is be reduced by ~95%.
    """

    T = 3600 * 24 / 3.0
    now = time.time()
    def txn():
        feedLoad = FeedLoad.get_by_key_name(feed.name)
        if feedLoad:
            deltaT = now - feedLoad.lastUpdateTimestamp
            feedLoad.load = increment + feedLoad.load * math.e**(-deltaT/T)
            feedLoad.lastUpdateTimestamp = now
        else:
            feedLoad = FeedLoad(key_name=feed.name, feed=feed)
        feedLoad.put()

    db.run_in_transaction(txn)

