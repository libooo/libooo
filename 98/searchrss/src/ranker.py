"""
Tries to recognize relevant results.
Idea: Relevant today's results should be relevant
even in competition with results for whole month.
"""
import logging

from searchrss.src import search

COMPETITION_DAYS = 30
MAX_COMPETITION_REQUESTS = 3

def _fetchAllResults(query, daysBack, maxRequests=None):
    results = []
    searchData = search.search(query, daysBack)
    results += searchData["results"]
    if maxRequests is None:
        maxRequests = len(searchData["cursor"]["pages"])

    for page in searchData["cursor"]["pages"][1:maxRequests]:
        start = page["start"]
        data = search.search(query, daysBack, start=start)
        results += data["results"]
    return results

def _searchCompetitionResults(query):
    return _fetchAllResults(query, COMPETITION_DAYS, MAX_COMPETITION_REQUESTS)

def _extractUrls(results):
    return [a["unescapedUrl"] for a in results]

def searchRelevant(query, daysBack=1):
    return searchBoth(query, daysBack)[0]

def searchBoth(query, daysBack=1):
    results = search.search(query, daysBack=daysBack)["results"]
    if len(results) == 0:
        return [], []

    relevant = []
    excluded = []
    competition = _searchCompetitionResults(query)
    competitionUrls = _extractUrls(competition)
    for result in results:
        if result["unescapedUrl"] in competitionUrls:
            relevant.append(result)
            #logging.info("Found at: %s", competitionUrls.index(result["unescapedUrl"]))
        else:
            excluded.append(result)
    logging.debug("Relevant rate: %s/%s", len(relevant), len(results))

    return relevant, excluded
