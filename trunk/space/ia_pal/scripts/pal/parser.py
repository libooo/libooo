from pal.tokenize import *
from pal.StringIO import *
from herschel.ia.dataset import Product
import pal.keyword

class parser:
	product = Product()
	toks = []
	queryType=''
	isAllAttrib=1
	isAllMeta=1
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
	def parse(self,queryStr,var='p',globNameSpace=globals(),localNameSpace=locals()):
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
				if not(pal.keyword.iskeyword(tokval) or globNameSpace.has_key(tokval) or localNameSpace.has_key(tokval)):
					if formerTokval!='.':
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
			if toknum==STRING:
				if formerTokval==var+'[':
					datasetContainsClause+=var+'.containsKey('+tokval+')'+' and '
			formerTokval=tokval
			formerToknum=toknum
			result.append((toknum, tokval))
		return metaContainsClause+datasetContainsClause+untokenize(result)
		
if __name__ == '__main__':

	p=parser()
	s = 'creator=="scott" and ABS(creationDate-endDate)<10'
	print p.parse(s)	
	print p.getQueryType()

	s = "type=='AbcProduct' and creator=='Scott' and ABS(cDate-endDate)>0"
	print p.parse(s, 'x')
	print p.getQueryType()

	s = ' ["a1"][3]=="scott" and ABS(cDate-endDate)<10'
	print p.parse(s)	
	print p.getQueryType()
	
