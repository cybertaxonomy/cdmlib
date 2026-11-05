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
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubjectEnum;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesSpringIntegrationTest;

/**
 * @author a.mueller
 */
public class PreferenceServiceImplTest  extends CdmServicesSpringIntegrationTest {

	@Autowired
	private IPreferenceService service;

/************ TESTS ********************************/

    @Test
    @CdmDataSet
    public void testService() {
    	Assert.assertNotNull(service);
    }

    @Test
    @CdmDataSet
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
    @CdmDataSet
    public void testCount() {
    	 long countStart = service.count();
         Assert.assertEquals("There should be 3 preference in the CDM store", 3, countStart);
    }

    @Test
    @CdmDataSet
    public void testPedicate() {
        List<CdmPreference> list = service.list(PreferencePredicate.Test);
        long n = list.size();
        Assert.assertEquals("There should be 2 test preferences in the CDM store", 2, n);

        PreferenceSubject editordistrSubject = PreferenceSubject.NewTaxEditorInstance()
                .with(PreferenceSubjectEnum.DistributionEditor);
        PrefKey key = CdmPreference.NewKey(editordistrSubject, PreferencePredicate.Test);
        CdmPreference pref = service.find(key);
        Assert.assertEquals("testForDistribution", pref.getValue());
    }

    @Test
    @CdmDataSet
    public void testSet() {
    	long countStart = service.count();
        Assert.assertEquals(3, countStart);

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
