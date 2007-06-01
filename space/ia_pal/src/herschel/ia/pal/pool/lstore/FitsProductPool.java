/* $Id: FitsProductPool.java,v 1.1 2007/02/18 14:45:36 bli Exp $
 * Copyright (c) 2006 NAOC
 */

package herschel.ia.pal.pool.lstore;

import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.io.fits.FitsArchive;
import herschel.ia.jconsole.api.JIDEUtilities;
import herschel.ia.pal.query.AttribQuery;
import herschel.ia.pal.query.FullQuery;
import herschel.ia.pal.query.MetaQuery;
import herschel.ia.pal.query.StorageQuery;
import herschel.ia.pal.query.StorageQueryVisitor;
import herschel.share.util.ConfigurationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.python.core.PyInteger;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * Fits implementation of Product Pool concept of the Access Layer. <br>
 * It use FITS format files to store products. Extra metadata index files were
 * maintained to increase searching performance. <br>
 * It was intend to be used indirectly through {@link LocalStoreFactory}.
 * 
 * @author libo@bao.ac.cn
 */
public class FitsProductPool implements LocalStore, StorageQueryVisitor {

	private final static Logger logger = Logger.getLogger("FitsProductPool");

	private transient Set<String> _list;

	private transient Set<String> _previous;

	private PythonInterpreter _jython;

	private LocalStoreContext _context = null;

	/**
	 * Create a FitsProductPool using the {@link LocalStoreContext}
	 * configuration.
	 */
	public FitsProductPool() {
		_jython = jython();
		_context = new LocalStoreContext();
	}

	private PythonInterpreter jython() {
		_jython = JIDEUtilities.getCallingInterpreter();

		if (_jython == null) {

			// Properties jyProp = new Properties();
			// jyProp.setProperty("python.security.respectJavaAccessibility",
			// "false");
			// Properties sysProp = (Properties) System.getProperties().clone();
			// PythonInterpreter.initialize(sysProp, jyProp, new String[0]);

			PySystemState.initialize();
			_jython = new PythonInterpreter();
			// _jython.exec("from herschel.ia.numeric.all import *");
			_jython.exec("from herschel.ia.numeric import *");

		}
		return _jython;
	}

	/**
	 * Returns true if the products with this urn exists in this product pool.
	 * 
	 * @param urn
	 * @return true if the products with this urn exists
	 */
	public boolean exists(String urn) throws IOException {
		File file = null;
		String fileName = null;

		fileName = UrnHelper.getFileNameByUrn(_context, urn);

		if (fileName != null) {
			file = new File(fileName);
		}
		boolean exists = (fileName != null)
				&& (!fileName.trim().equalsIgnoreCase("")) && file.exists();

		return exists;
	}

	/**
	 * Loads a Product belonging to specified URN.
	 * 
	 * @param urn
	 * @return product belonging to this urn
	 * @throws IOException
	 */
	public Product load(String urn) throws IOException {
		if (!exists(urn)) {
			throw new IOException("Requesting access to non existing item: "
					+ urn);
		}
		String fileName = UrnHelper.getFileNameByUrn(_context, urn);
		if (fileName == null)
			return null;
		File productFile = new File(fileName);
		try {
			FitsArchive fa = new FitsArchive();
			Product p = fa.load(productFile.getAbsolutePath());
			return p;
		} catch (IOException e) {
			IOException ioe = new IOException("Can not load item: " + urn
					+ " from '" + productFile.getAbsolutePath() + "'");
			ioe.initCause(e);
			throw ioe;
		}
	}

	/**
	 * Saves specified product and returns the designated URN. Note: may be
	 * called recursively by the ProductContext!
	 * 
	 * @param product
	 * @return urn
	 * @throws IOException
	 */
	public String save(Product product) throws IOException {
		return save(product, null);
	}

	/**
	 * Saves specified product with specified data file name and returns the
	 * designated URN. Note: may be called recursively by the ProductContext!
	 * 
	 * @param product
	 * @param name
	 *            data file name
	 * @return urn
	 * @throws IOException
	 */
	public String save(Product product, String name) throws IOException {
		return save(product, null, name, false);
	}

