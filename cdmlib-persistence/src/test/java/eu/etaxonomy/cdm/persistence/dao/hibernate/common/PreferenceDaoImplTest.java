/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 */
public class PreferenceDaoImplTest  extends CdmTransactionalSpringIntegrationTest {

	@Autowired
	private IPreferenceDao dao;

	@Autowired
	private IClassificationDao classificationDao;

/************ TESTS ********************************/

    @Test
    @CdmDataSet
    public void testDao() {
    	assertNotNull(dao);
    }

    @Test
    @CdmDataSet
    public void testGet() {
        PrefKey key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.NomenclaturalCode);
//        PrefKey key = CdmPreference.NewKey(PreferenceSubjectEnum.Database, PreferencePredicate.NomenclaturalCode);
        CdmPreference pref = dao.get(key);
        assertNotNull(pref, "CdmPreference for given key must exist");
        assertEquals("ICNAFP", pref.getValue());

        key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.Test);
//        key = CdmPreference.NewKey(PreferenceSubjectEnum.Database, PreferencePredicate.Test);
        pref = dao.get(key);
        assertNull(pref, "CdmPreference for given key must not exist");
    }

    @Test
    @CdmDataSet
    public void testCount() {
    	 long countStart = dao.count();
         assertEquals(1, countStart, "There should be 1 preference in the CDM store");
    }

    @Test
    @CdmDataSet
    public void testList() {
         List<CdmPreference> list = dao.list();
         assertEquals(1, list.size(), "There should be 1 preference in the CDM store");
         CdmPreference pref = list.get(0);
         assertNotNull(pref, "CdmPreference for given key must exist");
         assertEquals("ICNAFP", pref.getValue());
         assertTrue(pref.isDatabasePref());
    }

    @Test
    @CdmDataSet
    public void testSet() {
    	//test existing pref
        long countStart = dao.count();
        assertEquals(1, countStart);

        //add 1 pref
        CdmPreference pref = CdmPreference.NewInstance(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.Test, "200");
        int addedPrefs = 1;
        dao.set(pref);
	   	long count = dao.count();
	    assertEquals(countStart + addedPrefs, count, "There should be 1 new preference (resulting in 2 prefs)");

	    //override existing pref
	    pref = CdmPreference.NewInstance(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.NomenclaturalCode, "ICZN");
        dao.set(pref);
	   	count = dao.count();
	    assertEquals(countStart + addedPrefs, count, "There should still be only 1 new preference (resulting in 2 prefs)");

	    //override with default value
	    //delete default values, we decided to keep preferences with default values in db and remove them explicitly when wanted to use the default (#8062 and #8045)
	    pref = CdmPreference.NewInstance(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.NomenclaturalCode, PreferencePredicate.NomenclaturalCode.getDefaultValue().toString());
	    pref.setAllowOverride(true);
	    dao.set(pref);
	    count = dao.count();
        assertEquals(countStart + addedPrefs, count, "There should still be 1 preference left. Nomenclatural Code should not be delete though containing default value");

        //fully remove existing pref
        pref = CdmPreference.NewInstance(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.NomenclaturalCode, PreferencePredicate.NomenclaturalCode.getDefaultValue().toString());
        pref.setAllowOverride(true);
        dao.remove(pref.getKey());
        count = dao.count();
        assertEquals(addedPrefs, count, "There should be only 1 preference left. Nomenclatural Code should be delete");
        pref = dao.get(CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.NomenclaturalCode));
        assertNull(pref);
    }

    @Test
    @CdmDataSet("PreferenceDaoImplTest.testFindTaxonNodeString.xml")
    public void testFindTaxonNodeString() {
        Classification classification = classificationDao.findByUuid(uuidClassification);
        TaxonNode genusNode = classification.getRootNode().getChildNodes().get(0);
        TaxonNode speciesNode = genusNode.getChildNodes().get(0);

        CdmPreference pref = dao.find(speciesNode, PreferencePredicate.NomenclaturalCode.getKey());
        assertNotNull(pref, "CdmPreference for given key must exist");
        assertEquals("ICZN", pref.getValue());

        pref = dao.find(genusNode, PreferencePredicate.NomenclaturalCode.getKey());
        assertNotNull(pref, "CdmPreference for given key must exist");
        assertEquals("ICVCN", pref.getValue());

        pref = dao.find(classification.addChildTaxon(null, null, null), PreferencePredicate.NomenclaturalCode.getKey());
        assertNotNull(pref, "CdmPreference for given key must exist");
        assertEquals("ICNAFP", pref.getValue());
        assertTrue(pref.isDatabasePref());

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
            fileNameAppendix, true );
    }
}