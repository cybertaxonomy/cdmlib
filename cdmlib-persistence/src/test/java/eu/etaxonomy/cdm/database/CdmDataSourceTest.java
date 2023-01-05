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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * @author a.mueller
 * @since 18.12.2008
 */
public class CdmDataSourceTest {
	private static final Logger logger = LogManager.getLogger();

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
	@Before
	public void setUp() throws Exception {
		server = "myServer";
		database = "myDatabase";
		username = "myUsername";
		pwd = "myPassword";
		port = 80;
		datasource = CdmDataSource.NewMySqlInstance(server, database, port, username, pwd);
	}


//*************** TESTS ***********************************************

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

	@Test
	public void testCdmDataSource() {
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		H2Mode h2Mode = H2Mode.EMBEDDED;
		String filePath = "path";
		ICdmDataSource ds = new CdmDataSource(dbType, server, database, port, username, pwd, filePath, h2Mode);
		assertNotNull("new datasource() should not be null", ds);
	}

	@Test
	public void testGetName() {
		assertNotNull("datasource should not be null", datasource);
		assertEquals(database, datasource.getName());
	}

	@Test
	public void testGetDatasourceBean() {
		BeanDefinition bean = datasource.getDatasourceBean();
		assertNotNull(bean);
		assertEquals("the bean definition should have 4 properties: url, driverClassname, username, password", 4, bean.getPropertyValues().size());
		//TODO to be continued
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

	@Test
	public void testGetDatabase() {
		assertEquals(database, datasource.getDatabase());
	}

	@Test
	public void testGetDatabaseType() {
		assertEquals(DatabaseTypeEnum.MySQL, datasource.getDatabaseType());
	}

	@Test
	public void testGetFilePath() {
		assertEquals(null, datasource.getFilePath());
		String filePath = "path";
		ICdmDataSource ds = new CdmDataSource(DatabaseTypeEnum.H2, server, database, port, username, pwd, filePath, H2Mode.EMBEDDED);
		assertNotNull("new datasource() should not be null", ds);
		assertEquals(filePath, ds.getFilePath());
	}

	@Test
	public void testGetPort() {
		assertEquals(port, datasource.getPort());
	}

	@Test
	public void testGetServer() {
		assertEquals(server, datasource.getServer());
	}

	@Test
	public void testGetMode() {
		assertEquals(null, datasource.getFilePath());
		String filePath = "path";
		H2Mode mode = H2Mode.EMBEDDED;
		ICdmDataSource ds = new CdmDataSource(DatabaseTypeEnum.H2, server, database, port, username, pwd, filePath, mode);
		assertNotNull("new datasource() should not be null", ds);
		assertEquals(mode, ds.getMode());
	}

	@Test
	public void testGetPassword() {
		assertEquals(pwd, datasource.getPassword());
	}

	@Test
	public void testGetUsername() {
		assertEquals(username, datasource.getUsername());
	}

	@Test
	public void testTestConnection() {
		logger.warn("Not yet implemented");
	}
}
