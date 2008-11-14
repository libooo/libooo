
import urllib
import web
import time

from searchrss.src import config

RENDER = web.template.render("templates/", cache=not config.DEBUG)

def install():
    web.template.Template.globals["render"] = RENDER
    web.template.Template.globals["len"] = len
    web.template.Template.globals["int"] = int
    web.template.Template.globals["urlEncode"] = urlEncode
    web.template.Template.globals["formatDay"] = formatDay
    web.template.Template.globals["formatDatetime"] = formatDatetime

def page(pageName, title, extraHead=None):
    """ Returns template
    to render given page inside shell.
    """
    def body(*args, **kwargs):
        body = getattr(RENDER, pageName)(*args, **kwargs)
        return RENDER.shell(body, title, extraHead)
    return body

def urlEncode(value):
    if isinstance(value, unicode):
        value = value.encode("utf-8")
    return urllib.quote_plus(value)

def formatDay(timestamp=None):
    #TODO: what to do with the localtime?
    # The appengine machines use GMT-07 (California).
    if timestamp is None:
        timestamp = time.time()
    return time.strftime("%Y-%m-%d", time.localtime(timestamp))

def formatDatetime(utcDt):
    return utcDt.strftime("%Y-%m-%dT%H:%M:%SZ")

install()
