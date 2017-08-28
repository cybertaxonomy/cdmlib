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

import java.io.FileNotFoundException;
import java.time.ZonedDateTime;

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

	private ZonedDateTime dateTime;

	@Before
	public void setUp() {
		dateTime = ZonedDateTime.now();
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


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
