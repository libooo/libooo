/* $Id: PALParser.java,v 1.6 2007/12/21 08:47:30 bli Exp $
 * Copyright (c) 2006 NAOC
 */
package herschel.ia.pal.query.parser;

import herschel.share.interpreter.InterpreterFactory;
import herschel.share.interpreter.InterpreterUtil;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Parser to parse the simplified query expression to the original pal query
 * expression. e.g.<br>
 * "instrument=='foo' and width>=200 " was convert to
 * "p.meta.containsKey('width') and (instrument =='foo'and p.meta['width'].value
 * >=200)" by default.
 * 
 * <br>
 * User could specify whether non-existent meta data or attributes appearing in
 * the query should be quietly ignored by configuring hcss.ia.pal.query.isQuiet
 * in herschel\ia\pal\defns\pal.xml. The default value is true. If the
 * hcss.ia.pal.query.isQuiet is set to false the query will be converted to
 * simplly "instrument ='foo'and p.meta['width'].value >=200"
 * 
 * <br>
 * Also the parser could give out the proper query type to run the query by
 * scanning the properties appeared in the query.
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
	 * 
	 * @return FullQuery, MetaQuery or AttribQuery
	 */
	public String getQueryType() {
		return this._queryType;
	}

	/**
	 * set query type, e.g. FullQuery, MetaQuery or AttribQuery
	 * 
	 * @param queryType
	 *            FullQuery, MetaQuery or AttribQuery
	 */
	public void setQueryType(String queryType) {
		this._queryType = queryType;
	}

	/**
	 * 
	 * @param var
	 *            query "variable", e.g. "p"
	 * @param expression
	 *            simplified query expression, e.g. "instrument=='SPIRE' and
	 *            ABS(a-b)>5"
	 */
	public PALParser(String var, String expression, boolean isQuiet) {

		this._expression = expression;
		this._var = var;
		this._isQuiet = isQuiet;

		_jython = InterpreterUtil.getCallingInterpreter();
		if (_jython == null) {
			_jython = InterpreterFactory.getInterpreter();
		}
	}

	/**
	 * Return parsed query expression, e.g.<br>
	 * "instrument=='SPIRE' and ABS(a-b)>5" was convert to
	 * "p.instrument=='SPIRE' and ABS(a-b)>5"
	 * 
	 * @return parsed query expression
	 * @throws ParseException
	 */
	public String getParsedQuery() throws ParseException {
		_jython.exec("from herschel.ia.numeric.all import *");
		_jython
				.exec("from herschel.ia.pal.query.parser.jython.parser import *");
		_jython.set("__pal_query_expression__", _expression);
		_jython.set("__pal_query_var__", _var);
		_jython.exec("__pal_query_glob_namespace__=globals()");
		_jython.exec("__pal_query_local_namespace__=locals()");
		if (_isQuiet) {
			_jython.set("__pal_query_isQuiet__", 1);
		} else {
			_jython.set("__pal_query_isQuiet__", 0);
		}
		_jython.exec("__pal_query_palParser__=parser()");
		_jython
				.exec("__pal_query_parsedQuery__=__pal_query_palParser__.parse(__pal_query_expression__,__pal_query_var__,__pal_query_glob_namespace__,__pal_query_local_namespace__,__pal_query_isQuiet__)");
		_jython
				.exec("__pal_query_queryType__=__pal_query_palParser__.getQueryType()");
		PyObject parsedQuery = _jython.get("__pal_query_parsedQuery__");
		PyObject queryType = _jython.get("__pal_query_queryType__");
		this.setQueryType(queryType.toString());
		return parsedQuery.toString();
	}

}
