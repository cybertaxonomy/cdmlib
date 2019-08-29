/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.rights;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals(
                PermissionClass.TAXONNAME,
                PermissionClass.getValueOf(TaxonNameFactory.NewZoologicalInstance(Rank.GENUS()))
                );
        Assert.assertEquals(
                PermissionClass.TAXONNAME,
                PermissionClass.getValueOf(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()))
                );
    }

    @Test
    public void testTaxonBase(){

        Assert.assertEquals(
                PermissionClass.TAXONBASE,
                PermissionClass.getValueOf(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null))
                );
        Assert.assertEquals(
                PermissionClass.TAXONBASE,
                PermissionClass.getValueOf(Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null))
                );
    }

    @Test
    public void testDesriptionBase(){

        Assert.assertEquals(
                PermissionClass.DESCRIPTIONBASE,
                PermissionClass.getValueOf(TaxonDescription.NewInstance())
                );
        Assert.assertEquals(
                PermissionClass.DESCRIPTIONBASE,
                PermissionClass.getValueOf(TaxonNameDescription.NewInstance())
                );
        Assert.assertEquals(
                PermissionClass.DESCRIPTIONBASE,
                PermissionClass.getValueOf(SpecimenDescription.NewInstance())
                );
    }

    @Test
    public void testDESCRIPTIONELEMENTBASE(){

        Assert.assertEquals(
                PermissionClass.DESCRIPTIONELEMENTBASE,
                PermissionClass.getValueOf(Distribution.NewInstance())
                );
        Assert.assertEquals(
                PermissionClass.DESCRIPTIONELEMENTBASE,
                PermissionClass.getValueOf(CategoricalData.NewInstance())
                );
        Assert.assertEquals(
                PermissionClass.DESCRIPTIONELEMENTBASE,
                PermissionClass.getValueOf(CommonTaxonName.NewInstance("dmmy", Language.DEFAULT()))
                );
    }

    @Test
    public void testREFERENCE(){
        Assert.assertEquals(
                PermissionClass.REFERENCE,
                PermissionClass.getValueOf(ReferenceFactory.newArticle())
                );
    }

    @Test
    public void testREGISTRATION(){
        Assert.assertEquals(
                PermissionClass.REGISTRATION,
                PermissionClass.getValueOf(Registration.NewInstance())
                );
    }

    @Test
    public void testSPECIMENOROBSERVATIONBASE(){
        Assert.assertEquals(
                PermissionClass.SPECIMENOROBSERVATIONBASE,
                PermissionClass.getValueOf(FieldUnit.NewInstance())
                );
        Assert.assertEquals(
                PermissionClass.SPECIMENOROBSERVATIONBASE,
                PermissionClass.getValueOf(DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit))
                );
    }

}
