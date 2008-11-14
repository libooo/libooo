
import logging
import datetime
import google.appengine.ext.db as db

from searchrss.src import storage, ranker
from searchrss.src.model import Feed, FeedEntry

N_CHECKS_FOR_DUPLICATES = 100

def _nameQuery(query):
    #TODO: is a sanitization needed?
    return query[:500]

def createFeed(query):
    name = _nameQuery(query)
    feed = Feed.get_or_insert(key_name=name, name=name, query=query)
    if feed.nEntries == 0:
        updateFeed(feed)
    return feed

def _getNewResults(feed):
    results = ranker.searchRelevant(feed.query)
    existings = storage.getFeedEntries(feed, N_CHECKS_FOR_DUPLICATES)
    existingUrls = [entry.link for entry in existings]
    uniqueResults = []
    for result in results:
        if result["unescapedUrl"] not in existingUrls:
            uniqueResults.append(result)
    return uniqueResults

def _convertToFeedEntry(feed, result):
    title = result["title"]
    link = result["unescapedUrl"]
    summary = result["content"]
    visibleUrl = result["visibleUrl"]
    return FeedEntry(parent=feed,
            title=title, link=link, summary=summary, visibleUrl=visibleUrl)

def _addNewEntries(feed, entries):
    logging.info("Adding %s entries to: %r", len(entries), feed.name)
    # let the most relevant to have bigger index
    entries = list(reversed(entries))
    for i, entry in enumerate(entries):
        entry.index = feed.nEntries + i
    oldCount = feed.nEntries
    feed.nEntries += len(entries)

    def txn():
        storedFeed = db.get(feed.key())
        if storedFeed.nEntries != oldCount:
            logging.warning("Updated in the meantime: %s != %s",
                    storedFeed.nEntries, oldCount)
            return

        feed.put()
        db.put(entries)

    db.run_in_transaction(txn)

def updateFeed(feed):
    results = _getNewResults(feed)
    if not results:
        logging.info("No new results for: %r", feed.name)
        # feed.updated signalizes processed feed
        feed.put()
        return

    entries = []
    for result in results:
        entries.append(_convertToFeedEntry(feed, result))
    _addNewEntries(feed, entries)