	/**
	 * Saves specified product with specified data file name and returns the
	 * designated URN. User may import products to local store without saving
	 * data files in local store (Only query index infomations are saved). In
	 * this case, the param isInPlace should be set to true.
	 * 
	 * Note: may be called recursively by the ProductContext!
	 * 
	 * @param product
	 * @param dir
	 * @param name
	 * @param isInPlace
	 *            Indicates whether local store should save data file.
	 * @return urn
	 * @throws IOException
	 */
	public String save(Product product, String dir, String name,
			boolean isInPlace) throws IOException {
		String type = product.getClass().getName();
		if (dir == null) {
			dir = getProperDataFileDir(product);
		}
		File directory = new File(this.getContext().getStoreDir(), dir);
		if (!directory.exists())
			directory.mkdirs();
		int id = updateSeq(type);
		String urn = UrnHelper.getUrn(getId(), type, id);
		if (name == null) {
			name = getProperDataFileName(product, id);
		}
		File dataFile = new File(directory, name);
		if (!isInPlace) {
			if (dataFile.exists()) {
				throw new IOException("File '" + dataFile.getName() + "' in "
						+ directory + " already exist. ");
			}
			FitsArchive fa = new FitsArchive();
			try {
				fa.save(dataFile.getAbsolutePath(), product);
			} catch (IOException e) {
				IOException ioe = new IOException(
						"Failed to save product using FitsArchive. File:"
								+ dataFile.getAbsolutePath() + " URN: " + urn
								+ " " + e.getMessage());
				ioe.initCause(e);
				throw ioe;
			}
		}
		try {
			UrnHelper.updateFileNameUrnMapping(_context, urn, dir
					+ dataFile.getName(), isInPlace);
			RandomAccessFile raf = getMetaRaf(type);
			MetaManager _metaManager = new MetaManager(raf, _jython);
			_metaManager.saveMeta(product.getMeta(), type, id);
			raf.close();
			saveAttributes(product, urn);
		} catch (IOException e) {
			IOException ioe = new IOException(
					"Failed to save product index file. " + " urn: " + urn
							+ " " + e.getMessage());
			ioe.initCause(e);
			throw ioe;
		}
		return urn;
	}

	/**
	 * Loads the meta-data belonging to the product of specified URN.
	 * 
	 * @param urn
	 * @return metadata
	 * @throws IOException
	 */
	public MetaData meta(String urn) throws IOException {
		String type = UrnHelper.getTypeFromUrn(urn);
		RandomAccessFile raf = getMetaRaf(type);
		MetaManager _metaManager = new MetaManager(raf, _jython);
		MetaData meta = _metaManager.getMeta(UrnHelper.getTypeFromUrn(urn),
				UrnHelper.getIdFromUrn(urn));
		raf.close();
		return meta;
	}

	private RandomAccessFile getMetaRaf(String type) throws IOException {
		File file = new File(this.getContext().getIndexDir(), type + ".meta");
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		return raf;

	}

	/**
	 * Returns a list of URNs to products that match the specified query.
	 * 
	 * @param query
	 * @return a list of URNs.
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public Set<String> select(StorageQuery query) throws IOException,
			GeneralSecurityException {
		_previous = null;
		query.accept(this);
		Set<String> list = _list;
		_list = null;
		return list;
	}

	/**
	 * Returns a list of URNs to products within specified previous results that
	 * match the specified refinement query.
	 * 
	 * @param query -
	 *            new query to refine previous results' from a previous query
	 * @return a refined list of URNs
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public Set<String> select(StorageQuery query, Set<String> results)
			throws IOException, GeneralSecurityException {
		_previous = results;
		query.accept(this);
		Set<String> list = _list;
		_list = null;
		_previous = null;
		return list;
	}

	/**
	 * Full Query implementation.
	 */
	public void visit(FullQuery query) throws IOException {
		String type = query.getType().getName();
		_list = new TreeSet<String>();
		if (_previous == null) {
			List<String> targetFileList = getTargetFileNames(type);
			targetFileList.add(type);

			for (int i = 0; i < targetFileList.size(); i++) {
				type = (String) targetFileList.get(i);
				Set urns = UrnHelper.getUrnsByType(_context, type);
				if (urns != null) {
					Iterator iter = urns.iterator();
					while (iter.hasNext()) {
						String urn = (String) iter.next();
						Product product = load(urn);
						if (matches(product, query))
							_list.add(urn);
					}
				}
			}
		} else {
			for (String urn : _previous) {

				Product product = load(urn);
				if (matches(product, query))
					_list.add(urn);
			}
		}
	}

