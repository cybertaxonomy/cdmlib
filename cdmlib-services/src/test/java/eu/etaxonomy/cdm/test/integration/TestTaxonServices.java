/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.test.integration;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.babadshanjan
 * @created 26.11.2008
 */
public class TestTaxonServices {

	private static String dbName = "cdm_test_anahit";

	private static final String server = "192.168.2.10";
	private static final String username = "edit";
	private static final Logger logger = Logger.getLogger(TestTaxonServices.class);


	public static ICdmDataSource CDM_DB(String dbname) {

		logger.info("Setting DB " + dbname);
		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
		return datasource;
	}

	private static final ICdmDataSource db = TestTaxonServices.CDM_DB(dbName);

	private static CdmApplicationController
    getCdmApplicationController(
    		ICdmDataSource db, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) {

		CdmApplicationController appCtr = CdmApplicationController.NewInstance(db, dbSchemaValidation, omitTermLoading);

		return appCtr;
    }


	private void testMakeTaxonSynonym(CdmApplicationController appCtr) {

//		logger.info("Testing makeTaxonSynonym()");
//		TransactionStatus txStatus = appCtr.startTransaction();
//
//		Taxon oldTaxon = (Taxon)appCtr.getTaxonService().find(UUID.fromString("83a87f0c-e2c4-4b41-b603-4e77e7e53158"));
//		Taxon newAcceptedTaxon = (Taxon)appCtr.getTaxonService().find(UUID.fromString("0b423190-fcca-4228-86a9-77974477f160"));
//		SynonymRelationshipType synonymType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
//
//		Reference citation;
//		ReferenceFactory refFactory = ReferenceFactory.newInstance();
//		citation = refFactory.newBook();
//		AgentBase linne = appCtr.getAgentService().find(UUID.fromString("f6272e48-5b4e-40c1-b4e9-ee32334fa19f"));
//		citation.setAuthorTeam((TeamOrPersonBase)linne);
//		citation.setTitleCache("Make Taxon Synonym Test");
//		String microRef = "123";
//		appCtr.getReferenceService().save(citation);
//
//		appCtr.getTaxonService().changeAcceptedTaxonToSynonym(oldTaxon, newAcceptedTaxon, synonymType, citation, microRef);
//
//		appCtr.commitTransaction(txStatus);
	}


	private void testRemoveNameRelationship(CdmApplicationController appCtr) {

		logger.info("Testing testRemoveNameRelationship()");
		TransactionStatus txStatus = appCtr.startTransaction();

		BotanicalName name1, name2;
		AgentBase linne = appCtr.getAgentService().find(UUID.fromString("f6272e48-5b4e-40c1-b4e9-ee32334fa19f"));
		name1 = BotanicalName.NewInstance(Rank.SPECIES(),"Name1",null,"arvensis",null,(TeamOrPersonBase)linne,null,"p.1", null);
		name2 = BotanicalName.NewInstance(Rank.SPECIES(),"Name2",null,"lanzae",null,(TeamOrPersonBase)linne,null,"p.2", null);

		name1.addRelationshipToName(name2, NameRelationshipType.BASIONYM(), "ruleTo");
		name2.addRelationshipFromName(name1, NameRelationshipType.BASIONYM(), "ruleFrom");

		appCtr.getNameService().save(name1);
		appCtr.getNameService().save(name2);

		logger.info("Removing Name Relationships");

		Set<NameRelationship> name1FromRelations = name1.getRelationsFromThisName();
		NameRelationship nameRel = null;

		for (NameRelationship name1Rel: name1FromRelations) {
			nameRel = name1Rel;
		}

		name1.removeNameRelationship(nameRel);
//		name1.removeTaxonName(name2);
		appCtr.getNameService().save(name1);

		Taxon taxon = (Taxon)appCtr.getTaxonService().find(UUID.fromString("6a8be65b-94b6-4136-919a-02002e409158"));
		Set<Synonym> synonyms = taxon.getSynonyms();

//		List<TaxonBase> taxa = appCtr.getTaxonService().getAllTaxa(100, 0);
//		Set<Synonym> synonyms = null;
//		for (TaxonBase taxonBase: taxa) {
//		synonyms = taxonBase.getSynonyms();
//		}

		Synonym syn = null;
		for (Synonym synonym: synonyms) {
			if (synonym.getUuid().toString().equals("f7ad5713-70ce-42af-984f-865c1f126460")) {
				syn = synonym;
			}
		}
		taxon.removeSynonym(syn);
		appCtr.getTaxonService().save(taxon);

//		name1FromRelations.removeAll(name1FromRelations);

//		Set<NameRelationship> name2ToRelations = name2.getRelationsToThisName();
//		for (NameRelationship name2Rel: name2ToRelations) {
//		name2.removeNameRelationship(name2Rel);
//		}

		appCtr.commitTransaction(txStatus);
	}

	private void createNamedArea(CdmApplicationController appCtr) {

		logger.info("Start testing createNamedArea()");
		TransactionStatus txStatus = appCtr.startTransaction();

		NamedArea namedArea = NamedArea.NewInstance("MyTerm", "MyLabel", "MyLabelAbbr");
		UUID naid = appCtr.getTermService().save(namedArea).getUuid();

		Country woc = Country.NewInstance("NAR", "Narnia", "NN");
		UUID wocid = appCtr.getTermService().save(woc).getUuid();

		DefinedTermBase dtb = appCtr.getTermService().find(naid);
//		DefinedTermBase dtb =
//			appCtr.getTermService().getTermByUuid(UUID.fromString("dbcedb8b-ae38-45b0-a400-840babf68f9c"));
		logger.debug("NamedArea: " + dtb.toString());
//		dtb = TdwgArea.getAreaByTdwgAbbreviation("AGE-LP");
		//logger.debug("TdwgArea: " + dtb.toString());
		dtb = appCtr.getTermService().find(wocid);
//		dtb =
//			appCtr.getTermService().getTermByUuid(UUID.fromString("7cc278aa-b42a-4b5f-b7ad-0cbab0730da8"));
		logger.debug("Country: " + dtb.toString());

		appCtr.commitTransaction(txStatus);
		logger.info("End testing createNamedArea()");
	}

    public static void main(String[] args) {

		TestTaxonServices testClass = new TestTaxonServices();
        CdmApplicationController cdmApp =
        	getCdmApplicationController(db, DbSchemaValidation.VALIDATE, false);

        testClass.createNamedArea(cdmApp);
        //testClass.testMakeTaxonSynonym(cdmApp);
		//testClass.testRemoveNameRelationship(cdmApp);
        cdmApp.close();

	}
}
