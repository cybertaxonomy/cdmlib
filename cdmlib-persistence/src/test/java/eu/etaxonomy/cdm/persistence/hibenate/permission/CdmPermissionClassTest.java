/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibenate.permission;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;

/**
 * @author a.kohlbecker
 * @date Feb 25, 2014
 *
 */
public class CdmPermissionClassTest {

    @Test
    public void testTaxonName(){

        Assert.assertEquals(
                CdmPermissionClass.TAXONNAME,
                CdmPermissionClass.getValueOf(TaxonNameFactory.NewZoologicalInstance(Rank.GENUS()))
                );
        Assert.assertEquals(
                CdmPermissionClass.TAXONNAME,
                CdmPermissionClass.getValueOf(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()))
                );
    }

    @Test
    public void testTaxonBase(){

        Assert.assertEquals(
                CdmPermissionClass.TAXONBASE,
                CdmPermissionClass.getValueOf(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null))
                );
        Assert.assertEquals(
                CdmPermissionClass.TAXONBASE,
                CdmPermissionClass.getValueOf(Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null))
                );
    }

    @Test
    public void testDesriptionBase(){

        Assert.assertEquals(
                CdmPermissionClass.DESCRIPTIONBASE,
                CdmPermissionClass.getValueOf(TaxonDescription.NewInstance())
                );
        Assert.assertEquals(
                CdmPermissionClass.DESCRIPTIONBASE,
                CdmPermissionClass.getValueOf(TaxonNameDescription.NewInstance())
                );
        Assert.assertEquals(
                CdmPermissionClass.DESCRIPTIONBASE,
                CdmPermissionClass.getValueOf(SpecimenDescription.NewInstance())
                );
    }

    @Test
    public void testDESCRIPTIONELEMENTBASE(){

        Assert.assertEquals(
                CdmPermissionClass.DESCRIPTIONELEMENTBASE,
                CdmPermissionClass.getValueOf(Distribution.NewInstance())
                );
        Assert.assertEquals(
                CdmPermissionClass.DESCRIPTIONELEMENTBASE,
                CdmPermissionClass.getValueOf(CategoricalData.NewInstance())
                );
        Assert.assertEquals(
                CdmPermissionClass.DESCRIPTIONELEMENTBASE,
                CdmPermissionClass.getValueOf(CommonTaxonName.NewInstance("dmmy", Language.DEFAULT()))
                );
    }

    @Test
    public void testREFERENCE(){
        Assert.assertEquals(
                CdmPermissionClass.REFERENCE,
                CdmPermissionClass.getValueOf(ReferenceFactory.newArticle())
                );
    }

    @Test
    public void testREGISTRATION(){
        Assert.assertEquals(
                CdmPermissionClass.REGISTRATION,
                CdmPermissionClass.getValueOf(Registration.NewInstance())
                );
    }

    @Test
    public void testTYPEDESIGNATION(){
        Assert.assertEquals(
                CdmPermissionClass.TYPEDESIGNATIONBASE,
                CdmPermissionClass.getValueOf(SpecimenTypeDesignation.NewInstance())
                );
        Assert.assertEquals(
                CdmPermissionClass.TYPEDESIGNATIONBASE,
                CdmPermissionClass.getValueOf(NameTypeDesignation.NewInstance())
                );
    }

}