	/**
	 * 
	 * Attribute Query implementation.
	 */
	public void visit(AttribQuery query) throws IOException {
		int[] records = null;
		_list = new TreeSet<String>();
		if (query == null)
			return;

		// set up attribute scanner:
		String type = query.getType().getName();

		List<String> indexFileList = new ArrayList<String>();
		indexFileList = getTargetFileNames(type);
		indexFileList.add(type);

		for (int i = 0; i < indexFileList.size(); i++) {
			type = (String) indexFileList.get(i);
			File file = new File(getContext().getIndexDir(), type + ".attrib");
			if (file.exists()) {
				RandomAccessFile raf = new RandomAccessFile(file, "r");
				AttributesManager attrib = new AttributesManager(raf, _jython);

				// pass query

				if (_previous == null) {
					records = attrib.select(query);
				} else {
					int[] previous = extractRecords(_previous);
					records = attrib.select(query, previous);
				}

				if (records == null)
					continue;

				// convert found records to URNs
				for (int r : records)
					_list.add(UrnHelper.getUrn(getId(), type, r));

				raf.close();
			}
		}
	}

	/**
	 * Meta Query implementation.
	 */
	public void visit(MetaQuery query) throws IOException {
		_list = new TreeSet<String>();
		String type = query.getType().getName();
		List<String> metaFileList = new ArrayList<String>();
		metaFileList = getTargetFileNames(type);
		metaFileList.add(type);
		for (int i = 0; i < metaFileList.size(); i++) {
			type = (String) metaFileList.get(i);
			File file = new File(this.getContext().getIndexDir(), type
					+ ".meta");
			if (file.exists()) {
				RandomAccessFile raf = new RandomAccessFile(file, "r");
				MetaManager _metaManager = new MetaManager(raf, _jython);
				if (_previous == null) {

					int[] records = _metaManager.select(query);

					for (int r : records) {
						String urn = UrnHelper.getUrn(getId(), type, r);
						_list.add(urn);
					}

				} else {
					int[] previous = extractRecords(_previous);
					int[] records = _metaManager.select(query, previous);
					for (int r : records) {
						String urn = UrnHelper.getUrn(getId(), type, r);
						_list.add(urn);
					}
				}
				raf.close();
			}
		}
	}

	private boolean matches(Product product, StorageQuery query) {
		if (query.getVariable() == null || query.getWhere() == null)
			return true;

		_jython.set(query.getVariable(), product);
		boolean result = false;
		result = ((PyInteger) _jython.eval(query.getWhere())).getValue() != 0;
		_jython.set(query.getVariable(), null);
		return result;
	}

	/**
	 * Removes a Product belonging to specified URN.
	 * 
	 * @param urn
	 * @return true if the product is removed successfully
	 */
	public boolean remove(String urn) throws IOException {
		try {
			String type = UrnHelper.getTypeFromUrn(urn);
			int id = UrnHelper.getIdFromUrn(urn);
			// logger.info("remove the urn from the meta file");
			RandomAccessFile raf = getMetaRaf(type);
			MetaManager _metaManager = new MetaManager(raf, _jython);
			boolean metaRemoved = _metaManager.removeMeta(type, id);
			raf.close();
			removeAttributes(urn);
			if (!metaRemoved) {
				throw new IOException("Failed to remove meta: " + urn + ". ");
			}
		} catch (IOException e) {
			IOException ioe = new IOException("Failed to remove meta: " + urn
					+ ". ");
			ioe.initCause(e);
			throw ioe;
		}

		String fileName = UrnHelper.getFileNameByUrn(_context, urn);
		File dataFile = new File(fileName);
		boolean isDeleted = dataFile.delete();
		UrnHelper.removeFileNameByUrn(_context, urn);
		return isDeleted;

	}

