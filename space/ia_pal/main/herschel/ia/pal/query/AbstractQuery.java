/* $Id: AbstractQuery.java,v 1.3 2007/11/07 15:14:50 sguest Exp $
 * Copyright (c) 2007 ESA, NAOC, STFC
 */
package herschel.ia.pal.query;

import herschel.ia.dataset.Product;

abstract class AbstractQuery implements StorageQuery {

	private Class<? extends Product> _product;
	private String _where;
	private String _variable;
	private boolean _retrieveAllVersions;

	public AbstractQuery(Class<? extends Product> product, String variable,
                             String where) {
		_product = product;
		_variable = variable;
		_where = where;
		_retrieveAllVersions = false;
	}

	public AbstractQuery(Class<? extends Product> product, String variable,
                             String where, boolean retrieveAllVersions) {
		_product = product;
		_variable = variable;
		_where = where;
		_retrieveAllVersions = retrieveAllVersions;
	}

	/**
	 * Re-implemented for internal reasons.
	 */
	public int hashCode() {
		int id = 17; // arbitrary start value
		if (_product != null)
			id = 37 * id + _product.hashCode();
		if (_variable != null)
			id = 37 * id + _variable.hashCode();
		if (_where != null)
			id = 37 * id + _where.hashCode();
		return id;
	}

	/**
	 * Re-implemented for internal reasons.
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o.getClass() == getClass()))
			return false;

		AbstractQuery rhs = (AbstractQuery) o;
		if (_product == null) {
			if (rhs._product != null)
				return false;
		} else {
			if (_product != rhs._product)
				return false;
		}

		if (_variable == null) {
			if (rhs._variable != null)
				return false;
		} else {
			if (!_variable.equals(rhs._variable))
				return false;
		}

		if (_where == null) {
			if (rhs._where != null)
				return false;
		} else {
			if (!_where.equals(rhs._where))
				return false;
		}
		return true;
	}

	public String getWhere() {
		return _where;
	}

	public String getVariable() {
		return _variable;
	}

	public Class<? extends Product> getType() {
		return _product;
	}

	public boolean retrieveAllVersions() {
		return _retrieveAllVersions;
	}

	public String toString() {
		return getClass().getName() + "(" + getType().getName() + ",\""
				+ getVariable() + "\",\"" + getWhere() + "\")";
	}

}
