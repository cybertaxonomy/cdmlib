/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.taxa;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.io.jaxb.CdmImporter;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
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
		
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	for (String key: keys) {
    		
    		key = CdmUtils.removeDuplicateWhitespace(key.trim()).toString();
    		
    		String value = (String) record.get(key);
    		if (!value.equals("")) {
    			logger.debug(key + ": '" + value + "'");
    		}
    		
    		if (key.equalsIgnoreCase(ID_COLUMN)) {
    			try {
    				setId(Integer.parseInt(CdmUtils.removeDuplicateWhitespace(value.trim()).toString()));
    			} catch (NumberFormatException ex) {
    				success = false;
    				logger.error("Id " + getId() + " is not an integer");
    			}
    			
			} else if(key.equalsIgnoreCase(PARENT_ID_COLUMN)) {
    			try {
    				setParentId(Integer.parseInt(CdmUtils.removeDuplicateWhitespace(value.trim()).toString()));
    			} catch (NumberFormatException ex) {
    				success = false;
    				logger.error("ParentId " + getParentId() + " is not an integer");
    			}
				
			} else if(key.equalsIgnoreCase(RANK_COLUMN)) {
    			try {
    				setRank(CdmUtils.removeDuplicateWhitespace(value.trim()).toString());
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting rank " + getRank());
    			}
    			
			} else if(key.equalsIgnoreCase(SCIENTIFIC_NAME_COLUMN)) {
    			try {
    				setTaxonName(CdmUtils.removeDuplicateWhitespace(value.trim()).toString());
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting name " + getTaxonName());
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
		
		boolean success = true;
		Rank rank = null;
		
		try {
			rank = Rank.getRankByName(getRank());
		} catch (UnknownCdmTypeException ex) {
			success = false;
			logger.error(getRank() + " is not a valid rank.");
		}
		
		// Distinguish between botanical and zoological name, etc.
		// Take info from configurator.
		
		String name = getTaxonName();
		if (name != "") {
			NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();
			TaxonNameBase<?,?> taxonName = nc.getNewTaxonNameInstance(rank);
			taxonName.setTitleCache(name);
		}

//		if ( nc == NomenclaturalCode.ICZN()) {
//		ZoologicalName zooName = ZoologicalName.NewInstance(rank);
		
//		if () {
//			
//		}
		
		return success;
    }
}
