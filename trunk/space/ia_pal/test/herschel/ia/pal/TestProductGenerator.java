/* $Id: TestProductGenerator.java,v 1.7 2008/04/29 08:48:40 pbalm Exp $
 * Copyright (c) 2006 NAOC
 */
package herschel.ia.pal;

import herschel.ia.dataset.ArrayDataset;
import herschel.ia.dataset.Dataset;
import herschel.ia.dataset.DateParameter;
import herschel.ia.dataset.MetaData;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.StringParameter;
import herschel.ia.numeric.ArrayData;
import herschel.ia.numeric.Double1d;
import herschel.ia.numeric.Int1d;
import herschel.ia.numeric.toolbox.random.RandomUniform;
import herschel.share.fltdyn.time.FineTime;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

/**
 * A helper class to generate some kinds of testing Products.
 * 
 */
public class TestProductGenerator {

    public static final String TEST_TAG = "testTag";
    
    /**
     * 
     * Get a simple testing product. 
     * 
     * @return a simple product
     */
    public static Product getSimpleProduct() {
	double seed = Math.random();
	String creator = "";
	if (seed > 0.5) {
	    creator = "Tiger";
	} else {
	    creator = "Scott";
	}
	Product p = new Product();
	p.setCreator(creator);
	p.setInstrument("SPIRE");
	p.setDescription("Test Product");
	return p;
    }

    /**
     * Generate a 1M (fits file size) testing product.
     * @return a 1M (fits file size) testing product.
     */
    public static Product get1MProduct() {
	return getProductOfSize(1);
    }

    public static Product getProductOfSize(double sizeMB) {
	double seed = Math.random();
	String creator = "";
	if (seed > 0.5) {
	    creator = "Tiger";
	} else {
	    creator = "Scott";
	}
	Product p = new Product();
	p.setCreator(creator);
	p.setDescription("Test Product");
	MetaData meta = new MetaData();
	ArrayData data = new Int1d((int) (260000 * sizeMB));
	Dataset dataSet = new ArrayDataset(data);
	p.set("data", dataSet);
	meta.set("version", new StringParameter("1.0"));
	meta.set("creator", new StringParameter(creator));
	meta.set("creationDate", new DateParameter(new FineTime(new Date())));
	meta.set("startDate", new DateParameter(new FineTime(new Date())));
	meta.set("endDate", new DateParameter(new FineTime(new Date())));
	meta.set("instrument", new StringParameter("Spire"));
	meta.set("modelName", new StringParameter("Spire"));
	meta.set("type", new StringParameter("Spire"));
	p.setMeta(meta);
	return p;
    }

    /** 
     * Prepare a pool with very simple products for testing. The products contain an array of random numbers 
     * and no metadata.
     * This method fill the specified pool with nFiles products, to create a pool of roughly sizeGB GB.
     * 
     * @throws IOException, GeneralSecurityException
     * @throws IllegalArgumentException if the number of files is too large to fit in the allocated pool size.
     **/
    public static void fillPool(ProductPool pool, double sizeGB, int nFiles)
	    throws GeneralSecurityException, IOException {

	// C is a parameter, to tune the product size, so that the pool size comes out, according to the specified size.
	final double C = 134217800;

	int arraySize = (int) ((sizeGB * C) / nFiles);
	if (arraySize < 1) {
	    throw new IllegalArgumentException(
		    "Too many files for pool size -- can't create files that small.");
	}

	Double1d array = new Double1d(arraySize);
	ArrayData data = array.perform(RandomUniform.PROCEDURE);

	for (int i = 0; i < nFiles; i++) {

	    Product p = new Product();
	    p.set("ArrayA", new ArrayDataset(data));

	   pool.save(p);
	    
	   /*
	    {
		StringBuffer filename = new StringBuffer("/d/pbalm/tmp/fitsArchive/fitsFile.");
		filename.append(pool.getId());
		filename.append(".");
		filename.append(i);
		FitsArchive archive = new FitsArchive();
		archive.save(filename.toString(), p);
	    }*/
	}

    }

    /** 
     * Prepare a pool with products for testing.
     * This method fill the specified pool with nFiles products, of productSizeMB MB each.
     * 
     * @throws IOException, GeneralSecurityException
     * @throws IllegalArgumentException if the number of files is too large to fit in the allocated pool size.
     **/
    public static void fillPoolSmarterProducts(ProductPool pool, double productSizeMB, int nFiles)
	    throws GeneralSecurityException, IOException {

	ProductStorage store = new ProductStorage();
	store.register(pool);

	for (int i = 0; i < nFiles; i++) {
	    ProductRef ref = store.save(TestProductGenerator.getProductOfSize(productSizeMB));
	    
	    if(i==0) {
		store.setTag(TEST_TAG, ref.getUrn());
	    }
	}
    }

}
