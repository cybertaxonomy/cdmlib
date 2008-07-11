package eu.etaxonomy.cdm.test.function;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.jaxb.CdmDocumentBuilder;
import eu.etaxonomy.cdm.model.DataSet;
import eu.etaxonomy.cdm.model.DataSetTest;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;


public class TestJaxb {
	
	private static final Logger logger = Logger.getLogger(TestJaxb.class);
	
	private static final String dbName = "cdm_test_jaxb";
	private String server = "192.168.2.10";
	private String username = "edit";
	private String marshOut = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_marshalled.xml");

	private CdmDocumentBuilder cdmDocumentBuilder = null;
	
    public TestJaxb() {	
    }

    public void testInitDb() {
    	
		CdmApplicationController appCtr = null;
    	
		try {
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbName, username, password);
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
    
	public void testSerialize(){
		
		CdmApplicationController appCtr = null;

		try {
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbName, username, password);
			appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			

		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
		TransactionStatus txStatus = appCtr.startTransaction();
		DataSet dataSet = new DataSet();

		// get data from DB

		try {

			logger.info("Load data from DB ...");
			
			dataSet.setAgents(appCtr.getAgentService().getAllAgents(10, 0));
			dataSet.setTaxonomicNames(appCtr.getNameService().getAllNames(10, 0));
			dataSet.setReferences(appCtr.getReferenceService().getAllReferences(10, 0));

			// load Root taxa 
			
			List<Taxon> taxa = 
				appCtr.getTaxonService().getRootTaxa(null, CdmFetch.FETCH_CHILDTAXA(), false);
			
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
			//TODO: Store the children taxa as well
			dataSet.setTaxa(taxa);
			
		} catch (Exception e) {
			logger.error("data retrieving error");
		}

		try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
			cdmDocumentBuilder.marshal(dataSet, new FileWriter(marshOut));

		} catch (Exception e) {
			logger.error("marshalling error");
		} 
		appCtr.commitTransaction(txStatus);
		appCtr.close();
	}
	
	public void testDeserialize() {
		
		CdmApplicationController appCtr = null;

		try {
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbName, username, password);
			appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}

		TransactionStatus txStatus = appCtr.startTransaction();
		DataSet dataSet = new DataSet();
		
        // unmarshalling test XML file
		
		try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
			dataSet = cdmDocumentBuilder.unmarshal(dataSet, new File(marshOut));

		} catch (Exception e) {
			logger.error("unmarshalling error");
		} 
		
		// save data in DB
		
		// Currently it's sufficient to save the taxa only since all other data
		// related to the taxa, such as synonyms, are automatically saved as well.
		// FIXME: Clean getTaxa()/getTaxonBases() return parameters.
		
		appCtr.getTaxonService().saveTaxonAll(dataSet.getTaxonBases());

		appCtr.commitTransaction(txStatus);
		appCtr.close();

	}
	
	private void test(){
		
		System.out.println("Start Initializing");
		//testInitDb();
		System.out.println("\nEnd Initializing");

		System.out.println("\nStart Serializing");
		//testSerialize();
		System.out.println("\nEnd Serializing");
		
		System.out.println("\nStart Deserializing");
		testDeserialize();
		System.out.println("\nEnd Deserializing");
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
		BotanicalName name1, name2, nameRoot, nameFree, synName11, synName12, synName2, synNameFree;
		Taxon child1, child2, rootT, freeT;
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
		
//		rankSpecies = new Rank ();
//		rankSubspecies = new Rank();
//		rankGenus = new Rank();
		
//      Do something like this? If yes, FIXME: Stack overflow.
//		try {
//			rankSpecies = Rank.getRankByName("Species");
//			rankSubspecies = Rank.getRankByName("Subspecies");
//			rankGenus = Rank.getRankByName("Genus");
//			
//		} catch (UnknownCdmTypeException ex) {
//			ex.printStackTrace();
//		}
		
//		terms.add(rankSpecies);
//		terms.add(rankSubspecies);
//		terms.add(rankGenus);
		
		terms.add(keyword);
		
        // taxonomic names
		
		nameRoot = BotanicalName.NewInstance(rankGenus,"Calendula",null,null,null,linne,null,"p.100", null);
		
		name1 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"arvensis",null,linne,null,"p.1", null);
		synName11 = BotanicalName.NewInstance(rankSpecies,"Caltha",null,"arvensis",null,linne,null,"p.11", null);
		synName12 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"sancta",null,linne,null,"p.12", null);
		
		name2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzae",null,linne,null,"p.2", null);
		synName2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"echinata",null,linne,null,"p.2", null);
		
		nameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"intybus",null,linne,null,"p.200", null);
		synNameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"balearicum",null,linne,null,"p.2", null);

		taxonomicNames.add(nameRoot);
		taxonomicNames.add(name1);
		taxonomicNames.add(synName11);
		taxonomicNames.add(synName12);
		taxonomicNames.add(name2);
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
		
		rootT.addTaxonomicChild(child1, sec, "p.998");
		rootT.addTaxonomicChild(child2, sec, "p.987");
				
		taxa.add(rootT);
		taxa.add(freeT);
		taxa.add(child1);
		taxa.add(child2);
		
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
