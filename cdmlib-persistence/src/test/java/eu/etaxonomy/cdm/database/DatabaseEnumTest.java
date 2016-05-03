/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.database;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author a.mueller
 *
 */
public class DatabaseEnumTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DatabaseEnumTest.class);
	private static DatabaseTypeEnum dbEnum;
	private static DatabaseTypeEnum dbEnumSql2005;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dbEnum = DatabaseTypeEnum.MySQL;
		dbEnumSql2005 = DatabaseTypeEnum.SqlServer2005;
	}


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getName()}.
	 */
	@Test
	public void testGetName() {
		assertEquals("MySQL", DatabaseEnumTest.dbEnum.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getDriverClassName()}.
	 */
	@Test
	public void testGetDriverClassName() {
		assertEquals("com.mysql.jdbc.Driver", DatabaseEnumTest.dbEnum.getDriverClassName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getUrl()}.
	 */
	@Test
	public void testGetUrl() {
		assertEquals("jdbc:mysql://", DatabaseEnumTest.dbEnum.getUrl());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getDefaultPort()}.
	 */
	@Test
	public void testGetDefaultPort() {
		assertEquals(9001, DatabaseTypeEnum.HSqlDb.getDefaultPort());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getConnectionString(java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testGetConnectionStringStringStringInt() {
		ICdmDataSource cdmDataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test", 1234, null, null, null);
		assertEquals("jdbc:mysql://192.168.2.10:1234/cdm_test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull", DatabaseEnumTest.dbEnum.getConnectionString(cdmDataSource));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getConnectionString(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetConnectionStringStringString() {
		ICdmDataSource cdmDataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test", null, null);
		assertEquals("jdbc:mysql://192.168.2.10:3306/cdm_test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull", DatabaseEnumTest.dbEnum.getConnectionString(cdmDataSource));
		ICdmDataSource sqlServerDataSource = CdmDataSource.NewSqlServer2005Instance("192.168.2.10", "cdm_test", -1, null, null, null);
		assertEquals("jdbc:sqlserver://192.168.2.10:1433;databaseName=cdm_test;SelectMethod=cursor", DatabaseEnumTest.dbEnumSql2005.getConnectionString(sqlServerDataSource));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getAllTypes()}.
	 */
	@Test
	public void testGetAllTypes() {
		List<DatabaseTypeEnum> typeList = DatabaseTypeEnum.getAllTypes();
		assertEquals(8, typeList.size());
		assertEquals(DatabaseTypeEnum.HSqlDb, typeList.get(0));
		assertEquals(DatabaseTypeEnum.MySQL, typeList.get(1));
		assertEquals(DatabaseTypeEnum.ODBC, typeList.get(2));
		assertEquals(DatabaseTypeEnum.PostgreSQL, typeList.get(3));
		assertEquals(DatabaseTypeEnum.Oracle, typeList.get(4));
	//	assertEquals(DatabaseTypeEnum.SqlServer2000, typeList.get(5));
		assertEquals(DatabaseTypeEnum.SqlServer2005, typeList.get(5));
		assertEquals(DatabaseTypeEnum.Sybase, typeList.get(6));
		assertEquals(DatabaseTypeEnum.H2, typeList.get(7));
	}
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#byDriverClass(java.lang.String)}.
	 */
	@Test
	public void testGetDatabaseEnumByDriverClass() {
		//assertEquals(DatabaseTypeEnum.SqlServer2000, DatabaseTypeEnum.getDatabaseEnumByDriverClass("com.microsoft.jdbc.sqlserver.SQLServerDriver"));
		//does not work anymore as SQLServer driver is ambigous
		//assertEquals(DatabaseTypeEnum.SqlServer2000, DatabaseTypeEnum.getDatabaseEnumByDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver"));
		//assertEquals(DatabaseTypeEnum.SqlServer2005, DatabaseTypeEnum.getDatabaseEnumByDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver"));
		assertEquals(DatabaseTypeEnum.MySQL, DatabaseTypeEnum.byDriverClass("com.mysql.jdbc.Driver"));
		assertEquals(null, DatabaseTypeEnum.byDriverClass("com.microsoft.xxx"));	
	}

}
