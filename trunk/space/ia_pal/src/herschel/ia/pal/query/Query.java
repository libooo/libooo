package herschel.ia.pal.query;

import herschel.ia.dataset.Product;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.query.parser.PALParser;
import herschel.ia.pal.query.parser.ParseException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * The main interface for PAL query. It intends to "allow simple, non-cluttered
 * syntax to be executed". Proper query types, e.g. FullQuery, MetaQuery or
 * AttribQuery will be determinded by the properties used in query in the future.
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
	
	
	public String getQueryType(){
		return this._queryType;
	}
	
	public void setQueryType(String queryType){
		this._queryType=queryType;
	}

	private void parse(){
		PALParser p = new PALParser(_variable, _expression);
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
		parse();
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
		parse();
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
		parse();
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
	 * @see herschel.ia.pal.query.StorageQuery#accept(herschel.ia.pal.query.StorageQueryVisitor)
	 */
	public void accept(StorageQueryVisitor visitor) throws IOException,
			GeneralSecurityException {
		if(this.getQueryType().equalsIgnoreCase("AttribQuery")){
			AttribQuery query;
			query = new AttribQuery(_product, _variable, _where);
			visitor.visit(query);
		}
		else if(this.getQueryType().equalsIgnoreCase("MetaQuery")){
			MetaQuery query;
			query = new MetaQuery(_product, _variable, _where);
			visitor.visit(query);
		}
		else{
			FullQuery query;
			query = new FullQuery(_product, _variable, _where);
			visitor.visit(query);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see herschel.ia.pal.query.StorageQuery#getType()
	 */
	public Class getType() {
		return _product;
	}

	/*
	 * (non-Javadoc)
	 * @see herschel.ia.pal.query.StorageQuery#getVariable()
	 */
	public String getVariable() {
		return _variable;
	}

	/*
	 * (non-Javadoc)
	 * @see herschel.ia.pal.query.StorageQuery#getWhere()
	 */
	public String getWhere() {
		return _where;
	}
	
	public void setWhere(String where){
		this._where=where;
	}
}
