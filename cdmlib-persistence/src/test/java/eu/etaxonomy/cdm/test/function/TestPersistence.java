/* just for testing */
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class TestPersistence {
	private static final UUID TEST_TAXON_UUID = UUID.fromString("b3084573-343d-4279-ba92-4ab01bb47db5");
	static Logger logger = Logger.getLogger(TestPersistence.class);


	public void testAppController(){

		logger.info("Create name objects...");
		NonViralName<?> tn = NonViralName.NewInstance(Rank.SPECIES());
		BotanicalName tn3 = BotanicalName.NewInstance(Rank.SUBSPECIES());
		ZoologicalName parentName = ZoologicalName.NewInstance(Rank.FAMILY());

		logger.info("Create reference objects...");
		Reference sec = ReferenceFactory.newJournal();
		sec.setTitleCache("TestJournal", true);

		logger.info("Create taxon objects...");
		Taxon childTaxon = Taxon.NewInstance(tn, sec);
		Synonym syn = Synonym.NewInstance(tn3, sec);
		childTaxon.addSynonym(syn, SynonymRelationshipType.SYNONYM_OF());
		Taxon parentTaxon = Taxon.NewInstance(parentName, sec);
		parentTaxon.setUuid(TEST_TAXON_UUID);
		parentTaxon.addTaxonomicChild(childTaxon, sec, null);


		// test
		tn.setGenusOrUninomial("tn1-Genus1");
		tn3.setGenusOrUninomial("tn3-genus");

		logger.info("Create new Author agent...");
		Person team= Person.NewInstance();
		team.setTitleCache("AuthorAgent1", true);
		tn.setCombinationAuthorship(team);

		logger.info("Save objects ...");

//		appCtr.getTaxonService().saveTaxon(parentTaxon);
//
//		// load Name list
//		logger.info("Load existing names from db...");
//		List<TaxonNameBase> tnList = appCtr.getNameService().getAllNames(1000, 0);
//		for (TaxonNameBase tn2: tnList){
//			logger.info("Title: "+ tn2.getTitleCache() + " UUID: " + tn2.getUuid()+";");
//		}
//
//		// load Name list
//		logger.info("Load taxon from db...");
//		Taxon taxon = (Taxon)appCtr.getTaxonService().getTaxonByUuid(parentTaxon.getUuid());
//		logger.info("Parent: "+ taxon.toString());
//		for (Taxon child: taxon.getTaxonomicChildren()){
//			logger.info("Child: "+ child.toString());
//			for (Synonym synonym: child.getSynonyms()){
//				logger.info("Synonym: "+ synonym.toString());
//			}
//		}
//
//		// close
//		appCtr.close();
	}




	private void test(){
		System.out.println("Start ...");
		TestPersistence sc = new TestPersistence();
    	sc.testAppController();
    	System.out.println("\nEnd ...");
	}

	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestPersistence sc = new TestPersistence();
    	sc.test();
	}

}
