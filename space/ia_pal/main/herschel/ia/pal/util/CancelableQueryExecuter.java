package herschel.ia.pal.util;

import herschel.ia.pal.ProductRef;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.query.Query;
import herschel.ia.pal.query.StorageQuery;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 
 * An util to allow users to execute a query and cancel it if needed.
 *  
 * Code example:
 * <pre>
   		Future future= CancelableQueryExecuter.execute(storage, new Query("1=1"));
		// to get results
		Set results = (Set)future.get();
		// to cancel it
		future.cancel(true);
 * </pre>
 * @author libo@bao.ac.cn
 * @see java.util.concurrent.Future
 */
public class CancelableQueryExecuter {
	/**
	 * Return a Future of thread that runs the query 
	 * 
	 * @param query
	 * @return a Future of select thread
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static Future<Set<ProductRef>> execute(ProductStorage store, StorageQuery query) throws IOException,
			GeneralSecurityException {
		final ProductStorage _store = store;
		final StorageQuery _query = query;
		Callable<Set<ProductRef>> runQuery = new Callable<Set<ProductRef>>(){
			public Set<ProductRef> call() throws Exception {
				return _store.select(_query);
			}
		};
		
		ExecutorService exec = Executors.newSingleThreadExecutor();
		return exec.submit(runQuery);		
	}
}
