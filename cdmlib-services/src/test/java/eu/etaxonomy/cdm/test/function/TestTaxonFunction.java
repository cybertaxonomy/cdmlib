/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Ignore;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * <h2>NOTE</h2>
 * This is a test for sole development purposes, it is not
 * touched by mvn test since it is not matching the "\/**\/*Test" pattern,
 * but it should be annotate with @Ignore when running the project a s junit suite in eclipse
 *
 */
@Ignore
public class TestTaxonFunction {
	private static final Logger logger = Logger.getLogger(TestTaxonFunction.class);

	private CdmApplicationController getCdmApplicationController(String strDataSource, DbSchemaValidation hbm2dll){
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
		logger.info("init Database start ...");
		DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
		CdmApplicationController cdmApp = getCdmApplicationController("defaultMySql", hbm2dll);

		INonViralNameParser<?> parser = NonViralNameParserImpl.NewInstance();
		Reference sec = ReferenceFactory.newBook();
		sec.setTitleCache("ConceptRef", true);

		//root
		String rootName = "Hieracium L.";
		TaxonNameBase<?,?> botanicalName= parser.parseFullName(rootName);
		sec.setUuid(getRefUuid());
		Taxon genusTaxon = Taxon.NewInstance(botanicalName, sec);

		//child1
		String child1Name = "Hieracium asturianum Pau";
		TaxonNameBase<?,?> botSpecies= parser.parseFullName(child1Name);
		Taxon childTaxon = Taxon.NewInstance(botSpecies, sec);
		childTaxon.setTaxonomicParent(genusTaxon, null, null);

		//child2
		String child2Name = "Hieracium wolffii Zahn";
		TaxonNameBase<?,?> botSpecies2= parser.parseFullName(child2Name);
		Taxon childTaxon2 = Taxon.NewInstance(botSpecies2, sec);
		childTaxon2.setTaxonomicParent(childTaxon, null, null);

		//synonym
		String synonymName = "Acacium wolffii Syn.";
		TaxonNameBase<?,?> botSynName= parser.parseFullName(synonymName);

		Synonym synTaxon = Synonym.NewInstance(botSynName, sec);
		childTaxon2.addSynonym(synTaxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		//save
		cdmApp.getTaxonService().save(genusTaxon);

		//other taxon
		BotanicalName otherName = BotanicalName.NewInstance(Rank.GENUS());
		otherName.setTitleCache("otherName", true);
		Taxon otherTaxon = Taxon.NewInstance(otherName, sec);

		cdmApp.getTaxonService().save(otherTaxon);
		cdmApp.close();
		logger.info("init Database end ...");
	}

	private boolean testHasTaxonomicChild(){
		logger.info("testHasTaxonomicChild start ...");
		if (false){
			initDatabase();
		}
		CdmApplicationController cdmApp = getCdmApplicationController("defaultMySql", DbSchemaValidation.VALIDATE);
		Reference sec = cdmApp.getReferenceService().find(getRefUuid());
		List<Taxon> rootList = cdmApp.getTaxonService().getRootTaxa(sec, CdmFetch.NO_FETCH(), false);
		for (Taxon taxon:rootList){
			System.out.println(taxon);
			//taxon.getT
			taxon.hasTaxonomicChildren();
			Taxon child = taxon.getTaxonomicChildren().iterator().next();
			logger.warn("Child has children: " + child.hasTaxonomicChildren());
			Taxon child2 = child.getTaxonomicChildren().iterator().next();
			logger.warn("Child2 has children: " + child2.hasTaxonomicChildren());
			logger.warn("Child2 has synonym: " + child2.hasSynonyms());
			Synonym syn = child2.getSynonyms().iterator().next();
			logger.warn("Syn has reference: " + syn.getSec());
		}
		logger.info("testHasTaxonomicChild end . ..");
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
