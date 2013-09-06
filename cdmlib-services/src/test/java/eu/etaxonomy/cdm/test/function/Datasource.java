/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.function;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CdmUpdater;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class Datasource {
	private static final Logger logger = Logger.getLogger(Datasource.class);

	
	private void testNewConfigControler(){
		List<CdmPersistentDataSource> lsDataSources = CdmPersistentDataSource.getAllDataSources();
		DbSchemaValidation schema = DbSchemaValidation.VALIDATE;
		System.out.println(lsDataSources);
//		CdmPersistentDataSource dataSource = lsDataSources.get(0);
//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		
		String server = "localhost";
//		String database = "cdm_test";
		String database = "test";
		String username = "edit";
		ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));
		
		//SQLServer
		database = "CDMTest";
		int port = 1433;
		username = "pesiexport";
//		dataSource = CdmDataSource.NewSqlServer2005Instance(server, database, port, username, AccountStore.readOrStorePassword(server, database, username, null));
		
		//H2
		username = "sa";
//		dataSource = CdmDataSource.NewH2EmbeddedInstance(database, username, "sa", NomenclaturalCode.ICNAFP);
		
		
		CdmUpdater updater = new CdmUpdater();
		updater.updateToCurrentVersion(dataSource, DefaultProgressMonitor.NewInstance());
		
		
		//CdmPersistentDataSource.save(dataSource.getName(), dataSource);
		CdmApplicationController appCtr;
		appCtr = CdmApplicationController.NewInstance(dataSource,schema);
		
		String taxonNameStr = StringUtils.repeat("a", 750);
		TaxonNameBase<?,?> name = BotanicalName.NewInstance(Rank.GENUS());
		name.setTitleCache(taxonNameStr, true);
		appCtr.getNameService().save(name);
		appCtr.close();
	}
	
	private void testDatabaseChange() throws DataSourceNotFoundException{
		CdmApplicationController appCtr;
		appCtr = CdmApplicationController.NewInstance();
	
//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
//		String server = "192.168.2.10";
//		String database = "cdm_test_andreas";
//		String user = "edit";
//		String pwd = "wp5";
//		
		DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer2005;
		String server = "LAPTOPHP";
		String database = "cdmTest";
		String username = "edit";
		String password = "";
		
		ICdmDataSource dataSource = CdmDataSource.NewInstance(DatabaseTypeEnum.SqlServer2005, "LAPTOPHP", "cdmTest", DatabaseTypeEnum.SqlServer2005.getDefaultPort(), "edit", "");
		
		appCtr.getDatabaseService().saveDataSource("testSqlServer", dataSource);
		try {
			appCtr.getDatabaseService().connectToDatabase(dbType, server, database, username, password);
		} catch (TermNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		appCtr.close();
	}

	private void testSqlServer2005(){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.SqlServer2005;
		String server = "LAPTOPHP";
		String database = "cdmTest";
		String username = "edit";
		String password = "";
		
		ICdmDataSource dataSource = CdmDataSource.NewInstance(databaseTypeEnum, server, database, databaseTypeEnum.getDefaultPort(), username, password); 
		
		CdmPersistentDataSource ds = CdmPersistentDataSource.save("testSqlServer", dataSource);

		CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
		Person agent = Person.NewInstance();
		appCtr.getAgentService().save(agent);
		TaxonNameBase<?,?> tn = BotanicalName.NewInstance(null);
		appCtr.getNameService().save(tn);
		appCtr.close();
		
	}
	
	private void testPostgreServer(){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.PostgreSQL;
		String server = "192.168.1.17";
		String database = "cdm_test";
		String username = "edit";
		String password = "";
		
		ICdmDataSource dataSource = CdmDataSource.NewInstance(databaseTypeEnum, server, database, databaseTypeEnum.getDefaultPort(), username, password); 
		
		CdmPersistentDataSource ds = CdmPersistentDataSource.save("PostgreTest", dataSource);

		CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
		Person agent = Person.NewInstance();
		appCtr.getAgentService().save(agent);
		TaxonNameBase<?,?> tn = BotanicalName.NewInstance(null);
		appCtr.getNameService().save(tn);
		appCtr.close();

	}
	
	private void testLocalHsql() throws DataSourceNotFoundException{
		CdmApplicationController appCtr = null;
		try {
			CdmPersistentDataSource ds = CdmPersistentDataSource.NewLocalHsqlInstance();
			appCtr = CdmApplicationController.NewInstance(ds);
			List<?> l = appCtr.getNameService().list(null,5, 1,null,null);
			System.out.println(l);
			//Agent agent = new Agent();
			//appCtr.getAgentService().saveAgent(agent);
			appCtr.close();
		} catch (RuntimeException e) {
			logger.error("Runtime Exception");
			e.printStackTrace();
			appCtr.close();
			
		} catch (DataSourceNotFoundException e) {
			logger.error("Runtime Exception");
			e.printStackTrace();
		}
	}
	
	private void testLocalH2(){
		
		DbSchemaValidation validation = DbSchemaValidation.CREATE;
		ICdmDataSource ds = 
			CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "", null);
//			ds =
//				 CdmPersistentDataSource.NewInstance("localH2");
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds, validation);
		
		boolean exists = appCtr.getUserService().userExists("admin");
		try {
			BotanicalName name = BotanicalName.NewInstance(null);
			String nameCache = "testName";
			name.setNameCache(nameCache);
			name.setTitleCache(nameCache, true);
			Reference<?> ref = ReferenceFactory.newGeneric();
			ref.setTitleCache("mySec", true);
			Taxon taxon = Taxon.NewInstance(name, ref);
			TaxonDescription description = TaxonDescription.NewInstance();
			taxon.addDescription(description);
			NamedArea area1 = appCtr.getTermService().getAreaByTdwgAbbreviation("GER");
			Distribution distribution = Distribution.NewInstance(area1, PresenceTerm.PRESENT());
			description.addElement(distribution);
			
			List<Distribution> distrList = new ArrayList<Distribution>();
			distrList.add(distribution);
			List<NamedArea> areaList = new ArrayList<NamedArea>();
			areaList.add(area1);
			
		//	distribution.getInDescription().get
			appCtr.getTaxonService().save(taxon);

			System.out.println(taxon.getDescriptions().size());
			
			TransactionStatus txStatus = appCtr.startTransaction();
			
			Session session = appCtr.getSessionFactory().getCurrentSession();
			
			//String hqlQuery = "from DescriptionBase d join d.elements  as e " 
//				String hqlQuery = "from Taxon t join t.descriptions  as d "+
//				 " inner join d.elements e on e member of d "
//				+
//				"";//" where e.area = :namedArea " ; 
			String hqlQuery = "Select t from Distribution e join e.inDescription d join d.taxon t join t.name n "+
				" WHERE e.area in (:namedArea) AND n.nameCache = :nameCache ";				
			Query query = session.createQuery(hqlQuery);
			
			//query.setEntity("namedArea", area1);
			query.setParameter("nameCache", nameCache);
			query.setParameterList("namedArea", areaList);
			List resultList = query.list();
			//List list = appCtr.getCommonService().getHqlResult(hqlQuery);
			
			for (Object o:resultList){
				System.out.println(o);
			}
			appCtr.commitTransaction(txStatus);
			
			//System.out.println(l);
			//Agent agent = new Agent();
			//appCtr.getAgentService().saveAgent(agent);
			appCtr.close();
		} catch (RuntimeException e) {
			logger.error("Runtime Exception");
			e.printStackTrace();
			appCtr.close();
			
		}
	}
		
	private boolean testWritableResourceDirectory() throws IOException{
		CdmApplicationUtils.getWritableResourceDir();
		return true;
	}

	private boolean testH2(){
//		testLocalH2();
//		if (true)return true;

		DbSchemaValidation validation = DbSchemaValidation.CREATE;
		ICdmDataSource ds = 
			CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "", null);
			//CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "");
