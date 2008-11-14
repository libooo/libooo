
import logging
import time
import urllib
from google.appengine.api import urlfetch

def fetchContent(url, options={}, headers={}):
    url = prepareUrl(url, options)
    logging.debug("Fetching: %s", url)
    try:
        result = urlfetch.fetch(url, headers=headers)
    except urlfetch.DownloadError:
        logging.warning("Got DownloadError for: %s", url)
        # retry once
        time.sleep(0.1)
        result = urlfetch.fetch(url, headers=headers)

    if result.status_code != 200:
        raise IOError("Wrong code: %s, on: %s" % (result.status_code, url))
    return result.content

def prepareUrl(url, options={}):
    first = url.find("?") == -1
    for key, value in options.iteritems():
        if first:
            url += "?"
            first = False
        else:
            url += "&"
        url += "%s=%s" % (_urlquote(key), _urlquote(value))
    return url

def _urlquote(value):
    """ Quotes str or unicode for URL.
    Plain urllib.quote() would raise KeyError on unicode.
    """
    if isinstance(value, unicode):
        value = value.encode("utf-8")
    return urllib.quote(value)