	/**
	 * Get the index files syncronized with the products data files.
	 * 
	 * @return true if rebuild succeed.
	 */
	public boolean rebuildIndex() throws IOException {
		File dir = _context.getIndexDir();
		File[] files = dir.listFiles();
		boolean isSuccess = true;
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String name = file.getName();
			if (file.getName().endsWith(".urnMapping")) {
				String type = name.substring(0, name.indexOf(".urnMapping"));
				isSuccess = isSuccess && rebuildIndex(type);
			}
		}
		return isSuccess;

	}

	/**
	 * Get the index files syncronized with the products.
	 * 
	 * @param type
	 *            The class name of Product want to be syncronized.
	 * @return true if rebuild succeed.
	 */
	public boolean rebuildIndex(String type) throws IOException {
		RandomAccessFile raf = getMetaRaf(type);
		MetaManager _metaManager = new MetaManager(raf, _jython);
		boolean isMetaRemoved = removeMetaByType(type);

		boolean isAttributesRemoved = removeAttributesByType(type);
		if (!isMetaRemoved) {
			logger.info("Meta file can not be removed! " + " type: " + type);
			return false;
		}
		if (!isAttributesRemoved) {
			logger.info("Attributes file can not be removed! " + " type: "
					+ type);
			return false;
		}

		// Regenerate the metadata index file
		Set urns = UrnHelper.getUrnsByType(_context, type);
		if (urns != null) {
			Iterator iter = urns.iterator();
			while (iter.hasNext()) {
				String urn = (String) iter.next();
				String fileName = UrnHelper.getFileNameByUrn(_context, urn);
				File file = new File(fileName);
				FitsArchive fa = new FitsArchive();
				Product product = fa.load(file.getAbsolutePath());
				MetaData meta = product.getMeta();
				_metaManager.saveMeta(meta, urn);
				saveAttributes(product, urn);
			}
		}
		raf.close();
		return true;
	}

	/**
	 * Get the corresponding LocalStoreContext.
	 */
	public LocalStoreContext getContext() {
		return _context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.ProductPool#isAlive(herschel.ia.pal.pool.lstore.LocalStoreContext)
	 */
	public boolean isAlive() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.ProductPool#getId(herschel.ia.pal.pool.lstore.LocalStoreContext)
	 */
	public String getId() {
		return _context.getPoolId();
	}

	private void saveAttributes(Product p, String urn) throws IOException {
		// setup attribute adder:
		String type = p.getClass().getName();
		File indexDir = getContext().getIndexDir();
		File file = new File(indexDir, type + ".attrib");
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		AttributesManager attrib = new AttributesManager(raf, _jython);

		attrib.addRecord(p, urn);
		raf.close();
	}

	private void removeAttributes(String urn) throws IOException {
		// setup attribute adder:
		String type = UrnHelper.getTypeFromUrn(urn);
		File indexDir = getContext().getIndexDir();
		File file = new File(indexDir, type + ".attrib");
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		AttributesManager attrib = new AttributesManager(raf, _jython);

		attrib.removeRecord(urn);
		raf.close();
	}

	private boolean removeMetaByType(String type) throws IOException {
		File indexDir = getContext().getIndexDir();
		File file = new File(indexDir, type + ".meta");
		if (!file.exists()) {
			return true;
		}
		return file.delete();
	}

	private boolean removeAttributesByType(String type) throws IOException {
		File indexDir = getContext().getIndexDir();
		File file = new File(indexDir, type + ".attrib");
		if (!file.exists()) {
			return true;
		}
		return file.delete();
	}

	private int[] extractRecords(Set<String> urns) {
		int[] ids = new int[urns.size()];
		int current = 0;
		for (String urn : urns) {
			if (UrnHelper.isLegal(urn)) {
				ids[current++] = UrnHelper.getIdFromUrn(urn);
			}
		}
		return ids;
	}

	private List<String> getTargetFileNames(String type) {
		List<String> targetFileList = new ArrayList<String>();
		File dir = this.getContext().getIndexDir();
		File indexFile[] = dir.listFiles();
		for (int i = 0; i < indexFile.length; i++) {
			String fName = indexFile[i].getName();
			if (fName.endsWith(".meta")) {
				fName = fName.replaceAll(".meta", "");
				List superClassList = getSuperClasses(fName);
				if (superClassList.contains(type)) {
					targetFileList.add(fName);
				}
			}
		}
		return targetFileList;
	}

	private String getGenericSuperclass(String type) {
		Type clazz = null;
		String className = "";
		try {
			clazz = Class.forName(type).getGenericSuperclass();
		} catch (ClassNotFoundException e) {
			ConfigurationException ce = new ConfigurationException(type
					+ " could not be resolved to a type.");
			ce.initCause(e);
			throw ce;
		}
		if (clazz != null) {
			className = clazz.toString();
		} else {
			className = "";
		}
		className = className.replaceAll("class ", "");
		return className;

	}

	private List getSuperClasses(String type) {
		List<String> superClassList = new ArrayList<String>();

		// XXX not a proper place here
		if (!_context.isDeepSearch()) {
			return superClassList;
		}

		String superClass = getGenericSuperclass(type);
		while (!superClass.equals("java.lang.Object") && !superClass.equals("")) {
			superClassList.add(superClass);
			superClass = getGenericSuperclass(superClass);
		}
		return superClassList;
	}

	private synchronized int updateSeq(String type) throws IOException {
		File directory = new File(this.getContext().getStoreDir()
				+ File.separator + type);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		File seqFile = new File(directory, ".seq");
		int seq = 0;
		if (seqFile.exists()) {
			FileReader fileReader = new FileReader(seqFile);
			BufferedReader bReader = new BufferedReader(fileReader);
			String seqString = bReader.readLine();
			try {
				seq = Integer.parseInt(seqString);
			} catch (NumberFormatException e) {
				IOException ioe = new IOException(
						"Number Format Exception in .seq file. Directory: "
								+ directory);
				ioe.initCause(e);
				throw ioe;
			}
			seq++;
			bReader.close();
			fileReader.close();
		} else {
			seqFile.createNewFile();
		}

		FileWriter filewriter = new FileWriter(seqFile, false);
		PrintWriter printwriter = new PrintWriter(filewriter);
		printwriter.println(seq);
		printwriter.flush();
		printwriter.close();
		filewriter.close();
		return seq;

	}

	private String getProperDataFileName(Product p, int id) {

		String name = "";
		String type = "";
		try {
			if (p.getMeta().get("fileName") != null) {
				name = (String) p.getMeta().get("fileName").getValue();
				name = name.replace('/', '_');
				name = name.replace('\\', '_');
				if (!name.endsWith(_context.getDataFilePostfix())) {
					name = name + _context.getDataFilePostfix();
				}
				return name;
			} else {
				type = p.getType();
			}
		} catch (NullPointerException e) {
			// ignore
		}
		if (name == null || name.trim().length() < 1) {
			if (type != null
					&& !((type.equalsIgnoreCase("")) || (type
							.equalsIgnoreCase("Unknown")))) {
				name = type;
			} else {
				name = p.getClass().getSimpleName();
			}
		}
		return name + "_v" + id + _context.getDataFilePostfix();

	}

	private String getProperDataFileDir(Product product) {
		String type = product.getClass().getName();
		String dir = "";
		try {
			if (product.getMeta().get("context") != null) {
				dir = (String) product.getMeta().get("context").getValue();
				dir = dir.replace('/', File.separatorChar);
				dir = dir.replace('\\', File.separatorChar);
			}
		} catch (NullPointerException e) {
			// ignore
		}
		if (dir == null || dir.trim().equalsIgnoreCase("")) {
			dir = type;
		}
		if (!dir.endsWith(File.separator)) {
			dir += File.separator;
		}
		return dir;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.pool.lstore.LocalStore#setContext(herschel.ia.pal.pool.lstore.LocalStoreContext)
	 */
	public void setContext(LocalStoreContext context) {
		_context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.ProductPool#getProductClasses(herschel.ia.pal.pool.lstore.LocalStoreContext)
	 */
	public Set<Class> getProductClasses() throws IOException,
			GeneralSecurityException {
		return UrnHelper.getAllClasses(_context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.pool.lstore.LocalStore#save(herschel.ia.dataset.Product,
	 *      boolean)
	 */
	public String save(Product p, boolean isInPlace) throws IOException {
		return save(p, null, null, isInPlace);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see herschel.ia.pal.pool.lstore.LocalStore#ingest(herschel.ia.pal.pool.lstore.LocalStoreContext)
	 */
	public Set<String> ingest(File dir, boolean isInPlace) throws IOException {
		FitsIngestor ingestor = new FitsIngestor(this);
		return ingestor.ingest(dir, isInPlace);
	}

	/**
	 * Release all resources.
	 * 
	 */
	public void close() {
		_list = null;
		_previous = null;
		_context = null;
		System.gc();
	}

}
