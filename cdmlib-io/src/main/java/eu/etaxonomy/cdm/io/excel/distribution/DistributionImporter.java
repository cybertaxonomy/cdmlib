package eu.etaxonomy.cdm.io.excel.distribution;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public class DistributionImporter extends CdmIoBase implements ICdmIO {

    /* used */
    private static String EDIT_NAME_COLUMN = "EDIT";
    private static String TDWG_DISTRIBUTION_COLUMN = "TDWG";
    private static String STATUS_COLUMN = "Status";
    private static String LITERATURE_NUMBER_COLUMN = "Lit.";
    private static String LITERATURE_COLUMN = "Literature";
    /* not yet used */
    private static String VERNACULAR_NAME_COLUMN = "Vernacular";
    private static String HABITAT_COLUMN = "Habitat";
    private static String ISO_DISTRIBUTION_COLUMN = "ISO";
    private static String NOTES_COLUMN = "Notes";
    private static String PAGE_NUMBER_COLUMN = "Page";
    private static String INFO_COLUMN = "Info";
    
    private static String SEPARATOR = ",";

	private static final Logger logger = Logger.getLogger(DistributionImporter.class);
	
	private CdmApplicationController appCtr = null;

	@Override
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores) {
		
    	logger.debug("Importing distribution data");

    	appCtr = config.getCdmAppController();
    	
//    	try {
//			appCtr = 
//				CdmApplicationController.NewInstance(config.getDestination(), DbSchemaValidation.VALIDATE, true);
//
//		} catch (Exception e) {
//			logger.error("Error creating application controller");
//			e.printStackTrace();
//			System.exit(1);
//		}
		
		// read and save all rows of the excel worksheet
    	ArrayList<HashMap<String, String>> recordList = parseXLS(config.getSourceNameString());
    	if (recordList != null) {
    		HashMap<String,String> record = null;
    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			analyzeRecord(config.getDestination(), record);
//    			config.setDbSchemaValidation(DbSchemaValidation.UPDATE);
    		}
    	}
    	
		try {
	    	appCtr.close();
			logger.debug("End test distribution data import"); 
				
		} catch (Exception e) {
    		logger.error("Error clsing the application context");
    		e.printStackTrace();
		}
    	
    	return true;
	}
			

    private void analyzeRecord(ICdmDataSource db, HashMap record) {
    	/*
    	 * Relevant columns:
    	 * Name (EDIT)
    	 * Distribution TDWG
    	 * Status (only entries if not native) 
    	 * Literature number
    	 * Literature
    	*/
    	
        String editName = "";
        String distribution = "";
        ArrayList<String> distributionList = new ArrayList<String>();
        String status = "";
        ArrayList<String> statusList = new ArrayList<String>();
        String literatureNumber = "";
        String literature = "";
        
    	Set<String> keys = record.keySet();
    	
    	for (String key: keys) {
    		
    		String value = (String) record.get(key);
    		if (!value.equals("")) {
    			logger.debug("Key = " + key);
    			logger.debug("Value = " + value);
    		}
    		
    		if (key.contains(EDIT_NAME_COLUMN)) {
    			editName = value;
//            	logger.debug("Name = " + editName);
    			
			} else if(key.contains(TDWG_DISTRIBUTION_COLUMN)) {
				distributionList =  buildList(value);
				
			} else if(key.contains(STATUS_COLUMN)) {
				statusList = buildList(value);
				
			} else if(key.contains(LITERATURE_NUMBER_COLUMN)) {
				literatureNumber = value;
//            	logger.debug("Literature number = " + literatureNumber);
				
			} else if(key.contains(LITERATURE_COLUMN)) {
				literature = value;
//            	logger.debug("Literatur = " + literature);
			}
    	}
    	
    	// Store the data of this record in the DB
    	saveRecord(editName, distributionList, statusList, literatureNumber, literature);
    }
    
    
	/** 
	 *  Stores distribution data in the DB
	 */
    private void saveRecord(String taxonName, ArrayList<String> distributionList,
    		ArrayList<String> statusList, String literatureNumber, String literature) {

		TransactionStatus txStatus = appCtr.startTransaction();

		// Stores already processed descriptions
    	Map<Taxon, TaxonDescription> myDescriptions = new HashMap<Taxon, TaxonDescription>();

		try {
    		// get the matching names from the DB
    		List<TaxonNameBase> taxonNameBases = appCtr.getNameService().getNamesByName(taxonName);
    		if (taxonNameBases.isEmpty()) {
    			logger.error("Taxon name '" + taxonName + "' not found in DB");
    		} else {
    			logger.debug("Taxon found: '" + taxonName + "'");
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

    				// Get the description of this taxon from the database
//  				Set<TaxonDescription> descriptions = taxon.getDescriptions();
//  				if (!descriptions.isEmpty()) {
//  				logger.debug(descriptions.size() + " description(s) found");
//  				}

    				// If we have have created a description for this taxon earlier take this one.
    				// Otherwise, create a new description.
    				if (myDescriptions.containsKey(taxon)) {
    					myDescription = myDescriptions.get(taxon);
    				} else {
    					myDescription = TaxonDescription.NewInstance(taxon);
    					myDescriptions.put(taxon, myDescription);
    					taxon.addDescription(myDescription);
    				}

    				//status
    				PresenceAbsenceTermBase<?> status = PresenceTerm.NATIVE();
					
    				// Add the named areas
    				for (String distribution: distributionList) {

    					NamedArea namedArea = TdwgArea.getAreaByTdwgAbbreviation(distribution);
    					Distribution descDist = Distribution.NewInstance(namedArea, status);
    					myDescription.addElement(descDist);
    				}
    				
    				appCtr.getTaxonService().saveTaxon(taxon);
    	    		logger.debug("taxon saved");
    			}
    		} 
    		appCtr.commitTransaction(txStatus);
    		
    	} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
    	}
    }
    
    
    private ArrayList<String> buildList(String value) {
    	
    	ArrayList<String> resultList = new ArrayList<String>();
    	StringTokenizer st = new StringTokenizer(value, SEPARATOR);
        while (st.hasMoreTokens()) {
        	String listElement = st.nextToken();
            resultList.add(listElement);
//        	logger.debug("Next token = " + listElement);
        }
        return resultList;
    }
    

    /** Reads all rows of an Excel worksheet */
    private static ArrayList<HashMap<String, String>> parseXLS(String fileName) {
    	
    	ArrayList<HashMap<String, String>> recordList = new ArrayList<HashMap<String, String>>();

    	try {
    		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(fileName));
    		HSSFWorkbook wb = new HSSFWorkbook(fs);
    		HSSFSheet sheet = wb.getSheetAt(0);
    		HSSFRow row;
    		HSSFCell cell;

    		int rows; // No of rows
    		rows = sheet.getPhysicalNumberOfRows();

    		int cols = 0; // No of columns
    		int tmp = 0;

    		// This trick ensures that we get the data properly even if it doesn't start from first few rows
    		for(int i = 0; i < 10 || i < rows; i++) {
    			row = sheet.getRow(i);
    			if(row != null) {
    				tmp = sheet.getRow(i).getPhysicalNumberOfCells();
    				if(tmp > cols) cols = tmp;
    			}
    		}
    		HashMap<String, String> headers = null;
    		ArrayList<String> columns = new ArrayList<String>();
    		row = sheet.getRow(0);
    		for (int c = 0; c < cols; c++){
    			cell = row.getCell(c);
    			columns.add(cell.toString());
    		}
    		for(int r = 1; r < rows; r++) {
    			row = sheet.getRow(r);
    			headers = new HashMap<String, String>();
    			if(row != null) {
    				for(int c = 0; c < cols; c++) {
    					cell = row.getCell((short)c);
    					if(cell != null) {
    						headers.put(columns.get(c), cell.toString());
    					}
    				}
    			}
    			recordList.add(headers);
    		}

    	} catch(Exception ioe) {
    		ioe.printStackTrace();
    	}
    	return recordList;
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
