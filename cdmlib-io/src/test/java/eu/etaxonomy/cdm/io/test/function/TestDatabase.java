/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.test.function;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.jaxb.DataSet;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.babadshanjan
 * @created 28.10.2008
 */
public class TestDatabase {

	private static final String server = "192.168.2.10";
	private static final String username = "edit";

	private static final Logger logger = Logger.getLogger(TestDatabase.class);

	public static ICdmDataSource CDM_DB(String dbname) {

		logger.info("Setting DB " + dbname);
		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
		return datasource;
	}

    public static CdmApplicationController
    		initDb(ICdmDataSource db, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) {

		logger.info("Initializing database '" + db.getName() + "'");

		CdmApplicationController appCtrInit =
			CdmApplicationController.NewInstance(db, dbSchemaValidation, omitTermLoading);

		return appCtrInit;
    }

	public static void loadTestData(String dbname, CdmApplicationController appCtr) {

		logger.info("Loading test data into " + dbname);

		TransactionStatus txStatus = appCtr.startTransaction();
		DataSet dataSet = buildDataSet();

		appCtr.getTaxonService().save((List)dataSet.getTaxonBases());

		appCtr.commitTransaction(txStatus);
		appCtr.close();
    }

	/**
	 * This method constructs a small sample classification to test JAXB marshaling.
	 * The sample tree contains four taxa. The root taxon has two children taxa, and
	 * there is one "free" taxon without a parent and children.
	 */
	private static DataSet buildDataSet() {

		List<AgentBase> agents = new ArrayList<AgentBase>();
	    List<VersionableEntity> agentData = new ArrayList<VersionableEntity>();
	    //List<TermBase> terms = new ArrayList<TermBase>();
	    List<DefinedTermBase> terms = new ArrayList<DefinedTermBase>();
	    List<Reference> references = new ArrayList<Reference>();
	    List<TaxonNameBase> taxonomicNames = new ArrayList<TaxonNameBase>();
	    List<TaxonBase> taxonBases = new ArrayList<TaxonBase>();
	    List<AnnotatableEntity> homotypicalGroups;

		Reference citRef, sec;
		BotanicalName name1, name2, name21, nameRoot1, nameFree, synName11, synName12, synName2, synNameFree;
		BotanicalName nameRoot2, nameR2_1, nameR2_2;
		Taxon child1, child2, child21, root1T, root2T, freeT;
		Taxon childR2_1, childR2_2;
		Synonym syn11, syn12, syn2, synFree;
		Rank rankSpecies, rankSubspecies, rankGenus;

		// agents
		// - persons, institutions

		Person linne = new Person("Carl", "LinnÃ©", "L.");
		GregorianCalendar birth = new GregorianCalendar(1707, 4, 23);
		GregorianCalendar death = new GregorianCalendar(1778, 0, 10);
		TimePeriod period = TimePeriod.NewInstance(birth, death);
		linne.setLifespan(period);

//		Keyword keyword = Keyword.NewInstance("plantarum", "lat", "");
//		linne.addKeyword(keyword);

		Institution institute = Institution.NewInstance();

		agents.add(linne);
		agents.add(institute);

		// agent data
		// - contacts, addresses, memberships

		//Contact contact1 = new Contact();
		//contact1.setEmail("someone@somewhere.org");
		InstitutionalMembership membership = new InstitutionalMembership(institute, linne, period, "Biodiversity", "Head");
		//agentData.add(contact1);
        linne.getInstitutionalMemberships().add(membership);

		// terms
		// - ranks, keywords

		rankSpecies = Rank.SPECIES();
		rankSubspecies = Rank.SUBSPECIES();
		rankGenus = Rank.GENUS();

//		terms.add(keyword);

        // taxonomic names

		nameRoot1 = BotanicalName.NewInstance(rankGenus,"Calendula",null,null,null,linne,null,"p.100", null);

		name1 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"arvensis",null,linne,null,"p.1", null);
		synName11 = BotanicalName.NewInstance(rankSpecies,"Caltha",null,"arvensis",null,linne,null,"p.11", null);
		synName12 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"sancta",null,linne,null,"p.12", null);

		name2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzae",null,linne,null,"p.2", null);
		synName2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"echinata",null,linne,null,"p.2", null);

		name21 = BotanicalName.NewInstance(rankSubspecies,"Calendula",null,"lanzea","something",linne,null,"p.1", null);
		//name211 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzea",null,linne,null,"p.1", null);
		//name212 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzea",null,linne,null,"p.1", null);

		nameRoot2 =
			BotanicalName.NewInstance(rankGenus,"Sonchus",null,null,null,linne,null,"p.200", null);
		nameR2_1 = BotanicalName.NewInstance(rankSpecies,"Sonchus",null,"child1",null,linne,null,"p.1", null);
		nameR2_2 = BotanicalName.NewInstance(rankSpecies,"Sonchus",null,"child2",null,linne,null,"p.2", null);

		nameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"intybus",null,linne,null,"p.200", null);
		synNameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"balearicum",null,linne,null,"p.2", null);

		taxonomicNames.add(nameRoot1);
		taxonomicNames.add(name1);
		taxonomicNames.add(synName11);
		taxonomicNames.add(synName12);
		taxonomicNames.add(name2);
		taxonomicNames.add(name21);
		taxonomicNames.add(synName2);
		taxonomicNames.add(nameFree);
		taxonomicNames.add(synNameFree);
		taxonomicNames.add(nameRoot2);

        // references
		sec = ReferenceFactory.newBook();
		sec.setAuthorship(linne);
		sec.setTitleCache("Plant Speciation", true);
		references.add(sec);

		citRef = ReferenceFactory.newDatabase();
		citRef.setAuthorship(linne);
		citRef.setTitleCache("BioCASE", true);
		references.add(citRef);

		// taxa

		root1T = Taxon.NewInstance(nameRoot1, sec);
		root2T = Taxon.NewInstance(nameRoot2, sec);
		freeT = Taxon.NewInstance(nameFree, sec);
		child1 = Taxon.NewInstance(name1, sec);
		child2 = Taxon.NewInstance(name2, sec);
		child21 = Taxon.NewInstance(name21, sec);
		childR2_1 = Taxon.NewInstance(nameR2_1, sec);
		childR2_2 = Taxon.NewInstance(nameR2_2, sec);

		// synonyms

		synFree = Synonym.NewInstance(synNameFree, sec);
		syn11 = Synonym.NewInstance(synName11, sec);
		syn12 = Synonym.NewInstance(synName12, sec);
		syn2 = Synonym.NewInstance(synName2, sec);

		child1.addSynonym(syn11, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
		child1.addSynonym(syn12, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		child2.addSynonym(syn2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		freeT.addSynonym(synFree, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

		taxonBases.add(synFree);
		taxonBases.add(syn11);
		taxonBases.add(syn12);
		taxonBases.add(syn2);

		// taxonomic children

		root1T.addTaxonomicChild(child1, sec, "p.1010");
		root1T.addTaxonomicChild(child2, sec, "p.1020");
		child2.addTaxonomicChild(child21, sec, "p.2000");

		root2T.addTaxonomicChild(child1, sec, "p.1010");
		root2T.addTaxonomicChild(child2, sec, "p.1020");

		taxonBases.add(root1T);
		taxonBases.add(root2T);
		taxonBases.add(freeT);
		taxonBases.add(child1);
		taxonBases.add(child2);
		taxonBases.add(child21);
		taxonBases.add(childR2_1);
		taxonBases.add(childR2_2);

		DataSet dataSet = new DataSet();

		dataSet.setAgents(agents);
//		dataSet.setAgentData(agentData);
		dataSet.setTerms(terms);
		dataSet.setReferences(references);
		dataSet.setTaxonomicNames(taxonomicNames);
		dataSet.setTaxonBases((List)taxonBases);

		return dataSet;

	}
}
