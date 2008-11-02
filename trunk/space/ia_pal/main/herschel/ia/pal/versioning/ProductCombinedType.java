package herschel.ia.pal.versioning;

import java.util.logging.Logger;

/**
 * A class to define a Product type for the purposes of versioning.
 * It contains attributes corresponding to the class of the product,
 * and the type taken from the 'type' attribute of the product.
 * 
 * @deprecated Not needed any more after the refactoring of versioning.
 *
 */
public class ProductCombinedType {
	
	@SuppressWarnings("unused")
	private static Logger _LOGGER = Logger.getLogger(ProductCombinedType.class
			.getName());
	
	private static final String _DELIMITER = ",";

	private Class _productClass;

	private String _productType;

	public ProductCombinedType(Class productClass, String productType) {
		_productClass = productClass;
		_productType = productType;
	}

	public Class getProductClass() {
		return _productClass;
	}

	public String getProductType() {
		return _productType;
	}

	public String getStringRepresentation() {
		return _productClass.getName() + _DELIMITER + _productType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		String productClassStr = _productClass.getName();
		result = PRIME * result + ((productClassStr == null) ? 0 : productClassStr.hashCode());
		result = PRIME * result + ((_productType == null) ? 0 : _productType.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ProductCombinedType other = (ProductCombinedType) obj;
		if (_productClass == null) {
			if (other._productClass != null)
				return false;
		} else if (!_productClass.equals(other._productClass))
			return false;
		if (_productType == null) {
			if (other._productType != null)
				return false;
		} else if (!_productType.equals(other._productType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getStringRepresentation();
	}
}
