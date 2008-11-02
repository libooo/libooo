/**
 * 
 */
package herschel.ia.pal.performance;

import herschel.ia.dataset.Product;
import herschel.ia.pal.AbstractPalTestCase;
import herschel.ia.pal.PerformancePalTestGroup;
import herschel.ia.pal.ProductPool;
import herschel.ia.pal.ProductRef;
import herschel.ia.pal.ProductStorage;
import herschel.ia.pal.TestProductGenerator;
import herschel.ia.pal.performance.ResultSet.Configuration;
import herschel.ia.pal.query.AttribQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import junit.framework.TestResult;

/**
 * @author pbalm
 *
 */
public abstract class AbstractBenchmark extends PerformancePalTestGroup {

    /**
     * Benchmark configuration
     */

    //protected String[] benchmarks = {"Save", "Load"};

    // 1, 5 or 10 GB with a few different product sizes
    // -- optimized for available disk space
    //protected double[] size = {1, 5, 10};
    //protected double[] nFiles = {2, 3, 4};

    // 2 GB pool size with wide range of # of products
    //protected double[] size = {2, 10};
    //protected double[] nFiles = {2,3,4,5}; // in powers of 10: 10^2, 10^3, ...

    // Configured from the configuration file passed as an argument to the main method.
    protected Double[] size; // in MB
    protected Double[] nProducts; // in powers of 10: 10^2, 10^3, ...
    protected String[] benchmarks; // = {"AttribQuery", "FullQuery", "MetaQuery", "MetaQueryWhen", "MetaQueryMany","Save", "Load"};

    protected int repeat = 1; // for statistics

    /**
     * End -- Benchmark configuration
     */

    private static Logger log = Logger.getLogger("AbstractBenchmark");

    protected ResultSet resultSet = new ResultSet(getClass().getName());
    protected int startPoolIndex = 0;

    protected boolean doPrepare = false;
    protected boolean doRun = false;

    protected List<String> poolNames;
    
    protected Map<Configuration, ProductStorage> poolMap = null;

    static {
	MAX_POOLS = 100;
    }
    
    protected AbstractBenchmark(String name, AbstractPalTestCase testcase, String[] args) {
	super(name, testcase);
	

	if(args.length!=1) {
	    printUsageAndExit();
	}

	File configFile = new File(args[0]);

	if(!configFile.exists() || !configFile.canRead()) {
	    System.out.println("ERROR: Run parameters file: "+configFile+"\ndoes not exist or cannot be read.");
	    System.exit(1);
	}

	try {
	    BufferedReader configReader = new BufferedReader( new FileReader(configFile));
	    String line = null;
	    do {
		line = configReader.readLine(); 

		if(line!=null) {
		    line = line.trim(); // remove leading and trailing whitespace
		}

		if(line!=null && line.length()!=0 && line.charAt(0)!='#') {
		    StringTokenizer tokenizer = new StringTokenizer(line.trim(), "=");

		    String key = null;
		    String value = null;
		    if(tokenizer.hasMoreTokens()) {
			key = tokenizer.nextToken().trim();
		    }
		    if(tokenizer.hasMoreTokens()) {
			value = tokenizer.nextToken().trim();
		    }

		    if(key==null || value==null) {
			System.out.println("ERROR: Parse error when reading config file on line: "+line);
			System.exit(1);
		    }
		    else {
			//System.out.println("Setting property "+key+" to "+value);
		    }
		    
		    
		    if(key.equals("task")) {
			setTask(value);
		    }
		    else if(key.equals("prodSizes")) {
			setProductSizes(value);
		    }
		    else if(key.equals("nProducts")) {
			setNProducts(value);
		    }
		    else if(key.equals("benchmarks")) {
			setBenchmarks(value);
		    }
		    else if(key.equals("repeat")) {
			repeat = Integer.parseInt(value);
		    }
		    else if(key.equals("startPoolIndex")) {
			startPoolIndex = Integer.parseInt(value);
			//log.info("Start pool index in c'tor while parsing args: "+startPoolIndex+" (from string: "+value+")");
		    }
		    else if(key.equals("poolNames")) {
			setPoolNames(value);
		    }
		    else {
			
			// Try to find a method with introspection:
			try {
			    Method method = this.getClass().getMethod("setProperty_"+key, new Class<?>[] {String.class} );
				method.invoke(this, value);
			}
			catch(NoSuchMethodException e) {
			    System.out.println("ERROR: Unknown keyword "+key+" when reading config file on line: "+line);
			    System.exit(1);
			} catch (IllegalAccessException e) {
			    System.out.println("ERROR: Error setting property "+key+" when reading config file on line: "+line);
			    System.exit(1);
			} catch (InvocationTargetException e) {
			    System.out.println("ERROR: Error setting property "+key+" when reading config file on line: "+line);
			    System.exit(1);
			}
			
		    }

		}
	    }
	    while(line != null);
	}
	catch(FileNotFoundException e) {
	    System.out.println("ERROR: Configuration file disappeared while reading it?!");
	    System.exit(1);
	}
	catch(IOException e) {
	    System.out.println("ERROR: I/O exception while reading configuration file.");
	    System.exit(1);
	}

	if(size==null || size.length==0) {
	    System.out.println("ERROR: After reading config file, pool sizes have not been configured.");
	    System.exit(1);
	}
	if(nProducts==null || nProducts.length==0) {
	    System.out.println("ERROR: After reading config file, number of products have not been configured.");
	    System.exit(1);
	}
	if(benchmarks==null || benchmarks.length==0) {
	    System.out.println("ERROR: After reading config file, no benchmarks have been configured.");
	    System.exit(1);
	}

    }

