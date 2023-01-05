/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author a.mueller
 */
public class DatabaseTypeEnumTest {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static DatabaseTypeEnum dbEnum;
	private static DatabaseTypeEnum dbEnumSql2005;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dbEnum = DatabaseTypeEnum.MySQL;
		dbEnumSql2005 = DatabaseTypeEnum.SqlServer2005;
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getName()}.
	 */
	@Test
	public void testGetName() {
		assertEquals("MySQL", DatabaseTypeEnumTest.dbEnum.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getDriverClassName()}.
	 */
	@Test
	public void testGetDriverClassName() {
		assertEquals("com.mysql.cj.jdbc.Driver", DatabaseTypeEnumTest.dbEnum.getDriverClassName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getUrl()}.
	 */
	@Test
	public void testGetUrl() {
		assertEquals("jdbc:mysql://", DatabaseTypeEnumTest.dbEnum.getUrl());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getDefaultPort()}.
	 */
	@Test
	public void testGetDefaultPort() {
		assertEquals(9092, DatabaseTypeEnum.H2.getDefaultPort());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getConnectionString(java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testGetConnectionStringStringStringInt() {
		ICdmDataSource cdmDataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test", 1234, null, null);
		assertEquals("jdbc:mysql://192.168.2.10:1234/cdm_test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull", DatabaseTypeEnumTest.dbEnum.getConnectionString(cdmDataSource));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getConnectionString(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetConnectionStringStringString() {
		ICdmDataSource cdmDataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test", null, null);
		assertEquals("jdbc:mysql://192.168.2.10:3306/cdm_test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull", DatabaseTypeEnumTest.dbEnum.getConnectionString(cdmDataSource));
		ICdmDataSource sqlServerDataSource = CdmDataSource.NewSqlServer2005Instance("192.168.2.10", "cdm_test", -1, null, null);
		assertEquals("jdbc:sqlserver://192.168.2.10:1433;databaseName=cdm_test;SelectMethod=cursor", DatabaseTypeEnumTest.dbEnumSql2005.getConnectionString(sqlServerDataSource));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getAllTypes()}.
	 */
	@Test
	public void testGetAllTypes() {
		List<DatabaseTypeEnum> typeList = DatabaseTypeEnum.getAllTypes();
		assertEquals(10, typeList.size());
		assertEquals(DatabaseTypeEnum.MySQL, typeList.get(0));
		assertEquals(DatabaseTypeEnum.ODBC, typeList.get(1));
		assertEquals(DatabaseTypeEnum.PostgreSQL, typeList.get(2));
		assertEquals(DatabaseTypeEnum.Oracle, typeList.get(3));
	//	assertEquals(DatabaseTypeEnum.SqlServer2000, typeList.get(4));
		assertEquals(DatabaseTypeEnum.SqlServer2005, typeList.get(4));
		assertEquals(DatabaseTypeEnum.Sybase, typeList.get(5));
		assertEquals(DatabaseTypeEnum.H2, typeList.get(6));
		assertEquals(DatabaseTypeEnum.SqlServer2008, typeList.get(7));
        assertEquals(DatabaseTypeEnum.SqlServer2012, typeList.get(8));
        assertEquals(DatabaseTypeEnum.MariaDB, typeList.get(9));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#byDriverClass(java.lang.String)}.
	 */
	@Test
	public void testGetDatabaseTypeEnumByDriverClass() {
		//assertEquals(DatabaseTypeEnum.SqlServer2000, DatabaseTypeEnum.getDatabaseEnumByDriverClass("com.microsoft.jdbc.sqlserver.SQLServerDriver"));
		//does not work anymore as SQLServer driver is ambigous
		//assertEquals(DatabaseTypeEnum.SqlServer2000, DatabaseTypeEnum.getDatabaseEnumByDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver"));
		//assertEquals(DatabaseTypeEnum.SqlServer2005, DatabaseTypeEnum.getDatabaseEnumByDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver"));
		assertEquals(DatabaseTypeEnum.MySQL, DatabaseTypeEnum.byDriverClass("com.mysql.cj.jdbc.Driver"));
		assertEquals(null, DatabaseTypeEnum.byDriverClass("com.microsoft.xxx"));
	}

    @Test
    public void testGetDatabaseTypeEnumByConnectionString() {
        String connectionString = "jdbc:mysql://192.168.2.10:3306/cdm_test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull";
        DatabaseTypeEnum type = DatabaseTypeEnum.byConnectionString(connectionString);
        assertEquals(DatabaseTypeEnum.MySQL, type);
        String dbName = type.getDatabaseType().getDatabaseNameByConnectionString(connectionString);
        assertEquals("cdm_test", dbName);
    }
}