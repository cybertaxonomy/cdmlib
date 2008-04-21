package eu.etaxonomy.cdm.test.function;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class TestTaxonFunction {
	private static final Logger logger = Logger.getLogger(TestTaxonFunction.class);

	private CdmApplicationController getCdmApplicationController(String strDataSource, DbSchemaValidation hbm2dll){
		CdmApplicationController cdmApp= null;
		try {
			CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewInstance(strDataSource);
			cdmApp = CdmApplicationController.NewInstance(dataSource, hbm2dll);
		} catch (DataSourceNotFoundException e) {
			e.printStackTrace();
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
		return cdmApp;
		
	}
	
	private UUID getRefUuid(){
		return UUID.fromString("5d5363e2-f560-4da2-857d-dfa344b9f5ae");
	}

	private void initDatabase(){
		DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
		CdmApplicationController cdmApp = getCdmApplicationController("defaultMySql", hbm2dll);
			
		INameService nameService = cdmApp.getNameService();
		
		BotanicalName botanicalName = BotanicalName.NewInstance(Rank.GENUS());
		botanicalName.setTitleCache("Hieracium L.");
		botanicalName.setGenusOrUninomial("Hieracium");
//		botanicalName.setUninomial("Hieracium");
		botanicalName.setCombinationAuthorTeam(Person.NewInstance());
		botanicalName.getCombinationAuthorTeam().setNomenclaturalTitle("L.");
		Taxon genusTaxon = new Taxon();
		genusTaxon.setName(botanicalName);
		ReferenceBase sec = new Book();
		sec.setUuid(getRefUuid());
		genusTaxon.setSec(sec);
				
		BotanicalName botSpecies = BotanicalName.NewInstance(Rank.SPECIES());
		botSpecies.setTitleCache("Hieracium asturianum Pau");
		botSpecies.setGenusOrUninomial("Hieracium");
//		botSpecies.setUninomial("Hieracium");
		botSpecies.setSpecificEpithet("asturianum");
		botSpecies.setCombinationAuthorTeam(Person.NewInstance());
		botSpecies.getCombinationAuthorTeam().setNomenclaturalTitle("Pau");
		Taxon childTaxon = new Taxon();
		childTaxon.setName(botSpecies);
		childTaxon.setSec(null);
		childTaxon.setTaxonomicParent(genusTaxon, null, null);

		BotanicalName botSpecies2= BotanicalName.NewInstance(Rank.SPECIES());
		botSpecies2.setTitleCache("Hieracium wolffii Zahn");
		botSpecies2.setGenusOrUninomial("Hieracium");
//		botSpecies2.setUninomial("Hieracium");
		botSpecies2.setSpecificEpithet("wolffii");
		botSpecies2.setCombinationAuthorTeam(Person.NewInstance());
		botSpecies2.getCombinationAuthorTeam().setNomenclaturalTitle("Zahn");
		Taxon childTaxon2 = new Taxon();
		childTaxon2.setName(botSpecies2);
		childTaxon2.setSec(null);
		childTaxon2.setTaxonomicParent(genusTaxon, null, null);
		
		cdmApp.getTaxonService().saveTaxon(genusTaxon);
		cdmApp.close();

	}
	
	private boolean testHasTaxonomicChild(){
		if (false){
			initDatabase();
		}
		CdmApplicationController cdmApp = getCdmApplicationController("defaultMySql", DbSchemaValidation.VALIDATE);
		ReferenceBase sec = cdmApp.getReferenceService().getReferenceByUuid(getRefUuid());
		List<Taxon> rootList = cdmApp.getTaxonService().getRootTaxa(sec);
		for (Taxon taxon:rootList){
			System.out.println(taxon);
			taxon.hasTaxonomicChildren();
		}
		return true;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestTaxonFunction testClass = new TestTaxonFunction();
		testClass.testHasTaxonomicChild();
		System.out.println("End");
	}
	
}
