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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;

import eu.etaxonomy.cdm.database.CdmPersistentDataSource.DbProperties;

/**
 * @author a.mueller
 *
 */
public class CdmPersistentDataSourceTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmPersistentDataSourceTest.class);
	
	private static CdmPersistentDataSource dataSource;
	
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
		dataSource = CdmPersistentDataSource.NewInstance("default");
		assertNotNull(dataSource);
		//delete tmp
		String dataSourceString = "tmp";
		//delete
		try {
			CdmPersistentDataSource.delete(CdmPersistentDataSource.NewInstance(dataSourceString));
		} catch (DataSourceNotFoundException e) {
			//;
		}
		assertFalse(CdmPersistentDataSource.exists(dataSourceString));

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		//delete tmp
		String dataSourceString = "tmp";
		//delete
		try {
			CdmPersistentDataSource.delete(CdmPersistentDataSource.NewInstance(dataSourceString));
		} catch (DataSourceNotFoundException e) {
			//;
		}
		assertFalse(CdmPersistentDataSource.exists(dataSourceString));

	}
	
//********************** TESTS ***********************************************/

	@Test
	public void testDummy() {
		assertEquals(1,1);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#NewDefaultInstance()}.
	 */
	@Test
	public void testNewDefaultInstance() {
		try {
			CdmPersistentDataSource defaultDs = CdmPersistentDataSource.NewInstance("default");
			assertNotNull(defaultDs);
			assertEquals(CdmPersistentDataSource.NewInstance("default"), CdmPersistentDataSource.NewDefaultInstance());
		} catch (DataSourceNotFoundException e) {
			fail();
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#NewLocalHsqlInstance()}.
	 */
	@Test
	public void testNewLocalHsqlInstance() {
		try {
			assertEquals(CdmPersistentDataSource.NewInstance("localDefaultHsql"), CdmPersistentDataSource.NewLocalHsqlInstance());
		} catch (DataSourceNotFoundException e) {
			fail();
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#NewInstance(java.lang.String)}.
	 */
	@Test
	public void testNewInstance() {
		assertNotNull(dataSource);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getName()}.
	 */
	@Test
	public void testGetName() {
		assertEquals("default", dataSource.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDatabaseType()}.
	 */
	@Test
	public void testGetDatabaseType() {
		assertEquals(DatabaseTypeEnum.MySQL, dataSource.getDatabaseType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDbProperty(eu.etaxonomy.cdm.database.CdmPersistentDataSource.DbProperties)}.
	 */
	@Test
	public void testGetDbProperty() {
		assertEquals("com.mysql.jdbc.Driver", dataSource.getDatasourceProperty(DbProperties.DRIVER_CLASS));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDatasourceBean()}.
	 */
	@Test
	public void testGetDatasourceBean() {
		BeanDefinition beanDef = dataSource.getDatasourceBean();
		PropertyValues propValues = beanDef.getPropertyValues();
		String propName =  "driverClassName"; 
		assertEquals("com.mysql.jdbc.Driver", propValues.getPropertyValue(propName).getValue());
		propName =  "url"; 
		assertEquals("testUrl", propValues.getPropertyValue(propName).getValue());
		propName =  "username"; 
		assertEquals("testUser", propValues.getPropertyValue(propName).getValue());
		propName =  "password"; 
		assertEquals("testPassword", propValues.getPropertyValue(propName).getValue());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getHibernatePropertiesBean(eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL)}.
	 */
	@Test
	public void testGetHibernatePropertiesBeanHBM2DDL() {
		DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
		BeanDefinition beanDef = dataSource.getHibernatePropertiesBean(hbm2dll);
		PropertyValues propValues = beanDef.getPropertyValues();
		String propName =  "properties"; 
		PropertyValue propValue =  propValues.getPropertyValue(propName);
		assertNotNull(propValue);
		assertTrue( propValue.getValue() instanceof Properties);
		
		Properties properties = (Properties)propValue.getValue();
		assertEquals(hbm2dll.toString(), properties.getProperty("hibernate.hbm2ddl.auto"));
		assertEquals(dataSource.getDatabaseType().getHibernateDialect(), properties.getProperty("hibernate.dialect"));
		assertEquals(NoCachingRegionFactory.class.getName(), properties.getProperty("hibernate.cache.region.factory_class"));
		assertEquals(String.valueOf(false), properties.getProperty("hibernate.show_sql"));
		assertEquals(String.valueOf(false), properties.getProperty("hibernate.format_sql"));

		//null
		beanDef = dataSource.getHibernatePropertiesBean(null);
		propValues = beanDef.getPropertyValues();
		propName =  "properties"; 
		propValue =  propValues.getPropertyValue(propName);
		assertNotNull(propValue);
		assertTrue( propValue.getValue() instanceof Properties);
		
		properties = (Properties)propValue.getValue();
		assertEquals(DbSchemaValidation.VALIDATE.toString(), properties.getProperty("hibernate.hbm2ddl.auto"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getHibernatePropertiesBean(eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL, java.lang.Boolean, java.lang.Boolean, java.lang.Class)}.
	 */
	@Test
	public void testGetHibernatePropertiesBeanHBM2DDLBooleanBooleanClassOfQextendsCacheProvider() {
		DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
		boolean showSql = false;
		boolean formatSql = false;
		boolean registerSearchListener = false;
		Class<? extends RegionFactory> cacheProviderClass = NoCachingRegionFactory.class;
		
		BeanDefinition beanDef = dataSource.getHibernatePropertiesBean(hbm2dll, showSql, formatSql, registerSearchListener, cacheProviderClass);
		PropertyValues propValues = beanDef.getPropertyValues();
		String propName =  "properties"; 
		PropertyValue propValue =  propValues.getPropertyValue(propName);
		assertNotNull(propValue);
		assertTrue( propValue.getValue() instanceof Properties);
		
		Properties properties = (Properties)propValue.getValue();
		assertEquals(hbm2dll.toString(), properties.getProperty("hibernate.hbm2ddl.auto"));
		assertEquals(dataSource.getDatabaseType().getHibernateDialect(), properties.getProperty("hibernate.dialect"));
		assertEquals(cacheProviderClass.getName(), properties.getProperty("hibernate.cache.region.factory_class"));
		assertEquals(String.valueOf(showSql), properties.getProperty("hibernate.show_sql"));
		assertEquals(String.valueOf(formatSql), properties.getProperty("hibernate.format_sql"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#exists(java.lang.String)}.
	 */
	@Test
	public void testExists() {
		assertTrue(CdmPersistentDataSource.exists("default"));
		assertTrue(CdmPersistentDataSource.exists("localDefaultHsql"));
		assertFalse(CdmPersistentDataSource.exists("xlsj�dfl"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#save(java.lang.String, eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSaveStringDatabaseTypeEnumStringStringIntStringString() {
		String dataSourceString = "tmp";
		assertFalse(CdmPersistentDataSource.exists(dataSourceString));
		
		DatabaseTypeEnum databaseType = DatabaseTypeEnum.SqlServer2005;
		String servername = "server";
		String db = "database";
		String username = "username";
		String password = "password";
		int port = 1234;
		
		ICdmDataSource dataSource = CdmDataSource.NewInstance(databaseType, servername, db, port, username, password);
		
		CdmPersistentDataSource.save(dataSourceString, dataSource);
		assertTrue(CdmPersistentDataSource.exists(dataSourceString));
		
		CdmPersistentDataSource loadedDataSource = null;
		try {
			loadedDataSource = CdmPersistentDataSource.NewInstance(dataSourceString);
		} catch (DataSourceNotFoundException e1) {
			fail();
		}
		assertEquals(databaseType, dataSource.getDatabaseType());
		assertEquals(DatabaseTypeEnum.SqlServer2005.getDriverClassName(), loadedDataSource.getDatasourceProperty(DbProperties.DRIVER_CLASS));
		assertEquals("jdbc:sqlserver://server:1234;databaseName=database;SelectMethod=cursor", loadedDataSource.getDatasourceProperty(DbProperties.URL));
		assertEquals(username, loadedDataSource.getDatasourceProperty(DbProperties.USERNAME));
		assertEquals(password, loadedDataSource.getDatasourceProperty(DbProperties.PASSWORD));
		//delete
		try {
			CdmPersistentDataSource.delete(CdmPersistentDataSource.NewInstance(dataSourceString));
		} catch (DataSourceNotFoundException e) {
			fail();
		}
		assertFalse(CdmPersistentDataSource.exists(dataSourceString));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#save(java.lang.String, eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String)}.
	 */
	//@Test
	public void testSaveStringDatabaseTypeEnumStringStringStringString() {
		//see testSaveStringDatabaseTypeEnumStringStringIntStringString
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#delete(eu.etaxonomy.cdm.database.CdmPersistentDataSource)}.
	 */
	@Test
	public void testDelete() {
		testSaveStringDatabaseTypeEnumStringStringStringString();
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getAllDataSources()}.
	 */
	@Test
	public void testGetAllDataSources() {
		//assertEquals(6, CdmPersistentDataSource.getAllDataSources().size());  //does not run for all orders of tests of this class 
		assertEquals("default", CdmPersistentDataSource.getAllDataSources().get(0).getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#toString()}.
	 */
	@Test
	public void testToString() {
		String dataSourceName = "default";
		try {
			assertEquals(dataSourceName, CdmPersistentDataSource.NewInstance(dataSourceName).toString());
		} catch (DataSourceNotFoundException e) {
			fail();
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDataSourceInputStream()}.
	 */
	@Test
	public void testGetDataSourceInputStream() {
		FileInputStream is = CdmPersistentDataSource.getDataSourceInputStream();
		assertNotNull( is);
		int firstInput;
		try {
			firstInput = is.read();
			assertEquals("Input Stream should start with < (=Ascii(60))", 60, firstInput);
		} catch (IOException e) {
			fail("Exception occurred in datasource input stream read");
		}
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDataSourceOutputStream()}.
	 */
	@Ignore
	@Test
	public void testGetDataSourceOutputStream() {
		FileOutputStream os = CdmPersistentDataSource.getDataSourceOutputStream();
		assertNotNull(os);
	}

}
