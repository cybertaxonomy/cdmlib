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
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class PreferenceDaoTest  extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	IPreferenceDao dao;

	@SpringBeanByType
	IClassificationDao classificationDao;

/************ TESTS ********************************/

    @Test
    @DataSet
    public void testDao() {
    	Assert.assertNotNull(dao);
    }

    @Test
    @DataSet
    public void testGet() {
        PrefKey key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.NomenclaturalCode);
//        PrefKey key = CdmPreference.NewKey(PreferenceSubjectEnum.Database, PreferencePredicate.NomenclaturalCode);
        CdmPreference pref = dao.get(key);
        Assert.assertNotNull("CdmPreference for given key must exist", pref);
        Assert.assertEquals("ICNAFP", pref.getValue());

        key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.Test);
//        key = CdmPreference.NewKey(PreferenceSubjectEnum.Database, PreferencePredicate.Test);
        pref = dao.get(key);
        Assert.assertNull("CdmPreference for given key must not exist", pref);
    }


    @Test
    @DataSet
    public void testCount() {
    	 long countStart = dao.count();
         Assert.assertEquals("There should be 1 preference in the CDM store", 1, countStart);
    }

    @Test
    @DataSet
    public void testList() {
         List<CdmPreference> list = dao.list();
         Assert.assertEquals("There should be 1 preference in the CDM store", 1, list.size());
         CdmPreference pref = list.get(0);
         Assert.assertNotNull("CdmPreference for given key must exist", pref);
         Assert.assertEquals("ICNAFP", pref.getValue());
         Assert.assertTrue(pref.isDatabasePref());
    }

    @Test
    @DataSet
    public void testSet() {
    	 long countStart = dao.count();
         Assert.assertEquals(1, countStart);

         CdmPreference pref = CdmPreference.NewInstance(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.Test, "200");
//         CdmPreference pref = CdmPreference.NewInstance(PreferenceSubjectEnum.Database, PreferencePredicate.Test, "200");
        dao.set(pref);
	   	long count = dao.count();
	    Assert.assertEquals("There should be 1 new preference", countStart + 1, count);

	    pref = CdmPreference.NewInstance(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.NomenclaturalCode, "ICZN");
//        pref = CdmPreference.NewInstance(PreferenceSubjectEnum.Database, PreferencePredicate.NomenclaturalCode, "ICZN");
        dao.set(pref);

	   	count = dao.count();
	    Assert.assertEquals("There should be only 1 new preference", countStart + 1, count);
    }

    @Test
    @DataSet(value="eu.etaxonomy.cdm.persistence.dao.hibernate.common.PreferenceDaoTest.testFindTaxonNodeString.xml")
    public void testFindTaxonNodeString() {
        Classification classification = classificationDao.findByUuid(uuidClassification);
        TaxonNode genusNode = classification.getRootNode().getChildNodes().get(0);
        TaxonNode speciesNode = genusNode.getChildNodes().get(0);

        CdmPreference pref = dao.find(speciesNode, PreferencePredicate.NomenclaturalCode.getKey());
        Assert.assertNotNull("CdmPreference for given key must exist", pref);
        Assert.assertEquals("ICZN", pref.getValue());

        pref = dao.find(genusNode, PreferencePredicate.NomenclaturalCode.getKey());
        Assert.assertNotNull("CdmPreference for given key must exist", pref);
        Assert.assertEquals("ICVCN", pref.getValue());

        pref = dao.find(classification.addChildTaxon(null, null, null), PreferencePredicate.NomenclaturalCode.getKey());
        Assert.assertNotNull("CdmPreference for given key must exist", pref);
        Assert.assertEquals("ICNAFP", pref.getValue());
        Assert.assertTrue(pref.isDatabasePref());

    }

    private UUID uuidClassification = UUID.fromString("bbd2cdb4-8b83-4ef9-a553-c9629c3890aa");

//    @Test
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // 1. create the entities   and save them
        Classification classification = Classification.NewInstance("European Abies");
        classification.setUuid(uuidClassification);

        Taxon taxonGenus = Taxon.NewInstance(null, null);
        TaxonNode genusNode = classification.addChildTaxon(taxonGenus, null, null);

        Taxon taxonSpecies = Taxon.NewInstance(null, null);
        TaxonNode speciesNode = genusNode.addChildTaxon(taxonSpecies, null, null);

        Taxon taxonSubSpecies = Taxon.NewInstance(null, null);
        speciesNode.addChildTaxon(taxonSubSpecies, null, null);

        classificationDao.save(classification);


        // 2. end the transaction so that all data is actually written to the db
        setComplete();
        endTransaction();

        // use the fileNameAppendix if you are creating a data set file which need to be named differently
        // from the standard name. For example if a single test method needs different data then the other
        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
        String fileNameAppendix = "xxx";

        // 3.
        writeDbUnitDataSetFile(new String[] {
            "CLASSIFICATION","TAXONBASE", "TAXONNAME",
            "TAXONNODE",
            "HOMOTYPICALGROUP",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix );
    }
}
