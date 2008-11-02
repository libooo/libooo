#$Id: PalApiTest.py,v 1.6 2008/08/09 02:29:43 schen Exp $
#Copyright (c) 2006 NAOC
#
# This is the unit test script for the new PAL API. 

# @author libo@bao.ac.cn
# @version 1.3 2007-3-1

from herschel.ia.pal.pool.lstore import *
from herschel.ia.pal import *
from herschel.ia.pal.query import *
from herschel.ia.pal.query import Query
from herschel.ia.dataset import *
from herschel.ia.numeric import *
from herschel.ia.numeric import Double1d
from java.io import *
from java.util import *

from java.lang.Math import hypot

import unittest

class PalApiTest(unittest.TestCase):
	# create a local store, prepare test products
	try:
	    LStoreMaintenanceUtil.clear(LocalStoreFactory.getStore("test.PalApiTest"));
	except:
	    pass    
	store=LocalStoreFactory.getStore("test.PalApiTest");
	p=Product(creator="Scott", description="Tiger")
	p.meta['title']=StringParameter('Tiger')
	p.meta['keyword']=StringParameter('Scott')
	x=Double1d.range(100)
	t=TableDataset(description="This is a table")
	t["x"]=Column(x)
	p["myTable"]=t
	# save product
	store.save(p)  
	p.type='AbcProduct'
	store.save(p)
    
	def setUp(self):
		return

 	def tearDown(self):
		return
	   
	def testSimpleAttribQuery(self):
		query=Query("type=='AbcProduct' and creator=='Scott'") 
		self.assertEquals(query.where,"(p.type =='AbcProduct'and p.creator =='Scott')", query.where)
		self.assertEquals(query.queryType,"AttribQuery", query.queryType)
		results=self.store.select(query);
		self.assertEquals(results.size()>0,1,results.size())

	def testSimpleMetaQuery(self):
		query=Query("type=='AbcProduct' and width==5") 
		self.assertEquals(query.where,"p.meta.containsKey('width') and (p.type =='AbcProduct'and p.meta['width'].value ==5 )", query.where)
		self.assertEquals(query.queryType,"MetaQuery", query.queryType)
		results=self.store.select(query);
		self.assertEquals(results.size()>0,0,results.size())

	def testFullQuery(self):
		query=Query("['myTable']['x'].data[20] > 10")
		self.assertEquals(query.where,"p.containsKey('myTable') and (p['myTable']['x'].data [20 ]>10 )", query.where)
		self.assertEquals(query.queryType,"FullQuery", query.queryType)
		results=self.store.select(query);
		self.assertEquals(results.size()>0,1,results.size())

	#query iterator
	def testQueryIterator(self):
		map=HashMap()
		foo=Product()
		foo.creator='foo'
		foo.description='foo'
		map.put("foo", ProductRef(foo))
		bar=Product()
		bar.creator='bar'
		bar.description='bar'
		map.put("bar", ProductRef(bar))
		q = Query("creator=='foo'");
		r=q.match( map.values().iterator())
		for item in r:
			self.assertEquals(item.product.creator=='foo', 1)
			print item.product
			pass	

	#query Iterable
	def testQueryIterable(self):
		iter = ArrayList()
		foo=Product()
		foo.creator='foo'
		foo.description='foo'
		bar=Product()
		bar.creator='bar'
		bar.description='bar'
		iter.add(ProductRef(foo))
		iter.add(ProductRef(bar))
		q = Query("creator=='bar'");
		r=q.match(iter)
		for item in r:
			self.assertEquals(item.product.creator=='bar',1)
			print item.product
			pass

	#query context
	def testQueryContext(self):
		lc = ListContext();
		foo = Product("foo");
		bar = Product("bar");
		mc = MapContext();
		foo2 = Product("foo2");
		bar2 = Product("bar2");
		mc.getRefs().put("foo", ProductRef(foo2));
		mc.getRefs().put("bar", ProductRef(bar2));
		lc.getRefs().add(ProductRef(foo));
		lc.getRefs().add(ProductRef(bar));
		lc.getRefs().add(ProductRef(mc));
		q = Query("1==1");
		r=q.match(lc)
		self.assertEquals(r.size()==4,1)
		for item in r:
		    print item.product
		    pass

	#test SPR-3360
	def testSPR3360(self):
		q = Query (Product, "1")
		print q.getQueryType()
		self.assertEquals(q.getQueryType()=='AttribQuery',1)

	def makeProduct (self, ra, dec):
	    p = Product (type="M51")
	    p.meta["raNominal"] = DoubleParameter (ra)
	    p.meta["decNominal"] = DoubleParameter (dec)
	    p.meta["releaseStatus"] = StringParameter ("public")
	    p.meta["quality"] = StringParameter ("good")
	    return p
		
	def testQueryWithMethodInNameSpace(self):
		print "enter testQueryWithMethodInNameSpace..."
		u1=self.store.save (self.makeProduct(202.34, 47.255))
		u2=self.store.save (self.makeProduct(202.33, 47.27))
		print u1, " ", u2
		m51_ra = 202.35
		m51_dec = 47.26
		radius = 500. / 60.
		#print hypot (202.34 - m51_ra, 47.255 - m51_dec)
		#print hypot (202.33 - m51_ra, 47.27 - m51_dec)
		#print radius
		q = Query ("type=='M51' and releaseStatus=='public' and quality=='good' and \
			hypot (raNominal - m51_ra, decNominal - m51_dec) <= radius")
		print q.where
		results = self.store.select(q)
		#self.assertEquals(results.size()==2,1,results.size())
		for item in results:
			p=self.store.load(item)
			print p.meta["raNominal"], " ", p.meta["quality"]
			pass
			
	def testSelfSpecifiedMeta(self):
		title = 'abc'
		keyword = 'xyz'
		query = Query("title=='Tiger' and meta['keyword']=='Scott'")		
		print query.where
		#self.assertEquals(query.where,"p.meta.containsKey('title') and p.meta.containsKey('keyword') and (p.meta['title'].value =='Tiger'and p.meta ['keyword'].value=='Scott')", query.where)
		self.assertEquals(query.queryType,"MetaQuery", query.queryType)		
		results=self.store.select(query);
		self.assertEquals(results.size()>0,1,results.size())		
	
					
if __name__ == '__main__':
	unittest.main()

