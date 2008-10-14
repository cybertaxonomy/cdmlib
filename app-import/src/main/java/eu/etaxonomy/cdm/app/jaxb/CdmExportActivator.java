/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.app.jaxb;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
//import org.unitils.database.annotations.TestDataSource;
//import org.unitils.database.annotations.Transactional;
//import org.unitils.database.util.TransactionMode;
//import org.unitils.spring.annotation.SpringApplicationContext;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.jaxb.CdmExporter;
import eu.etaxonomy.cdm.io.jaxb.DataSet;
import eu.etaxonomy.cdm.io.jaxb.JaxbExportImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
 
/**
 * @author a.babadshanjan
 * @created 25.09.2008
 */
public class CdmExportActivator {
	
	private static final Logger logger = Logger.getLogger(CdmExportActivator.class);
	
    /* SerializeFrom DB **/
	private static final String sourceDbName = "cdm_test_jaxb2";
	private static final String destinationDbName = "cdm_test_jaxb2";
	
	/** NUMBER_ROWS_TO_RETRIEVE = 0 is the default case to retrieve all rows.
	 * For testing purposes: If NUMBER_ROWS_TO_RETRIEVE >0 then retrieve 
	 *  as many rows as specified for agents, references, etc. 
	 *  Only root taxa and no synonyms and relationships are retrieved. */
	private static final int NUMBER_ROWS_TO_RETRIEVE = 0;
	
	private static final String server = "192.168.2.10";
	private static final String username = "edit";

	public static ICdmDataSource CDM_DB(String dbname) {

		logger.info("Setting DB " + dbname);
		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
		return datasource;
	}
	
	private static final ICdmDataSource sourceDb = CdmExportActivator.CDM_DB(sourceDbName);
	private static final ICdmDataSource destinationDb = CdmExportActivator.CDM_DB(destinationDbName);
	
	private static boolean doAgents = true;
	private static boolean doAgentData = true;
	private static boolean doLanguageData = true;
	private static boolean doFeatureData = true;
	private static boolean doDescriptions = true;
	private static boolean doMedia = true;
	private static boolean doOccurrences = true;
	private static boolean doReferences = true;
	private static boolean doReferencedEntities = true;
	private static boolean doRelationships = true;
	private static boolean doSynonyms = true;
	private static boolean doTaxonNames = true;
	private static boolean doTaxa = true;
	private static boolean doTerms = true;
	private static boolean doTermVocabularies = true;
	private static boolean doHomotypicalGroups = true;
	
	private String marshOutOne = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_marshalled.xml");
	private JaxbExportImportConfigurator expImpConfigurator = null;

