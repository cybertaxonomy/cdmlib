package eu.etaxonomy.cdm.test.function;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.jaxb.CdmDocumentBuilder;
import eu.etaxonomy.cdm.jaxb.DataSet;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DefinedTermDaoImpl;


public class TestJaxb {
	
	private static final Logger logger = Logger.getLogger(TestJaxb.class);
	
	//private static final String serializeFromDb = "cdm_test_jaxb";
	private static final String serializeFromDb = "cdm_test_anahit";
	private static final String deserializeToDb = "cdm_test_jaxb2";
	private String server = "192.168.2.10";
	private String username = "edit";
	private String marshOutOne = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_marshalled.xml");
	private String marshOutTwo = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_roundtrip.xml");
	//private String test = new String( System.getProperty("user.home") + File.separator + "cdm_test.xml");

	private CdmDocumentBuilder cdmDocumentBuilder = null;
	
    public TestJaxb() {	
    }

    public void testInitDb(String dbname) {
    	
		logger.info("Initializing DB " + "dbname");
		
		CdmApplicationController appCtr = null;
		try {
			String pwd = AccountStore.readOrStorePassword(dbname, server, username, null);
			
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, pwd);
			appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}

		DataSet dataSet = buildDataSet();
		
		TransactionStatus txStatus = appCtr.startTransaction();
		
		appCtr.getTaxonService().saveTaxonAll(dataSet.getTaxonBases());

		appCtr.commitTransaction(txStatus);
		appCtr.close();

    }

    
    public void traverse (List<Taxon> taxonCollection, DataSet dataSet) {

    	// The following collections store data of a particular horizontal level, 
    	// such as all synonyms, relationships, and children of all taxa of this level.

    	ArrayList<Taxon> children_ = new ArrayList<Taxon>();
    	Set<Synonym> synonyms_ = new HashSet();
    	Set<TaxonRelationship> taxonRelationships_ = new HashSet();
    	Set<SynonymRelationship> synonymRelationships_ = new HashSet();

    	for (Taxon taxon: taxonCollection) {

    		try {
    			
    			logger.info("taxon: " + taxon.toString());
    			
    			// get the synonyms and synonym relationships
    			if (taxon.hasSynonyms() == true) {

    				Set<Synonym> synonyms = taxon.getSynonyms();
    				Set<SynonymRelationship> synonymRelationships = taxon.getSynonymRelations();

    				for (Synonym synonym: synonyms) {
    					logger.info("synonym: " + synonym.toString());
    					synonyms_.add(synonym);
    				}

    				for (SynonymRelationship synonymRelationship: synonymRelationships) {
    					logger.info("synonym relationship: " + synonymRelationship.toString());
    					synonymRelationships_.add(synonymRelationship);
    				}

    				// If calling dataSet.addSynonyms() inside this for loop
    				// get ConcurrentModificationException.
    			}

    			// get the taxon relationships
    			if (taxon.hasTaxonRelationships() == true) {

    				Set<TaxonRelationship> taxonRelationships = taxon.getTaxonRelations();

    				for (TaxonRelationship taxonRelationship: taxonRelationships) {
    					logger.info("taxon relationship: " + taxonRelationship.toString());
    					taxonRelationships_.add(taxonRelationship);
    				}
    			}

    			// get the children
    			logger.info("# children: " + taxon.getTaxonomicChildrenCount());
    			if (taxon.hasTaxonomicChildren() == true) {

    				Set<Taxon> children = taxon.getTaxonomicChildren();

    				for (Taxon child: children) {
    					children_.add(child);
    					logger.info("child: "+ child.toString());
    				}
    			}
    		} catch (Exception e) {
    			logger.error("error retrieving taxon data");
    			e.printStackTrace();
    		}
    	}

    	try {
    		if (synonyms_ != null) {
    			dataSet.addSynonyms(synonyms_);
    		}
    		if (synonymRelationships_ != null) {
    			dataSet.addRelationships(synonymRelationships_);
    		}
    		if (taxonRelationships_ != null) {
    			dataSet.addRelationships(taxonRelationships_);
    		}
    		if (children_ != null) {
    			dataSet.addTaxa(children_);
    		} 

    	} catch (Exception e) {
    		logger.error("error setting DataSet structure");
    		e.printStackTrace();
    	}

    	if (children_ != null && children_.size() > 0) {
    		traverse(children_, dataSet);
    	} 
    }
    
    
    public void testSerialize(String dbname, String filename) {
    	
		logger.info("Serializing DB " + dbname + " to file " + filename);

		CdmApplicationController appCtr = null;

    	try {
    		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
    		
    		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
    		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, serializeFromDb, username, password);
    		appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);

    	} catch (DataSourceNotFoundException e) {
    		logger.error("datasource error");
    	} catch (TermNotFoundException e) {
    		logger.error("defined terms not found");
    	}
    	
    	TransactionStatus txStatus = appCtr.startTransaction();
    	DataSet dataSet = new DataSet();
    	List<Taxon> taxa = null;
    	List<DefinedTermBase> terms = null;

    	// get data from DB

    	try {
    		logger.info("Load data from DB ...");

    		// get root taxa 

    		taxa = appCtr.getTaxonService().getRootTaxa(null, null, false);
    		
    		// CdmFetch options not yet implemented
    		//appCtr.getTaxonService().getRootTaxa(null, CdmFetch.NO_FETCH(), false);

    	} catch (Exception e) {
    		logger.info("error while fetching root taxa");
    	}

    	try {
    		
    		int nbrRows;
    		
    		nbrRows = appCtr.getAgentService().count(Agent.class);
    		logger.info("# Agents: " + nbrRows);
    		//logger.info("    # Team: " + appCtr.getAgentService().count(Team.class));
    		dataSet.setAgents(appCtr.getAgentService().getAllAgents(nbrRows, 0));
    		
    		nbrRows = appCtr.getTermService().count(DefinedTermBase.class);
    		logger.info("# DefinedTermBase: " + nbrRows);
    		dataSet.setTerms(appCtr.getTermService().getAllDefinedTerms(nbrRows, 0));

    		nbrRows = appCtr.getReferenceService().count(ReferenceBase.class);
    		logger.info("# ReferenceBase: " + nbrRows);
    		dataSet.setReferences(appCtr.getReferenceService().getAllReferences(nbrRows, 0));
    		
    		nbrRows = appCtr.getNameService().count(TaxonNameBase.class);
    		logger.info("# TaxonNameBase: " + nbrRows);
    		//logger.info("    # Taxon: " + appCtr.getNameService().count(BotanicalName.class));
    		dataSet.setTaxonomicNames(appCtr.getNameService().getAllNames(nbrRows, 0));
    		
    		dataSet.setTaxa(taxa);
    		
    		dataSet.setSynonyms(new HashSet<Synonym>());
    		dataSet.setRelationships(new HashSet<RelationshipBase>());
    		dataSet.setHomotypicalGroups(new HashSet<HomotypicalGroup>());
    		
    	} catch (Exception e) {
    		logger.info("error setting root data");
    	}

    	traverse(taxa, dataSet);
    	
    	try {
    		cdmDocumentBuilder = new CdmDocumentBuilder();
    		cdmDocumentBuilder.marshal(dataSet, new FileWriter(filename));

    	} catch (Exception e) {
    		logger.error("marshalling error");
    		e.printStackTrace();
    	} 
    	appCtr.commitTransaction(txStatus);
    	appCtr.close();
    	
    }
    

	public void testDeserialize(String dbname, String filename) {
		
		logger.info("Deserializing file " + filename + " to DB " + dbname);

		CdmApplicationController appCtr = null;

		try {
    		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
			
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
			appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}

		DataSet dataSet = new DataSet();
		
        // unmarshalling XML file
		
		try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
			dataSet = cdmDocumentBuilder.unmarshal(dataSet, new File(filename));

		} catch (Exception e) {
			logger.error("unmarshalling error");
			e.printStackTrace();
		} 
		
		// save data in DB
		
		Collection<TaxonBase> taxonBases;
		
		TransactionStatus txStatus = appCtr.startTransaction();
		
		// Currently it's sufficient to save the taxa only since all other data
		// related to the taxa, such as synonyms, are automatically saved as well.
		
