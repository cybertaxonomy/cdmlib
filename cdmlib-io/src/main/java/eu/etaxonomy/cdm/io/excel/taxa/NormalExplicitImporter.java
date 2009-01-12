/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.taxa;

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */
public class NormalExplicitImporter extends TaxonExcelImporterBase {
	
	private static final Logger logger = Logger.getLogger(NormalExplicitImporter.class);
	
//	@Override
//	protected boolean doInvoke(IImportConfigurator config,
//			Map<String, MapWrapper<? extends CdmBase>> stores) {
//		
//		boolean success = true;
//		
//		success = super.doInvoke(config, stores); 
//		
//    	return success;
//	}

	@Override
	protected boolean isIgnore(IImportConfigurator config) {
		return false;
	}
	
	@Override
    protected boolean analyzeRecord(HashMap<String, String> record) {
		
		if (logger.isDebugEnabled()) { logger.debug("analyzeRecord() entered"); }

		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	for (String key: keys) {
    		
    		key = CdmUtils.removeDuplicateWhitespace(key.trim()).toString();
    		
    		String value = (String) record.get(key);
    		if (!value.equals("")) {
    			logger.debug(key + ": '" + value + "'");
        		value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
    		}
    		
    		if (key.equalsIgnoreCase(ID_COLUMN)) {
    			try {
    				Float fobj = new Float(Float.parseFloat(value));
    				int ivalue = fobj.intValue();
        			logger.debug("ivalue = '" + ivalue + "'");
    				setId(ivalue);
    			} catch (NumberFormatException ex) {
    				success = false;
    				logger.error("Id " + value + " is not an integer");
    			}
    			
			} else if(key.equalsIgnoreCase(PARENT_ID_COLUMN)) {
    			try {
    				Float fobj = new Float(Float.parseFloat(value));
    				int ivalue = fobj.intValue();
        			logger.debug("ivalue = '" + ivalue + "'");
    				setParentId(ivalue);
    			} catch (NumberFormatException ex) {
    				success = false;
    				logger.error("ParentId " + value + " is not an integer");
    			}
				
			} else if(key.equalsIgnoreCase(RANK_COLUMN)) {
    			try {
    				setRank(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting rank " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(SCIENTIFIC_NAME_COLUMN)) {
    			try {
    				setTaxonName(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting name " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(AUTHOR_COLUMN)) {
    			try {
    				setAuthor(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting author " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(NAME_STATUS_COLUMN)) {
    			try {
    				setNameStatus(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting name status " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(VERNACULAR_NAME_COLUMN)) {
    			try {
    				setCommonName(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting vernacular name " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(LANGUAGE_COLUMN)) {
    			try {
    				setLanguage(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting language " + value);
    			}
    			
			} else {
				success = false;
				logger.error("Unexpected column header " + key);
			}
    	}
    	return success;
    }
	
	/** 
	 *  Stores taxa records in DB
	 */
	@Override
    protected boolean saveRecord() {
		
		if (logger.isDebugEnabled()) { logger.debug("saveRecord() entered"); }
		
		boolean success = true;
		Rank rank = null;
		CdmApplicationController appCtr = getApplicationController();
		
		// Determine the rank
		
		try {
			rank = Rank.getRankByName(getRank());
		} catch (UnknownCdmTypeException ex) {
			success = false;
			logger.error(getRank() + " is not a valid rank.");
		}
		
		// Depending on the setting of the nomenclatural code in the configurator (botanical code, zoological code, etc.),
		// create the corresponding taxon name object. 
		
		String name = getTaxonName();
		if (name != "") {
			NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();
			if (nc != null) {
				TaxonNameBase<?,?> taxonName = nc.getNewTaxonNameInstance(rank);
				taxonName.setTitleCache(name);
				taxonName.setFullTitleCache(name);
				appCtr.getNameService().saveTaxonName(taxonName);
			} else {
				logger.error("Nomenclatural code is null");
				success = false;
			}
		}
		
		
		return success;
    }
}
