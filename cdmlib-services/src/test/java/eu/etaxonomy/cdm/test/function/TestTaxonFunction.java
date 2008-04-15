package eu.etaxonomy.cdm.test.function;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class TestTaxonFunction {

	private CdmApplicationController getCdmApplicationController(String strDataSource, HBM2DDL hbm2dll){
		CdmApplicationController cdmApp= null;
		try {
			CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewInstance(strDataSource);
			cdmApp = CdmApplicationController.NewInstance(dataSource, hbm2dll);
		} catch (DataSourceNotFoundException e) {
			e.printStackTrace();
		}
		return cdmApp;
		
	}
	
	private UUID getRefUuid(){
		return UUID.fromString("5d5363e2-f560-4da2-857d-dfa344b9f5ae");
	}

	private void initDatabase(){
		HBM2DDL hbm2dll = CdmPersistentDataSource.HBM2DDL.CREATE;
		CdmApplicationController cdmApp = getCdmApplicationController("defaultMySql", hbm2dll);
			
		INameService nameService = cdmApp.getNameService();
		
		BotanicalName botanicalName = new BotanicalName(Rank.GENUS());
		botanicalName.setTitleCache("Hieracium L.");
		botanicalName.setGenusOrUninomial("Hieracium");
//		botanicalName.setUninomial("Hieracium");
		botanicalName.setCombinationAuthorTeam(new Person());
		botanicalName.getCombinationAuthorTeam().setTitleCache("L.");
		Taxon genusTaxon = new Taxon();
		genusTaxon.setName(botanicalName);
		ReferenceBase sec = new Book();
		sec.setUuid(getRefUuid());
		genusTaxon.setSec(sec);
				
		BotanicalName botSpecies = new BotanicalName(Rank.SPECIES());
		botSpecies.setTitleCache("Hieracium asturianum Pau");
		botSpecies.setGenusOrUninomial("Hieracium");
//		botSpecies.setUninomial("Hieracium");
		botSpecies.setSpecificEpithet("asturianum");
		botSpecies.setCombinationAuthorTeam(new Person());
		botSpecies.getCombinationAuthorTeam().setTitleCache("Pau");
		Taxon childTaxon = new Taxon();
		childTaxon.setName(botSpecies);
		childTaxon.setSec(null);
		childTaxon.setTaxonomicParent(genusTaxon, null, null);

		BotanicalName botSpecies2= new BotanicalName(Rank.SPECIES());
		botSpecies2.setTitleCache("Hieracium wolffii Zahn");
		botSpecies2.setGenusOrUninomial("Hieracium");
//		botSpecies2.setUninomial("Hieracium");
		botSpecies2.setSpecificEpithet("wolffii");
		botSpecies2.setCombinationAuthorTeam(new Person());
		botSpecies2.getCombinationAuthorTeam().setTitleCache("Zahn");
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
		CdmApplicationController cdmApp = getCdmApplicationController("defaultMySql", HBM2DDL.VALIDATE);
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
