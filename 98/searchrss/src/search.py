
import time

import logging
import re
from django.utils import simplejson
from searchrss.src import fetch

# List of searches from
# http://code.google.com/apis/ajaxsearch/documentation/reference.html#_fonje_urlbase
#SEARCHES = ("web", "local", "video", "blogs", "news", "books", "images", "patent")
# But the 'daterange:' operator is working only for 'web'.
SEARCHES = ("web",)
SEARCH_URL = "http://ajax.googleapis.com/ajax/services/search/%s"
SEARCHER_PATTERN = re.compile(r"\bsearch:([a-z]+)")

HEADERS = {
        "Referer": "http://lever.appspot.com/",
        }
EMPTY_RESPONSE_DATA = {
        "results": [],
        "cursor": {
            "pages": [
                { "start": 0, "label": 1 },
                ],
            "estimatedResultCount": 0,
        }
    }

def _timestamp2Julian(timestamp=None):
    """
    Calculates number of days since Julian epoch (January 1, 4713 BC)
    and the given UTC timestamp.
    Algorithm taken from:
    http://www.webexhibits.org/calendars/calendar-christian.html
    """
    t = time.gmtime(timestamp)
    a = (14 - t.tm_mon)//12
    y = t.tm_year + 4800 - a
    m = t.tm_mon + 12*a - 3
    return t.tm_mday + ((153*m + 2)//5) + 365*y + y//4 - y//100 + y//400 - 32045

def _getJulianDaysFromNow(days):
    """ Returns Julian date for now + days.
    """
    now = time.time()
    yesterday = now + days*24*3600
    return _timestamp2Julian(yesterday)

def _convert(data, convertor, *path):
    item = data
    for i, name in enumerate(path):
        if i + 1 < len(path):
            item = item[name]
        else:
            item[name] = convertor(item[name])

def _extractSearcher(query):
    searcher = "web"
    for name in SEARCHER_PATTERN.findall(query.lower()):
        if name in SEARCHES:
            searcher = name
    query = SEARCHER_PATTERN.sub("", query)
    return searcher, query

def search(query, daysBack=None, start=0):
    searcher, query = _extractSearcher(query)
    searchUrl = SEARCH_URL % searcher
    responseData = _search(searchUrl, query, daysBack, start)
    responseData["searcher"] = searcher
    return responseData

def _search(searchUrl, query, daysBack, start):
    if daysBack is not None:
        julianTomorrow = _getJulianDaysFromNow(1)
        julianYesterday = _getJulianDaysFromNow(-daysBack)
        query = "daterange:%s-%s %s" % (julianYesterday, julianTomorrow, query)

    options = {
            "v": "1.0",
            "rsz": "large",
            "q": query,
            "start": str(start),
            }
    logging.debug("Searching from %s: %r", start, query)
    try:
        content = fetch.fetchContent(searchUrl, options, HEADERS)
        response = simplejson.loads(content)
        if response.get("responseStatus") != 200:
            logging.error("Invalid search response: %s", content)
            return EMPTY_RESPONSE_DATA
        searchData = response.get("responseData", EMPTY_RESPONSE_DATA)
        if not searchData.get("results") or not searchData.get("cursor"):
            return EMPTY_RESPONSE_DATA

        _convert(searchData, int, "cursor", "estimatedResultCount")
        for page in searchData["cursor"]["pages"]:
            _convert(page, int, "start")
        return searchData
    except Exception:
        logging.exception("Unable to search for: %r", query)
        return EMPTY_RESPONSE_DATA

