/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesSpringIntegrationTest;

/**
 * Non-transactional integration test (#7138 workaround with transactional base +
 * {@code commit()} no longer needed after Unitils → Spring Test / {@code @CdmDataSet}).
 *
 * @author a.kohlbecker
 * @since Jun 23, 2017
 */
@CdmDataSet(cleanSweep = true) // the dataset cleans up the DerivedUnits created in the tests
public class DerivedUnitConverterIntegrationTest extends CdmServicesSpringIntegrationTest {

    @Autowired
    private IOccurrenceService service;

    @Test
    public void toMediaSpecimen_issue7114() throws DerivedUnitConversionException {

        assertEquals(0, service.list(null, null, null, null, null).size());

        DerivedUnit du = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        du.setTitleCache("test derived unit", true);
        SpecimenTypeDesignation std = SpecimenTypeDesignation.NewInstance();
        std.setTypeSpecimen(du);
        du = service.save(du); // intermediate save is essential for this test
        DerivedUnitConverter<MediaSpecimen> converter = new DerivedUnitConverter<>(std);
        SpecimenTypeDesignation newDu = converter.convertTo(MediaSpecimen.class, SpecimenOrObservationType.StillImage);
        assertEquals(du, converter.oldDerivedUnit());
        service.saveOrUpdate(newDu.getTypeSpecimen());
        service.delete(du);
        assertEquals(1, service.list(null, null, null, null, null).size());
        assertEquals(1, service.list(MediaSpecimen.class, null, null, null, null).size());
    }

    @Test
    public void toDerivedUnit_issue7114() throws DerivedUnitConversionException {

        assertEquals(0, service.list(null, null, null, null, null).size());

        MediaSpecimen du = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
        du.setTitleCache("test media specimen", true);
        SpecimenTypeDesignation std = SpecimenTypeDesignation.NewInstance();
        std.setTypeSpecimen(du);
        DerivedUnitConverter<DerivedUnit> duc = new DerivedUnitConverter<>(std);
        du = service.save(du); // intermediate save is essential for this test
        duc.convertTo(DerivedUnit.class, SpecimenOrObservationType.PreservedSpecimen);

        assertEquals(1, service.list(null, null, null, null, null).size());
        assertEquals(1, service.list(DerivedUnit.class, null, null, null, null).size());
    }

    /**
     * Test with DerivedUnit which is used in a couple of associations to prevent from
     * org.hibernate.ObjectDeletedException: deleted object would be re-saved by cascade ...
     */
    @Test
    @CdmDataSet(cleanSweep = true, value = "DerivedUnitConverterIntegrationTest.cascadeDelete.xml")
    public void cascadeDelete() throws DerivedUnitConversionException {

        UUID uuid = UUID.fromString("10eceb2c-9b51-458e-8dcd-2cb92cc558a9");
        MediaSpecimen du = (MediaSpecimen) service.load(uuid, Arrays.asList("*",
                "derivedFrom.*",
                "derivedFrom.originals.*",
                "derivedFrom.originals.derivationEvents",
                "specimenTypeDesignations.typifiedNames.typeDesignations",
                "specimenTypeDesignations.typifiedNames.homotypicalGroup",
                "specimenTypeDesignations.annotations",
                "specimenTypeDesignations.markers",
                "specimenTypeDesignations.registrations",
                "specimenTypeDesignations.sources",
                "derivedFrom.originals.gatheringEvent.$",
                "derivedFrom.originals.gatheringEvent.country",
                "derivedFrom.originals.gatheringEvent.collectingAreas",
                "derivedFrom.originals.gatheringEvent.actor.teamMembers",
                "derivedFrom.originals.derivationEvents.derivatives"));
        SpecimenTypeDesignation specimenTypeDesignation = du.getSpecimenTypeDesignations().iterator().next();
        DerivedUnitConverter<DerivedUnit> duc = new DerivedUnitConverter<>(specimenTypeDesignation);
        SpecimenTypeDesignation newSpecimenTypeDesignation = duc.convertTo(DerivedUnit.class, SpecimenOrObservationType.HumanObservation);
        DerivedUnit target = newSpecimenTypeDesignation.getTypeSpecimen();

        assertEquals(2, service.list(null, null, null, null, null).size());
        assertEquals(1, service.list(DerivedUnit.class, null, null, null, null).size());
        assertEquals(1, service.list(FieldUnit.class, null, null, null, null).size());
    }
}
