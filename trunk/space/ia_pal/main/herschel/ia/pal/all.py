"""
Imports all the Product Access Layer entities required for Users into
the global namespace.
"""

# 1) load entities in ia_pal (but not package 'rule'; it is for developers!):
from herschel.ia.pal import *
from herschel.ia.pal.query import *
from herschel.ia.pal.rule import *
from herschel.ia.pal.pool import *
from herschel.ia.pal.managers import *


# 2) The following sub-packages are imported on availability basis
#    as they are build independently.
#    It follows the same mechanism as specified in:
#    "HCSS-WP-9.2: Numeric Contribution",
#    which has the intent to split-up heavy-weight HCSS DP modules (in this
#    case 'ia_pal') into smaller ones.
for subpkg in ['browser',
               'pool.cache',
               'pool.serial',
               'pool.simple',
               'pool.db',
               'pool.lstore',
               'managers']:
    try:
        exec("from herschel.ia.pal.%s import *"%subpkg)
    except ImportError,e:
        print "WARNING: Missing herschel.ia.pal.%s"%subpkg
        pass
    pass
del(subpkg)
# ---