    private void setTask(String task) {
	if(!task.equals("prepare") &&
		!task.equals("run") &&
		!task.equals("prepareAndRun") ) {
	    System.out.print("ERROR: Reading config: invalid task: "+task+"\nTask should be one of: prepare, run, prepareAndRun");
	    System.exit(1);
	}

	if(task.equals("prepareAndRun")) {
	    doPrepare = true;
	    doRun = true;
	}
	else if(task.equals("prepare")) {
	    doPrepare = true;
	}
	else if(task.equals("run")) {
	    doRun = true;
	}
    }

    private void setNProducts(String nProds) {
	StringTokenizer tokenizer = new StringTokenizer(nProds, ",");

	List<Double> nProdList = new ArrayList<Double>();

	try {
	    while(tokenizer.hasMoreTokens()) {
		nProdList.add(Double.parseDouble(tokenizer.nextToken()));
	    }

	    nProducts = new Double[nProdList.size()];
	    nProdList.toArray(nProducts);
	}
	catch(NumberFormatException e) {
	    System.out.print("ERROR: Number format exception reading numbers of products.");
	    System.exit(1);
	}
    }

    private void setProductSizes(String sizeStr) {
	StringTokenizer tokenizer = new StringTokenizer(sizeStr, ",");

	List<Double> nSizeList = new ArrayList<Double>();

	try {
	    while(tokenizer.hasMoreTokens()) {
		nSizeList.add(Double.parseDouble(tokenizer.nextToken()));
	    }

	    size = new Double[nSizeList.size()];
	    nSizeList.toArray(size);
	}
	catch(NumberFormatException e) {
	    System.out.print("ERROR: Number format exception reading sizes of products.");
	    System.exit(1);
	}
    }
    
    private void setBenchmarks(String config) {
	StringTokenizer tokenizer = new StringTokenizer(config, ",");

	List<String> benchmarksList = new ArrayList<String>();

	String test = null;
	try {
	    while(tokenizer.hasMoreTokens()) {

		test = tokenizer.nextToken().trim();
		getTestMethod(test); // will throw NoSuchMethod if it doesn't exist.

		benchmarksList.add(test);
	    }

	    benchmarks = new String[benchmarksList.size()];
	    benchmarksList.toArray(benchmarks);
	}
	catch(NoSuchMethodException e) {
	    System.out.println("ERROR: No such benchmark: "+test);
	    System.exit(1);
	}
    }
    
