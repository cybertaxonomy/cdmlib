// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import java.util.List;

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
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmMetaData#getSchemaVersion()}.
	 */
	@Test
	public void testGetSchemaVersion() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmMetaData#propertyList()}.
	 */
	@Test
	public void testPropertyList() {
		List<CdmMetaData> propList = CdmMetaData.propertyList();
		assertNotNull(propList);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmMetaData#CdmMetaData(eu.etaxonomy.cdm.model.common.CdmMetaData.MetaDataPropertyName, java.lang.String)}.
	 */
	@Test
	public void testCdmMetaData() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmMetaData#getPropertyName()}.
	 */
	@Test
	public void testGetPropertyName() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmMetaData#setPropertyName(eu.etaxonomy.cdm.model.common.CdmMetaData.MetaDataPropertyName)}.
	 */
	@Test
	public void testSetPropertyName() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmMetaData#getValue()}.
	 */
	@Test
	public void testGetValue() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmMetaData#setValue(java.lang.String)}.
	 */
	@Test
	public void testSetValue() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmMetaData#getCurrentSchemaVersion(int)}.
	 */
	@Test
	public void testGetCurrentSchemaVersion() {
		String strSchemaVersion = CdmMetaData.getCurrentSchemaVersion(2);
		assertNotNull(strSchemaVersion);
		int indexFirst = strSchemaVersion.indexOf(".");
		int indexLast = strSchemaVersion.lastIndexOf(".");
		assertTrue(indexFirst >0 );
		assertTrue(indexFirst == indexLast);
		
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmMetaData#getDatabaseSchemaVersion(java.util.Map, int)}.
	 */
	@Test
	public void testGetDatabaseSchemaVersion() {
		//TODO
	}

}
