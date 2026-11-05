/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.rights;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.kohlbecker
 * @since Feb 25, 2014
 */
public class CdmPermissionClassTest {

    @Test
    public void testTaxonName(){

        assertEquals(
                PermissionClass.TAXONNAME,
                PermissionClass.getValueOf(TaxonNameFactory.NewZoologicalInstance(Rank.GENUS()))
                );
        assertEquals(
                PermissionClass.TAXONNAME,
                PermissionClass.getValueOf(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()))
                );
    }

    @Test
    public void testTaxonBase(){

        assertEquals(
                PermissionClass.TAXONBASE,
                PermissionClass.getValueOf(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null))
                );
        assertEquals(
                PermissionClass.TAXONBASE,
                PermissionClass.getValueOf(Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null))
                );
    }

    @Test
    public void testDesriptionBase(){

        assertEquals(
                PermissionClass.DESCRIPTIONBASE,
                PermissionClass.getValueOf(TaxonDescription.NewInstance())
                );
        assertEquals(
                PermissionClass.DESCRIPTIONBASE,
                PermissionClass.getValueOf(TaxonNameDescription.NewInstance())
                );
        assertEquals(
                PermissionClass.DESCRIPTIONBASE,
                PermissionClass.getValueOf(SpecimenDescription.NewInstance())
                );
    }

    @Test
    public void testDESCRIPTIONELEMENTBASE(){

        assertEquals(
                PermissionClass.DESCRIPTIONELEMENTBASE,
                PermissionClass.getValueOf(Distribution.NewInstance())
                );
        assertEquals(
                PermissionClass.DESCRIPTIONELEMENTBASE,
                PermissionClass.getValueOf(CategoricalData.NewInstance())
                );
        assertEquals(
                PermissionClass.DESCRIPTIONELEMENTBASE,
                PermissionClass.getValueOf(CommonTaxonName.NewInstance("dmmy", Language.DEFAULT()))
                );
    }

    @Test
    public void testREFERENCE(){
        assertEquals(
                PermissionClass.REFERENCE,
                PermissionClass.getValueOf(ReferenceFactory.newArticle())
                );
    }

    @Test
    public void testREGISTRATION(){
        assertEquals(
                PermissionClass.REGISTRATION,
                PermissionClass.getValueOf(Registration.NewInstance())
                );
    }

    @Test
    public void testSPECIMENOROBSERVATIONBASE(){
        assertEquals(
                PermissionClass.SPECIMENOROBSERVATIONBASE,
                PermissionClass.getValueOf(FieldUnit.NewInstance())
                );
        assertEquals(
                PermissionClass.SPECIMENOROBSERVATIONBASE,
                PermissionClass.getValueOf(DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit))
                );
    }
}