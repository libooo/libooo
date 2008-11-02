package herschel.ia.pal.util;

import herschel.ia.dataset.Parameter;
import herschel.ia.dataset.Product;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Helper class to get the fits file name according to the product's metadata
 * values.
 * 
 * Export Fits file name as:
 * 
 * 'h<product/instrument><subinst><obsid/od>_<bbid>_<level><xxxxx>_<yyy>v<NN>'
 * 
 * @author libo@bao.ac.cn
 * 
 */
public class HerschelNames {

	private final static Logger logger = Logger.getLogger(HerschelNames.class
			.getName());

	private static List<String> metaItems = new ArrayList<String>();
	static {
		metaItems.add("instrument");
		metaItems.add("subsystem");
		metaItems.add("subinstrumentId");
		metaItems.add("obsid");
		metaItems.add("bbid");
		metaItems.add("level");
		metaItems.add("type");
		metaItems.add("slice");
		metaItems.add("version");
	}

	private static List<String> underscoreMetaItems = new ArrayList<String>();
	static {
		underscoreMetaItems.add("bbid");
		underscoreMetaItems.add("level");
		underscoreMetaItems.add("slice");
	}

	private static String getExportName(Product p) {
		StringBuffer sb = new StringBuffer("h");
		for (String item : metaItems) {
			if (p.getMeta().containsKey(item) && p.getMeta().get(item) != null) {
				
				Object value = p.getMeta().get(item).getValue();
				if (underscoreMetaItems.contains(item)) {
					sb.append("_");
				}
				if (item.equalsIgnoreCase("version")) {
					sb.append("v");
				}
				if (item.equalsIgnoreCase("type")) {
					if(((String)value).equalsIgnoreCase("Unknown")){
						value = p.getClass().getSimpleName();
					}
				}
				if (item.equalsIgnoreCase("instrument")) {
					if(((String)value).equalsIgnoreCase("Unknown")){
						value = "";
					}
				}
				if(item.equalsIgnoreCase("bbid") || item.equalsIgnoreCase("obsid")){
					//The hex values for obsid & bbid should be formatted using the 
					//full 32-bit range, ie an 8-character field. So 10 -> 0000000a
					value = Long.toHexString(((Long)value).longValue());
					StringBuffer sbZero= new StringBuffer();
					if(((String)value).length()<8){
						for(int i=0;i<(8-((String)value).length());i++){
							sbZero.append("0");
						}
					}
					value=sbZero.append(value);
				}	
				sb.append(value);
			}
		}

		return sb.toString().toLowerCase();
	}

	/**
	 * Get the fits file name according to the product's metadata values. A
	 * filename should be constructed this way:
	 * 
	 * If the fileName keyword is present, use it unmodified (exception: add
	 * ".fits" if necessary, plus timestamp if applicable - see below).
	 * 
	 * Otherwise attempt to construct a filename using the rules in
	 * http://www.rssd.esa.int/llink/livelink/fetch/2000/414493/10737/491269/506156/Herschel_Products_Defnitions_Document?nodeid=2766178&vernum=-2
	 * 
	 * Standard metadata names are given in
	 * http://www.herschel.be/twiki/bin/view/Hcss/ProductMetaDataConvention
	 * 
	 * The relevant ones are: 1. instrument 2. subsystem or subinstrumentId (is
	 * which one dependent on the instrument?) 3. obsid 4. bbid 5. level (*note*
	 * not in list!) 6. type 7. version
	 * 
	 * (There is also a slice count element, but no metadata name exists for it
	 * as far as I can see. Probably one ought to de defined).
	 * 
	 * Metadata keywords that do not exist should be omitted from the name.
	 * 
	 * Note that "version" is now considered a user-specified metadata item and
	 * should not be incremented or created automatically. It has no relation to
	 * "versioning" in the PAL.
	 * 
	 * @param p
	 * @param isInPlace
	 * @return
	 */
	public static String getDataFileName(Product p, boolean isInPlace) {

		String name = "";
		if (p.getMeta().containsKey("fileName")
				&& p.getMeta().get("fileName") != null) {
			Parameter pFileName = p.getMeta().get("fileName");
			name = (String) pFileName.getValue();
			name = name.replace('/', '_');
			name = name.replace('\\', '_');

		} else {
			name = getExportName(p);
		}
		return name;
	}

	
	/**
	 * Appending a timestamp to the name.
	 * @param name
	 * @return
	 */
	public static String appendTimestamp(String name) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'");
		Date now = new Date(System.currentTimeMillis());
		String formatedTimestamp = df.format(now);
//		logger.info("formatedTimestamp: " + formatedTimestamp);
		return name.concat("_" + formatedTimestamp);

	}

}
