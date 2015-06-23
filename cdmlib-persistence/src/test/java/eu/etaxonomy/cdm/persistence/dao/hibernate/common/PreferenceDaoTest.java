/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
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
    @DataSet
    public void testDao() {
    	Assert.assertNotNull(dao);
    }

    @Test
    @DataSet
    public void testGet() {
        PrefKey key = CdmPreference.NewKey(PreferenceSubject.Database, PreferencePredicate.NomenclaturalCode);
        CdmPreference pref = dao.get(key);
        Assert.assertNotNull("CdmPreference for given key must exist", pref);
        Assert.assertEquals("ICNAFP", pref.getValue());

        key = CdmPreference.NewKey(PreferenceSubject.Database, PreferencePredicate.Test);
        pref = dao.get(key);
        Assert.assertNull("CdmPreference for given key must not exist", pref);
    }


    @Test
    @DataSet
    public void testCount() {
    	 int countStart = dao.count();
         Assert.assertEquals("There should be 1 preference in the CDM store", 1, countStart);
    }

    @Test
    @DataSet
    public void testSet() {
    	 int countStart = dao.count();
         Assert.assertEquals(1, countStart);

    	CdmPreference pref = CdmPreference.NewInstance(PreferenceSubject.Database, PreferencePredicate.Test, "200");
        dao.set(pref);
	   	int count = dao.count();
	    Assert.assertEquals("There should be 1 new preference", countStart + 1, count);

        pref = CdmPreference.NewInstance(PreferenceSubject.Database, PreferencePredicate.NomenclaturalCode, "ICZN");
        dao.set(pref);

	   	count = dao.count();
	    Assert.assertEquals("There should be only 1 new preference", countStart + 1, count);

    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
