package herschel.ia.pal.query;

import junit.framework.Test;
import junit.framework.TestSuite;
import herschel.ia.pal.versioning.MetaDataTest;
import herschel.share.util.ConfiguredTestCase;

public class QueryTest extends ConfiguredTestCase {

	public QueryTest(String name) {
		super(name);
	}
	
	public void testOverrideAssignmentOperator1(){
		Query q = new Query("instrument='spire' and creator=='foo'");
		System.out.println(q.getWhere());
		assertTrue(q.getWhere().equalsIgnoreCase("(p.instrument =='spire'and p.creator =='foo')"));	
	} 
	
	public void testOverrideAssignmentOperator2(){
		Query q = new Query("width>=1");
		System.out.println(q.getWhere());
		assertTrue(q.getWhere().equalsIgnoreCase("p.meta.containsKey('width') and (p.meta['width'].value >=1 )"));	
	} 
	
	public void testOverrideAssignmentOperator3(){
		Query q = new Query("width>=1 and instrument!='spire'");
		System.out.println(q.getWhere());
		assertTrue(q.getWhere().equalsIgnoreCase("p.meta.containsKey('width') and (p.meta['width'].value >=1 and p.instrument !='spire')"));	
	} 
	
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(QueryTest.class);
        return suite;
    }
}
