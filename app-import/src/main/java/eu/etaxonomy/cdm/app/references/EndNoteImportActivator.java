package eu.etaxonomy.cdm.app.references;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.reference.endnote.in.EndnoteImportConfigurator;

public class EndNoteImportActivator {
	/* SerializeFrom DB **/
	//private static final ICdmDataSource cdmSource = CdmDestinations.localH2Diptera();
	private static final ICdmDataSource cdmDestination = CdmDestinations.localH2Diptera();
	
	// Import:
	private static String importFileName =	"file:/C:/EndNoteTest.xml";
	

	

	private static final Logger logger = Logger.getLogger(EndNoteImportActivator.class);

	
	public static String chooseFile(String[] args) {
		if(args == null)
			return null;
		for (String dest: args){
			if (dest.endsWith(".xml")){
				return args[0];
			}
		}
		return null;
	}

	private void invokeImport(String importFileParam, ICdmDataSource destination) {
		EndnoteImportConfigurator endNoteImportConfigurator;
		if (importFileParam !=null && destination != null){
			endNoteImportConfigurator = EndnoteImportConfigurator.NewInstance(importFileParam, destination);
		}else if (destination != null){			
			endNoteImportConfigurator = EndnoteImportConfigurator.NewInstance(importFileName, destination);
		} else if (importFileParam !=null ){
			endNoteImportConfigurator = EndnoteImportConfigurator.NewInstance(importFileParam, cdmDestination);
		} else{
			endNoteImportConfigurator = EndnoteImportConfigurator.NewInstance(importFileName, cdmDestination);
		}
		
		CdmDefaultImport<EndnoteImportConfigurator> endNoteImport = 
			new CdmDefaultImport<EndnoteImportConfigurator>();


		// invoke import
		logger.debug("Invoking Jaxb import");

		endNoteImport.invoke(endNoteImportConfigurator, destination, true);

	}

	
	private CdmApplicationController initDb(ICdmDataSource db) {

		// Init source DB
		CdmApplicationController appCtrInit = null;

		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.VALIDATE, true);

		return appCtrInit;
	}

	
	// Load test data to DB
	private void loadTestData(CdmApplicationController appCtrInit) {

		TestDatabase.loadTestData("", appCtrInit);
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		EndNoteImportActivator sc = new EndNoteImportActivator();
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		String file = chooseFile(args)!= null ? chooseFile(args) : importFileName;
		CdmApplicationController appCtr = null;
		appCtr = sc.initDb(destination);
		//sc.loadTestData(appCtr);
				
		sc.invokeImport(file, destination);
	}

}
