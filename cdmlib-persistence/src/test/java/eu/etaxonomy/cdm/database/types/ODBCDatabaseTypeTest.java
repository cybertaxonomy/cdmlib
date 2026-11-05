/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;

/**
 * @author a.mueller
 * @since 18.12.2008
 */
public class ODBCDatabaseTypeTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private static DatabaseTypeEnum enumType;

	private CdmPersistentDataSource dataSource;
	private String server;
	private String dbName;
	private int port;
	private String username;
	private String password;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		enumType = DatabaseTypeEnum.ODBC;
	}

	@BeforeEach
	public void setUp() throws Exception {
		server = "server";
		dbName = "db";
		port = 80;
		username = "user";
		password = "wd";
		dataSource = CdmPersistentDataSource.save(
				"sybaseTest", CdmDataSource.NewInstance(enumType, server, dbName, port, username, password));
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetConnectionStringICdmDataSource() {
		String expected = "jdbc:odbc:" + server ;
		assertEquals(expected, enumType.getConnectionString(dataSource));
	}

	@Test
	public void testGetConnectionStringICdmDataSourceInt() {
		port = 357;
		String expected = "jdbc:odbc:" + server ;
		assertEquals(expected, ((OdbcDatabaseType)enumType.getDatabaseType()).getConnectionString(dataSource, port));
	}

	@Test
	public void testGetDatabaseNameByConnectionString() {
		dbName = null;
		assertEquals(dbName, dataSource.getDatabase());
	}

	@Test
	public void testGetServerNameByConnectionStringString() {
		assertEquals(server, dataSource.getServer());
	}

	@Test
	public void testGetPortByConnectionStringString() {
		port = -1;
		assertEquals(port, dataSource.getPort());
	}
}