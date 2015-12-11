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

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;

import org.apache.log4j.Logger;
import org.hibernate.mapping.Column;
import org.junit.Ignore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.config.CdmPersistentSourceUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * <h2>NOTE</h2>
 * This is a test for sole development purposes, it is not
 * touched by mvn test since it is not matching the "\/**\/*Test" pattern,
 * but it should be annotate with @Ignore when running the project a s junit suite in eclipse
 *
 */
@Ignore
public class TestDatabase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TestDatabase.class);

	private void test(){
		System.out.println("Start TestDatabase");
		//testNewDatabaseConnection();
		//testFacts();
//		testNewDatasourceClass();
		testHybridRelationships();
	//	testPaddie();
		System.out.println("\nEnd TestDatabase");
	}

	public void testNewDatabaseConnection() throws DataSourceNotFoundException{

		Column coL;
		boolean omitTermLoading = false;
		Resource applicationContextResource = new ClassPathResource(CdmApplicationController.DEFAULT_APPLICATION_CONTEXT_RESOURCE);
		CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewDefaultInstance();
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(applicationContextResource, dataSource, DbSchemaValidation.CREATE, omitTermLoading);
		appCtr.NewConversation();
		appCtr.NewConversation();

		//CdmApplicationController appCtr = CdmApplicationController.NewInstance(DbSchemaValidation.CREATE);
		appCtr.close();
	}

	public void testNewDatasourceClass(){
//			String server = "192.168.2.10";
//			String database = "cdm_test_andreasM";
//			String username = "edit";
//			String password = CdmUtils.readInputLine("Password: ");
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;

//			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, database, username, password);
		ICdmDataSource datasource = CdmDataSource.NewH2EmbeddedInstance("CDM", "sa", "", null);
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);

		Rank genus = Rank.GENUS();
		BotanicalName botanicalName = BotanicalName.NewInstance(genus);
		botanicalName.setGenusOrUninomial("GenusName");

		IJournal journal = ReferenceFactory.newJournal();
		journal.setTitleCache("Afro+Doc", true);

		//			Taxon taxon = Taxon.NewInstance(botanicalName, journal);
//			Taxon taxon2 = Taxon.NewInstance(botanicalName2, null);
//		botanicalName.getTitleCache();

		Taxon taxon1 = Taxon.NewInstance(botanicalName,(Reference)journal);
		appCtr.getTaxonService().save(taxon1);
		BotanicalName homotypName = BotanicalName.NewInstance(Rank.SUBGENUS(), botanicalName.getHomotypicalGroup());
		homotypName.setGenusOrUninomial("Subgenus");
		homotypName.setInfraGenericEpithet("homotyp");

		//Synonym synonym = Synonym.NewInstance(homotypName, journal);

		System.out.println("Taxa of " + botanicalName + ": " + botanicalName.getTaxonBases());
		System.out.println("Synonyms of " + homotypName + ": " + homotypName.getSynonyms());

		HomotypicalGroup homotypicalGroup = taxon1.getHomotypicGroup();
		System.out.println("HomotypicNames of " + botanicalName + ":" + homotypicalGroup.getTypifiedNames());
		System.out.println("HomotypicSynonymsByGroup of " + taxon1 + ":" + taxon1.getHomotypicSynonymsByHomotypicGroup());
		System.out.println("HomotypicSynonymsBySynonymy of " + taxon1 + ":" + taxon1.getHomotypicSynonymsByHomotypicRelationship());

//			appCtr.getTaxonService().saveTaxon(taxon2);
//			appCtr.getTaxonService().saveTaxon(taxon);

		appCtr.close();
	}


	public void testHybridRelationships(){

//			String database = "cdm";
//			String username = "sa";

		String server = "192.168.2.10";
		String database = "cdm_test_andreasM";
		String username = "edit";
		String password = CdmUtils.readInputLine("Password: ");
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, database, username, password);
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);

		Rank genus = Rank.GENUS();
		BotanicalName parentName = BotanicalName.NewInstance(genus);
		parentName.setGenusOrUninomial("parent");

		BotanicalName childName = BotanicalName.NewInstance(genus);
		childName.setGenusOrUninomial("child");
		parentName.addHybridChild(childName, HybridRelationshipType.FIRST_PARENT(), null);

		//save
		appCtr.getNameService().save(parentName);


		appCtr.close();
	}

	public void testPaddie(){
		String server = "PADDIE";
		String database = "edit_test";
		String username = "andreas";
		String password = CdmUtils.readInputLine("Password: ");
		DbSchemaValidation validation = DbSchemaValidation.VALIDATE;
		ICdmDataSource datasource = CdmDataSource.NewSqlServer2005Instance(server, database, -1, username, password, null);
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, validation);

		Rank genus = Rank.GENUS();
		BotanicalName botanicalName = BotanicalName.NewInstance(genus);
		botanicalName.setGenusOrUninomial("GenusName");

		IJournal journal = ReferenceFactory.newJournal();
		journal.setTitle("JournalTitel");

		//			Taxon taxon = Taxon.NewInstance(botanicalName, journal);
