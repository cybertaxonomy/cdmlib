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

import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class TestModel {
	private static final UUID TEST_TAXON_UUID = UUID.fromString("b3084573-343d-4279-ba92-4ab01bb47db5");
	static Logger logger = Logger.getLogger(TestModel.class);

	public void testSomething(){

		logger.info("Create name objects...");
		logger.info(NomenclaturalStatusType.NUDUM().getRepresentation(Language.LATIN()).getAbbreviatedLabel());
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

		// setup listeners
		PropertyChangeTest listener = new PropertyChangeTest();
		tn.addPropertyChangeListener(listener);
		tn3.addPropertyChangeListener(listener);

		// test listeners
		tn.setGenusOrUninomial("tn1-Genus1");
		tn3.setGenusOrUninomial("tn3-genus");
		tn3.getGenusOrUninomial();

		logger.info("Create new Author agent...");
		Person team= Person.NewInstance();
		team.addPropertyChangeListener(listener);
		team.setTitleCache("AuthorAgent1", true);
		tn.setCombinationAuthorship(team);
	}

	public void testParentRelation(){
		TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(Rank.SPECIES());
		Reference ref = ReferenceFactory.newJournal();
		Taxon parent = Taxon.NewInstance(taxonName, ref);
		Taxon child = Taxon.NewInstance(taxonName, null);
		parent.addTaxonomicChild(child, null, null);
		if (child.getTaxonomicParent() != parent){
			logger.warn("Error");
		}
	}

	public void testDescription(){
		Reference ref = ReferenceFactory.newJournal();
		Taxon taxon = Taxon.NewInstance(null, ref);
		TaxonDescription desc = TaxonDescription.NewInstance();
		taxon.addDescription(desc);
		taxon.removeDescription(desc);
	}

	public void testTDWG(){
//		NamedArea tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("GER");
//		NamedArea tdwgArea2 = TdwgArea.getAreaByTdwgLabel("Qatar");
//		System.out.println(tdwgArea.getLabel());
//		System.out.println(tdwgArea2.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel());
	}

	private void test(){
		System.out.println("Start ...");
		TestModel sc = new TestModel();
		//sc.testSomething();
		//sc.testParentRelation();
		//sc.testDescription();
		sc.testTDWG();
		System.out.println("\nEnd ...");
	}

	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestModel sc = new TestModel();
		sc.test();
	}

}
