/* just for testing */

package eu.etaxonomy.cdm.test.function;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class TestDatabase {
	static Logger logger = Logger.getLogger(TestDatabase.class);
	
	
	public void testNewDatabaseConnection(){
		try {
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(DbSchemaValidation.CREATE);
			IDatabaseService dbService = appCtr.getDatabaseService();
			INameService nameService = appCtr.getNameService();
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	public void testNewDatasourceClass(){
		try {
			String server = "192.168.2.10";
			String database = "cdm_1_1";
			String username = "edit";
			String password = CdmUtils.readInputLine("Password: ");
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, database, username, password);
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, DbSchemaValidation.CREATE);
			
			Rank genus = Rank.GENUS();
			BotanicalName botanicalName = BotanicalName.NewInstance(genus);
			botanicalName.setGenusOrUninomial("GenusName");
		
			Journal journal = new Journal();
			journal.setTitle("JournalTitel");
			
			//			Taxon taxon = Taxon.NewInstance(botanicalName, journal);
//			Taxon taxon2 = Taxon.NewInstance(botanicalName2, null);
	//		botanicalName.getTitleCache();
			Rank.SPECIES();
			Taxon taxon1 = Taxon.NewInstance(botanicalName,journal);
			appCtr.getTaxonService().saveTaxon(taxon1);
			BotanicalName homotypName = BotanicalName.NewInstance(Rank.SUBGENUS(), botanicalName.getHomotypicalGroup());
			homotypName.setGenusOrUninomial("Subgenus");
			homotypName.setInfraGenericEpithet("homotyp");
			
			Synonym synonym = Synonym.NewInstance(homotypName, journal);
			
			System.out.println("Taxa of " + botanicalName + ": " + botanicalName.getTaxonBases());
			
			HomotypicalGroup homotypicalGroup = taxon1.getHomotypicGroup();
			System.out.println("HomotypicNames of " + botanicalName + ":" + homotypicalGroup.getTypifiedNames());
			
//			appCtr.getTaxonService().saveTaxon(taxon2);
//			appCtr.getTaxonService().saveTaxon(taxon);
			
			IDatabaseService dbService = appCtr.getDatabaseService();
			INameService nameService = appCtr.getNameService();
			appCtr.close();

		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	public void testPaddie(){
		 UUID taxonUUID;
		 boolean isInitialized;
		

		try {
			String server = "PADDIE";
			String database = "edit_test";
			String username = "andreas";
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation validation = DbSchemaValidation.VALIDATE;
			ICdmDataSource datasource = CdmDataSource.NewSqlServer2005Instance(server, database, username, password);
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, validation);
			
			Rank genus = Rank.GENUS();
			BotanicalName botanicalName = BotanicalName.NewInstance(genus);
			botanicalName.setGenusOrUninomial("GenusName");
		
			Journal journal = new Journal();
			journal.setTitle("JournalTitel");
			
			//			Taxon taxon = Taxon.NewInstance(botanicalName, journal);
//			Taxon taxon2 = Taxon.NewInstance(botanicalName2, null);
	//		botanicalName.getTitleCache();
			Rank.SPECIES();
			appCtr.getNameService().saveTaxonName(botanicalName);

//			appCtr.getTaxonService().saveTaxon(taxon2);
//			appCtr.getTaxonService().saveTaxon(taxon);
			
			IDatabaseService dbService = appCtr.getDatabaseService();
			INameService nameService = appCtr.getNameService();
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	
	private void test(){
		System.out.println("Start TestDatabase");
		//testNewDatabaseConnection();
		testNewDatasourceClass();
	//	testPaddie();
		System.out.println("\nEnd TestDatabase");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestDatabase sc = new TestDatabase();
    	sc.test();
	}

}