    private void setPoolNames(String names) {
	StringTokenizer tokenizer = new StringTokenizer(names, ",");

	poolNames = new ArrayList<String>();

	String poolName = null;
	while(tokenizer.hasMoreTokens()) {
	    poolName = tokenizer.nextToken().trim();
	    poolNames.add(poolName);
	}

    }

    private void printUsageAndExit() {
	System.out.println("Usage: java "+getClass().getName()+" optionFile\nWhere optionFile is a text file containing the options.");
	System.exit(0);
    }
    
    protected abstract ProductPool getPool(String poolName);

    private void run(Configuration config, ProductStorage pool) throws IOException, GeneralSecurityException, InvocationTargetException {
	// Run test
	log.info("Running benchmark: "+config);

	Exception exception = null;

	try {
	    long time = benchmark(pool, config.benchmark);
	    // Store result
	    resultSet.addTime(config, time);

	    // Basically ignore any exceptions.
	    // The arguments are hardcoded, and there is no security manager.
	    // This cannot happen (famous last words).
	} catch (NoSuchMethodException e) {
	    log.warning("Invalid benchmark name. No such method: test"+config.benchmark);
	} catch (IllegalArgumentException e) {
	    exception = e;
	} catch (IllegalAccessException e) {
	    exception = e;
	}
	finally {
	    if(exception!=null) {
		log.severe("Exception thrown: "+exception);
		exception.printStackTrace();
	    }
	}

    }

