package eu.etaxonomy.cdm.io.excel.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class DistributionImporter extends CdmIoBase<IImportConfigurator> implements ICdmIO<IImportConfigurator> {

    /* used */
    private static final String EDIT_NAME_COLUMN = "EDIT";
    private static final String TDWG_DISTRIBUTION_COLUMN = "TDWG";
    private static final String STATUS_COLUMN = "Status";
    private static final String LITERATURE_NUMBER_COLUMN = "Lit.";
    private static final String LITERATURE_COLUMN = "Literature";
    /* not yet used */
//    private static final String VERNACULAR_NAME_COLUMN = "Vernacular";
//    private static final String HABITAT_COLUMN = "Habitat";
//    private static final String CONTROL_COLUMN = "Control";
//    private static final String TRANSLATED_COLUMN = "Translated";
//    private static final String ISO_DISTRIBUTION_COLUMN = "ISO";
//    private static final String NOTES_COLUMN = "Notes";
//    private static final String PAGE_NUMBER_COLUMN = "Page";
//    private static final String INFO_COLUMN = "Info";
    
	private static final Logger logger = Logger.getLogger(DistributionImporter.class);
	
	private CdmApplicationController appCtr = null;
	// Stores already processed descriptions
	Map<Taxon, TaxonDescription> myDescriptions = new HashMap<Taxon, TaxonDescription>();

	@Override
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores) {
		
    	logger.debug("Importing distribution data");
    	
		// read and save all rows of the excel worksheet
    	ArrayList<HashMap<String, String>> recordList = ExcelUtils.parseXLS(config.getSourceNameString());
    	if (recordList != null) {
    		HashMap<String,String> record = null;
    		TransactionStatus txStatus = startTransaction();

    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			analyzeRecord(record);
    		}
    		commitTransaction(txStatus);
    	}
    	
		try {
			logger.debug("End distribution data import"); 
				
		} catch (Exception e) {
    		logger.error("Error closing the application context");
    		e.printStackTrace();
		}
    	
    	return true;
	}
			

	/** 
	 *  Reads the data of one Excel sheet row
	 */
    private void analyzeRecord(HashMap<String,String> record) {
    	/*
    	 * Relevant columns:
    	 * Name (EDIT)
    	 * Distribution TDWG
    	 * Status (only entries if not native) 
    	 * Literature number
    	 * Literature
    	*/
    	
        String editName = "";
        ArrayList<String> distributionList = new ArrayList<String>();
        String status = "";
        String literatureNumber = "";
        String literature = "";
        
    	Set<String> keys = record.keySet();
    	
    	for (String key: keys) {
    		
    		String value = (String) record.get(key);
    		if (!value.equals("")) {
    			logger.debug(key + ": '" + value + "'");
    		}
    		
    		if (key.contains(EDIT_NAME_COLUMN)) {
    			editName = (String) CdmUtils.removeDuplicateWhitespace(value.trim());
    			
			} else if(key.contains(TDWG_DISTRIBUTION_COLUMN)) {
				distributionList =  CdmUtils.buildList(value);
				
			} else if(key.contains(STATUS_COLUMN)) {
				status = (String) CdmUtils.removeDuplicateWhitespace(value.trim());
				
			} else if(key.contains(LITERATURE_NUMBER_COLUMN)) {
				literatureNumber = (String) CdmUtils.removeDuplicateWhitespace(value.trim());
				
			} else if(key.contains(LITERATURE_COLUMN)) {
				literature = (String) CdmUtils.removeDuplicateWhitespace(value.trim());
				
			} else {
				logger.warn("Column " + key + " ignored");
			}
    	}
    	
    	// Store the data of this record in the DB
    	if (!editName.equals("")) {
    		saveRecord(editName, distributionList, status, literatureNumber, literature);
    	}
    }
    
    
	/** 
	 *  Stores the data of one Excel sheet row in the database
	 */
    private void saveRecord(String taxonName, ArrayList<String> distributionList,
    		String status, String literatureNumber, String literature) {

		try {
    		// get the matching names from the DB
    		List<TaxonNameBase<?,?>> taxonNameBases = appCtr.getNameService().findNamesByTitle(taxonName);
    		if (taxonNameBases.isEmpty()) {
    			logger.error("Taxon name '" + taxonName + "' not found in DB");
    		} else {
    			logger.debug("Taxon found");
    		}

    		// get the taxa for the matching names
    		for(TaxonNameBase dbTaxonName: taxonNameBases) {

    			Set<Taxon> taxa = dbTaxonName.getTaxa();
    			if (taxa.isEmpty()) {
    				logger.warn("No taxon found for name '" + taxonName + "'");
    			} else if (taxa.size() > 1) {
    				logger.warn("More than one taxa found for name '" + taxonName + "'");
    			}

    			for(Taxon taxon: taxa) {

    				TaxonDescription myDescription = null;

    				// If we have created a description for this taxon earlier, take this one.
    				// Otherwise, create a new description.
    				// We don't update any existing descriptions in the database at this point.
    				if (myDescriptions.containsKey(taxon)) {
    					myDescription = myDescriptions.get(taxon);
    				} else {
    					myDescription = TaxonDescription.NewInstance(taxon);
    					taxon.addDescription(myDescription);
    					myDescriptions.put(taxon, myDescription);
    				}

    				// Status
    				PresenceAbsenceTermBase<?> presenceAbsenceStatus = PresenceTerm.NewInstance();
    				if (status.equals("")) {
    					presenceAbsenceStatus = PresenceTerm.NATIVE();
    				} else {
    					presenceAbsenceStatus = PresenceTerm.getPresenceTermByAbbreviation(status);
    				}
    				// TODO: Handle absence case. 
    				// This case has not yet occurred in the excel input file, though.
					
    				/* Set to true if taxon needs to be saved if at least one new distribution exists */
    				boolean save = false;
    				
    				// TDWG areas
    				for (String distribution: distributionList) {

                        /* Set to true if this distribution is a new one*/
        				boolean ignore = false;
        				
    					if(!distribution.equals("")) {
    						NamedArea namedArea = TdwgArea.getAreaByTdwgAbbreviation(distribution);
        					TaxonDescription taxonDescription = myDescriptions.get(taxon);
        					if (namedArea != null) {    
    		    				// Check against existing distributions and ignore the ones that occur multiple times
            					Set<DescriptionElementBase> myDescriptionElements = taxonDescription.getElements();
    	    					for(DescriptionElementBase descriptionElement : myDescriptionElements) {
    	    						if (descriptionElement instanceof Distribution) {
    	    							if (namedArea == ((Distribution)descriptionElement).getArea()) {
    	    								ignore = true;
    	    	    						logger.debug("Distribution ignored: " + distribution);
    	    		    					break;
     	    							}
    	    						}
    	    					}
    	    					// Create new distribution if not yet exist
    	    					if (ignore == false) {
    	    						save = true;
    	    						Distribution newDistribution = Distribution.NewInstance(namedArea, presenceAbsenceStatus);
    	    						myDescription.addElement(newDistribution);
    	    						logger.debug("Distribution created: " + newDistribution.toString());
    	    					}
    						}
    					}
    				}
    				if (save == true) {
    					appCtr.getTaxonService().saveTaxon(taxon);
    					logger.debug("Taxon saved");
    				}
    			}
    		} 
    	} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
    	}
    }
    
    
	@Override
	protected boolean doCheck(IImportConfigurator config) {
		boolean result = true;
		logger.warn("No check implemented for distribution data import");
		return result;
	}
	

	@Override
	protected boolean isIgnore(IImportConfigurator config) {
		return false;
	}

}
