package herschel.ia.pal.rule;

import herschel.ia.dataset.Product;
import herschel.ia.pal.ContextRuleException;
import herschel.ia.pal.MapContext;
import herschel.ia.pal.Context;
import herschel.ia.pal.ListContext;
import herschel.ia.pal.ProductRef;
import herschel.ia.dataset.StringParameter;
import herschel.share.predicate.AnyPredicate;
import herschel.share.predicate.Predicate;
import herschel.share.predicate.AllPredicate;

import java.util.Map.Entry;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test suite for JUnit.
 *
 * @author Jorgo Bakker
 */
public class AllTests extends TestCase {

	private static Logger _LOGGER = Logger.getLogger("Rule Tests");

	public class Foo extends Product {
		private static final long serialVersionUID = 1L;
	}

	public class Bar extends Foo {
		private static final long serialVersionUID = 1L;
	}

	public void testProductSubClass() {
		Product x = new Product();
		Product y = new Foo();
		Product z = new Bar();
		Predicate<ProductRef> predicate = new InstanceOf(Foo.class);

		assertFalse(predicate.evaluate(new ProductRef(x)));
		assertTrue(predicate.evaluate(new ProductRef(y)));
		assertTrue(predicate.evaluate(new ProductRef(z)));
	}

	public void testProductStrictClass() {
		Product x = new Product();
		Product y = new Foo();
		Product z = new Bar();
		Predicate<ProductRef> predicate = new StrictInstanceOf(Foo.class);

		assertFalse(predicate.evaluate(new ProductRef(x)));
		assertTrue(predicate.evaluate(new ProductRef(y)));
		assertFalse(predicate.evaluate(new ProductRef(z)));
	}

	public void testRuleMeta() {
		Product a = new Product();
		ProductRef pRefa = new ProductRef(a);
		Product b = new Product();   	
		ProductRef pRefb = new ProductRef(b);
		Product c = new Product();   	
		ProductRef pRefc = new ProductRef(c);
		// Set up some metadata for products	
		a.getMeta().set("Shutter", new StringParameter("off"));
		b.getMeta().set("Temp", new StringParameter("high"));
		b.getMeta().set("Shutter", new StringParameter("on"));

		// Test with MetaPresent predicate
		MetaPresent metaPresent = new MetaPresent("Temp");
		MetaPresent metaPresent2 = new MetaPresent("Shutter");

		// Check that product has "Temp" meta keyword
		assertTrue(metaPresent.evaluate(pRefb));
		assertFalse(metaPresent.evaluate(pRefa));

		// Check ANDing of rules
		Predicate<ProductRef> andPred = metaPresent.and(metaPresent2);
		// Check if product has both "Temp" and "Shutter" parameters
		assertTrue(andPred.evaluate(pRefb));
		assertFalse(andPred.evaluate(pRefa));

		// Check ORing of predicate rules
		Predicate<ProductRef> orPred = metaPresent.or(metaPresent2);
		// Check if product has either "Temp" or "Shutter" parameters
		assertTrue(orPred.evaluate(pRefb));
		assertTrue(orPred.evaluate(pRefa));
		assertFalse(orPred.evaluate(pRefc));

		// Check MetaEquals predicate
		MetaEquals metaEqPred = new MetaEquals("Shutter", "on");
		assertTrue(metaEqPred.evaluate(pRefb));
		assertFalse(metaEqPred.evaluate(pRefa));

		// Check ANDing between different types of predicate
		Predicate<ProductRef> andPred2 = metaEqPred.and(metaPresent);
		assertTrue(andPred2.evaluate(pRefb));

		// Check ORing between different types of predicate
		Predicate<ProductRef> orPred2 = metaEqPred.or(metaPresent);
		assertTrue(orPred2.evaluate(pRefb));
		assertFalse(orPred2.evaluate(pRefa)); 
		
	}