//			Taxon taxon2 = Taxon.NewInstance(botanicalName2, null);
//		botanicalName.getTitleCache();
		Rank.SPECIES();
		appCtr.getNameService().save(botanicalName);

//			appCtr.getTaxonService().saveTaxon(taxon2);
//			appCtr.getTaxonService().saveTaxon(taxon);

		appCtr.close();
	}


	public void testContact(){
//			String server = "192.168.2.10";
//			String database = "cdm_test_andreasM";
//			String username = "edit";
//			String password = CdmUtils.readInputLine("Password: ");
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;

//			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, database, username, password);
		//ICdmDataSource datasource = CdmDataSource.NewH2EmbeddedInstance("CDM", "sa", "");
		ICdmDataSource datasource = cdm_test_anahit2();
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
		Person person = Person.NewTitledInstance("TestPerson");
		Contact contact1 = new Contact();
		Set<String> set = new HashSet<String>();
		set.add("email1");
		set.add("c@d.org");
//			contact1.setEmail(set);
//			person.setContact(contact1);
		appCtr.getAgentService().save(person);
		appCtr.close();
		System.out.println("End");
	}


	public void testNewVersion(){
		System.out.println("Start");
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;

//			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, database, username, password);
		ICdmDataSource datasource = CdmDataSource.NewH2EmbeddedInstance("CDM", "sa", "", null);
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
		BotanicalName botName = BotanicalName.NewInstance(Rank.SPECIES());
		botName.setGenusOrUninomial("Genus");
		botName.setSpecificEpithet("species");
		appCtr.getNameService().save(botName);
		List<?> names = appCtr.getNameService().findNamesByTitle("Genus species");

		names.size();
//			AgentBase person = Person.NewTitledInstance("TestPerson");
//			Contact contact1 = new Contact();
//			Set<String> set = new HashSet<String>();
//			set.add("email1");
//			set.add("c@d.org");
////			contact1.setEmail(set);
//			person.setContact(contact1);
//			appCtr.getAgentService().save(person);
		appCtr.close();
		System.out.println("End");
	}

	public void testDataSourceWithNomenclaturalCode(){
		String dataSourceName = "test";
		NomenclaturalCode code = NomenclaturalCode.ICZN;
//		ICdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("test", "sa", "", code);
		String password = CdmUtils.readInputLine("Password: ");
		ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test_niels2", 3306, "edit", password, code);
		CdmPersistentDataSource.save(dataSourceName, dataSource);

		try {
			CdmPersistentDataSource loadedDataSource = CdmPersistentDataSource.NewInstance(dataSourceName);
//			CdmApplicationController.NewInstance(loadedDataSource, DbSchemaValidation.CREATE);

			NomenclaturalCode loadedCode = loadedDataSource.getNomenclaturalCode();
			Assert.assertEquals(code, loadedCode);

			CdmPersistentSourceUtils.delete(loadedDataSource);

		} catch (DataSourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public void testLoadedAnnotationGetAnnotatedObjectCall(){
		String password = CdmUtils.readInputLine("Password: ");
		ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test_niels2", 3306, "edit", password, NomenclaturalCode.ICNAFP);

		CdmApplicationController appCtr = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.UPDATE);

		ConversationHolder conversation = appCtr.NewConversation();

		// make the taxon and description elements
		Taxon taxon = Taxon.NewInstance(null, null);

		TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);

		UUID taxonDescriptionUuid = taxonDescription.getUuid();

		Feature featureAnatomy = Feature.ANATOMY();

		TextData textData = TextData.NewInstance();
		textData.addAnnotation(Annotation.NewInstance(null, null));

		assertNotNull(textData.getAnnotations().iterator().next().getAnnotatedObj());

		textData.setFeature(featureAnatomy);

		taxonDescription.addElement(textData);

		appCtr.getTaxonService().save(taxon);

		conversation.commit(false);
		// end of creation phase


		// load the new taxon in a new conversation to assure that it was loaded into a new session
		// if you are willing to blame it on conversations, please rewrite into two methods
		// the result will be the same
		ConversationHolder newConversation = appCtr.NewConversation();
		DescriptionBase<?> loadedDescription = appCtr.getDescriptionService().load(taxonDescriptionUuid);

		TextData descriptionElement = (TextData) loadedDescription.getElements().iterator().next();

		Annotation annotation = descriptionElement.getAnnotations().iterator().next();

		// this should not be null
		assertNotNull(annotation.getAnnotatedObj());

	}


	public static ICdmDataSource cdm_test_anahit2(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_anahit2";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}

	/**
	 * initializes source
	 * @return true, if connection establisehd
	 */
	private static ICdmDataSource makeDestination(String cdmServer, String cdmDB, int port, String cdmUserName, String pwd ){
		//establish connection
		pwd = AccountStore.readOrStorePassword(cdmServer, cdmDB, cdmUserName, pwd);
		//TODO not MySQL
		ICdmDataSource destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB, port, cdmUserName, pwd, null);
		return destination;

	}

	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestDatabase sc = new TestDatabase();
//    	sc.testNewDatabaseConnection();
//		sc.testDataSourceWithNomenclaturalCode();
		sc.testLoadedAnnotationGetAnnotatedObjectCall();
	}

}
