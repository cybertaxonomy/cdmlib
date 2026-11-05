/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author e.-m.lee
 * @since 09.03.2010
 */
public class CdmMetaDataTest {

	@Test
	public void testGetSchemaVersion() {
		//TODO
	}

	@Test
	public void testDefaultMetaData() {
		List<CdmMetaData> propList = CdmMetaData.defaultMetaData();
		assertNotNull(propList);
	}

	@Test
	public void testCdmMetaData() {
		//TODO
	}

	@Test
	public void testGetPropertyName() {
		//TODO
	}

	@Test
	public void testSetPropertyName() {
		//TODO
	}

	@Test
	public void testGetValue() {
		//TODO
	}

	@Test
	public void testSetValue() {
		//TODO
	}

	@Test
	public void testGetDbSchemaVersion() {
		String strSchemaVersion = CdmMetaData.getDbSchemaVersion();
		assertNotNull(strSchemaVersion);
		int indexFirst = strSchemaVersion.indexOf(".");
		assertTrue(indexFirst >0 );
//		int indexLast = strSchemaVersion.lastIndexOf(".");
//		assertTrue(indexLast == 7, "DB schema version is not in the correct format");
		String regEx = "\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}\\.\\d{8}";
		assertTrue(strSchemaVersion.matches(regEx), "DB schema version is not in the correct format");
	}

	@Test
	public void testGetDatabaseSchemaVersion() {
		//TODO
	}

	@Test
	public void testCompareVersion(){
		String version1 = "2.1.2.5.12343244";
		String version2 = "2.1.3.5.11654354";
		String version3 = "2.1.2";

		int compare = CdmMetaData.compareVersion(version1, version2, 4, null);
		assertEquals(-1, compare, "Result should be -1");

		compare = CdmMetaData.compareVersion(version2, version1, 4, null);
		assertEquals(1, compare, "Result should be 1");

		compare = CdmMetaData.compareVersion(version2, version1, 2, null);
		assertEquals(0, compare, "Result should be 0");

		compare = CdmMetaData.compareVersion(version2, version1, null, null);
		assertEquals(1, compare, "Result should be 1");

		compare = CdmMetaData.compareVersion(version1, version3, 3, null);
		assertEquals(0, compare, "Result should be 0");

		boolean exception = false;
		try{
			compare = CdmMetaData.compareVersion("test", version1, null, null);
		}catch(RuntimeException e){
			exception = true;
		}

		assertTrue(exception, "Should have thrown an exception on incorrect input values");
		exception = false;

		try{
			compare = CdmMetaData.compareVersion(version1, version2, 7, null);
		}catch(RuntimeException e){
			exception = true;
		}

		assertTrue(exception, "Should have thrown an exception on incompatible depth");
		exception = false;
	}
}