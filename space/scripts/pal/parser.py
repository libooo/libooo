from pal.tokenize import *
from pal.StringIO import *
import pal.keyword

class parser:
	toks = []
	def ter(self,n,v,ts,te,l):
		self.toks.append((n,v,ts,te,l))	
	def parse(self,s,v='p',gl=globals(),lo=locals()):
		self.toks=[]	
		tokenize(StringIO(s).readline,self.ter)
		g = self.toks
		result = []
		#print 'locals: ',lo
		#print 'globals: ',gl
		for toknum, tokval, a, b, c in g:
			#print 'toknum=',toknum, 'tokval=',tokval
			if toknum == NAME:
				if not(pal.keyword.iskeyword(tokval) or gl.has_key(tokval) or lo.has_key(tokval)):
					#print '-- toknum=',toknum, 'tokval=',tokval, 'gl.has_key',gl.has_key(tokval), 'lo.has_key',lo.has_key(tokval)
					tokval=v+'.'+ tokval
			result.append((toknum, tokval))
		#print untokenize(result)
		return untokenize(result)
		
if __name__ == '__main__':
	p=parser()
	s = 'creator=="scott" and ABS(createDate-endDate)<10'
	print p.parse(s)	
	s = "type=='AbcProduct' and creator=='Scott' and ABS(creationDate-endDate)>0"
	print p.parse(s, 'x')

	
