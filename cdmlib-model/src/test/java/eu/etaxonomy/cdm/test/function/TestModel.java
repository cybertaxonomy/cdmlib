/* just for testing */


package eu.etaxonomy.cdm.test.function;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

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




public class TestModel {
	private static final UUID TEST_TAXON_UUID = UUID.fromString("b3084573-343d-4279-ba92-4ab01bb47db5");
	static Logger logger = Logger.getLogger(TestModel.class);
	
	
	public void testAppController(){
		
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
	}

	private void test(){
		System.out.println("Start");
		TestModel sc = new TestModel();
    	//testTermApi();
    	testAppController();
		//testRootTaxa();
    	System.out.println("\nEnd");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestModel sc = new TestModel();
    	sc.test();
	}

}