	public void testRuleType() {
		Product a = new Product("prodA");
		a.setType("PACS");
		ProductRef pRefa = new ProductRef(a);
		Product b = new Product("prodB");  
		b.setType("SPIRE");
		ProductRef pRefb = new ProductRef(b);
		Product c = new Product("prodC");  
		c.setType("PACS");
		ProductRef pRefc = new ProductRef(c);
		
		TypeExistsRule rule = new TypeExistsRule();
		MyMap testMap = new MyMap(rule);
		rule.setContext(testMap);
	   	
		try {
			testMap.getRefs().put("A", pRefa);
		}
		catch (ContextRuleException cre) {
			fail("ContextRuleException thrown unexpectedly");
		}

		try {
			testMap.getRefs().put("B", pRefb);
		}
		catch (ContextRuleException cre1) {
			fail("ContextRuleException thrown unexpectedly");
		}
		try {
			testMap.getRefs().put("C", pRefc);
		}
		catch (ContextRuleException cre2) {
			return;
		}
		fail("ContextRuleException not thrown as expected");
	}
	
	
	public void testRuleTypeList() {
		Product a = new Product();
		ProductRef pRefa = new ProductRef(a);
		Product b = new Product();   	
		ProductRef pRefb = new ProductRef(b);
		Product c = new Product();   	
		ProductRef pRefc = new ProductRef(c);
		// Set up some metadata for products	
		a.getMeta().set("Shutter", new StringParameter("off"));
		b.getMeta().set("Temp", new StringParameter("high"));
		b.getMeta().set("Shutter", new StringParameter("on"));
		
		MetaPresent metaPresent = new MetaPresent("Temp");
		MyList ml = new MyList(metaPresent);
		// This should pass adding rule test - no exception thrown
		try {
			ml.getRefs().add(0, pRefb);
		}
		catch (ContextRuleException cre) {
			fail("ContextRuleException thrown unexpectedly");
		}
		
//		 This should fail adding rule test - exception thrown
		try {
			ml.getRefs().set(0, pRefa);
		}
		catch (ContextRuleException cre2) {
			_LOGGER.info("Exception is "+cre2.getMessage());
			return;
		}
		fail("ContextRuleException not thrown as expected");
		
	}
	
	
	public class MyMap extends MapContext {
		private static final long serialVersionUID = 1L;
		private Predicate<Entry<String, ProductRef>> _rule;

		public MyMap(Predicate<Entry<String, ProductRef>> addingRule) {
			_rule = addingRule;
		}

		public Predicate<Entry<String, ProductRef>> getAddingRule() {
			return _rule;
		}

	}

	public class MyList extends ListContext {
		private static final long serialVersionUID = 1L;
		private Predicate<ProductRef> _rule;

		public MyList(Predicate<ProductRef> addingRule) {
			_rule = addingRule;
		}

		public Predicate<ProductRef> getAddingRule() {
			return _rule;
		}

	}
	
	public void testAlias() {
		Predicate<Entry<String, ProductRef>> rule = new AnyPredicate<Entry<String, ProductRef>>() {
			{
				rules.add(new Alias("x", Foo.class, true));
				rules.add(new Alias("y", Foo.class)); // allows sub-classes
				rules.add(new Alias("z", Product.class)); // allows sub-classes
			}
		};
		MapContext context = new MyMap(rule);

		ProductRef x = new ProductRef(new Foo());
		ProductRef y = new ProductRef(new Bar());
		ProductRef z = new ProductRef(new Product());

		assertTrue(add(context, "x", x));
		assertFalse(add(context, "x", y)); // ERROR, value(y) is not allowed
		assertFalse(add(context, "x", z)); // ERROR, value(y) is not allowed
		assertTrue(add(context, "y", x));
		assertTrue(add(context, "y", y));
		assertFalse(add(context, "y", z)); // ERROR, value(y) is not allowed
		assertTrue(add(context, "z", x));
		assertTrue(add(context, "z", y));
		assertTrue(add(context, "z", z)); // ERROR, value(y) is not allowed
	}

	private boolean add(MapContext context, String key, ProductRef ref) {
		try {
			context.getRefs().put(key, ref);
			return true;
		} catch (ContextRuleException e) {
			return false;
		}
	}

	/**
	 * Test suite holding all tests for this sub-system.
	 */
	public static Test suite() {
		return new TestSuite(AllTests.class);
	}

	/**
	 * Allows you to run all tests as an application.
	 */
	public static void main(String[] arguments) {
		TestRunner.run(suite());
	}

}
