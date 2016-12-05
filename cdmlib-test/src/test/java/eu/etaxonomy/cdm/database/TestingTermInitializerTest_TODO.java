/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

import org.dbunit.dataset.filter.ExcludeTableFilter;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class TestingTermInitializerTest_TODO extends CdmIntegrationTest {

    private final UUID taxonomicallyIncludedInUuid = UUID.fromString("d13fecdf-eb44-4dd7-9244-26679c05df1c");

    @Before
    public void setUp() {}

//    @Test  uncomment for creating datasets
    public void testPrintDataSet() {
        try {
            ExcludeTableFilter filter = new ExcludeTableFilter();
            filter.excludeTable("Rights");  //throws exception with H2
            printDataSetWithNull(new FileOutputStream("NewDataSet.xml"), null, filter, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInit() {
        assertNotNull("TermInitializer should have initialized Language.DEFAULT", Language.DEFAULT());
        assertEquals("Language.DEFAULT should equal Language.ENGLISH", Language.DEFAULT(), Language.ENGLISH());
    }

    @Test
    public void testMarkerTypeIds() {
        assertEquals("We expect MarkerType.TO_BE_CHECKED to have an id of 893",893,MarkerType.TO_BE_CHECKED().getId());
    }

    @Test
    public void testFeatureIds() {
        assertEquals("We expect Feature.ECOLOGY to have an id of 922",922,Feature.ECOLOGY().getId());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType#TAXONOMICALLY_INCLUDED_IN()}.
     */
    @Test
    public final void testTermsAreLoaded() {
        assertNotNull("TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN should have been initialized",TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN());
        assertEquals("TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN should have a uuid of " + taxonomicallyIncludedInUuid.toString(),taxonomicallyIncludedInUuid, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN().getUuid());
        assertNotNull("SynonymType.INFERREDEPITHET should be loaded" + SynonymType.INFERRED_EPITHET_OF());
    }

    @Test
    public void testUnlazyStatusTerms() {
            assertNotNull("NomenclaturalStatusType.SUPERFLUOUS should exist",NomenclaturalStatusType.SUPERFLUOUS());
            assertNotNull("NomenclaturalStatusType.NUDUM should exist",NomenclaturalStatusType.NUDUM());
            assertNotNull("NomenclaturalStatusType.ILLEGITIMATE should exist",NomenclaturalStatusType.ILLEGITIMATE());
            assertNotNull("NomenclaturalStatusType.INVALID should exist",NomenclaturalStatusType.INVALID());
            assertNotNull("NomenclaturalStatusType.CONSERVED should exist",NomenclaturalStatusType.CONSERVED());
            assertNotNull("NomenclaturalStatusType.ALTERNATIVE should exist",NomenclaturalStatusType.ALTERNATIVE());
            assertNotNull("NomenclaturalStatusType.REJECTED should exist",NomenclaturalStatusType.REJECTED());
            assertNotNull("NomenclaturalStatusType.REJECTED_PROP should exist",NomenclaturalStatusType.REJECTED_PROP());
            assertNotNull("NomenclaturalStatusType.PROVISIONAL should exist",NomenclaturalStatusType.PROVISIONAL());
            assertNotNull("NomenclaturalStatusType.SUBNUDUM should exist",NomenclaturalStatusType.SUBNUDUM());
            assertNotNull("NomenclaturalStatusType.OPUS_UTIQUE_OPPR should exist",NomenclaturalStatusType.OPUS_UTIQUE_OPPR());
            assertNotNull("NomenclaturalStatusType.VALID should exist",NomenclaturalStatusType.VALID());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
