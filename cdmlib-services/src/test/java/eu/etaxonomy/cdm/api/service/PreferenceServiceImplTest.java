/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class PreferenceServiceImplTest  extends CdmIntegrationTest {

	@SpringBeanByType
	IPreferenceService service;

/************ TESTS ********************************/

    @Test
    @DataSet
    public void testService() {
    	Assert.assertNotNull(service);
    }

    @Test
    @DataSet
    public void testGet() {
        PrefKey key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.NomenclaturalCode);
        CdmPreference pref = service.find(key);
        Assert.assertNotNull("CdmPreference for given key must exist", pref);
        Assert.assertEquals("ICNAFP", pref.getValue());

        key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.Test);
        pref = service.find(key);
        Assert.assertNull("CdmPreference for given key must not exist", pref);
    }


    @Test
    @DataSet
    public void testCount() {
    	 long countStart = service.count();
         Assert.assertEquals("There should be 1 preference in the CDM store", 1, countStart);
    }

    @Test
    @DataSet
    public void testSet() {
    	long countStart = service.count();
        Assert.assertEquals(1, countStart);

    	CdmPreference pref = CdmPreference.NewDatabaseInstance(PreferencePredicate.Test, "200");
    	service.set(pref);
	   	long count = service.count();
	    Assert.assertEquals("There should be 1 new preference", countStart + 1, count);


        pref = CdmPreference.NewDatabaseInstance( PreferencePredicate.NomenclaturalCode, "ICZN");
        service.set(pref);

	   	count = service.count();
	    Assert.assertEquals("There should be only 1 new preference", countStart + 1, count);

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