    public CdmExportActivator() {	

		expImpConfigurator = new JaxbExportImportConfigurator();
		
		expImpConfigurator.setMaxRows(NUMBER_ROWS_TO_RETRIEVE);
		
		expImpConfigurator.setDoAgents(doAgents);
		expImpConfigurator.setDoAgentData(doAgentData);
		expImpConfigurator.setDoLanguageData(doLanguageData);
		expImpConfigurator.setDoFeatureData(doFeatureData);
		expImpConfigurator.setDoDescriptions(doDescriptions);
		expImpConfigurator.setDoMedia(doMedia);
		expImpConfigurator.setDoOccurrences(doOccurrences);
		expImpConfigurator.setDoReferences(doReferences);
		expImpConfigurator.setDoReferencedEntities(doReferencedEntities);
		expImpConfigurator.setDoRelationships(doRelationships);
		expImpConfigurator.setDoSynonyms(doSynonyms);
		expImpConfigurator.setDoTaxonNames(doTaxonNames);
		expImpConfigurator.setDoTaxa(doTaxa);
		expImpConfigurator.setDoTerms(doTerms);
		expImpConfigurator.setDoTermVocabularies(doTermVocabularies);
		expImpConfigurator.setDoHomotypicalGroups(doHomotypicalGroups);
		
		expImpConfigurator.setCdmSource(sourceDb);
		expImpConfigurator.setCdmDestination(destinationDb);

   }

    
    private void invoke(){

    	CdmExporter cdmExporter = new CdmExporter();

    	/* ********* INIT ****************************/
    	
    	// Init source DB
    	expImpConfigurator.setCdmSourceSchemaValidation(DbSchemaValidation.VALIDATE);
		CdmApplicationController appCtrInit = null;
			expImpConfigurator.getSourceAppController(expImpConfigurator.getCdmSource(), true);
		try {
			appCtrInit = CdmApplicationController.NewInstance(expImpConfigurator.getCdmSource(), expImpConfigurator.getCdmSourceSchemaValidation(), false);
		} catch (DataSourceNotFoundException e) {
			logger.error("Could not connect to database");
		}catch (TermNotFoundException e) {
			logger.error("Terms not found in database. " +
			"This error should not happen since preloaded terms are not expected for this application.");
		}

		// Load some test data to source DB
//    	loadTestData(sourceDbName, appCtrInit);
    	
//    	testMakeTaxonSynonym(appCtrInit);
    	testRemoveNameRelationship(appCtrInit);
    	
    	// Init destination DB
//    	expImpConfigurator.setCdmDestSchemaValidation(DbSchemaValidation.CREATE);
//		CdmApplicationController appCtr = 
//			expImpConfigurator.getDestinationAppController(expImpConfigurator.getCdmDestination(), true);

    	/* ********* SERIALIZE ***********************/
    	
    	// Reset DbSchemaValidation
//    	expImpConfigurator.setCdmSourceSchemaValidation(DbSchemaValidation.VALIDATE);
    	
    	// Retrieve taxa, synonyms, and relationships through traversing the taxonomic tree.
//    	cdmExporter.doSerializeTaxonTree(expImpConfigurator, marshOutOne);

    	// Retrieve data, including taxa, synonyms, and relationships via services.
//     	cdmExporter.doSerialize(expImpConfigurator, marshOutOne);

    	/* ********* DESERIALIZE *********************/
    	
//     	cdmExporter.doDeserialize(expImpConfigurator, marshOutOne);

    }

	
    public CdmApplicationController initDb(String dbname, CdmApplicationController appCtr) {
    	
		logger.info("Loading test data into " + dbname);
		return appCtr;
    }

    
    public void loadTestData(String dbname, CdmApplicationController appCtr) {
    	
		logger.info("Loading test data into " + dbname);
		
		TransactionStatus txStatus = appCtr.startTransaction();
		DataSet dataSet = buildDataSet();
		
		appCtr.getTaxonService().saveTaxonAll(dataSet.getTaxa());

		appCtr.commitTransaction(txStatus);
		appCtr.close();
    }

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		CdmExportActivator sc = new CdmExportActivator();
    	sc.invoke();
	}

	
	/**
	 * This method constructs a small sample taxonomic tree to test JAXB marshaling.
	 * The sample tree contains four taxa. The root taxon has two children taxa, and
	 * there is one "free" taxon without a parent and children.
	 */
	private DataSet buildDataSet() {

		List<Agent> agents = new ArrayList<Agent>();
	    List<VersionableEntity> agentData = new ArrayList<VersionableEntity>();
	    //List<TermBase> terms = new ArrayList<TermBase>();
	    List<DefinedTermBase> terms = new ArrayList<DefinedTermBase>();
	    List<ReferenceBase> references = new ArrayList<ReferenceBase>();
	    List<TaxonNameBase> taxonomicNames = new ArrayList<TaxonNameBase>();
	    List<Taxon> taxa = new ArrayList<Taxon>();
	    List<Synonym> synonyms = new ArrayList<Synonym>();
	    List<AnnotatableEntity> homotypicalGroups;

		StrictReferenceBase citRef, sec;
		BotanicalName name1, name2, name21, nameRoot, nameFree, synName11, synName12, synName2, synNameFree;
		Taxon child1, child2, child21, rootT, freeT;
		Synonym syn11, syn12, syn2, synFree;
		Rank rankSpecies, rankSubspecies, rankGenus;

		// agents 
		// - persons, institutions 

		Person linne = new Person("Carl", "Linné", "L.");
		GregorianCalendar birth = new GregorianCalendar(1707, 4, 23);
		GregorianCalendar death = new GregorianCalendar(1778, 0, 10);
		TimePeriod period = new TimePeriod(birth, death);
		linne.setLifespan(period);

		Keyword keyword = Keyword.NewInstance("plantarum", "lat", "");
		linne.addKeyword(keyword);

		Institution institute = Institution.NewInstance();

		agents.add(linne);
		agents.add(institute);

		// agent data
		// - contacts, addresses, memberships

		//Contact contact1 = new Contact();
		//contact1.setEmail("someone@somewhere.org");
		InstitutionalMembership membership 
		= new InstitutionalMembership(institute, linne, period, "Biodiversity", "Head");
		//agentData.add(contact1);

		agentData.add(membership);

		// terms
		// - ranks, keywords

		rankSpecies = Rank.SPECIES();
		rankSubspecies = Rank.SUBSPECIES();
		rankGenus = Rank.GENUS();
		
		terms.add(keyword);
		
        // taxonomic names
		
		nameRoot = BotanicalName.NewInstance(rankGenus,"Calendula",null,null,null,linne,null,"p.100", null);
		
		name1 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"arvensis",null,linne,null,"p.1", null);
		synName11 = BotanicalName.NewInstance(rankSpecies,"Caltha",null,"arvensis",null,linne,null,"p.11", null);
		synName12 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"sancta",null,linne,null,"p.12", null);
		
		name2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzae",null,linne,null,"p.2", null);
		synName2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"echinata",null,linne,null,"p.2", null);
		
		name21 = BotanicalName.NewInstance(rankSubspecies,"Calendula",null,"lanzea","something",linne,null,"p.1", null);
		//name211 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzea",null,linne,null,"p.1", null);
		//name212 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzea",null,linne,null,"p.1", null);
		
		nameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"intybus",null,linne,null,"p.200", null);
		synNameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"balearicum",null,linne,null,"p.2", null);

		taxonomicNames.add(nameRoot);
		taxonomicNames.add(name1);
		taxonomicNames.add(synName11);
		taxonomicNames.add(synName12);
		taxonomicNames.add(name2);
		taxonomicNames.add(name21);
		taxonomicNames.add(synName2);
		taxonomicNames.add(nameFree);
		taxonomicNames.add(synNameFree);
		
        // references
		
		sec = Book.NewInstance();
		sec.setAuthorTeam(linne);
		sec.setTitleCache("Plant Speciation");
		references.add(sec);
		
		citRef = Database.NewInstance();
		citRef.setAuthorTeam(linne);
		citRef.setTitleCache("BioCASE");
		references.add(citRef);

		// taxa
		
		rootT = Taxon.NewInstance(nameRoot, sec);
		freeT = Taxon.NewInstance(nameFree, sec);
		child1 = Taxon.NewInstance(name1, sec);
		child2 = Taxon.NewInstance(name2, sec);
		child21 = Taxon.NewInstance(name21, sec);
		
		// synonyms
		
		synFree = Synonym.NewInstance(synNameFree, sec);
		syn11 = Synonym.NewInstance(synName11, sec);
		syn12 = Synonym.NewInstance(synName12, sec);
		syn2 = Synonym.NewInstance(synName2, sec);
		
		child1.addSynonym(syn11, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
		child1.addSynonym(syn12, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		child2.addSynonym(syn2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		freeT.addSynonym(synFree, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

		synonyms.add(synFree);
		synonyms.add(syn11);
		synonyms.add(syn12);
		synonyms.add(syn2);
		
		// taxonomic children
		
		rootT.addTaxonomicChild(child1, sec, "p.1010");
		rootT.addTaxonomicChild(child2, sec, "p.1020");
		child2.addTaxonomicChild(child21, sec, "p.2000");
				
		taxa.add(rootT);
		taxa.add(freeT);
		taxa.add(child1);
		taxa.add(child2);
		taxa.add(child21);
		
		DataSet dataSet = new DataSet();
		
		dataSet.setAgents(agents);
		dataSet.setAgentData(agentData);
		dataSet.setTerms(terms);
		dataSet.setReferences(references);
		dataSet.setTaxonomicNames(taxonomicNames);
		dataSet.setTaxa(taxa);
		dataSet.setSynonyms(synonyms);
		
		return dataSet;

	}
	
	private void testMakeTaxonSynonym(CdmApplicationController appCtr) {
		
		logger.info("Testing makeTaxonSynonym()");
		TransactionStatus txStatus = appCtr.startTransaction();
		
		Taxon oldTaxon = (Taxon)appCtr.getTaxonService().getTaxonByUuid(UUID.fromString("83a87f0c-e2c4-4b41-b603-4e77e7e53158"));
		Taxon newAcceptedTaxon = (Taxon)appCtr.getTaxonService().getTaxonByUuid(UUID.fromString("0b423190-fcca-4228-86a9-77974477f160"));
		SynonymRelationshipType synonymType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
		
		ReferenceBase citation;
		citation = Book.NewInstance();
		Agent linne = appCtr.getAgentService().getAgentByUuid(UUID.fromString("f6272e48-5b4e-40c1-b4e9-ee32334fa19f"));
		citation.setAuthorTeam((TeamOrPersonBase)linne);
		citation.setTitleCache("Make Taxon Synonym Test");
		String microRef = "123";
		appCtr.getReferenceService().saveReference(citation);
		
		appCtr.getTaxonService().makeTaxonSynonym(oldTaxon, newAcceptedTaxon, synonymType, citation, microRef);

		appCtr.commitTransaction(txStatus);
		appCtr.close();
		
	}

	private void testRemoveNameRelationship(CdmApplicationController appCtr) {
		
		logger.info("Testing testRemoveNameRelationship()");
		TransactionStatus txStatus = appCtr.startTransaction();
		
		BotanicalName name1, name2;
		Agent linne = appCtr.getAgentService().getAgentByUuid(UUID.fromString("f6272e48-5b4e-40c1-b4e9-ee32334fa19f"));
		name1 = BotanicalName.NewInstance(Rank.SPECIES(),"Name1",null,"arvensis",null,(TeamOrPersonBase)linne,null,"p.1", null);
		name2 = BotanicalName.NewInstance(Rank.SPECIES(),"Name2",null,"lanzae",null,(TeamOrPersonBase)linne,null,"p.2", null);
		
		name1.addRelationshipToName(name2, NameRelationshipType.BASIONYM(), "ruleTo");
		name2.addRelationshipFromName(name1, NameRelationshipType.BASIONYM(), "ruleFrom");
			
		appCtr.getNameService().saveTaxonName(name1);
		appCtr.getNameService().saveTaxonName(name2);
		
		logger.info("Removing Name Relationships");
		
		Set<NameRelationship> name1FromRelations = name1.getRelationsFromThisName();
		NameRelationship nameRel = null;
		
		for (NameRelationship name1Rel: name1FromRelations) {
			nameRel = name1Rel;
		}

        name1.removeNameRelationship(nameRel);
//		name1.removeTaxonName(name2);
		appCtr.getNameService().saveTaxonName(name1);
        
		Taxon taxon = (Taxon)appCtr.getTaxonService().getTaxonByUuid(UUID.fromString("6a8be65b-94b6-4136-919a-02002e409158"));
		Set<Synonym> synonyms = taxon.getSynonyms();
		
//		List<TaxonBase> taxa = appCtr.getTaxonService().getAllTaxa(100, 0);
//		Set<Synonym> synonyms = null;
//		for (TaxonBase taxonBase: taxa) {
//			synonyms = taxonBase.getSynonyms();
//		}
		
		Synonym syn = null;
		for (Synonym synonym: synonyms) {
			if (synonym.getUuid().toString().equals("f7ad5713-70ce-42af-984f-865c1f126460")) {
				syn = synonym;
			}
		}
		taxon.removeSynonym(syn);
		appCtr.getTaxonService().saveTaxon(taxon);
		
//		name1FromRelations.removeAll(name1FromRelations);
		
//		Set<NameRelationship> name2ToRelations = name2.getRelationsToThisName();
//		for (NameRelationship name2Rel: name2ToRelations) {
//			name2.removeNameRelationship(name2Rel);
//		}
		
		appCtr.commitTransaction(txStatus);
		appCtr.close();

	}
		
	private void createNameRelationship(CdmApplicationController appCtr) {
		
		TransactionStatus txStatus = appCtr.startTransaction();

		appCtr.commitTransaction(txStatus);
		appCtr.close();
		
	}
	

}
