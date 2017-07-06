/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 *
 * @author a.kohlbecker
 * @since Jun 23, 2017
 *
 */
public class DerivedUnitConverterTest extends Assert {

    @Test
    public void toMediaSpecimen() throws DerivedUnitConversionException {
        DerivedUnit du = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        du.setTitleCache("test derived unit", true);
        DerivedUnitConverter<MediaSpecimen> duc = new DerivedUnitConverter<MediaSpecimen>(du);
        MediaSpecimen target = duc.convertTo(MediaSpecimen.class, SpecimenOrObservationType.StillImage);
        assertNotNull(target);
        assertEquals(SpecimenOrObservationType.StillImage, target.getRecordBasis());
        assertEquals("test derived unit", target.getTitleCache());
    }

    public void toDerivedUnit() throws DerivedUnitConversionException {
        MediaSpecimen du = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
        du.setTitleCache("test media specimen", true);
        DerivedUnitConverter<DerivedUnit> duc = new DerivedUnitConverter<DerivedUnit>(du);
        DerivedUnit target = duc.convertTo(DerivedUnit.class, SpecimenOrObservationType.PreservedSpecimen);
        assertNotNull(target);
        assertEquals(SpecimenOrObservationType.PreservedSpecimen, target.getRecordBasis());
        assertEquals("test media specimen", target.getTitleCache());
    }

}
