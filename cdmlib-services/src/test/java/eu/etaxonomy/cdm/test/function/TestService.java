/* just for testing */


package eu.etaxonomy.cdm.test.function;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;



public class TestService {
	private static final UUID TEST_TAXON_UUID = UUID.fromString("b3084573-343d-4279-ba92-4ab01bb47db5");
	static Logger logger = Logger.getLogger(TestService.class);
	private static CdmApplicationController appCtr;
	
	
	public void testAppController() {
		logger.info("Create name objects...");
		NonViralName nvn = new NonViralName(Rank.SPECIES());
 		
		BotanicalName bn = new BotanicalName(Rank.SUBSPECIES());
		ZoologicalName zn = new ZoologicalName(Rank.FAMILY());
		
		logger.info("Create reference objects...");
		ReferenceBase sec = new Journal();
		sec.setTitleCache("TestJournal");
		
		logger.info("Create taxon objects...");
		Taxon childTaxon = Taxon.NewInstance(nvn, sec);
		Synonym syn = Synonym.NewInstance(bn, sec);
		childTaxon.addSynonym(syn, SynonymRelationshipType.SYNONYM_OF());
 		appCtr.getTaxonService().saveTaxon(childTaxon);

 		
 		Taxon parentTaxon = Taxon.NewInstance(zn, sec);
		parentTaxon.setUuid(TEST_TAXON_UUID);
		parentTaxon.addTaxonomicChild(childTaxon, sec, null);
		
		
		// test 
		nvn.setGenusOrUninomial("Nonvirala");
		bn.setGenusOrUninomial("Abies");
		
		logger.info("Create new Author agent...");
		Person team= new Person();
		team.setTitleCache("AuthorAgent1");
		nvn.setCombinationAuthorTeam(team);
		
		logger.info("Save objects ...");
 		appCtr.getTaxonService().saveTaxon(parentTaxon);
		
		// load Name list 
		logger.info("Load existing names from db...");
		List<TaxonNameBase> tnList = appCtr.getNameService().getAllNames(1000, 0);
		for (TaxonNameBase tn2: tnList){
			logger.info("Title: "+ tn2.getTitleCache() + " UUID: " + tn2.getUuid()+";");
		}
	}

	public void testRootTaxa(){
		// load Name list 
		logger.info("Load existing names from db...");
		List<TaxonNameBase> tnList = appCtr.getNameService().getAllNames(1000, 0);
		for (TaxonNameBase tn2: tnList){
			logger.info("Title: "+ tn2.getTitleCache() + " UUID: " + tn2.getUuid()+";");
		}
		
		// load Root taxa 
		logger.info("Load taxon from db...");
		List<Taxon> taxa = appCtr.getTaxonService().getRootTaxa(null);
		for (Taxon rt: taxa){
			logger.info("Root taxon: "+ rt.toString());
			for (Taxon child: rt){
				logger.info("Child: "+ child.toString());
				logger.info("  Child.higherTaxon: "+ child.getTaxonomicParent().toString());
				for (Synonym synonym: child.getSynonyms()){
					logger.info("  Child synonyms: "+ synonym.toString());
				}
			}
		}
	}

	public void testTermApi(){
		ITermService ts = (ITermService)appCtr.getTermService();
		//DefinedTermBase dt = ts.getTermByUri("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		//logger.warn(dt.toString());
		//TODO: fix ts.listTerms(0,100)
//		List<DefinedTermBase> dts = ts.listTerms(0,100); 
//		int i = 0;
//		for (DefinedTermBase d: dts){
//			i++;
//			if (i > 10) break;
//			logger.info(d.toString());
//		}
	}
	
	public void testDeleteTaxa(){
		ITaxonService taxonService = (ITaxonService)appCtr.getTaxonService();
		TaxonNameBase taxonName = new BotanicalName(Rank.SPECIES());
		ReferenceBase ref = new Journal();
		Taxon taxon1 = Taxon.NewInstance(taxonName, ref);
		Taxon taxon2 = Taxon.NewInstance(taxonName, null);
		logger.info("Save taxon ...");
		UUID uuidTaxon1 = taxonService.saveTaxon(taxon1);
		logger.info("  UUID: " + uuidTaxon1);
		UUID uuidTaxon2 = taxonService.saveTaxon(taxon2);
		logger.info("  UUID: " + uuidTaxon2);
		logger.info("Remove taxon ...");
		UUID uuid = taxonService.removeTaxon(taxon1);
		logger.info("  UUID: " + uuid);
	}

	public void testDeleteRelationship(){
		ITaxonService taxonService = (ITaxonService)appCtr.getTaxonService();
		TaxonNameBase taxonName = new BotanicalName(Rank.SPECIES());
		ReferenceBase ref = new Journal();
		Taxon parent = Taxon.NewInstance(taxonName, ref);
		Taxon child = Taxon.NewInstance(taxonName, null);
		parent.addTaxonomicChild(child, null, null);
		
		logger.info("Save taxon ...");
		UUID uuidTaxon1 = taxonService.saveTaxon(parent);
		logger.info("  UUID: " + uuidTaxon1);
		UUID uuidTaxon2 = taxonService.saveTaxon(child);
		logger.info("  UUID: " + uuidTaxon2);
		
		
//		Set<TaxonRelationship> set = parent.getRelationsToThisTaxon();
//		for (TaxonRelationship rel : set){
//			if (rel.getType().equals(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN())){
//				parent.removeTaxonRelation(rel);
//			}
//		}
		
	}

	public void regenerateTaxonTitleCache(){
		ITaxonService taxonService = (ITaxonService)appCtr.getTaxonService();
		taxonService.generateTitleCache();
	}
	
	private void test(){
		System.out.println("Start ...");
    	//testAppController();
		//testRootTaxa();
		//testTermApi();
		//testDeleteTaxa();
		//testDeleteRelationship();
		regenerateTaxonTitleCache();
    	System.out.println("\nEnd ...");
	}
	
	private void init(){
		try {
			//appCtr = new CdmApplicationController(CdmDataSource.NewInstance("defaultMySql"), HBM2DDL.CREATE);
			appCtr = CdmApplicationController.NewInstance(CdmPersistentDataSource.NewInstance("rel1_1"));
			//appCtr = new CdmApplicationController(HBM2DDL.CREATE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestService sc = new TestService();
		sc.init();
    	sc.test();
		appCtr.close();
	}

}
