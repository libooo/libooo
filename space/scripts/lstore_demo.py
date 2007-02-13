# This is a demo script to show how local store works. 

# @author libo@bao.ac.cn
# @version 1.3 2007-1-8

from herschel.ia.pal.pool.lstore import *
from herschel.ia.pal import *
from herschel.ia.pal.query import *
from herschel.ia.pal.query import Query
from herschel.ia.dataset import *
from java.io import *

# create a local store
store=LocalStoreFactory.getStore("test");
# get context information
print store.getContext().getStoreDir();
# rebuild index, this is intended be called when the index files are corruptted.  
print store.rebuildIndex()

p=Product(creator="Scott", description="Tiger");
# save product
urn=store.save(p);
print urn;


p.type='AbcProduct';
# save prodcut with specified file name.
print store.save(p, 'AbcProduct');

# save prodcut with specified directory.
p.meta["context"] = StringParameter ("/Product_01")
print store.save(p);
p.meta["context"] = StringParameter ("/Product_01/foo")
print store.save(p);
p.meta["context"] = StringParameter ("/Product_01/foo/bar")
print store.save(p);

# get metadata
meta=store.meta(urn);
print meta;

# Full Query
query=FullQuery(Product,"p","p.creator=='Scott'")
results=store.select(query)
for item in results:
    print item, store.load(item).creator
    pass

# Attribute Query
query=AttribQuery(Product,"p","p.creator=='Scott'")
results=store.select(query)
for item in results:
    print item
    pass

# Meta Query
query=MetaQuery(Product,"p","p.creator=='Scott'")
results=store.select(query)
for item in results:
    print item, store.load(item).creator
    pass

# Staged Query

query=MetaQuery(Product,"p","p.type=='AbcProduct'")
results=store.select(query, results)
print results

# In Place Ingesting 
storeFoo=LocalStoreFactory.getStore("foo");
storeFoo.ingest(store.getContext().getStoreDir(), 1);
print storeFoo.select(query);
#print storeFoo.load("urn:foo:herschel.ia.dataset.Product:97").type

# new Query API
from pal.parser import *
p=parser()
a=3
b=4
query=Query( "type=='AbcProduct' and creator=='Scott' and ABS(a-b)>0")
#query.where will be converted to: 
#  query=Query( "type=='AbcProduct' and creator=='Scott' and ABS(a-b)>0")
print query.where
results=store.select(query);
print results

#dummy user defined function
def sumABB(a,b):
	return a+b+b

#query with user defined function
query=Query("type=='AbcProduct' and creator=='Scott' and sumABB(a, b)<0")
#query.where will be converted to: 
#  p.type =='AbcProduct'and p.creator =='Scott'and sumABB (a ,b )<0 
print query.where
results=store.select(query);
print results

#trivial one, variable used in query could be specified
query=Query(Product,'x', "type=='AbcProduct' and creator=='Scott' and sumABB(a, b)>0")
#query.where will be converted to: 
#  x.type =='AbcProduct'and x.creator =='Scott'and sumABB (a ,b )>0  
print query.where
results=store.select(query);
print results

#data mining query
#can not work yet! 
query=Query("['A1']['flux'].data > 0.01")
# should get p['A1']['flux'].data > 0.01, but not current "['A1']['flux'].p.data >0.01"
print query.where


