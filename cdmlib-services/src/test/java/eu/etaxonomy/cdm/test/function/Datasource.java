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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class Datasource {
	private static final Logger logger = Logger.getLogger(Datasource.class);

	
	private void testNewConfigControler(){
		List<CdmPersistentDataSource> lsDataSources = CdmPersistentDataSource.getAllDataSources();
		System.out.println(lsDataSources);
		CdmPersistentDataSource dataSource = lsDataSources.get(0);
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
//		ICdmDataSource dataSource = CdmDataSource.NewInstance(dbType, "192.168.2.10", "cdm_test_andreas", dbType.getDefaultPort() + "", "edit", "", null, null);
		CdmPersistentDataSource.save(dataSource.getName(), dataSource);
		CdmApplicationController appCtr;
		appCtr = CdmApplicationController.NewInstance(dataSource);
		appCtr.close();
	}
	
	private void testDatabaseChange(){
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

	private void testSqlServer(){
		DbSchemaValidation validation = DbSchemaValidation.CREATE;
		CdmDataSource ds = 
			CdmDataSource.NewSqlServer2005Instance("LENOVO-T61", "NielsTest", -1, "Niels", "test", null);
			//CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "");
//		ds =
//			 CdmPersistentDataSource.NewInstance("localH2");
		try {
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds, validation);
			String sql = "SELECT name, id FROM sys.sysobjects WHERE (xtype = 'U')"; //all tables
			ResultSet rs = ds.executeQuery(sql);
			while (rs.next()){
				String tableName = rs.getString("name");
				long tableId = rs.getLong("id");
				sql = "SELECT name FROM sys.sysobjects WHERE xtype='F' and parent_obj = " +  tableId;//get foreignkeys
				ResultSet rsFk = ds.executeQuery(sql);
				while (rsFk.next()){
					String fk = rsFk.getString("name");
					sql = " ALTER TABLE "+tableName+" DROP CONSTRAINT "+fk + "";
					ds.executeUpdate(sql);
				}
				
			}
			
			Person agent = Person.NewInstance();
			appCtr.getAgentService().save(agent);
			TaxonNameBase tn = BotanicalName.NewInstance(null);
			appCtr.getNameService().save(tn);
			appCtr.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		TaxonNameBase tn = BotanicalName.NewInstance(null);
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
		TaxonNameBase tn = BotanicalName.NewInstance(null);
		appCtr.getNameService().save(tn);
		appCtr.close();

	}
	
	private void testLocalHsql(){

		CdmPersistentDataSource ds = CdmPersistentDataSource.NewLocalHsqlInstance();
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
		try {
			List l = appCtr.getNameService().list(null,5, 1,null,null);
			System.out.println(l);
			//Agent agent = new Agent();
			//appCtr.getAgentService().saveAgent(agent);
			appCtr.close();
		} catch (RuntimeException e) {
			logger.error("Runtime Exception");
			e.printStackTrace();
			appCtr.close();
			
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
			ReferenceFactory refFactory = ReferenceFactory.newInstance();
			ReferenceBase ref = refFactory.newGeneric();
			ref.setTitleCache("mySec", true);
			Taxon taxon = Taxon.NewInstance(name, ref);
			TaxonDescription description = TaxonDescription.NewInstance();
			taxon.addDescription(description);
			NamedArea area1 = TdwgArea.getAreaByTdwgAbbreviation("GER");
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
		//testNewConfigControler();
    	//testDatabaseChange();
		
		//testSqlServer();
		
		//CdmUtils.findLibrary(au.com.bytecode.opencsv.CSVReader.class);
		//testPostgreServer();
		//testLocalHsql();
		//testLocalH2();
		//testWritableResourceDirectory();
		testH2();
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
