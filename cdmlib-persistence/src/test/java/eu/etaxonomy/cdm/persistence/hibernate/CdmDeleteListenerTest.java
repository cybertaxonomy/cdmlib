// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

import static org.junit.Assert.assertNotNull;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 17.09.2009
 * @version 1.0
 */
public class CdmDeleteListenerTest extends CdmTransactionalIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmDeleteListenerTest.class);

	@SpringBeanByType
	private ITaxonNameDao taxonNameDao;

	private UUID uuid;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		uuid = UUID.fromString("44415fc0-1703-11df-8a39-0800200c9a66");
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.hibernate.CdmDeleteListener#onDelete(org.hibernate.event.DeleteEvent, java.util.Set)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	@DataSet("CdmDeleteListenerTest.xml")
	@ExpectedDataSet
	public void testOnDelete() throws Exception {
		NonViralName name = (NonViralName)taxonNameDao.findByUuid(uuid);
		/**
		 * Ended up with some horrible hibernate errors otherwise
		 */
		taxonNameDao.refresh(name, LockMode.READ, null);
		assertNotNull(name);
		Set<NameRelationship> relations = name.getNameRelations();
		Assert.assertEquals("There must be 1 name relationship", 1, relations.size());
		name.removeNameRelationship(relations.iterator().next());
		
		Set<HybridRelationship> hybridRels = name.getHybridParentRelations();
		Assert.assertEquals("There must be 1 parent relationship", 1, hybridRels.size());
		
		taxonNameDao.saveOrUpdate(name);
		setComplete();
		endTransaction();
	}
}
