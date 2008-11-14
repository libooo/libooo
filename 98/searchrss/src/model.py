
import google.appengine.ext.db as db

class Feed(db.Model):
    """
    key_name ... feed name
    """
    name = db.StringProperty(required=True)
    query = db.TextProperty(required=True)
    updated = db.DateTimeProperty(auto_now=True)
    nEntries = db.IntegerProperty(default=0, required=True)

class FeedEntry(db.Model):
    """
    id ... a number
    parent ... Feed
    index ... relevance and freshness index, it increments with new entries.
    Most relevant entries from the same day are added with bigger index.
    """
    index = db.IntegerProperty(default=0, required=True)
    updated = db.DateTimeProperty(auto_now=True)
    link = db.LinkProperty(required=True)
    title = db.StringProperty(required=True)
    summary = db.TextProperty(required=True)
    visibleUrl = db.TextProperty(required=True)

class FeedLoad(db.Model):
    """
    key_name ... feed name
    """
    feed = db.ReferenceProperty(Feed, required=True)
    load = db.FloatProperty(default=0.0, required=True)
    lastUpdateTimestamp = db.FloatProperty(default=0.0, required=True)

