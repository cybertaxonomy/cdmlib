/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @since 13.02.2019
 */
public class ReferenceServiceImplTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ReferenceServiceImplTest.class);

    private static final UUID REFERENCE1_UUID = UUID.fromString("4857d1f5-77d1-4583-87c4-f0d08fcfefcc");
    private static final UUID REFERENCE2_UUID = UUID.fromString("fcdfb0cc-9ef6-48e3-ad56-492614491c73");
    private static final UUID REFERENCE3_UUID = UUID.fromString("2821f503-5dd1-49b1-8a1c-8ed623e89e10");
    private static final UUID REFERENCE4_UUID = UUID.fromString("f2d2614c-f652-437b-8b4f-f5f7242df5af");

    @SpringBeanByType
    private IReferenceService service;

/* ******************** TESTS ********************************************/

    @Test  //7874 //8030
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="ReferenceServiceImplTest.testUpdateCaches.xml")
    public void testUpdateCaches() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{

        Field titleCacheField = IdentifiableEntity.class.getDeclaredField("titleCache");
        titleCacheField.setAccessible(true);
        Field abbrevCacheField = Reference.class.getDeclaredField("abbrevTitleCache");
        abbrevCacheField.setAccessible(true);

        Reference ref1 = service.load(REFERENCE1_UUID);
        Reference ref2 = service.load(REFERENCE2_UUID);
        Reference ref3 = service.load(REFERENCE3_UUID);
        Reference ref4 = service.load(REFERENCE4_UUID);

        assertEquals("TitleCache should be the persisted one", "--needs update--", titleCacheField.get(ref1));
        assertEquals("AbbrevCache should be the persisted one", "-still wrong-", abbrevCacheField.get(ref1));

        assertEquals("TitleCache should be the persisted one", "Reference2", titleCacheField.get(ref2));
        assertEquals("AbbrevCache should be the persisted one", "-needs update-", abbrevCacheField.get(ref2));

        assertEquals("TitleCache should be the persisted one", "-wrong-", titleCacheField.get(ref3));
        assertEquals("AbbrevCache should be the persisted one", "Protec. ref.", abbrevCacheField.get(ref3));

        assertEquals("TitleCache should be the persisted one", "Inref", titleCacheField.get(ref4));

        service.updateCaches();

        assertEquals("Expecting titleCache to be updated", "Species plantarum", titleCacheField.get(ref1));
        assertEquals("Expecting nameCache to be updated", "Sp. Pl.", abbrevCacheField.get(ref1));

        assertEquals("Expecting titleCache to be updated", "Reference2", titleCacheField.get(ref2));
        assertEquals("Expecting nameCache to not be updated", "ref. 2", abbrevCacheField.get(ref2));

        assertEquals("Expecting titleCache to be updated", "Reference three", titleCacheField.get(ref3));
        assertEquals("Expecting nameCache to be updated", "Protec. ref.", abbrevCacheField.get(ref3));

        assertEquals("Expecting error message for self-referencing in-refererence", "-- invalid inreference (self-referencing) --", titleCacheField.get(ref4));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}