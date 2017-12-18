/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;

import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 *
 * @author a.kohlbecker
 * @since Jun 23, 2017
 *
 */
@Ignore
public class DerivedUnitConverterIntegrationTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private IOccurrenceService service;

    @Test
    public void toMediaSpecimen_issue7114() throws DerivedUnitConversionException {

        assertEquals(0, service.list(null, null, null, null, null).size());

        DerivedUnit du = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        du.setTitleCache("test derived unit", true);
        du = (DerivedUnit) service.save(du); // intermediate save is essential for this test
        DerivedUnitConverter<MediaSpecimen> duc = DerivedUnitConverterFactory.createDerivedUnitConverter(du, MediaSpecimen.class);
        MediaSpecimen target = duc.convertTo(MediaSpecimen.class, SpecimenOrObservationType.StillImage);
        service.save(target);

        assertEquals(1, service.list(null, null, null, null, null).size());
        assertEquals(1, service.list(MediaSpecimen.class, null, null, null, null).size());
    }

    @Test
    public void toDerivedUnit_issue7114() throws DerivedUnitConversionException {

        assertEquals(0, service.list(null, null, null, null, null).size());

        MediaSpecimen du = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
        du.setTitleCache("test media specimen", true);
        DerivedUnitConverter<DerivedUnit> duc = DerivedUnitConverterFactory.createDerivedUnitConverter(du, DerivedUnit.class);
        du = (MediaSpecimen) service.save(du); // intermediate save is essential for this test
        DerivedUnit target = duc.convertTo(DerivedUnit.class, SpecimenOrObservationType.PreservedSpecimen);
        service.save(target);

        assertEquals(1, service.list(null, null, null, null, null).size());
        assertEquals(1, service.list(DerivedUnit.class, null, null, null, null).size());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
       // using empty database

    }

}
