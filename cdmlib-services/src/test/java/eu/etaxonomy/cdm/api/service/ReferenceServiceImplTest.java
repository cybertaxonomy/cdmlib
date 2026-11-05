/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 13.02.2019
 */
public class ReferenceServiceImplTest extends CdmServicesTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private static final UUID REFERENCE1_UUID = UUID.fromString("4857d1f5-77d1-4583-87c4-f0d08fcfefcc");
    private static final UUID REFERENCE2_UUID = UUID.fromString("fcdfb0cc-9ef6-48e3-ad56-492614491c73");
    private static final UUID REFERENCE3_UUID = UUID.fromString("2821f503-5dd1-49b1-8a1c-8ed623e89e10");
    private static final UUID REFERENCE4_UUID = UUID.fromString("f2d2614c-f652-437b-8b4f-f5f7242df5af");

    @Autowired
    private IReferenceService service;

/* ******************** TESTS ********************************************/

    @Test  //7874 //8030
    @CdmDataSet(cleanSweep = true, value = "ReferenceServiceImplTest.testUpdateCaches.xml")
    public void testUpdateCaches() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{

        Field titleCacheField = IdentifiableEntity.class.getDeclaredField("titleCache");
        titleCacheField.setAccessible(true);
        Field abbrevCacheField = Reference.class.getDeclaredField("abbrevTitleCache");
        abbrevCacheField.setAccessible(true);

        Reference ref1 = service.load(REFERENCE1_UUID);
        Reference ref2 = service.load(REFERENCE2_UUID);
        Reference ref3 = service.load(REFERENCE3_UUID);
        Reference ref4 = service.load(REFERENCE4_UUID);

        assertEquals("--needs update--", titleCacheField.get(ref1), "TitleCache should be the persisted one");
        assertEquals("-still wrong-", abbrevCacheField.get(ref1), "AbbrevCache should be the persisted one");

        assertEquals("Reference2", titleCacheField.get(ref2), "TitleCache should be the persisted one");
        assertEquals("-needs update-", abbrevCacheField.get(ref2), "AbbrevCache should be the persisted one");

        assertEquals("-wrong-", titleCacheField.get(ref3), "TitleCache should be the persisted one");
        assertEquals("Protec. ref.", abbrevCacheField.get(ref3), "AbbrevCache should be the persisted one");

        assertEquals("Inref", titleCacheField.get(ref4), "TitleCache should be the persisted one");

        service.updateCaches();

        assertEquals("Species plantarum", titleCacheField.get(ref1), "Expecting titleCache to be updated");
        assertEquals("Sp. Pl.", abbrevCacheField.get(ref1), "Expecting nameCache to be updated");

        assertEquals("Reference2", titleCacheField.get(ref2), "Expecting titleCache to be updated");
        assertEquals("L., ref. 2", abbrevCacheField.get(ref2), "Expecting nameCache to not be updated");

        assertEquals("Linnaeus, C.: Reference three", titleCacheField.get(ref3), "Expecting titleCache to be updated");
        assertEquals("Protec. ref.", abbrevCacheField.get(ref3), "Expecting nameCache to be updated");

        assertEquals("-- invalid inreference (self-referencing) --", titleCacheField.get(ref4), "Expecting error message for self-referencing in-refererence");
    }

    @Test  //7874 //8030
    @CdmDataSet(cleanSweep = true, value = "ReferenceServiceImplTest.testUpdateCaches.xml")
    public void testGetUuidAndAbbrevTitleCacheForAuthorID() {
    	List<UuidAndTitleCache<Reference>> result = service.getUuidAndAbbrevTitleCacheForAuthorID(null, 1, null);
    	assertEquals(1, result.size());
    	result = service.getUuidAndAbbrevTitleCacheForAuthorID(null, 2, null);
    	assertEquals(2, result.size());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}