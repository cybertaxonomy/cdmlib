/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream.mapping;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.stream.mapping.IImportMapping.CdmKey;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;

/**
 * @author a.mueller
 * @since 26.03.2012
 */
public class DatabaseMappingTest {

    private static final Logger logger = LogManager.getLogger();

	private DatabaseMapping mapping;

	private final String mappingId = "MappingKey123";

	@BeforeEach
	public void setUp() throws Exception {
		mapping = new DatabaseMapping(mappingId);
	}

//******************* TESTS *****************************/

	@Test
	@Disabled
	public void testGetDatabase(){

		ICdmDataSource datasource = mapping.getDatabase();
		assertNotNull(datasource, "Datasource should not be null");
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
	@Disabled
	public void testPutGetExists(){

		ICdmDataSource datasource = mapping.getDatabase();
		assertNotNull(datasource, "Datasource should not be null");

		TaxonName botName1 = TaxonNameFactory.NewBotanicalInstance(null);
		int id = 23;
		botName1.setId(id);
		String sourceNS = "sourceNS";
		String sourceId = "sourceName1";
		mapping.putMapping(sourceNS, sourceId, botName1);
		Set<CdmKey> result = mapping.get(sourceNS, sourceId);
		assertNotNull(result, "Result should not be null");
		assertFalse(result.isEmpty(), "Result should not be empty");

		boolean exists = mapping.exists(sourceNS, sourceId, TaxonName.class);
		assertTrue(exists, "Mapping should exist");
		exists = mapping.exists(sourceNS + "xyz", sourceId, TaxonName.class);
		assertFalse(exists, "Mapping with wrong namespace should not exist");
		exists = mapping.exists(sourceNS + "xyz", sourceId, TaxonName.class);
		assertFalse(exists, "Mapping with wrong ID should not exist");
	}
}
