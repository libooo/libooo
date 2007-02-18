package herschel.ia.pal.query.parser;

import herschel.ia.jconsole.api.JIDEUtilities;
import herschel.share.interpreter.InterpreterFactory;
import herschel.share.util.Configuration;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Add query "variable" to the simplified query expression, e.g.<br>
 * "instrument=='SPIRE' and ABS(a-b)>5" was convert to "p.instrument=='SPIRE' and ABS(a-b)>5"
 * 
 * <p>The strategy is:
 * <li>parse expression to tokens</li>
 * <li>check token if it is keyword, in globals() or in locals()</li>
 * <li>if not, add "variable" </li>
 * <p> Parser fragment:
 * <pre>
			if toknum == NAME:
					if not(pal.keyword.iskeyword(tokval) or gl.has_key(tokval) or lo.has_key(tokval)):
							tokval=v+'.'+ tokval 
   </pre>
 * <p>
 * 
 * @author libo@bao.ac.cn
 */
public class PALParser {

	private PythonInterpreter _jython;
	private String _expression;
	private String _var;

	private String _queryType;
	
	private boolean _isQuiet;
	/**
	 * get query type, e.g. FullQuery, MetaQuery or AttribQuery
	 * @return FullQuery, MetaQuery or AttribQuery
	 */
	public String getQueryType(){
		return this._queryType;
	}
	
	/**
	 * set query type, e.g. FullQuery, MetaQuery or AttribQuery
	 * @param queryType FullQuery, MetaQuery or AttribQuery
	 */
	public void setQueryType(String queryType){
		this._queryType=queryType;
	}
	
	/**
	 * 
	 * @param var query "variable", e.g. "p"
	 * @param expression simplified query expression, e.g. "instrument=='SPIRE' and ABS(a-b)>5"
	 */
	public PALParser( String var, String expression, boolean isQuiet) {
		
		this._expression=expression;
		this._var=var;
		this._isQuiet = isQuiet;
		
		_jython = JIDEUtilities.getCallingInterpreter();
		if (_jython == null) {
			_jython =InterpreterFactory.getInterpreter();
		}
	}
	
	/**
	 * Return parsed query expression, e.g.<br>
	 * "instrument=='SPIRE' and ABS(a-b)>5" was convert to "p.instrument=='SPIRE' and ABS(a-b)>5"
	 * @return parsed query expression
	 * @throws ParseException
	 */
	public String getParsedQuery() throws ParseException{
		_jython.exec("from herschel.ia.numeric.all import *");
		_jython.exec("from pal.parser import *");
		_jython.set("expression", _expression);
		_jython.set("var", _var);
		_jython.exec("globNameSpace=globals()");
		_jython.exec("localNameSpace=locals()");
		if(_isQuiet){
			_jython.set("isQuiet", 1);
		}else{
			_jython.set("isQuiet", 0);
		}
		_jython.exec("p=parser()");
		_jython.exec("parsedQuery=p.parse(expression,var,globNameSpace,localNameSpace,isQuiet)");
		_jython.exec("queryType=p.getQueryType()");
		PyObject parsedQuery=_jython.get("parsedQuery");
		PyObject queryType=_jython.get("queryType");
		this.setQueryType(queryType.toString());
		return parsedQuery.toString();
	}
	
	
}
