package eu.etaxonomy.cdm.test.function;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.impl.SessionFactoryImpl;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
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
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class Datasource {
	private static final Logger logger = Logger.getLogger(Datasource.class);

	
	private void testNewConfigControler(){
		List<CdmPersistentDataSource> lsDataSources = CdmPersistentDataSource.getAllDataSources();
		System.out.println(lsDataSources);
		CdmPersistentDataSource dataSource = lsDataSources.get(0);
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		CdmPersistentDataSource.save(dataSource.getName(), dbType, "192.168.2.10", "cdm_test_andreas", "edit", "");
		CdmApplicationController appCtr;
		try {
			appCtr = CdmApplicationController.NewInstance(dataSource);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}

	}
	
	private void testDatabaseChange(){
		CdmApplicationController appCtr;
		try {
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
			
			appCtr.getDatabaseService().saveDataSource("testSqlServer", dbType, server, database, username, password);
			appCtr.getDatabaseService().connectToDatabase(dbType, server, database, username, password);
			
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}

	private void testSqlServer(){
		DbSchemaValidation validation = DbSchemaValidation.CREATE;
		CdmDataSource ds = 
			CdmDataSource.NewSqlServer2005Instance("LENOVO-T61", "NielsTest", "Niels", "test");
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
			appCtr.getAgentService().saveAgent(agent);
			TaxonNameBase tn = BotanicalName.NewInstance(null);
			appCtr.getNameService().saveTaxonName(tn);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
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
		CdmPersistentDataSource ds = CdmPersistentDataSource.save("testSqlServer", databaseTypeEnum, server, database, username, password);
		try {
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
			Person agent = Person.NewInstance();
			appCtr.getAgentService().saveAgent(agent);
			TaxonNameBase tn = BotanicalName.NewInstance(null);
			appCtr.getNameService().saveTaxonName(tn);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	private void testPostgreServer(){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.PostgreSQL;
		String server = "192.168.1.17";
		String database = "cdm_test";
		String username = "edit";
		String password = "";
		CdmPersistentDataSource ds = CdmPersistentDataSource.save("PostgreTest", databaseTypeEnum, server, database, username, password);
		try {
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
			Person agent = Person.NewInstance();
			appCtr.getAgentService().saveAgent(agent);
			TaxonNameBase tn = BotanicalName.NewInstance(null);
			appCtr.getNameService().saveTaxonName(tn);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	private void testLocalHsql(){
		try {
			CdmPersistentDataSource ds = CdmPersistentDataSource.NewLocalHsqlInstance();
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
			try {
				List l = appCtr.getNameService().getAllNames(5, 1);
				System.out.println(l);
				//Agent agent = new Agent();
				//appCtr.getAgentService().saveAgent(agent);
				appCtr.close();
			} catch (RuntimeException e) {
				logger.error("Runtime Exception");
				e.printStackTrace();
				appCtr.close();
				
			}
		} catch (DataSourceNotFoundException e) {
			logger.error("LOCAL HSQL");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	private void testLocalH2(){
		try {
			DbSchemaValidation validation = DbSchemaValidation.CREATE;
			ICdmDataSource ds = 
				CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "");
//			ds =
//				 CdmPersistentDataSource.NewInstance("localH2");
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds, validation);
			
			boolean exists = appCtr.getUserService().userExists("admin");
			try {
				BotanicalName name = BotanicalName.NewInstance(null);
				String nameCache = "testName";
				name.setNameCache(nameCache);
				name.setTitleCache(nameCache);
				ReferenceBase ref = Generic.NewInstance();
				ref.setTitleCache("mySec");
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
		} catch (DataSourceNotFoundException e) {
			logger.error("Error in LOCAL HSQL");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
		
	private boolean testWritableResourceDirectory() throws IOException{
		CdmApplicationUtils.getWritableResourceDir();
		return true;
	}

	private boolean testH2(){
		testLocalH2();
		if (true)return true;
		try{
			DbSchemaValidation validation = DbSchemaValidation.CREATE;
			ICdmDataSource ds = 
				CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "");
				//CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "");
	//		ds =
	//			 CdmPersistentDataSource.NewInstance("localH2");
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds, validation);
			try {
				StrictReferenceBase ref = Generic.NewInstance();
				ref.setTitle("SdfEWsddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
						   "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
						   "dwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww" +
						   "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
				appCtr.getReferenceService().save(ref);
				
				SessionFactoryImpl sf = (SessionFactoryImpl)appCtr.getSessionFactory();
				//sf.get
				Session session;
				//session.get
				Map cmd = sf.getAllClassMetadata();
				for (Object o: cmd.keySet()){
					Object value = cmd.get(o);
					try {
						Class.forName((String)value);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(value);
				}
				
				TaxonNameBase name = BotanicalName.NewInstance(null);
				UUID uuid = appCtr.getNameService().saveOrUpdate(name);
				List l = appCtr.getNameService().getAllNames(5, 1);
				System.out.println(l);
				//Agent agent = new Agent();
				//appCtr.getAgentService().saveAgent(agent);
				appCtr.close();
				return true;
			} catch (RuntimeException e) {
				logger.error("Runtime Exception");
				e.printStackTrace();
				appCtr.close();
				
			}
		} catch (DataSourceNotFoundException e) {
			logger.error("Error in LOCAL HSQL");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
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
