package herschel.ia.pal.query.parser;

import herschel.ia.jconsole.api.JIDEUtilities;
import herschel.share.interpreter.InterpreterFactory;

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

	/**
	 * 
	 * @param var query "variable", e.g. "p"
	 * @param expression simplified query expression, e.g. "instrument=='SPIRE' and ABS(a-b)>5"
	 */
	public PALParser( String var, String expression) {
		
		this._expression=expression;
		this._var=var;
		
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
		_jython.set("e", _expression);
		_jython.set("v", _var);
		_jython.exec("g=globals()");
		PyObject g=_jython.get("g");
		_jython.exec("l=locals()");
		PyObject l=_jython.get("l");
		_jython.set("gl", g);
		_jython.set("lo", l);
		_jython.exec("r=parser().parse(e,v,gl,lo)");
		PyObject pe=_jython.get("r");
		return pe.toString();
	}
	
	
}
