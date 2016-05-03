/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class DaoBaseTest  extends CdmIntegrationTest{


    @SpringBeanByType
    private  TaxonDaoHibernateImpl daoBaseTester;

/************ TESTS ********************************/

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase#getSession()}.
     */
    @Test
    public void testGetSession() {
        assertNotNull(daoBaseTester.getSession());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
