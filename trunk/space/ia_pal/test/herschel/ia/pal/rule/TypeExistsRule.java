package herschel.ia.pal.rule;

import herschel.ia.pal.ProductRef;
import herschel.ia.dataset.Product;
import herschel.ia.pal.MapContext;
import herschel.share.predicate.AbstractPredicate;

import java.util.Map.Entry;
import java.util.Set;
import java.util.Iterator;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class TypeExistsRule extends AbstractPredicate<Entry<String,ProductRef>> {
	protected MapContext context;


	public TypeExistsRule() {
		super();
		// TODO Auto-generated constructor stub
	}


	public void setContext(MapContext context) {
		this.context = context;
	}


	public boolean evaluate(Entry<String,ProductRef> entry) {
		// test if context already includes a product type
		try {
		String addType = entry.getValue().getProduct().getType();
		Iterator it = context.getRefs().keySet().iterator();
		// boolean typeExists = false;
			while (it.hasNext()) {
				String ty = context.getProduct((String)it.next()).getType();
				// If ANY of the products is of this type, it should return false.
				if (ty.equals(addType)) {
					return false;
				}
			}
			return true;
		}
		catch (IOException ioe) {
		}
		catch (GeneralSecurityException gse) {
		}

		return true;
	}

}

