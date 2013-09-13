/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.hibernate.view;

import static org.junit.Assert.assertNotNull;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.view.IAuditEventDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @author ben.clark
 *
 */
public class AuditEventDaoTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByType	
	private IAuditEventDao auditEventDao;
	
	private DateTime dateTime;
	
	@Before
	public void setUp() {
		dateTime = new DateTime();
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.view.AuditEventDao#findByDate()}.
	 */
	@Test
	@DataSet
	public void testFindByDate() {
		AuditEvent auditEvent = auditEventDao.findByDate(dateTime);
		assertNotNull(auditEvent);
	}
}
