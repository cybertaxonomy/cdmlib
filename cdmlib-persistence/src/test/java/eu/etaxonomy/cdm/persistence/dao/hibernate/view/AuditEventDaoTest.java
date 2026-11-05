/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.view.IAuditEventDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @author ben.clark
 */
public class AuditEventDaoTest extends CdmTransactionalSpringIntegrationTest {

	@Autowired
	private IAuditEventDao auditEventDao;

	private ZonedDateTime dateTime;

	@BeforeEach
	public void setUp() {
		dateTime = ZonedDateTime.now();
	}

	@Test
	@CdmDataSet
	public void testFindByDate() {
		AuditEvent auditEvent = auditEventDao.findByDate(dateTime);
		assertNotNull(auditEvent);
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}