    /**
     * Do the processing for this benchmark and return elapsed time.
     * 
     * @param pool: ProductPool to run test on.
     * @param test: Name of benchmark. If e.g. "Load" assumes existence of method testLoad.
     * @return elapsed time
     * @throws NoSuchMethodException: If no such benchmark defined: A method called 
     * \"test\"+test from the argument.
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    private long benchmark(ProductStorage pool, String test) throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IllegalAccessException {

	if(getStorage()==null || !getStorage().equals(pool)) {
	    // This is a different storage than what we set before, change it.
	    setStorage(pool);
	}

	// Locate the test method to call
	Method testMethod = getTestMethod(test);

	// Prepare for test
	Object[] testArguments = null;

	// Run test
	// Note that the test is responsible for starting/stopping the timer!
	testMethod.invoke(this, testArguments);

	long time = timer.read();

	log.info("Finished benchmark "+test+" -- elapsed time: "+ResultSet.timeFormat.format(time)+" ms.");

	// Calculate time spent
	return time;
    }
    
    private Method getTestMethod(String benchmark) throws NoSuchMethodException {
	String testMethodName = "test"+benchmark;
	Class<?>[] testParameters = null; // corresponds to void, method takes no arguments
	return getClass().getMethod(testMethodName, testParameters);
    }

    public void initializePools() {

	Exception exception = null;
	try {
	    if(doPrepare) {
		poolMap = preparePools(size, nProducts);
	    }
	    else {
		poolMap = locatePools();
	    }

	    if(!doRun) {
		log.info("Pools are ready for use. Now run benchmark with 'run' argument.");
		System.exit(0);
	    }
	} catch (IOException e) {
	    exception = e;
	    e.printStackTrace();
	} catch (GeneralSecurityException e) {
	    exception = e;
	    e.printStackTrace();
	}

	if(exception!=null) {
	    log.severe("Error occurred preparing pools: "+exception.getMessage());
	}
    }

    public TestResult run() {
	Set<Configuration> poolKeys = poolMap.keySet();

	log.info("Running benchmarks -- "+poolKeys.size()+" pools prepared.");

	for(Configuration poolKey  : poolKeys) {

	    for(String benchmarkName : benchmarks) {
		Exception exception = null;

		try {
		    Configuration runConfiguration = new Configuration();
		    runConfiguration.benchmark = benchmarkName;
		    runConfiguration.size = poolKey.size;
		    runConfiguration.nProducts = poolKey.nProducts;
		    runConfiguration.poolIndex =  poolKey.poolIndex;

		    for(int cycle = 0; cycle<repeat; cycle++) {
			run(runConfiguration, poolMap.get(poolKey));
		    }
		} catch (InvocationTargetException e) {
		    exception = e;
		} catch (IOException e) {
		    exception = e;
		} catch (GeneralSecurityException e) {
		    exception = e;
		}
		finally {
		    if(exception!=null) {
			log.severe("Error running benchmark "+benchmarkName+": "+exception.getMessage());
			exception.printStackTrace();
		    }
		}

	    }
	}

	return new TestResult();
    }

    public void output() {
	resultSet.print();
    }

    protected Map<Configuration, ProductStorage> preparePools(Double[] sizes, Double[] nFiles) throws IOException, GeneralSecurityException {

	Map<Configuration, ProductStorage> poolMap = new HashMap<Configuration, ProductStorage>();

	int poolIndex = startPoolIndex;
	log.info("Starting with index "+poolIndex);
	
	for(double size : sizes) {
	    for(double n : nFiles) {
		int numberOfFiles = (int) Math.pow(10, n);

		Configuration config = new Configuration();
		config.size = size;
		config.nProducts = numberOfFiles;
		config.poolIndex = poolIndex;

		ProductPool pool = preparePool(poolIndex, size, numberOfFiles);
		ProductStorage storage = new ProductStorage();
		storage.register(pool);

		poolMap.put(config, storage);

		poolIndex++;
	    }
	}

	return poolMap;
    }

    protected abstract Map<Configuration, ProductStorage> locatePools() throws IOException, GeneralSecurityException;

    protected ProductPool preparePool(int poolIndex, double size, int nFiles) throws IOException, GeneralSecurityException {
	log.info("Preparing pool "+poolIndex+" with "+nFiles+" products of "+size+" MB.");

	ProductPool pool = getPool(poolIndex); // changed this from createPool
	cleanPool(poolIndex);
	
	TestProductGenerator.fillPoolSmarterProducts(pool, size, nFiles);

	log.info("Pool with index "+poolIndex+" ready for use.");
	return pool;
    }
    
    protected int getNProducts(ProductStorage store) throws IOException, GeneralSecurityException {
	final AttribQuery query = new AttribQuery(Product.class, "p", "1");
	return store.select(query).size();
    }

    protected double getSize(ProductStorage storage) throws IOException, GeneralSecurityException {
	if(storage.tagExists(TestProductGenerator.TEST_TAG)) {
	    ProductRef ref = storage.load(TestProductGenerator.TEST_TAG);
	    return ref.getSize()/1000000.;
	}
	else {
	    log.warning("No tag "+TestProductGenerator.TEST_TAG+" found, can't determine product size.");
	    return -1;
	}
    }

    //
    //  Benchmark implementations
    //

    public void testSave() throws GeneralSecurityException {
	log.info("testSave");
	List<Product> products = new ArrayList<Product>();

	try {
	    // Generate the products for this test in memory
	    for (int i = 0; i < 10; i++) {
		products.add( TestProductGenerator.get1MProduct() );
	    }

	    List<ProductRef> savedUrnList = new ArrayList<ProductRef>(products.size());

	    timer.start();
	    for(Product product: products) {
		savedUrnList.add( getStorage().save(product) );
	    }
	    timer.stop();

	    // Clean up after myself:
	    ProductStorage store = getStorage();
	    for(ProductRef pRef : savedUrnList) {
		store.remove(pRef.getUrn());
	    }
	}
	catch(IOException e) {
	    // Must catch this one because we're overriding a method that doesn't throw IOException.
	    log.severe(e.getMessage());
	    throw new RuntimeException(e);
	}

    }


}
