package eu.etaxonomy.cdm.test.function;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
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
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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


public class TestJaxb {
	
	private static final Logger logger = Logger.getLogger(TestJaxb.class);
	
	//private static final String serializeFromDb = "cdm_test_jaxb";
	private static final String serializeFromDb = "cdm_test_anahit";
	
	private static final String deserializeToDb = "cdm_test_jaxb2";
	//private static final String deserializeToDb = "cdm_test_anahit2";
	
	private String server = "192.168.2.10";
	private String username = "edit";
	private String marshOutOne = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_marshalled.xml");
	private String marshOutTwo = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_roundtrip.xml");

	/** NUMBER_ROWS_TO_RETRIEVE = 0 is the default case to retrieve all rows. */
	private static final int NUMBER_ROWS_TO_RETRIEVE = 0;
	
	/** For testing purposes: If NUMBER_ROWS_TO_RETRIEVE >0 then retrieve 
	 *  as many rows as specified for agents, references, etc. 
	 *  Only root taxa and no synonyms and relationships are retrieved. */
	//private static final int NUMBER_ROWS_TO_RETRIEVE = 10;
	
	private CdmDocumentBuilder cdmDocumentBuilder = null;
	
    public TestJaxb() {	
    }

    public void initDb(String dbname) {
    	
    	CdmApplicationController appCtr = initPreloadedDb(dbname);
    	loadTestData(dbname, appCtr);
    }
    
    public CdmApplicationController initPreloadedDb(String dbname) {
    	
		logger.info("Initializing DB " + dbname);
		
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
		
		return appCtr;
    }

    public void loadTestData(String dbname, CdmApplicationController appCtr) {
    	
		logger.info("Loading test data into " + dbname);
		
		DataSet dataSet = buildDataSet();
		
		TransactionStatus txStatus = appCtr.startTransaction();
		
		logger.info("Initializing DB " + dbname + " with test data");
		appCtr.getTaxonService().saveTaxonAll(dataSet.getTaxonBases());

		appCtr.commitTransaction(txStatus);
		appCtr.close();
    }

