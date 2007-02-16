from pal.tokenize import *
from pal.StringIO import *
from herschel.ia.dataset import Product
import pal.keyword

class parser:
	product = Product()
	toks = []
	queryType='FullQuery'
	isAllAttrib=1
	isAllMeta=1
	def getQueryType(self):
		if self.isAllAttrib:
			self.queryType='AttribQuery'
		elif self.isAllMeta:
			self.queryType='MetaQuery'
		return self.queryType
	def ter(self,n,v,ts,te,l):
		self.toks.append((n,v,ts,te,l))	
	def parse(self,s,v='p',gl=globals(),lo=locals()):
		self.toks=[]	
		tokenize(StringIO(s).readline,self.ter)
		g = self.toks
		result = []
		mcc=''
		acc=''
		#print 'locals: ',lo
		#print 'globals: ',gl
		formerToknum=ENDMARKER
		formerTokval=''
		for toknum, tokval, a, b, c in g:
			#print 'toknum=',toknum, 'tokval=',tokval, ' formerTokval=', formerTokval 
			if toknum == NAME:
				if not(pal.keyword.iskeyword(tokval) or gl.has_key(tokval) or lo.has_key(tokval)):
					#print '-- toknum=',toknum, 'tokval=',tokval, 'gl.has_key',gl.has_key(tokval), 'lo.has_key',lo.has_key(tokval)
					if formerTokval!='.':
						if self.product.meta.containsKey(tokval)==0:
							self.isAllAttrib=0
							mcc+=v+'.meta.containsKey(\''+tokval+'\')'+' and '
							tokval= v+'.meta[\''+ tokval+'\'].value'
						else:
							tokval=v+"."+tokval
			
			if tokval=='[':
				if formerToknum != NAME and formerTokval !=']':
					tokval=v+tokval
					self.isAllAttrib=0
					self.isAllMeta=0
			if toknum==STRING:
				if formerTokval==v+'[':
					acc+=v+'.containsKey('+tokval+')'+' and '
			formerTokval=tokval
			formerToknum=toknum
			result.append((toknum, tokval))
		return mcc+acc+untokenize(result)
		
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
	
