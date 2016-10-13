// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;


import java.lang.reflect.Method;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.v24_25.SchemaUpdater_24_25;
import eu.etaxonomy.cdm.database.update.v24_25.TermUpdater_24_25;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;

/**
 * @author a.mueller
 * @date 14.09.2010
 *
 */
public class CdmUpdaterTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmUpdaterTest.class);

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
	}

// ******************** TESTS ****************************************************/

	@Ignore
	@Test
	public void testUpdateToCurrentVersion() {
		CdmUpdater cdmUpdater = new CdmUpdater();
		ICdmDataSource datasource = cdm_test_algaterra();
		try {
			boolean connectionAvailable = datasource.testConnection();
			Assert.assertTrue("Testdatabase is not available", connectionAvailable);
		} catch (ClassNotFoundException e) {
			Assert.fail();
		} catch (SQLException e) {
			Assert.fail();
		}
		cdmUpdater.updateToCurrentVersion(datasource, null);
	}

	private static ICdmDataSource cdm_test_andreasM(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_andreasM";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	private static ICdmDataSource cdm_test_algaterra(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_edit_algaterra";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

//	private static ICdmDataSource cdm_import(){
//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
//		String cdmServer = "160.45.63.151";
//		String cdmDB = "cdm_production_campanulaceae";
//		String cdmUserName = "edit";
//		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
//	}

	private static ICdmDataSource makeDestination(DatabaseTypeEnum dbType, String cdmServer, String cdmDB, int port, String cdmUserName, String pwd ){
		//establish connection
		pwd = AccountStore.readOrStorePassword(cdmServer, cdmDB, cdmUserName, pwd);
		ICdmDataSource destination;
		if(dbType.equals(DatabaseTypeEnum.MySQL)){
			destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB, port, cdmUserName, pwd);
		} else if(dbType.equals(DatabaseTypeEnum.PostgreSQL)){
			destination = CdmDataSource.NewPostgreSQLInstance(cdmServer, cdmDB, port, cdmUserName, pwd);
		} else {
			//TODO others
			throw new RuntimeException("Unsupported DatabaseType");
		}
		return destination;
	}

	@Test
	public void testRecursiveCallSchemaUpdater(){
		CdmUpdater updater = new CdmUpdater();
		ISchemaUpdater currentUpdater = null;
		try {
			Method method =  CdmUpdater.class.getDeclaredMethod("getCurrentSchemaUpdater");
			method.setAccessible(true);

			currentUpdater = (ISchemaUpdater)method.invoke(updater);
		} catch (Exception e) {
			Assert.fail("CdmUpdater.getCurrentSchemaUpdater not found:" + e.getMessage());;
		}
		ISchemaUpdater lastUpdater = null;
		ISchemaUpdater tmpUpdater = currentUpdater;

		int i = 0;
		//get very first schema updater available (= SchemaUpdater_24_25) by recursive call to getPreviousUpdater
		while (tmpUpdater.getPreviousUpdater() != null && i++<1000){
			tmpUpdater = tmpUpdater.getPreviousUpdater();
			lastUpdater = tmpUpdater;
		}
		Assert.assertNotNull("Current Updater must not be null", currentUpdater);
		Assert.assertEquals("Very first schema updater must be schemaUpdater_24_25. Something seems to be wrong in recursive call of getPreviousSchemaUpdater", SchemaUpdater_24_25.class, lastUpdater.getClass());

		i = 0;
		while (tmpUpdater.getNextUpdater() != null && i++<1000){
			tmpUpdater = tmpUpdater.getNextUpdater();
			lastUpdater = tmpUpdater;
		}
		Assert.assertEquals("Current schema updater not found by recursive call firstUpdater.getNextUpdater()", currentUpdater.getClass(), lastUpdater.getClass());

		//test correct schema version string
		Assert.assertEquals(CdmMetaData.getDbSchemaVersion(), currentUpdater.getTargetVersion());
	}

	@Test
	public void testRecursiveCallTermUpdater(){
		CdmUpdater updater = new CdmUpdater();
		ITermUpdater currentUpdater = null;
		try {
			Method method =  CdmUpdater.class.getDeclaredMethod("getCurrentTermUpdater");
			method.setAccessible(true);
			currentUpdater = (ITermUpdater)method.invoke(updater);
		} catch (Exception e) {
			Assert.fail("CdmUpdater.getCurrentTermUpdater not found:" + e.getMessage());;
		}
		Assert.assertNotNull("Current Updater must not be null", currentUpdater);
		ITermUpdater lastUpdater = null;
		ITermUpdater tmpUpdater = currentUpdater;
		int i = 0;
		//get very first term updater available (= TermUpdater_24_25) by recursive call to getPreviousUpdater
		while (tmpUpdater.getPreviousUpdater() != null && i++<1000){
			tmpUpdater = tmpUpdater.getPreviousUpdater();
			lastUpdater = tmpUpdater;
		}

		Assert.assertEquals(TermUpdater_24_25.class, lastUpdater.getClass());

		i = 0;
		while (tmpUpdater.getNextUpdater() != null && i++<1000){
			tmpUpdater = tmpUpdater.getNextUpdater();
			lastUpdater = tmpUpdater;
		}
		Assert.assertEquals("Current term updater not found by recursive call firstUpdater.getNextUpdater()", currentUpdater.getClass(), lastUpdater.getClass());

		//test correct schema version string
		Assert.assertEquals(CdmMetaData.getTermsVersion(), currentUpdater.getTargetVersion());
	}

}
