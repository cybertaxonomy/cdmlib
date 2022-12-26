/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.types;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;

/**
 * @author a.mueller
 * @since 18.12.2008
 */
public class SqlServer2000DatabaseTypeTest {

	private CdmPersistentDataSource dataSource;
	private static DatabaseTypeEnum enumType;
	private String server;
	private String dbName;
	private int port;
	private String username;
	private String password;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		enumType = DatabaseTypeEnum.SqlServer2005;
	}

	@Before
	public void setUp() throws Exception {
		server = "server";
		dbName = "db";
		port = 80;
		username = "user";
		password = "wd";
		dataSource = CdmPersistentDataSource.save(
				"postgreSqlTest",
				CdmDataSource.NewInstance(enumType, server, dbName, port, username, password));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.types.PostgreSQLDatabaseType#getConnectionString(eu.etaxonomy.cdm.database.ICdmDataSource)}.
	 */
	@Test
	public void testGetConnectionStringICdmDataSource() {
		//OLD: String expected = "jdbc:microsoft:sqlserver://" + server + ":" + port + ";databaseName=" + dbName + ";SelectMethod=cursor";
		String expected = "jdbc:sqlserver://" + server + ":" + port + ";databaseName=" + dbName + ";SelectMethod=cursor";
		assertEquals(expected, enumType.getConnectionString(dataSource));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.types.PostgreSQLDatabaseType#getConnectionString(eu.etaxonomy.cdm.database.ICdmDataSource, int)}.
	 */
	@Test
	public void testGetConnectionStringICdmDataSourceInt() {
		port = 357;
		//OLD: String expected = "jdbc:microsoft:sqlserver://" + server + ":" + port + ";databaseName=" + dbName + ";SelectMethod=cursor";
		String expected = "jdbc:sqlserver://" + server + ":" + port + ";databaseName=" + dbName + ";SelectMethod=cursor";
		assertEquals(expected, ((SqlServer2005DatabaseType)enumType.getDatabaseType()).getConnectionString(dataSource, port));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.types.PostgreSQLDatabaseType#getDatabaseNameByConnectionString(java.lang.String)}.
	 */
	@Test
	public void testGetDatabaseNameByConnectionString() {
		assertEquals(dbName, dataSource.getDatabase());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getServerNameByConnectionString(java.lang.String)}.
	 */
	@Test
	public void testGetServerNameByConnectionStringString() {
		assertEquals(server, dataSource.getServer());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getPortByConnectionString(java.lang.String)}.
	 */
	@Test
	public void testGetPortByConnectionStringString() {
		assertEquals(port, dataSource.getPort());
	}
}