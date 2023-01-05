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

import org.junit.After;
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
public class OracleDatabaseTypeTest {

    private static DatabaseTypeEnum enumType;

	private CdmPersistentDataSource dataSource;
	private String server;
	private String dbName;
	private int port;
	private String username;
	private String password;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		enumType = DatabaseTypeEnum.Oracle;
	}

	@Before
	public void setUp() throws Exception {
		server = "server";
		dbName = "db";
		port = 80;
		username = "user";
		password = "wd";
		dataSource = CdmPersistentDataSource.save(
				"oracleTest",
				CdmDataSource.NewInstance(enumType, server, dbName, port, username, password));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetConnectionStringICdmDataSource() {
		String expected = "jdbc:oracle:thin:@" + server + ":" + port + ":" + dbName ;
		assertEquals(expected, enumType.getConnectionString(dataSource));
	}

	@Test
	public void testGetConnectionStringICdmDataSourceInt() {
		port = 357;
		String expected = "jdbc:oracle:thin:@" + server + ":" + port + ":" + dbName ;
		assertEquals(expected, ((OracleDatabaseType)enumType.getDatabaseType()).getConnectionString(dataSource, port));
	}

	@Test
	public void testGetDatabaseNameByConnectionString() {
		assertEquals(dbName, dataSource.getDatabase());
	}

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