//		if ((agents = dataSet.getAgents()) != null) {
//		appCtr.getAgentService().saveAgentAll(agents);
//		}
		
		// FIXME: Clean getTaxa()/getTaxonBases() return parameters.
		
		// Need to get the taxa and the synonyms here.
		if ((taxonBases = dataSet.getTaxonBases_()) != null) {
		appCtr.getTaxonService().saveTaxonAll(taxonBases);
		}

		appCtr.commitTransaction(txStatus);
		appCtr.close();

	}
	
	private void test(){
		
		//testInitDb(serializeFromDb);
	    testSerialize(serializeFromDb, marshOutOne);
		//testDeserialize(deserializeToDb, marshOutOne);
		//testSerialize(deserializeToDb, marshOutTwo);
		}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestJaxb sc = new TestJaxb();
    	sc.test();
	}

	/**
	 * This method constructs a small sample taxonomic tree to test JAXB marshaling.
	 * The sample tree contains four taxa. The root taxon has two children taxa, and
	 * there is one "free" taxon without a parent and children.
	 */
	// TODO: Put this code to an appropriate place.
	// Can it be in eu.etaxonomy.cdb.model.DataSetTest?
	private DataSet buildDataSet() {

		List<Agent> agents;
	    List<VersionableEntity> agentData;
	    List<TermBase> terms;
	    List<ReferenceBase> references;
	    List<TaxonNameBase> taxonomicNames;
	    List<Taxon> taxa;
	    Set<Synonym> synonyms;
	    List<AnnotatableEntity> homotypicalGroups;

	    agents = new ArrayList<Agent>();
		agentData = new ArrayList<VersionableEntity>();
		terms = new ArrayList<TermBase>();
	    references = new ArrayList<ReferenceBase>();
		taxonomicNames = new ArrayList<TaxonNameBase>();
		taxa = new ArrayList<Taxon>();
		synonyms = new HashSet<Synonym>();
		
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
	
}
