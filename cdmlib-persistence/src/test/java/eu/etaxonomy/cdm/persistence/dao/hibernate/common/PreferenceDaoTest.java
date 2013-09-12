/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmPreference;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class PreferenceDaoTest  extends CdmIntegrationTest {

	@SpringBeanByType
	IPreferenceDao dao;
	
/************ TESTS ********************************/

    @Test
    public void testDao() {
    	Assert.assertNotNull(dao);
    }

	
    @Test
    @Ignore
    public void testSet() {
        CdmPreference pref = CdmPreference.NewInstance("Aber", "www", "200");
        dao.set(pref);
        
    }
}