//		ds =
//			 CdmPersistentDataSource.NewInstance("localH2");
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds, validation);
		try {
			BotanicalName botName1 = BotanicalName.NewInstance(Rank.SPECIES());
			BotanicalName botName2 = BotanicalName.NewInstance(Rank.SPECIES());
			BotanicalName hybridName = BotanicalName.NewInstance(Rank.SPECIES());
			botName1.addRelationshipToName(botName2, NameRelationshipType.ORTHOGRAPHIC_VARIANT(), null);
			UUID uuid1 = botName1.getUuid();
			UUID uuid2 = botName2.getUuid();
			try {
				Logger loggerTrace = logger.getLogger("org.hibernate.type");
				loggerTrace.setLevel(Level.TRACE);
				System.out.println(logger.getName());
				
				appCtr.getNameService().save(botName1);
				ResultSet rs = ds.executeQuery("Select count(*) as n FROM NameRelationship");
				rs.next();
				int c = rs.getInt("n");
				System.out.println("Begin :" + c);
				
				botName1.removeRelationToTaxonName(botName2);
				botName1.setSpecificEpithet("DELETED");
				botName2.addHybridParent(hybridName, HybridRelationshipType.FIRST_PARENT(), null);
				
				TransactionStatus tx = appCtr.startTransaction();
				appCtr.getNameService().saveOrUpdate(botName2);
				rs = ds.executeQuery("Select count(*) as n FROM NameRelationship");
				rs.next();
				c = rs.getInt("n");
				System.out.println("End: " + c);
	
				appCtr.commitTransaction(tx);
				
				appCtr.getNameService().saveOrUpdate(botName1);
				
				rs = ds.executeQuery("Select count(*) as n FROM NameRelationship");
				rs.next();
				c = rs.getInt("n");
				System.out.println("End: " + c);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//Agent agent = new Agent();
			//appCtr.getAgentService().saveAgent(agent);
			appCtr.close();
			return true;
		} catch (RuntimeException e) {
			logger.error("Runtime Exception");
			e.printStackTrace();
			appCtr.close();
			
		}
		return false;
	}
	
	private void test(){
		System.out.println("Start Datasource");
		testNewConfigControler();
    	//testDatabaseChange();
		
		//testSqlServer();
		
		//CdmUtils.findLibrary(au.com.bytecode.opencsv.CSVReader.class);
		//testPostgreServer();
		//testLocalHsql();
		//testLocalH2();
		//testWritableResourceDirectory();
//		testH2();
		System.out.println("\nEnd Datasource");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		Datasource cc = new Datasource();
    	cc.test();
	}

}
