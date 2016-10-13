/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;

import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @created 18.12.2008
 */
public class CdmDataSourceTest {
	private static final Logger logger = Logger.getLogger(CdmDataSourceTest.class);

	private static String server;
	private static String database;
	private static String username;
	private static String pwd;
	private static int port;
	private static ICdmDataSource datasource;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		server = "myServer";
		database = "myDatabase";
		username = "myUsername";
		pwd = "myPassword";
		port = 80;
		datasource = CdmDataSource.NewMySqlInstance(server, database, port, username, pwd);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//*************** TESTS ***********************************************

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#NewMySqlInstance(java.lang.String, java.lang.String, java.lang.String, java.lang.String, NomenclaturalCode)}.
	 */
	@Test
	public void testNewMySqlInstanceStringStringStringString() {
		ICdmDataSource ds = CdmDataSource.NewMySqlInstance(server, database, username, pwd);
		assertNotNull("NewMySqlInstance(String, String, String, String) should not return null ", ds);
		assertEquals(server, ds.getServer());
		assertEquals(database, ds.getDatabase());
		assertEquals(username, ds.getUsername());
		assertEquals(pwd, ds.getPassword());
		assertEquals(DatabaseTypeEnum.MySQL, ds.getDatabaseType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#NewMySqlInstance(java.lang.String, java.lang.String, int, java.lang.String)}.
	 */
	@Test
	public void testNewMySqlInstanceStringStringIntStringString() {
		ICdmDataSource ds = CdmDataSource.NewMySqlInstance(server, database, port, username, pwd);
		assertNotNull("NewMySqlInstance(String, String, int, String, String) should not return null ", ds);
		assertEquals(server, ds.getServer());
		assertEquals(database, ds.getDatabase());
		assertEquals(username, ds.getUsername());
		assertEquals(pwd, ds.getPassword());
		assertEquals(port, ds.getPort());
		assertEquals(DatabaseTypeEnum.MySQL, ds.getDatabaseType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#NewSqlServer2005Instance(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testNewSqlServer2005InstanceStringStringStringString() {
		ICdmDataSource ds = CdmDataSource.NewSqlServer2005Instance(server, database, -1, username, pwd);
		assertNotNull("NewSqlServer2005Instance(String, String, String, String) should not return null ", ds);
		assertEquals(server, ds.getServer());
		assertEquals(database, ds.getDatabase());
		assertEquals(username, ds.getUsername());
		assertEquals(pwd, ds.getPassword());
		assertEquals(DatabaseTypeEnum.SqlServer2005, ds.getDatabaseType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#NewSqlServer2005Instance(java.lang.String, java.lang.String, int, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testNewSqlServer2005InstanceStringStringIntStringString() {
		ICdmDataSource ds = CdmDataSource.NewSqlServer2005Instance(server, database, port, username, pwd);
		assertNotNull("NewSqlServer2005Instance(String, String, String, String) should not return null ", ds);
		assertEquals(server, ds.getServer());
		assertEquals(database, ds.getDatabase());
		assertEquals(username, ds.getUsername());
		assertEquals(pwd, ds.getPassword());
		assertEquals(port, ds.getPort());
		assertEquals(DatabaseTypeEnum.SqlServer2005, ds.getDatabaseType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#NewH2EmbeddedInstance(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testNewH2EmbeddedInstanceStringStringString() {
		ICdmDataSource ds = CdmDataSource.NewH2EmbeddedInstance(database, username, pwd, null);
		assertNotNull("NewH2EmbeddedInstance(String, String, String) should not return null ", ds);
		assertEquals(null, ds.getServer());  //TODO right?
		assertEquals(database, ds.getDatabase());
		assertEquals(username, ds.getUsername());
		assertEquals(pwd, ds.getPassword());
		assertEquals(DatabaseTypeEnum.H2, ds.getDatabaseType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#NewH2InMemoryInstance()}.
	 */
	@Test
	public void testNewH2InMemoryInstance() {
		ICdmDataSource ds = CdmDataSource.NewH2InMemoryInstance();
		assertNotNull("NewH2InMemoryInstance() should not return null ", ds);
		assertEquals(DatabaseTypeEnum.H2, ds.getDatabaseType());
		assertEquals(null, ds.getServer());
		assertEquals(null, ds.getDatabase());
		assertEquals("sa", ds.getUsername()); //TODO right?
		assertEquals("", ds.getPassword());
		assertEquals(DatabaseTypeEnum.H2, ds.getDatabaseType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#CdmDataSource(eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.database.H2Mode)}.
	 */
	@Test
	public void testCdmDataSource() {
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		H2Mode h2Mode = H2Mode.EMBEDDED;
		String filePath = "path";
		ICdmDataSource ds = new CdmDataSource(dbType, server, database, port, username, pwd, filePath, h2Mode);
		assertNotNull("new datasource() should not be null", ds);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getName()}.
	 */
	@Test
	public void testGetName() {
		assertNotNull("datasource should not be null", datasource);
		assertEquals(database, datasource.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getDatasourceBean()}.
	 */
	@Test
	public void testGetDatasourceBean() {
		BeanDefinition bean = datasource.getDatasourceBean();
		assertNotNull(bean);
		assertEquals("the bean definition should have 4 properties: url, driverClassname, username, password", 4, bean.getPropertyValues().size());
		//TODO to be continued
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getHibernatePropertiesBean(eu.etaxonomy.cdm.database.DbSchemaValidation)}.
	 */
	@Test
	public void testGetHibernatePropertiesBeanDbSchemaValidation() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getHibernatePropertiesBean(eu.etaxonomy.cdm.database.DbSchemaValidation, java.lang.Boolean, java.lang.Boolean, java.lang.Class)}.
	 */
	@Test
	public void testGetHibernatePropertiesBeanDbSchemaValidationBooleanBooleanClassOfQextendsCacheProvider() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getInitMethodName()}.
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#setInitMethodName(java.lang.String)}.
	 */
	@Test
	public void testGetSetInitMethodName() {
		String initMethodName = "init";
		((CdmDataSource)datasource).setInitMethodName(initMethodName);
		assertEquals(initMethodName, ((CdmDataSource)datasource).getInitMethodName());
		((CdmDataSource)datasource).setInitMethodName(null);
		assertEquals(null, ((CdmDataSource)datasource).getInitMethodName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getDestroyMethodName()}.
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#setDestroyMethodName(java.lang.String)}.
	 */
	@Test
	public void testGetSetDestroyMethodName() {
		String destroyMethodName = "destroy";
		((CdmDataSource)datasource).setDestroyMethodName(destroyMethodName);
		assertEquals(destroyMethodName, ((CdmDataSource)datasource).getDestroyMethodName());
		((CdmDataSource)datasource).setDestroyMethodName(null);
		assertEquals(null, ((CdmDataSource)datasource).getDestroyMethodName());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getDatabase()}.
	 */
	@Test
	public void testGetDatabase() {
		assertEquals(database, datasource.getDatabase());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getDatabaseType()}.
	 */
	@Test
	public void testGetDatabaseType() {
		assertEquals(DatabaseTypeEnum.MySQL, datasource.getDatabaseType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getFilePath()}.
	 */
	@Test
	public void testGetFilePath() {
		assertEquals(null, datasource.getFilePath());
		String filePath = "path";
		ICdmDataSource ds = new CdmDataSource(DatabaseTypeEnum.H2, server, database, port, username, pwd, filePath, H2Mode.EMBEDDED);
		assertNotNull("new datasource() should not be null", ds);
		assertEquals(filePath, ds.getFilePath());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getPort()}.
	 */
	@Test
	public void testGetPort() {
		assertEquals(port, datasource.getPort());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getServer()}.
	 */
	@Test
	public void testGetServer() {
		assertEquals(server, datasource.getServer());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getMode()}.
	 */
	@Test
	public void testGetMode() {
		assertEquals(null, datasource.getFilePath());
		String filePath = "path";
		H2Mode mode = H2Mode.EMBEDDED;
		ICdmDataSource ds = new CdmDataSource(DatabaseTypeEnum.H2, server, database, port, username, pwd, filePath, mode);
		assertNotNull("new datasource() should not be null", ds);
		assertEquals(mode, ds.getMode());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getPassword()}.
	 */
	@Test
	public void testGetPassword() {
		assertEquals(pwd, datasource.getPassword());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSource#getUserName()}.
	 */
	@Test
	public void testGetUsername() {
		assertEquals(username, datasource.getUsername());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmDataSourceBase#testConnection()}.
	 */
	@Test
	public void testTestConnection() {
		logger.warn("Not yet implemented");
	}
}
