# This is a demo script to show how the new PAL API works. 

# @author libo@bao.ac.cn
# @version 1.3 2007-2-17

from herschel.ia.pal.pool.lstore import *
from herschel.ia.pal import *
from herschel.ia.pal.query import *
from herschel.ia.pal.query import Query
from herschel.ia.dataset import *
from java.io import *

# create a local store, prepare test products
store=LocalStoreFactory.getStore("test");
p=Product(creator="Scott", description="Tiger")
x=Double1d.range(100)
t=TableDataset(description="This is a table")
t["x"]=Column(x)
p["myTable"]=t
# save product
urn=store.save(p);
print urn;

p.type='AbcProduct';
print store.save(p);

# new Query API
from pal.parser import *
query = Query ("instrument=='Foo' and width>=200 ")
# User could specify whether non-existent meta data or attributes appearing in the query should be
# quietly ignored by configuring hcss.ia.pal.query.isQuiet in herschel\ia\pal\defns\pal.xml. The default
# value is true. The converted result as shown by query.where will be: 
# p.meta.containsKey('width') and (instrument =='foo'and p.meta['width'].value >=200)
# During run-time if metadata does not contain 'with' there would be no warning or complaints given.
print query.where
# if the hcss.ia.pal.query.isQuiet is set to false
query.setQuiet(0)
# the query will be converted to simplly
#  instrument ='Foo'and p.meta['width'].value >=200
print query.where

#queryType is MetaQuery
print query.queryType

a=3
query=Query( "type=='AbcProduct' and creator=='Scott' and ABS(a-b)>0")
#query.where will be converted to: 
#  p.meta.containsKey('b') and p.type =='AbcProduct'and p.creator =='Scott'and ABS (a -p.meta['b'].value )>0 
print query.where
#queryType is MetaQuery
print query.queryType

#dummy user defined function
def sum(a,c):
	return a+c

#query with user defined function
a=3
c=4
query=Query("type=='AbcProduct' and creator=='Scott' and sum(a, c)>0")
#query.where will be converted to: 
#  p.type =='AbcProduct'and p.creator =='Scott'and sum (a , c)>0 
print query.where
#queryType is AttribQuery
print query.queryType
results=store.select(query);
print results

#data mining query
query=Query("['myTable']['x'].data[20] > 10")
#query.where will be converted to: 
#  p.containsKey('myTable') and p['myTable']['x'].data [20 ]>10 
print query.where
#queryType is FullQuery
print query.queryType
results=store.select(query);
print results
