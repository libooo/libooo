/* $Id: Query.java,v 1.3 2007/03/01 13:45:39 bli Exp $
 * Copyright (c) 2006 NAOC
 */
package herschel.ia.pal.query;

import herschel.ia.dataset.Product;
import herschel.ia.pal.Context;
import herschel.ia.pal.JythonInterpretor;
import herschel.ia.pal.ListContext;
import herschel.ia.pal.MapContext;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.query.parser.PALParser;
import herschel.ia.pal.query.parser.ParseException;
import herschel.share.util.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The main interface for PAL query. It intends to "allow simple, non-cluttered
 * syntax to be executed". Proper query types, e.g. FullQuery, MetaQuery or
 * AttribQuery will be determinded by the properties used in query.
 * 
 * @jexample Example of an query: q=Query("type=='AbcProduct' and
 *           creator=='Scott'")
 * 
 * q=Query("foo.Product","type=='AbcProduct' and creator=='Scott'")
 * 
 * 
 * @author libo@bao.ac.cn
 * 
 */
public class Query implements StorageQuery {

	private static final long serialVersionUID = 1L;

	private Class _product;

	private String _where;

	private String _variable;

	private String _expression;

	private String _queryType;

	private boolean isQuiet = Configuration
			.getBoolean("hcss.ia.pal.query.isQuiet");

	private JythonInterpretor _jython = JythonInterpretor.getInstance();

	/**
	 * get query type, e.g. FullQuery, MetaQuery or AttribQuery, by scanning 
	 * the properties appeared in the query.
	 * @return FullQuery, MetaQuery or AttribQuery
	 */
	public String getQueryType() {
		return this._queryType;
	}

	/**
	 * set query type, e.g. FullQuery, MetaQuery or AttribQuery
	 * @param queryType FullQuery, MetaQuery or AttribQuery
	 */
	public void setQueryType(String queryType) {
		this._queryType = queryType;
	}

	private void parse() {
		PALParser p = new PALParser(_variable, _expression, isQuiet());
		try {
			setWhere(p.getParsedQuery());
			setQueryType(p.getQueryType());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Query all products with the given expression. This automatically assigns
	 * a query "variable" and selects the correct Attrib, Meta, or Full query.
	 */
	public Query(String expression) {
		_product = Product.class;
		_variable = "p";
		_expression = expression;

	}

	/**
	 * Query products of the given class (or subclasses) with the given
	 * expression. This automatically assigns a query "variable" and selects the
	 * correct Attrib, Meta, or Full query.
	 */
	public Query(Class c, String expression) {
		_product = c;
		_variable = "c";
		_expression = expression;

	}

	/**
	 * Query products of the given class (or subclasses) with the given
	 * expression and query variable. This automatically selects the correct
	 * Attrib, Meta, or Full query.
	 */
	public Query(Class c, String variable, String expression) {
		_product = c;
		_variable = variable;
		_expression = expression;

	}

	/**
	 * Search the given storage and match the expression. This is included only
	 * for completeness, duplicates ProductStorage method.
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public Set<ProductRef> match(ProductStorage storage) throws IOException,
			GeneralSecurityException {
		return storage.select(this);
	}

	/**
	 * Refine a previous storage query. This is included only for completeness,
	 * duplicates ProductStorage method.
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public Set<ProductRef> match(ProductStorage storage,
			Set<ProductRef> previous) throws IOException,
			GeneralSecurityException {
		return storage.select(this, previous);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.query.StorageQuery#accept(herschel.ia.pal.query.StorageQueryVisitor)
	 */
	public void accept(StorageQueryVisitor visitor) throws IOException,
			GeneralSecurityException {
		if (this.getQueryType().equalsIgnoreCase("AttribQuery")) {
			AttribQuery query;
			query = new AttribQuery(_product, _variable, _where);
			visitor.visit(query);
		} else if (this.getQueryType().equalsIgnoreCase("MetaQuery")) {
			MetaQuery query;
			query = new MetaQuery(_product, _variable, _where);
			visitor.visit(query);
		} else {
			FullQuery query;
			query = new FullQuery(_product, _variable, _where);
			visitor.visit(query);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.query.StorageQuery#getType()
	 */
	public Class getType() {
		return _product;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.query.StorageQuery#getVariable()
	 */
	public String getVariable() {
		return _variable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.query.StorageQuery#getWhere()
	 */
	public String getWhere() {
		parse();
		return _where;
	}

	/**
	 * set the query clause
	 * @param where the query clause
	 */
	public void setWhere(String where) {
		this._where = where;
	}

	/**
	 * 
	 * @return true if non-existent meta data or attributes appearing the query
	 *         will be quietly ignored
	 */
	public boolean isQuiet() {
		return isQuiet;
	}

	/**
	 * Controls whether non-existent meta data or attributes appearing the query
	 * will be quietly ignored.
	 * 
	 * @param isNonExistFieldsIgnored
	 */
	public void setQuiet(boolean isQuiet) {
		this.isQuiet = isQuiet;
	}

	/**
	 * Match the products where the expression evaluates to true.
	 */
	public Set<ProductRef> match(Iterator<ProductRef> iter) throws IOException,
			GeneralSecurityException {
		Set<ProductRef> result = new HashSet<ProductRef>();
		while (iter.hasNext()) {
			ProductRef o = iter.next();

			if (_jython.matches(o.getProduct(), this)) {
				result.add(o);
			}

		}

		return result;
	}

	/**
	 * Match the products where the expression evaluates to true.
	 */
	public Set<ProductRef> match(Iterable<ProductRef> i) throws IOException,
			GeneralSecurityException {
		return match(i.iterator());
	}

	/**
	 * Recursively search a context and match the expression ie subcontexts are
	 * also searched. This could also be done by Context implementing Iterable.
	 */
	public Set<ProductRef> match(Context context) throws IOException,
			GeneralSecurityException {
		Set<ProductRef> result = new HashSet<ProductRef>();
		Set<ProductRef> refs = context.getAllRefs();
		for(ProductRef ref:refs){
			Product p = ref.getProduct();
			if(p instanceof Context){
				result.addAll(match((Context)p));
			}
			else{
				if (_jython.matches(p, this)) {
					result.add(ref);
				}
			}
		}	
		return result;
	}
}
