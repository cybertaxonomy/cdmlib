/**
 * 
 */
package eu.etaxonomy.cdm.database;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author a.mueller
 *
 */
public class DatabaseEnumTest {
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
		assertEquals("MySQL", this.dbEnum.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getDriverClassName()}.
	 */
	@Test
	public void testGetDriverClassName() {
		assertEquals("com.mysql.jdbc.Driver", this.dbEnum.getDriverClassName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getUrl()}.
	 */
	@Test
	public void testGetUrl() {
		assertEquals("jdbc:mysql://", this.dbEnum.getUrl());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getDefaultPort()}.
	 */
	@Test
	public void testGetDefaultPort() {
		assertEquals(9001, this.dbEnum.HSqlDb.getDefaultPort());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getConnectionString(java.lang.String, java.lang.String, int)}.
	 */
	@Ignore
	@Test
	public void testGetConnectionStringStringStringInt() {
		ICdmDataSource cdmDataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test", 1234, null, null);
		assertEquals("jdbc:mysql://192.168.2.10:1234/cdm_test", this.dbEnum.getConnectionString(cdmDataSource));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getConnectionString(java.lang.String, java.lang.String)}.
	 */
	@Ignore
	@Test
	public void testGetConnectionStringStringString() {
		ICdmDataSource cdmDataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test", null, null);
		assertEquals("jdbc:mysql://192.168.2.10:3306/cdm_test", this.dbEnum.getConnectionString(cdmDataSource));
		ICdmDataSource sqlServerDataSource = CdmDataSource.NewSqlServer2005Instance("192.168.2.10", "cdm_test", null, null);
		assertEquals("jdbc:sqlserver://192.168.2.10:1433;databaseName=cdm_test;SelectMethod=cursor", this.dbEnumSql2005.getConnectionString(sqlServerDataSource));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getAllTypes()}.
	 */
	@Test
	public void testGetAllTypes() {
		List<DatabaseTypeEnum> typeList = dbEnum.getAllTypes();
		assertEquals(7, typeList.size());
		assertEquals(dbEnum.HSqlDb, typeList.get(0));
		assertEquals(dbEnum.MySQL, typeList.get(1));
		assertEquals(dbEnum.ODBC, typeList.get(2));
		assertEquals(dbEnum.PostgreSQL, typeList.get(3));
		assertEquals(dbEnum.SqlServer2000, typeList.get(4));
		assertEquals(dbEnum.SqlServer2005, typeList.get(5));
		assertEquals(dbEnum.H2, typeList.get(6));
	}
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseTypeEnum#getDatabaseEnumByDriverClass(java.lang.String)}.
	 */
	@Test
	public void testGetDatabaseEnumByDriverClass() {
		assertEquals(dbEnum.SqlServer2000, dbEnum.getDatabaseEnumByDriverClass("com.microsoft.jdbc.sqlserver.SQLServerDriver"));
		assertEquals(dbEnum.SqlServer2005, dbEnum.getDatabaseEnumByDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver"));
		assertEquals(null, dbEnum.getDatabaseEnumByDriverClass("com.microsoft.xxx"));	
	}

}