    private void setFlatData (CdmApplicationController appCtr, DataSet dataSet, int numberOfRows) {
    	
	int agentRows = numberOfRows;
	int definedTermBaseRows = numberOfRows;
	int referenceBaseRows = numberOfRows;
	int taxonNameBaseRows = numberOfRows;
	
	if (agentRows <= 0) { agentRows = appCtr.getAgentService().count(Agent.class); }
	logger.info("# Agents: " + agentRows);
	//logger.info("    # Team: " + appCtr.getAgentService().count(Team.class));
	dataSet.setAgents(appCtr.getAgentService().getAllAgents(agentRows, 0));
	
	if (definedTermBaseRows <= 0) { definedTermBaseRows = appCtr.getTermService().count(DefinedTermBase.class); }
	logger.info("# DefinedTermBase: " + definedTermBaseRows);
	dataSet.setTerms(appCtr.getTermService().getAllDefinedTerms(definedTermBaseRows, 0));

	if (referenceBaseRows <= 0) { referenceBaseRows = appCtr.getReferenceService().count(ReferenceBase.class); }
	logger.info("# ReferenceBase: " + referenceBaseRows);
	dataSet.setReferences(appCtr.getReferenceService().getAllReferences(referenceBaseRows, 0));
	
	if (taxonNameBaseRows <= 0) { taxonNameBaseRows = appCtr.getNameService().count(TaxonNameBase.class); }
	logger.info("# TaxonNameBase: " + taxonNameBaseRows);
	//logger.info("    # Taxon: " + appCtr.getNameService().count(BotanicalName.class));
	dataSet.setTaxonomicNames(appCtr.getNameService().getAllNames(taxonNameBaseRows, 0));
	
    }
	
    
    private void setAllDataFlat (CdmApplicationController appCtr, DataSet dataSet, int numberOfRows) {
    	
    	int agentRows = numberOfRows;
    	int definedTermBaseRows = numberOfRows;
    	int referenceBaseRows = numberOfRows;
    	int taxonNameBaseRows = numberOfRows;
    	int taxonBaseRows = numberOfRows;
    	
    	if (agentRows == 0) { agentRows = appCtr.getAgentService().count(Agent.class); }
    	logger.info("# Agents: " + agentRows);
    	//logger.info("    # Team: " + appCtr.getAgentService().count(Team.class));
    	dataSet.setAgents(appCtr.getAgentService().getAllAgents(agentRows, 0));
    	
    	if (definedTermBaseRows == 0) { definedTermBaseRows = appCtr.getTermService().count(DefinedTermBase.class); }
    	logger.info("# DefinedTermBase: " + definedTermBaseRows);
    	dataSet.setTerms(appCtr.getTermService().getAllDefinedTerms(definedTermBaseRows, 0));

    	if (referenceBaseRows == 0) { referenceBaseRows = appCtr.getReferenceService().count(ReferenceBase.class); }
    	logger.info("# ReferenceBase: " + referenceBaseRows);
    	dataSet.setReferences(appCtr.getReferenceService().getAllReferences(referenceBaseRows, 0));
    	
    	if (taxonNameBaseRows == 0) { taxonNameBaseRows = appCtr.getNameService().count(TaxonNameBase.class); }
    	logger.info("# TaxonNameBase: " + taxonNameBaseRows);
    	//logger.info("    # Taxon: " + appCtr.getNameService().count(BotanicalName.class));
    	dataSet.setTaxonomicNames(appCtr.getNameService().getAllNames(taxonNameBaseRows, 0));
    	
    	if (taxonBaseRows == 0) { taxonBaseRows = appCtr.getTaxonService().count(TaxonBase.class); }
    	logger.info("# TaxonBase: " + taxonBaseRows);
		dataSet.setTaxa(new ArrayList<Taxon>());
		dataSet.setSynonyms(new ArrayList<Synonym>());
    	List<TaxonBase> tb = appCtr.getTaxonService().getAllTaxa(taxonBaseRows, 0);
    	for (TaxonBase taxonBase : tb) {
    		if (taxonBase instanceof Taxon) {
				dataSet.addTaxon((Taxon)taxonBase);
			} else if (taxonBase instanceof Synonym) {
				dataSet.addSynonym((Synonym)taxonBase);
			} else {
	    		logger.error("entry of wrong type: " + taxonBase.toString());
			}
    	}
    	
        // TODO: 
    	// retrieve taxa and synonyms separately
    	// need correct count for taxa and synonyms
//    	if (taxonBaseRows == 0) { taxonBaseRows = appCtr.getTaxonService().count(TaxonBase.class); }
//    	logger.info("# Synonym: " + taxonBaseRows);
//		dataSet.setSynonyms(new ArrayList<Synonym>());
//    	dataSet.setSynonyms(appCtr.getTaxonService().getAllSynonyms(taxonBaseRows, 0));

    }

