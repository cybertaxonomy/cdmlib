/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author muellera
 * @since 01.12.2025
 */
public class AnnotatableEntityTest extends EntityTestBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private Annotation annotation;
    private Set<UUID> editorialUuids;

    private Marker marker;
    private Set<UUID> completeMarkerUuids;

    @BeforeEach
    public void setUp() throws Exception {
        annotation = Annotation.NewDefaultLanguageInstance("Anno");
        annotation.setAnnotationType(AnnotationType.EDITORIAL());

        UUID editorialUuid = AnnotationType.EDITORIAL().getUuid();
        editorialUuids = new HashSet<>();
        editorialUuids.add(editorialUuid);

        marker = Marker.NewInstance(MarkerType.COMPLETE(), false);
        UUID completeMarkerUuid = MarkerType.COMPLETE().getUuid();
        completeMarkerUuids = new HashSet<>();
        completeMarkerUuids.add(completeMarkerUuid);
    }

    @Test
    public void testHasSupplementalDataUuids() {

        boolean ignoreSources = false;
        Identifier annotatableEntity = Identifier.NewInstance("abc", IdentifierType.IDENTIFIER_NAME_WFO());
        assertFalse(annotatableEntity.hasSupplementalData());
        assertFalse(annotatableEntity.hasSupplementalData(editorialUuids, ignoreSources));

        annotatableEntity.addAnnotation(annotation);
        assertTrue(annotatableEntity.hasSupplementalData());
        assertFalse(annotatableEntity.hasSupplementalData(editorialUuids, ignoreSources));
        assertTrue(annotatableEntity.hasSupplementalData(new HashSet<>(), ignoreSources));
        assertTrue(annotatableEntity.hasSupplementalData(completeMarkerUuids, ignoreSources));

        //revert
        annotatableEntity.removeAnnotation(annotation);
        assertFalse(annotatableEntity.hasSupplementalData());

        //test with marker
        annotatableEntity.addMarker(marker);
        assertTrue(annotatableEntity.hasSupplementalData());
        assertFalse(annotatableEntity.hasSupplementalData(completeMarkerUuids, ignoreSources));
        assertTrue(annotatableEntity.hasSupplementalData(editorialUuids, ignoreSources));

        //test with missing uuid filter
        editorialUuids = null;
        assertTrue(annotatableEntity.hasSupplementalData(editorialUuids, ignoreSources));

    }

}
