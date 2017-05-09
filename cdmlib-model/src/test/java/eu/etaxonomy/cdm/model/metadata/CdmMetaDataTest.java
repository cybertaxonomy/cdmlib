/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author e.-m.lee
 * @date 09.03.2010
 *
 */
public class CdmMetaDataTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.metadata.CdmMetaData#getSchemaVersion()}.
	 */
	@Test
	public void testGetSchemaVersion() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.metadata.CdmMetaData#propertyList()}.
	 */
	@Test
	public void testDefaultMetaData() {
		List<CdmMetaData> propList = CdmMetaData.defaultMetaData();
		assertNotNull(propList);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.metadata.CdmMetaData#CdmMetaData(eu.etaxonomy.cdm.model.metadata.CdmMetaData.MetaDataPropertyName, java.lang.String)}.
	 */
	@Test
	public void testCdmMetaData() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.metadata.CdmMetaData#getPropertyName()}.
	 */
	@Test
	public void testGetPropertyName() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.metadata.CdmMetaData#setPropertyName(eu.etaxonomy.cdm.model.metadata.CdmMetaData.MetaDataPropertyName)}.
	 */
	@Test
	public void testSetPropertyName() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.metadata.CdmMetaData#getValue()}.
	 */
	@Test
	public void testGetValue() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.metadata.CdmMetaData#setValue(java.lang.String)}.
	 */
	@Test
	public void testSetValue() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.metadata.CdmMetaData#getCurrentSchemaVersion(int)}.
	 */
	@Test
	public void testGetDbSchemaVersion() {
		String strSchemaVersion = CdmMetaData.getDbSchemaVersion();
		assertNotNull(strSchemaVersion);
		int indexFirst = strSchemaVersion.indexOf(".");
		int indexLast = strSchemaVersion.lastIndexOf(".");
		assertTrue(indexFirst >0 );
		assertTrue("DB schema version is not in the correct format", indexLast == 7);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.metadata.CdmMetaData#getDatabaseSchemaVersion(java.util.Map, int)}.
	 */
	@Test
	public void testGetDatabaseSchemaVersion() {
		//TODO
	}

	@Test
	public void testCompareVersion(){
		String version1 = "2.1.2.5.12343244234";
		String version2 = "2.1.3.5.11654354355";
		String version3 = "2.1.2";

		int compare = CdmMetaData.compareVersion(version1, version2, 4, null);
		Assert.assertEquals("Result should be -1", -1, compare);

		compare = CdmMetaData.compareVersion(version2, version1, 4, null);
		Assert.assertEquals("Result should be 1", 1, compare);

		compare = CdmMetaData.compareVersion(version2, version1, 2, null);
		Assert.assertEquals("Result should be 0", 0, compare);

		compare = CdmMetaData.compareVersion(version2, version1, null, null);
		Assert.assertEquals("Result should be 1", 1, compare);

		compare = CdmMetaData.compareVersion(version1, version3, 3, null);
		Assert.assertEquals("Result should be 0", 0, compare);

		boolean exception = false;
		try{
			compare = CdmMetaData.compareVersion("test", version1, null, null);
		}catch(RuntimeException e){
			exception = true;
		}

		Assert.assertTrue("Should have thrown an exception on incorrect input values", exception);
		exception = false;

		try{
			compare = CdmMetaData.compareVersion(version1, version2, 7, null);
		}catch(RuntimeException e){
			exception = true;
		}

		Assert.assertTrue("Should have thrown an exception on incompatible depth", exception);
		exception = false;

	}

}
