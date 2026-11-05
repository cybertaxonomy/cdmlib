/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

import org.dbunit.dataset.filter.ExcludeTableFilter;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.test.integration.spring.CdmSpringIntegrationTest;

public class TestingTermInitializerTest extends CdmSpringIntegrationTest {

    private final UUID misappliedNameForUuid = UUID.fromString("1ed87175-59dd-437e-959e-0d71583d8417");

//    @Test  uncomment for creating datasets
    public void testPrintDataSet() {
        try {
            ExcludeTableFilter filter = new ExcludeTableFilter();
            filter.excludeTable("Rights");  //throws exception with H2
            printDataSet(new FileOutputStream("NewDataSet.xml"), null, filter, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInit() {
        assertNotNull(Language.DEFAULT(), "TermInitializer should have initialized Language.DEFAULT");
        assertEquals(Language.DEFAULT(), Language.ENGLISH(), "Language.DEFAULT should equal Language.ENGLISH");
    }

    @Test
    public void testMarkerTypeIds() {
        assertEquals(893, MarkerType.TO_BE_CHECKED().getId(), "We expect MarkerType.TO_BE_CHECKED to have an id of 893");
    }

    @Test
    public void testFeatureIds() {
        assertEquals(922, Feature.ECOLOGY().getId(), "We expect Feature.ECOLOGY to have an id of 922");
    }

    @Test
    public final void testTermsAreLoaded() {
        assertNotNull(TaxonRelationshipType.MISAPPLIED_NAME_FOR(), "TaxonRelationshipType.IS_MISAPPLIED_NAME_FOR should have been initialized");
        assertEquals(misappliedNameForUuid, TaxonRelationshipType.uuidMisappliedNameFor, "TaxonRelationshipType.IS_MISAPPLIED_NAME_FORs should have a uuid of " + misappliedNameForUuid.toString());
        assertNotNull("SynonymType.INFERREDEPITHET should be loaded" + SynonymType.INFERRED_EPITHET_OF);
    }

    @Test
    public void testUnlazyStatusTerms() {
            assertNotNull(NomenclaturalStatusType.SUPERFLUOUS(), "NomenclaturalStatusType.SUPERFLUOUS should exist");
            assertNotNull(NomenclaturalStatusType.NUDUM(), "NomenclaturalStatusType.NUDUM should exist");
            assertNotNull(NomenclaturalStatusType.ILLEGITIMATE(), "NomenclaturalStatusType.ILLEGITIMATE should exist");
            assertNotNull(NomenclaturalStatusType.INVALID(), "NomenclaturalStatusType.INVALID should exist");
            assertNotNull(NomenclaturalStatusType.CONSERVED(), "NomenclaturalStatusType.CONSERVED should exist");
            assertNotNull(NomenclaturalStatusType.ALTERNATIVE(), "NomenclaturalStatusType.ALTERNATIVE should exist");
            assertNotNull(NomenclaturalStatusType.REJECTED(), "NomenclaturalStatusType.REJECTED should exist");
            assertNotNull(NomenclaturalStatusType.REJECTED_PROP(), "NomenclaturalStatusType.REJECTED_PROP should exist");
            assertNotNull(NomenclaturalStatusType.PROVISIONAL(), "NomenclaturalStatusType.PROVISIONAL should exist");
            assertNotNull(NomenclaturalStatusType.SUBNUDUM(), "NomenclaturalStatusType.SUBNUDUM should exist");
            assertNotNull(NomenclaturalStatusType.OPUS_UTIQUE_OPPR(), "NomenclaturalStatusType.OPUS_UTIQUE_OPPR should exist");
            assertNotNull(NomenclaturalStatusType.VALID(), "NomenclaturalStatusType.VALID should exist");
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
