/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.taxa;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */
public class NormalExplicitImporter extends TaxonExcelImporterBase {
	
	private static final Logger logger = Logger.getLogger(NormalExplicitImporter.class);
	
	@Override
	protected boolean isIgnore(IImportConfigurator config) {
		return false;
	}
	
	@Override
    protected boolean analyzeRecord(HashMap<String, String> record) {
		
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	TaxonLight taxonLight = new TaxonLight();
    	setTaxonLight(taxonLight);
    	
    	for (String key: keys) {
    		
    		key = CdmUtils.removeDuplicateWhitespace(key.trim()).toString();
    		
    		String value = (String) record.get(key);
    		if (!value.equals("")) {
    			if (logger.isDebugEnabled()) { logger.debug(key + ": '" + value + "'"); }
        		value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
    		}
    		
    		if (key.equalsIgnoreCase(ID_COLUMN)) {
    			try {
    				Float fobj = new Float(Float.parseFloat(value));
    				int ivalue = fobj.intValue();
    				if (logger.isDebugEnabled()) { logger.debug("Id formatted: '" + ivalue + "'"); }
        			getTaxonLight().setId(ivalue);
    			} catch (NumberFormatException ex) {
    				success = false;
    				logger.error("Id " + value + " is not an integer");
    			}
    			
			} else if(key.equalsIgnoreCase(PARENT_ID_COLUMN)) {
    			try {
    				Float fobj = new Float(Float.parseFloat(value));
    				int ivalue = fobj.intValue();
    				if (logger.isDebugEnabled()) { logger.debug("ParentId formatted: '" + ivalue + "'"); }
        			getTaxonLight().setParentId(ivalue);
    			} catch (NumberFormatException ex) {
    				success = false;
    				logger.error("ParentId " + value + " is not an integer");
    			}
				
			} else if(key.equalsIgnoreCase(RANK_COLUMN)) {
    			try {
    				getTaxonLight().setRank(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting rank " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(SCIENTIFIC_NAME_COLUMN)) {
    			try {
    				getTaxonLight().setScientificName(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting name " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(AUTHOR_COLUMN)) {
    			try {
    				getTaxonLight().setAuthor(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting author " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(NAME_STATUS_COLUMN)) {
    			try {
    				getTaxonLight().setNameStatus(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting name status " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(VERNACULAR_NAME_COLUMN)) {
    			try {
    				getTaxonLight().setCommonName(value);
    			} catch (Exception ex) {
    				success = false;
    				logger.error("Error setting vernacular name " + value);
    			}
    			
			} else if(key.equalsIgnoreCase(LANGUAGE_COLUMN)) {
    			try {
    				getTaxonLight().setLanguage(value);
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
		
		boolean success = true;
		Rank rank = null;
		
		CdmApplicationController appCtr = getApplicationController();
		
		String rankStr = getTaxonLight().getRank();
		String taxonNameStr = getTaxonLight().getScientificName();
		String authorStr = getTaxonLight().getAuthor();
		String nameStatus = getTaxonLight().getNameStatus();
		String commonNameStr = getTaxonLight().getCommonName();
		
		if (!taxonNameStr.equals("")) {

			// Determine the rank
			try {
				rank = Rank.getRankByName(rankStr);
			} catch (UnknownCdmTypeException ex) {
				success = false;
				logger.error(rankStr + " is not a valid rank.");
			}
			
            // Create the name
			// Depending on the setting of the nomenclatural code in the configurator 
			// (botanical code, zoological code, etc.), create the corresponding taxon name object. 
			NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();
			TaxonNameBase<?,?> taxonNameBase = nc.getNewTaxonNameInstance(rank);
			taxonNameBase.setTitleCache(taxonNameStr);
			taxonNameBase.setFullTitleCache(taxonNameStr);
			
			// Create the author
			if (!authorStr.equals("")) {
				if (getAuthors().contains(authorStr)) {
					if (logger.isDebugEnabled()) { logger.debug("Author '" + authorStr + "' is already loaded"); }
				} else {
					getAuthors().add(authorStr);
					Person author = Person.NewTitledInstance(authorStr);
					taxonNameBase.setCreatedBy(author);
				}
			}
			
			// Create the nomenclatural status
			try {
			NomenclaturalStatusType statusType = 
				NomenclaturalStatusType.getNomenclaturalStatusTypeByLabel(nameStatus);
			taxonNameBase.addStatus(NomenclaturalStatus.NewInstance(statusType));
			} catch (UnknownCdmTypeException ex) {
				logger.warn("'" + nameStatus + "' is not a valid nomenclatural status label");
			}
			// Create the taxon
			Taxon taxon = Taxon.NewInstance(taxonNameBase, null);
		
			// Add the parent relationship
			if (getTaxonLight().getParentId() != 0) {
				Taxon parentTaxon = findParentTaxon(getTaxonLight());
				if (parentTaxon != null) {
					parentTaxon.addTaxonomicChild(taxon, null, null);
					UUID parentUuid = appCtr.getTaxonService().saveTaxon(parentTaxon);
					if (logger.isDebugEnabled()) { 
						logger.debug("Child '" + getTaxonLight().getScientificName() + "' added to parent '" 
								+ parentTaxon.getTitleCache() + " (" + parentUuid + ")"); 
						}
				} else {
					logger.warn("Taxonomic parent not found for '" + taxonNameStr + "'");
				}
			}

			// Save the taxon
			UUID taxonUuid = appCtr.getTaxonService().saveTaxon(taxon);
//			if (logger.isDebugEnabled()) { logger.debug("taxonUuid = " + taxonUuid); }
			
			// Add the taxon representation to the processed taxa map
			if (getTaxaMap().containsKey(getTaxonLight())) {
				logger.info("Taxon name '" + taxonNameStr + "' is already loaded");
				return true;
			} else { 
				getTaxaMap().put(getTaxonLight(), taxonUuid);
			}
			//Set the previous taxon
			setPreviousTaxonUuid(taxonUuid);
			
			} else 	{ 
				// add common name to previous taxon
				
				UUID taxonUuid = getPreviousTaxonUuid();
				Language language = appCtr.getTermService().getLanguageByIso(getTaxonLight().getLanguage());
				CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance(commonNameStr, language);
				TaxonBase taxonBase = appCtr.getTaxonService().findByUuid(taxonUuid);
				try {
					TaxonDescription description = TaxonDescription.NewInstance((Taxon)taxonBase);
					description.addElement(commonTaxonName);
					logger.info("Common name '" + commonNameStr + "' added to '" + taxonNameStr + "'");
				} catch (ClassCastException ex) {
					logger.error("'" + taxonNameStr + "' is not a taxon instance.");
				}
			}
		
		return success;
    }

	
	private Taxon findParentTaxon(TaxonLight taxonLight) {
		
		UUID parentTaxonUuid = null;
		Taxon parentTaxon = null;
		
		for (TaxonLight tLight : getTaxaMap().keySet()) {
//			logger.debug("tLight.getId() = " + tLight.getId());
//			logger.debug("taxonLight.getParentId() = " + taxonLight.getParentId());
			if (tLight.getId() == taxonLight.getParentId()) {
				parentTaxonUuid = getTaxaMap().get(tLight);
				break;
			}
		}
		
		if (parentTaxonUuid == null) {
			logger.warn("Parent taxon of " + taxonLight.getScientificName() + " unknown." +
			" Ignoring parent-child relationship.");
			return null;
		}
		
		TaxonBase parentTaxonBase = getApplicationController().getTaxonService().findByUuid(parentTaxonUuid);
		try {
			parentTaxon = (Taxon)parentTaxonBase;
		} catch (ClassCastException ex) {
			logger.error("'" + taxonLight.getScientificName() + "' is not a taxon instance.");
		}
		return parentTaxon;
	}
	
}
