package herschel.ia.pal.performance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class ResultSet {

    public static final DecimalFormat timeFormat = new DecimalFormat ("#,###,##0");
    public static final String format = new String ("%,6d");
    private static enum Column { NAME, SIZE, NPRODS, INDEX, TIME, ACC, PLUSMIN, NRUNS  };

    private static Map<Column, Integer> colWidth = new HashMap<Column, Integer>();
    
    private static Logger log = Logger.getLogger("ResultSet");

    private List<Writer> outputWriter = new ArrayList<Writer>();
    private boolean outputWriterEnabled;
    private static final String SEP = ";"; // separator in CSV format
    
    private String resultSetName;

    static {
	colWidth.put(Column.NAME, 20);
	colWidth.put(Column.SIZE, 12);
	colWidth.put(Column.NPRODS, 9);
	colWidth.put(Column.INDEX, 7);
	colWidth.put(Column.TIME, 11);
	colWidth.put(Column.PLUSMIN, 5);
	colWidth.put(Column.ACC, 7);
	colWidth.put(Column.NRUNS, 10);
    }
    
    /**
     * A benchmark configuration: The name of the benchmark plus
     * what properties of the pool, that the benchmark was run on.
     */
    public static class Configuration implements Comparable<Configuration> {
	/**
	 * The name of the benchmark that was run (what was tested).
	 */
	public String benchmark; 

	/**
	 * Size (in GB) of the pool the result was obtained on.
	 */
	public double size;

	/**
	 * Number of Products in the pool, that the result was obtained on.
	 */
	public int nProducts;  // [#]

	/**
	 * The index of the pool, e.g. 1 for pool lstore.test1
	 */
	public int poolIndex;

	private static final DecimalFormat sizeFormat = new DecimalFormat("#0.000");

	
	public int compareTo(Configuration other) {
	    final int order = -1; // set to 1 for reverse order or 0 for no sorting

	    if(benchmark == null) {
		if(other.benchmark!=null) {
		    return order;
		}
	    }
	    else {
		int name = benchmark.compareTo(other.benchmark);
		if(name!=0) {
		    return name;
		}
	    }

	    if(Math.abs(  (other.size / this.size) - 1) > 0.2 ) {
		// I consider two pools that are within 20% of the same size, as having the same size.
		// The differences are induced by the additional indexing info for more files, etc.
		return (other.size > this.size) ? order : -order;
	    }

	    if(other.nProducts != this.nProducts) {
		return other.nProducts > this.nProducts ? order : -order;
	    }

	    if(other.poolIndex != this.poolIndex) {
		return other.poolIndex > this.poolIndex ? order : -order;
	    }

	    return 0;
	}

	public boolean equals(Object o) {
	    if(o == null) {
		return false;
	    }
	    if(o instanceof Configuration) {
		return compareTo( (Configuration) o) == 0;
	    }
	    else {
		return false;
	    }
	}

	public int hashCode() {
	    return 0;
	}

	public String toString() {
	    if(size==0 && nProducts==0) {
		return benchmark+" on pool with index "+poolIndex;
	    }
	    else {
		return benchmark+" on pool with "+nProducts+" products of "+sizeFormat.format(size)+" MB each (index "+poolIndex+")";
	    }
	}
    }


    /**
     * The time measurements obtained on this pool.
     */
    private Map<Configuration, List<Long>> data = new TreeMap<Configuration, List<Long> >();   // time measurements in milliseconds

    /**
     * Create a result set and specify a name to be printed above the results (when print() is called).
     */
    public ResultSet(String name) {
	resultSetName = name;
    }

    public void addTime(Configuration config, long time) {
	List<Long> times = data.get(config);
	if(times==null) {
	    times = new ArrayList<Long>(); // NB Must be ArrayList -- must maintain insertion order to identify first measurement later on
	    Configuration clone = new Configuration();
	    clone.benchmark = config.benchmark;
	    clone.nProducts = config.nProducts;
	    clone.size = config.size;
	    clone.poolIndex = config.poolIndex;

	    data.put(clone, times);
	}
	times.add(time);
    }

    public double getTime(Configuration c) {
	
	List<Long> times = data.get(c);
	List<Long> timesWithoutFirst= shouldExcludeFirstMeasurement(times);
	
	if(timesWithoutFirst==null) {
	    return getAverage(times);
	}
	else {
	    return getAverage(timesWithoutFirst);
	}
	
    }
    
    private List<Long> shouldExcludeFirstMeasurement(List<Long> times) {
	
	List<Long> timesWithoutFirst = new ArrayList<Long>();
	boolean flag = false;
	for(long time : times) {
	    if(flag) {
		timesWithoutFirst.add(time);
	    }
	    else {
		flag = true;
	    }
	}

	double first = times.size()>3 ? times.get(0) : -1;
	double diff = Math.abs(first - getAverage(timesWithoutFirst));
	
	// See if the first measurement is significantly slower than the rest
	// Significant = difference larger than 3 times the accuracy of the rest
	if(times.size()>3 && diff > 3*getStandardDeviation(timesWithoutFirst)) {
	    return timesWithoutFirst;
	}
	else {
	    return null;
	}
    }
    
    private double getAverage(List<Long> times) {
	double average = 0;
	for(long time : times) {
	    average += time;
	}
	average = average/times.size();
	return average;
    }


    /**
     * Provide simple standard deviation on the list of times. 
     * The variance is: Sum( (<x> - x)^2 ) / (n-1)
     * The standard deviation is the square root of the variance.
     * 
     * @param c
     * @return
     */
    public double getTimeAccuracy(Configuration c) {
	List<Long> times = data.get(c);
	List<Long> timesWithoutFirst = shouldExcludeFirstMeasurement(times);
	if(timesWithoutFirst==null) {
	    return getStandardDeviation(times);
	}
	else {
	    return getStandardDeviation(timesWithoutFirst);
	}
    }
    
    private double getStandardDeviation(List<Long> times) {
	// See: http://teacher.nsrl.rochester.edu/phy_labs/AppendixB/AppendixB.html

	double average = getAverage(times);
	double variance = 0;

	for(long time : times) {
	    variance += (average-time)*(average-time);
	}

	int n = times.size()-1;
	if(n<1) {
	    n=1;
	}

	return Math.sqrt(variance/n);
    }

    /**
     * Get the number of data points (time measurements) for this Configuration.
     *
     */
    public int getN(Configuration c) {
	return data.get(c).size();
    }

    public void print() {
	Date now = new Date();
	int build = herschel.share.util.Configuration.getBuildNumber();

	File[] outFiles = getOutputFiles(now, build);
	outputWriterEnabled = true;

	try {
	    for(File outputFile : outFiles) {
    	    if(!outputFile.createNewFile()) {
    		log.severe("\n\n Could not create output file: "+outputFile);
    	    }
    	    if(!outputFile.canWrite()) {
    		log.severe("\n\n Can't write to output file: "+outputFile);
    	    }
    	    outputWriter.add( new FileWriter(outputFile) );
	    }
	    
	}
	catch(IOException e) {
	    log.severe("Couldn't open file for output -- output on screen is all you're getting...");
	    outputWriterEnabled = false;
	}

	try {
	    StringBuilder buf = new StringBuilder();
	    buf.append("# ==================================================================================");
	    buf.append("\n# Results for: "+resultSetName);
	    buf.append("\n# Benchmark result obtained at "+now+" -- build "+build);
	    buf.append("\n# ==================================================================================\n");

	    printStr(buf.toString());
	    printCSV(buf.toString());
	    
	    printInColumn("#   Name", colWidth.get(Column.NAME));
	    printInColumn("Size [MB]", colWidth.get(Column.SIZE));
	    printInColumn("# prods", colWidth.get(Column.NPRODS));
	    printInColumn("pool", colWidth.get(Column.INDEX));
	    printInColumn("    Time [ms]", colWidth.get(Column.TIME)+colWidth.get(Column.PLUSMIN)+colWidth.get(Column.ACC));
	    println("");
	    //println("#\tName\t\tSize [MB]\t# Products\t pool \t\t  Time [ms]");
	    
	    printCSV("# Name"+SEP+"Size [MB]"+SEP+"# Products"+SEP+"pool"+SEP+"Time [ms]"+SEP+SEP+"Accuracy [ms]\n");

	    for(Configuration c : data.keySet()) {
		print(c);
	    }

	    println("# Output written to file: "+outFiles[0]+" and "+outFiles[1]);
	    
	    for(Writer writer : outputWriter) {
		writer.close();
	    }
	    
	}
	catch(IOException e) {
	    log.severe("\n\nIOException: "+e.getMessage());
	}
    }

    private void printStr(String str) {
	System.out.print(str);
	
	if(outputWriterEnabled) {
	    try {
		outputWriter.get(0).append(str);
	    }
	    catch(IOException e) {
		log.severe("IOException: "+e.getMessage());
	    }
	}
    }

    private void println(String str) {
	printStr(str);
	printStr("\n");
    }

    private void print(Configuration c) throws IOException {
	//printStr("\t");
	printInColumn(c.benchmark, colWidth.get(Column.NAME));
	printCSV(c.benchmark+SEP);
	//printStr("\t");
	String size = String.format("%"+colWidth.get(Column.SIZE)+".3f", c.size);
	printStr(size);
	printCSV(c.size+SEP);
	//printStr("\t\t");
	printInColumn(c.nProducts, colWidth.get(Column.NPRODS));
	printCSV(c.nProducts+SEP);
	//printStr("\t\t");
	printInColumn(c.poolIndex, colWidth.get(Column.INDEX));
	printCSV(c.poolIndex+SEP);
	//printStr("\t");
	long time = Math.round(getTime(c));
	printInColumn(time, colWidth.get(Column.TIME));
	printCSV(time+SEP);
	printInColumn(" +/- ", colWidth.get(Column.PLUSMIN));
	printCSV("+/-"+SEP);
	long accuracy = Math.round(getTimeAccuracy(c));
	printInColumn(accuracy, colWidth.get(Column.ACC));
	printCSV(accuracy+SEP);
	List<Long> times = data.get(c);
	List<Long> timesWithoutFirst = shouldExcludeFirstMeasurement(times);
	String msg;
	if(timesWithoutFirst==null) {
	    msg = "(n="+times.size()+")";
	}
	else {
	    msg = "(n="+timesWithoutFirst.size()+", first run excluded: "+String.format(format, times.get(0))+" ms)";
	}
	println("  "+msg);
	printCSV(msg+"\n");
	
    }

    private void printInColumn(String str, int columnWidth) throws IOException {
	for(int i=0; i<(columnWidth-str.length()); i++) {
	    printStr(" ");
	}
	printStr(str);
    }
    
    private void printInColumn(Number number, int columnWidth) throws IOException {
	String str = String.format(format, number);
	printInColumn(str, columnWidth);
    }

    private File[] getOutputFiles(Date now, int build) {

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_H-m-s");

	String tmpDir = herschel.share.util.Configuration.getProperty("java.io.tmpdir");
	String name = "benchmark."+resultSetName+".build"+build+"."+dateFormat.format(now);
	File[] files = {new File( tmpDir, name+".txt"), new File( tmpDir, name+".csv") };
	
	return files;
	
    }
    
    private void printCSV(String str) throws IOException {
	outputWriter.get(1).append(str);
    }

}
