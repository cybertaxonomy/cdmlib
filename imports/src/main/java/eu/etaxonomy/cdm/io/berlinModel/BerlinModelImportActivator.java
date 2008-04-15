package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelImport;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelSources;
import eu.etaxonomy.cdm.io.source.Source;


/**
 * @author a.mueller
 *
 */
public class BerlinModelImportActivator {
	private static Logger logger = Logger.getLogger(BerlinModelImportActivator.class);

	//database validation status (create, update, validate ...)
	static HBM2DDL hbm2dll = HBM2DDL.CREATE;
	static final Source berlinModelSource = BerlinModelSources.editWP6();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_1_1();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import cichorieae from BerlinModel ("+ berlinModelSource.getDatabase() + ") ...");
		CdmApplicationController cdmApp;
		
		//make BerlinModel Source
		Source source = berlinModelSource;
		ICdmDataSource destination = cdmDestination;
		
		BerlinModelImportConfigurator bmImportConfigurator = BerlinModelImportConfigurator.NewInstance(source,  destination);
		bmImportConfigurator.setDoNameStatus(false);
//		bmImportConfigurator.setDoTaxa(false);
		bmImportConfigurator.setDoFacts(false);
//		bmImportConfigurator.setDoRelNames(false);
		bmImportConfigurator.setHbm2dll(HBM2DDL.CREATE);
		
		// invoke import
		BerlinModelImport bmImport = new BerlinModelImport();
		bmImport.doImport(bmImportConfigurator);

		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ")...");
	}
	
	

	
	

}
