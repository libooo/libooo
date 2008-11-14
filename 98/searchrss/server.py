
import wsgiref.handlers
from google.appengine.ext import webapp

import logging

from searchrss.src import config,ranker,formatter, handler, storage

N_FEED_ENTRIES = 20

class Handler(webapp.RequestHandler):
    def show(self, output):
        self.response.out.write(output)

    def showTemplate(self, pageName, title, extraHead=None):
        pageTemplate = formatter.page(pageName, title, extraHead)
        def outputer(*args, **kwargs):
            self.show(pageTemplate(*args, **kwargs))
        return outputer

class Search(Handler):
    def get(self):
        query = self.request.get("q")
        if not query:
            self.redirect("/")
            return

        if self.request.get("feedme"):
            from searchrss.src import feeder
            feed = feeder.createFeed(query)
            self.redirect("/search/feed?name=" + formatter.urlEncode(feed.name))
            return

        start = handler.validN(self.request.get("start"))
        results, excluded = ranker.searchBoth(query, daysBack=1)
        extraHead = formatter.RENDER.part_feed_link(query)
        self.showTemplate("search", query + " - " + _(u"Today's Results"),
                extraHead)(query, results, excluded)

class FeedList(Handler):
    def get(self):
        limit = 6
        #TODO: show the most visited feeds, not the newest
        feeds = storage.getNewestFeeds(limit)
        self.showTemplate("feeds", _(u"Daily Search Results"))(feeds)

class FeedExport(Handler):
    def get(self):
        limit = handler.validN(self.request.get("limit", N_FEED_ENTRIES))
        name = self.request.get("name")
        if not name:
            self.redirect("/search/feeds")
            return

        feed = storage.getFeed(name)
        if feed is None:
            logging.warning("No such feed: %r", name)
            self.redirect("/search?q="+formatter.urlEncode(name))
            return

        storage.updateFeedLoad(feed, 1)
        self.response.headers["Content-Type"] = "application/atom+xml; charset=utf-8"
        feedEntries = storage.getFeedEntries(feed, limit)
        self.show(formatter.RENDER.feed(feed, feedEntries))

class Compute(Handler):
    def get(self):
        from searchrss.src import compute
        what = self.request.get("what")
        compute.compute(what)
        self.response.headers["Content-Type"] = "text/plain; charset=utf-8"
        self.show("OK")

class Info(Handler):
    def get(self):
        self.showTemplate("info", _(u"About Lever"))()

app = webapp.WSGIApplication(
        [
            ('/', FeedList),
            ('/search', Search),
            ('/search/', Search),
            ('/search/feeds', FeedList),
            ('/search/feeds/', FeedList),
            ('/search/feed', FeedExport),
            ('/search/feed/', FeedExport),
            ('/compute', Compute),
            ('/info', Info),
        ],
        debug=config.DEBUG)

def main():
    wsgiref.handlers.CGIHandler().run(app)

if __name__ == "__main__":
    global _
    _ = lambda x: x
    main()