    private void traverse (List<Taxon> taxonCollection, DataSet dataSet) {

    	// The following collections store data of a particular horizontal level, 
    	// such as all synonyms, relationships, and children of all taxa of this level.

    	ArrayList<Taxon> children_ = new ArrayList<Taxon>();
    	Set<Synonym> synonyms_ = new HashSet();
    	Set<TaxonRelationship> taxonRelationships_ = new HashSet();
    	Set<SynonymRelationship> synonymRelationships_ = new HashSet();
    	// TODO: Count number of taxa etc. to restrict number of retrieved objects
    	int numberOfTaxa = 0;

    	for (Taxon taxon: taxonCollection) {
    		
    		numberOfTaxa++;

    		try {
    			
    			logger.debug("taxon: " + taxon.toString());
    			
    			// get the synonyms and synonym relationships
    			if (taxon.hasSynonyms() == true) {

    				Set<Synonym> synonyms = taxon.getSynonyms();
    				Set<SynonymRelationship> synonymRelationships = taxon.getSynonymRelations();

    				for (Synonym synonym: synonyms) {
    					logger.debug("synonym: " + synonym.toString());
    					synonyms_.add(synonym);
    				}

    				for (SynonymRelationship synonymRelationship: synonymRelationships) {
    					logger.debug("synonym relationship: " + synonymRelationship.toString());
    					synonymRelationships_.add(synonymRelationship);
    				}

    				// If calling dataSet.addSynonyms() inside this for loop
    				// get ConcurrentModificationException.
    			}

    			// get the taxon relationships
    			if (taxon.hasTaxonRelationships() == true) {

    				Set<TaxonRelationship> taxonRelationships = taxon.getTaxonRelations();

    				for (TaxonRelationship taxonRelationship: taxonRelationships) {
    					logger.debug("taxon relationship: " + taxonRelationship.toString());
    					taxonRelationships_.add(taxonRelationship);
    				}
    			}

    			// get the children
    			logger.debug("# children: " + taxon.getTaxonomicChildrenCount());
    			if (taxon.hasTaxonomicChildren() == true) {

    				Set<Taxon> children = taxon.getTaxonomicChildren();

    				for (Taxon child: children) {
    					children_.add(child);
    					logger.debug("child: "+ child.toString());
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
    
    
    // traverse the taxonomic tree to retrieve taxa, synonyms and relationships
    public void doSerialize(String dbname, String filename) {
    	
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
    	
    	TransactionStatus txStatus = appCtr.startTransaction(true);
    	DataSet dataSet = new DataSet();
    	List<Taxon> taxa = null;
    	List<DefinedTermBase> terms = null;

    	// get data from DB

    	try {
    		logger.info("Load data from DB: " + dbname);

    		setFlatData(appCtr, dataSet, NUMBER_ROWS_TO_RETRIEVE);
    		
    		taxa = appCtr.getTaxonService().getRootTaxa(null, null, false);
    		// CdmFetch options not yet implemented
    		//appCtr.getTaxonService().getRootTaxa(null, CdmFetch.NO_FETCH(), false);
    		dataSet.setTaxa(taxa);
    		
    		dataSet.setSynonyms(new ArrayList<Synonym>());
    		dataSet.setRelationships(new HashSet<RelationshipBase>());
    		dataSet.setHomotypicalGroups(new HashSet<HomotypicalGroup>());
    		
    	} catch (Exception e) {
    		logger.error("error setting root data");
    	}

        // traverse the taxonomic tree
    	
    	if (NUMBER_ROWS_TO_RETRIEVE <= 0) { traverse(taxa, dataSet); }
    	
		logger.info("All data retrieved");
		
    	try {
    		cdmDocumentBuilder = new CdmDocumentBuilder();
    		logger.info("DocumentBuilder created");
    		
    		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"), true);

    		cdmDocumentBuilder.marshal(dataSet, writer);
    		
    		// TODO: Split into one file per data set member to see whether performance improves?

    		logger.info("XML file written");
    		logger.info("Filename is: " + filename);
    		
    	} catch (Exception e) {
    		logger.error("marshalling error");
    		e.printStackTrace();
    	} 
    	appCtr.commitTransaction(txStatus);
    	appCtr.close();
    	
    }
    

    // get all data directly from the services, including taxa, synonyms, and relationships
    public void doSerializeFlat(String dbname, String filename) {
    	
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

    		setAllDataFlat(appCtr, dataSet, NUMBER_ROWS_TO_RETRIEVE);
    		
    	} catch (Exception e) {
    		logger.info("error setting data");
    	}

    	try {
    		cdmDocumentBuilder = new CdmDocumentBuilder();
    		FileWriter writer = new FileWriter(filename);
    		logger.info("Output Stream Encoding: " + writer.getEncoding());
    		cdmDocumentBuilder.marshal(dataSet, writer);

    	} catch (Exception e) {
    		logger.error("marshalling error");
    		e.printStackTrace();
    	} 
    	appCtr.commitTransaction(txStatus);
    	appCtr.close();
    	
    }
    

	public void doDeserialize(String dbname, String filename) {
		
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
		
		//Loads terms and some test data to DB.
		//initDb(serializeFromDb);
		
		//Loads terms to DB.
		//initPreloadedDb(serializeFromDb);
		
	    doSerialize(serializeFromDb, marshOutOne);
		
		//For tests to retrieve all data via services rather than traversing the tree.
	    //doSerializeFlat(serializeFromDb, marshOutOne);
	    
		doDeserialize(deserializeToDb, marshOutOne);
	    
		//doSerialize(deserializeToDb, marshOutTwo);
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
	    List<Synonym> synonyms;
	    List<AnnotatableEntity> homotypicalGroups;

	    agents = new ArrayList<Agent>();
		agentData = new ArrayList<VersionableEntity>();
		terms = new ArrayList<TermBase>();
	    references = new ArrayList<ReferenceBase>();
		taxonomicNames = new ArrayList<TaxonNameBase>();
		taxa = new ArrayList<Taxon>();
		synonyms = new ArrayList<Synonym>();
		
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
