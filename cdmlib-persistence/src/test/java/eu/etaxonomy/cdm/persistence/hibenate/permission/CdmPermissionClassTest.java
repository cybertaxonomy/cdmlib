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
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
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
    public void testCdmPermissionClass(){
        Assert.assertEquals(
                CdmPermissionClass.TAXONNAMEBASE,
                CdmPermissionClass.getValueOf(ZoologicalName.NewInstance(Rank.GENUS()))
                );
        Assert.assertEquals(
                CdmPermissionClass.TAXONNAMEBASE,
                CdmPermissionClass.getValueOf(BotanicalName.NewInstance(Rank.GENUS()))
                );
        Assert.assertEquals(
                CdmPermissionClass.TAXONBASE,
                CdmPermissionClass.getValueOf(Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null))
                );
        Assert.assertEquals(
                CdmPermissionClass.TAXONBASE,
                CdmPermissionClass.getValueOf(Synonym.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null))
                );
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

}
