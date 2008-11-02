#$Id: parser.py,v 1.2 2008/08/09 02:21:59 schen Exp $
#Copyright (c) 2006 NAOC
#

from herschel.ia.pal.query.parser.jython.tokenize import *
from herschel.ia.pal.query.parser.jython.StringIO import *
from herschel.ia.dataset import Product
import herschel.ia.pal.query.parser.jython.keyword

class parser:
	product = Product()
	toks = []
	queryType=''
	isAllAttrib=1
	isAllMeta=1
	metaFlag=0
	def getQueryType(self):
		if self.isAllAttrib:
			self.queryType='AttribQuery'
		elif self.isAllMeta:
			self.queryType='MetaQuery'
		else: 
			self.queryType='FullQuery'
		return self.queryType
	def tokeater(self,toknum,tokval,spos,epos,line):
		self.toks.append((toknum,tokval,spos,epos,line))	
	def parse(self,queryStr,var='p',globNameSpace=globals(),localNameSpace=locals(),isQuiet=1):
		self.toks=[]	
		tokenize(StringIO(queryStr).readline,self.tokeater)
		tokens = self.toks
		result = []
		metaContainsClause=''
		datasetContainsClause=''
		formerToknum=ENDMARKER
		formerTokval=''
		for toknum, tokval, spos, epos, line in tokens:
			if toknum == NAME:
				if not(herschel.ia.pal.query.parser.jython.keyword.iskeyword(tokval) or globNameSpace.has_key(tokval) or localNameSpace.has_key(tokval)):
					if tokval=='meta':
						self.isAllAttrib=0
						self.metaFlag=1
						tokval=var+"."+tokval
					elif formerTokval!='.' :
						if self.product.meta.containsKey(tokval)==0:
							self.isAllAttrib=0
							metaContainsClause+=var+'.meta.containsKey(\''+tokval+'\')'+' and '
							tokval= var+'.meta[\''+ tokval+'\'].value'
						else:
							tokval=var+"."+tokval
			if tokval=='[':
				if formerToknum != NAME and formerTokval !=']':
					tokval=var+tokval
					self.isAllAttrib=0
					self.isAllMeta=0
			if tokval==']':
				if self.metaFlag==1:
					self.metaFlag=0
					tokval=tokval+'.value'						
			if toknum==STRING:
				if formerTokval==var+'[':
					datasetContainsClause+=var+'.containsKey('+tokval+')'+' and '
				elif formerTokval == '[' and self.metaFlag==1:
					metaContainsClause += var + '.meta.containsKey('+tokval+')'+' and '
			formerTokval=tokval
			formerToknum=toknum
			result.append((toknum, tokval))
            	if isQuiet:
                	parsedQuery = metaContainsClause+datasetContainsClause+'('+untokenize(result)+')'
            	else:
                	parsedQuery = untokenize(result)
		return parsedQuery
		
if __name__ == '__main__':
	p=parser()
	s = 'creator=="scott" and ABS(creationDate-endDate)<10'
	print p.parse(s)	
	print p.getQueryType()

	s = "type=='AbcProduct' and creator=='Scott' and ABS(cDate-endDate)>0"
	print p.parse(s, 'x')
	print p.getQueryType()

	s = ' ["a1"][3].value=="scott" and ABS(cDate-endDate)<10'
	print p.parse(s)	
	print p.getQueryType()
	
	s = 'title=="Tiger" and meta["keyword"]=="scott"'
	print p.parse(s)
	print p.getQueryType()
	
