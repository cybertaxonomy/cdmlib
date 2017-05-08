/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;


import java.sql.SQLException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.dwca.in.IImportMapping.CdmKey;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;

/**
 * @author a.mueller
 * @date 26.03.2012
 *
 */
public class DatabaseMappingTest {
	private static final Logger logger = Logger.getLogger(DatabaseMappingTest.class);


	private DatabaseMapping mapping;

	private final String mappingId = "MappingKey123";

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
		mapping = new DatabaseMapping(mappingId);
	}

//******************* TESTS *****************************/

	@Test
	@Ignore
	public void testGetDatabase(){

		ICdmDataSource datasource = mapping.getDatabase();
		Assert.assertNotNull("Datasource should not be null", datasource);
		String sql = "INSERT INTO " + DatabaseMapping.TABLE_IMPORT_MAPPING+ "( task_id, source_namespace, source_id, destination_namespace, destination_id)  ";
		sql += "VALUES ( 'mappingID', 'ns1', 'source1', 'destNs', 'dest1')";
		try {
			datasource.executeUpdate(sql);
		} catch (SQLException e) {
			//maybe it just only exists
			logger.warn("Error occurred");
		}
	}

	@Test
	@Ignore
	public void testPutGetExists(){

		ICdmDataSource datasource = mapping.getDatabase();
		Assert.assertNotNull("Datasource should not be null", datasource);

		TaxonName<?,?> botName1 = TaxonNameFactory.NewBotanicalInstance(null);
		int id = 23;
		botName1.setId(id);
		String sourceNS = "sourceNS";
		String sourceId = "sourceName1";
		mapping.putMapping(sourceNS, sourceId, botName1);
		Set<CdmKey> result = mapping.get(sourceNS, sourceId);
		Assert.assertNotNull("Result should not be null", result);
		Assert.assertFalse("Result should not be empty", result.isEmpty());

		boolean exists = mapping.exists(sourceNS, sourceId, TaxonName.class);
		Assert.assertTrue("Mapping should exist", exists);
		exists = mapping.exists(sourceNS + "xyz", sourceId, TaxonName.class);
		Assert.assertFalse("Mapping with wrong namespace should not exist", exists);
		exists = mapping.exists(sourceNS + "xyz", sourceId, TaxonName.class);
		Assert.assertFalse("Mapping with wrong ID should not exist", exists);

	}

}
