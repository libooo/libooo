#$Id: pal_api_demo.py,v 1.4 2007/09/11 14:28:12 bli Exp $
#Copyright (c) 2006 NAOC
#
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
print store.save(p);

p.creator="Bob"
print store.save(p);

p.type='AbcProduct';
print store.save(p);

# a simple util to show the result of the query for demonstration 
def showRS(rs):
	for r in rs:
		p = store.load(r)
		print r, " - creator: "+ p.creator 

def testQuery(query):
	rs=store.select(query);
	showRS(rs);

# new Query API
query = Query ("instrument=='foo' and width>=200 ")
# User could specify whether non-existent meta data or attributes appearing in the query should be
# quietly ignored by configuring hcss.ia.pal.query.isQuiet in herschel\ia\pal\defns\pal.xml. The default
# value is true. The converted result as shown by query.where will be: 
# p.meta.containsKey('width') and (instrument =='foo'and p.meta['width'].value >=200)
# During run-time if metadata does not contain 'with' there would be no warning or complaints given.
print query.where
# if the hcss.ia.pal.query.isQuiet is set to false
query.setQuiet(0)
# the query will be converted to simplly
#  instrument ='foo'and p.meta['width'].value >=200
print query.where

#queryType is MetaQuery
print query.queryType

name='Bob'
query=Query( "creator==name")
#query.where will be converted to: 
#  p.creator ==name 
print query.where
#queryType is AttribQuery
print query.queryType

testQuery(query)

#dummy user defined function
def getScott():
	return "Scott"

#query with user defined function
query=Query("type=='AbcProduct' and creator==getScott()")
#query.where will be converted to: 
#  p.type =='AbcProduct'and p.creator ==getScott ()
print query.where
#queryType is AttribQuery
print query.queryType
testQuery(query);

#data mining query
query=Query("['myTable']['x'].data[20] > 10")
#query.where will be converted to: 
#  p.containsKey('myTable') and p['myTable']['x'].data [20 ]>10 
print query.where
#queryType is FullQuery
print query.queryType
testQuery(query)

#prepare data
foo=Product()
foo.creator='foo'
foo.description='foo'
bar=Product()
bar.creator='bar'
bar.description='bar'

#query context
lc = ListContext();
lc.getRefs().add(ProductRef(foo));
lc.getRefs().add(ProductRef(bar));
q = Query("creator=='foo'");
r=q.match(lc)
for item in r:
    print item.product

#query iterator
from java.util import *
map=HashMap()
map.put("foo", ProductRef(foo))
map.put("bar", ProductRef(bar))
q = Query("creator=='foo'");
r=q.match( map.values().iterator())
for item in r:
    print item.product

#query Iterable
iter = ArrayList()
iter.add(ProductRef(foo))
iter.add(ProductRef(bar))
q = Query("creator=='bar'");
r=q.match(iter)
for item in r:
    print item.product

