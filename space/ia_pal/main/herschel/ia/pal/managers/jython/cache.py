# Wrap a cached pool around a pool of the default type
from herschel.ia.pal.pool.cache import CachedPool
from herschel.ia.pal.managers import PoolCreatorFactory
pool = CachedPool (PoolCreatorFactory.createPool (_1, PoolCreatorFactory.DEFAULT_POOLTYPE))
