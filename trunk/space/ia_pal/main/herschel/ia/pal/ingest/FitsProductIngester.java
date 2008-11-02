package herschel.ia.pal.ingest;

import herschel.ia.dataset.Product;
import herschel.ia.io.fits.FitsArchive;
import herschel.ia.pal.ProductStorage;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

public class FitsProductIngester implements ProductIngester {

	private Logger logger = Logger.getLogger(FitsProductIngester.class
			.getName());

	public void ingest(ProductStorage store, ProductIngesterParam param) {
		File dir = ((FitsProductIngesterParam) param).getDir();
		try {
			ingest(store, dir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void ingest(ProductStorage store, File dir)
			throws GeneralSecurityException, IOException {
		System.out.println(dir + " ingested. ");
		if (dir == null || !dir.exists()) {
			throw new IOException("Directory: " + dir + " not exists.");
		}
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				ingest(store, file);
			else {
				loadProduct(store, file);
			}
		}
	}

	/**
	 * Load product from file
	 * 
	 * @param file
	 * @param isInPlace
	 * @return urn
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	private void loadProduct(ProductStorage store, File file)
			throws GeneralSecurityException, IOException {
		if (file.getName().endsWith(".fits") || file.getName().endsWith(".fit")) {
			Product product = null;
			FitsArchive fa = new FitsArchive();
			try {
				product = fa.load(file.getAbsolutePath());
			} catch (IOException e) {
				// SPR-4651: print a warning message and continued.
				logger
						.warning("Got a exception when load fits file using FitsArchive. File name: "
								+ file.getAbsolutePath()
								+ " Error Message: "
								+ e.getMessage());
			}

			store.save(product);
		}

	}
}
