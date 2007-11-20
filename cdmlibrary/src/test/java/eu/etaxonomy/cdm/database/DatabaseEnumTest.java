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
import org.junit.Test;


/**
 * @author a.mueller
 *
 */
public class DatabaseEnumTest {
	private static final Logger logger = Logger.getLogger(DatabaseEnumTest.class);
	private static DatabaseEnum dbEnum;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dbEnum = DatabaseEnum.MySQL;
	}


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseEnum#getName()}.
	 */
	@Test
	public void testGetName() {
		assertEquals("MySQL", this.dbEnum.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseEnum#getDriverClassName()}.
	 */
	@Test
	public void testGetDriverClassName() {
		assertEquals("com.mysql.jdbc.Driver", this.dbEnum.getDriverClassName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseEnum#getUrl()}.
	 */
	@Test
	public void testGetUrl() {
		assertEquals("jdbc:mysql://", this.dbEnum.getUrl());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseEnum#getDefaultPort()}.
	 */
	@Test
	public void testGetDefaultPort() {
		assertEquals(9001, this.dbEnum.HSqlDb.getDefaultPort());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseEnum#getConnectionString(java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testGetConnectionStringStringStringInt() {
		assertEquals("jdbc:mysql://192.168.2.10:1234/cdm_test", this.dbEnum.getConnectionString("192.168.2.10", "cdm_test", 1234));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseEnum#getConnectionString(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetConnectionStringStringString() {
		assertEquals("jdbc:mysql://192.168.2.10:3306/cdm_test", this.dbEnum.getConnectionString("192.168.2.10", "cdm_test"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseEnum#getAllTypes()}.
	 */
	@Test
	public void testGetAllTypes() {
		List<DatabaseEnum> typeList = dbEnum.getAllTypes();
		assertEquals(4, typeList.size());
		assertEquals(dbEnum.HSqlDb, typeList.get(0));
		assertEquals(dbEnum.MySQL, typeList.get(1));
		assertEquals(dbEnum.ODBC, typeList.get(2));
		assertEquals(dbEnum.SqlServer, typeList.get(3));
	}
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.DatabaseEnum#getDatabaseEnumByDriverClass(java.lang.String)}.
	 */
	@Test
	public void testGetDatabaseEnumByDriverClass() {
		assertEquals(dbEnum.SqlServer, dbEnum.getDatabaseEnumByDriverClass("com.microsoft.jdbc.sqlserver.SQLServerDriver"));
		assertEquals(null, dbEnum.getDatabaseEnumByDriverClass("com.microsoft.xxx"));	
	}

}
