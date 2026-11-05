/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 17.09.2009
 */
public class CdmDeleteListenerTest extends CdmTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private ITaxonNameDao taxonNameDao;

	private UUID uuid;

	@BeforeEach
	public void setUp() throws Exception {
		uuid = UUID.fromString("44415fc0-1703-11df-8a39-0800200c9a66");
	}

	@Test
	@CdmDataSet("CdmDeleteListenerTest.xml")
	@CdmExpectedDataSet
	public void testOnDelete() throws Exception {
		TaxonName name = taxonNameDao.findByUuid(uuid);
		/**
		 * Ended up with some horrible hibernate errors otherwise
		 */
		taxonNameDao.refresh(name, new LockOptions(LockMode.READ), null);
		assertNotNull(name);
//		int nRels = taxonDao.countAllRelationships();  //TODO needs fixing on test side or dao method side as it jumps into auditing
//		assertEquals(2, nRels, "There should be 2 relationships");
		Set<NameRelationship> relations = name.getNameRelations();
		assertEquals(1, relations.size(), "There must be 1 name relationship");
		name.removeNameRelationship(relations.iterator().next());

		Set<HybridRelationship> hybridRels = name.getHybridParentRelations();
		assertEquals(1, hybridRels.size(), "There must be 1 parent relationship");

		taxonNameDao.saveOrUpdate(name);

		setComplete();
		endTransaction();
		startNewTransaction();

//		nRels = taxonDao.countAllRelationships();
//		assertEquals(1, nRels, "There should be 1 relationship now");
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}