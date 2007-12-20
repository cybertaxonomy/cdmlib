/* just for testing */


package eu.etaxonomy.cdm.test.function;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;




public class SpringControl {
	private static final UUID TEST_TAXON_UUID = UUID.fromString("b3084573-343d-4279-ba92-4ab01bb47db5");
	static Logger logger = Logger.getLogger(SpringControl.class);
	
	
	public void testAppController(){
		CdmApplicationController appCtr = new CdmApplicationController();
		
		logger.info("Create name objects...");
		NonViralName tn = new NonViralName(Rank.SPECIES());
		BotanicalName tn3 = new BotanicalName(Rank.SUBSPECIES());
		ZoologicalName parentName = new ZoologicalName(Rank.FAMILY());
		
		logger.info("Create reference objects...");
		ReferenceBase sec = new Journal();
		sec.setTitleCache("TestJournal");
		
		logger.info("Create taxon objects...");
		Taxon childTaxon = Taxon.NewInstance(tn, sec);
		Synonym syn = Synonym.NewInstance(tn3, sec);
		childTaxon.addSynonym(syn, SynonymRelationshipType.SYNONYM_OF());
		Taxon parentTaxon = Taxon.NewInstance(parentName, sec);
		parentTaxon.setUuid(TEST_TAXON_UUID);
		parentTaxon.addTaxonomicChild(childTaxon, sec, null);
		
		// setup listeners
		PropertyChangeTest listener = new PropertyChangeTest();
		tn.addPropertyChangeListener(listener);
		tn3.addPropertyChangeListener(listener);

		// test listeners
		tn.setUninomial("tn1-Genus1");
		tn3.setUninomial("tn3-genus");
		tn3.getUninomial();
		
		logger.info("Create new Author agent...");
		Person team= new Person();
		team.addPropertyChangeListener(listener);
		team.setTitleCache("AuthorAgent1");
		tn.setCombinationAuthorTeam(team);
		
		logger.info("Save objects ...");
 		appCtr.getTaxonService().saveTaxon(parentTaxon);
		
		// load Name list 
		logger.info("Load existing names from db...");
		List<TaxonNameBase> tnList = appCtr.getNameService().getAllNames(1000, 0);
		for (TaxonNameBase tn2: tnList){
			logger.info("Title: "+ tn2.getTitleCache() + " UUID: " + tn2.getUuid()+";");
		}
		
		// load Name list 
		logger.info("Load taxon from db...");
		Taxon taxon = (Taxon)appCtr.getTaxonService().getTaxonByUuid(parentTaxon.getUuid());
		logger.info("Parent: "+ taxon.toString());
		for (Taxon child: taxon.getTaxonomicChildren()){
			logger.info("Child: "+ child.toString());
			for (Synonym synonym: child.getSynonyms()){
				logger.info("Synonym: "+ synonym.toString());
			}
		}
		
		// close 
		appCtr.close();
	}

	public void testRootTaxa(){
		
		CdmApplicationController appCtr = new CdmApplicationController();
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
		
		// close 
		appCtr.close();
		
	}
	public void testTermApi(){
		CdmApplicationController appCtr = new CdmApplicationController();
		ITermService ts = (ITermService)appCtr.getTermService();
		//DefinedTermBase dt = ts.getTermByUri("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		//logger.warn(dt.toString());
		List<DefinedTermBase> dts = ts.listTerms(); 
		int i = 0;
		for (DefinedTermBase d: dts){
			i++;
			if (i > 10) break;
			logger.info(d.toString());
		}
	}

	private void test(){
		System.out.println("Start");
		SpringControl sc = new SpringControl();
    	//testTermApi();
    	//testAppController();
		testRootTaxa();
    	System.out.println("\nEnd");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		SpringControl sc = new SpringControl();
    	sc.test();
	}

}
