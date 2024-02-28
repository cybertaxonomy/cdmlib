/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.dto.portal.ContainerDto;
import eu.etaxonomy.cdm.api.dto.portal.FeatureDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author muellera
 * @since 26.02.2024
 */
public class PortalDtoLoaderTest extends CdmTransactionalIntegrationTest {

    private UUID taxonUuid1 = UUID.fromString("075d1b8c-91d3-4b10-93b7-08ac872d09e8");

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private IPortalDtoService portalDtoService;

    @Test
    public void test() {
        createTestData();
        commitAndStartNewTransaction();
        TaxonPageDtoConfiguration config = new TaxonPageDtoConfiguration();
        config.setWithSpecimens(false);
        config.setTaxonUuid(taxonUuid1);
        TaxonPageDto dto = portalDtoService.taxonPageDto(config);

        Assert.assertNotNull(dto);
        List<TaggedText> list = dto.getTaggedName();
        Assert.assertEquals("Genus", list.get(0).getText());

        //facts
        ContainerDto<FeatureDto> taxonFacts = dto.getTaxonFacts();
        Assert.assertEquals(1, taxonFacts.getCount());

    }

    private void createTestData() {
        Person author = Person.NewInstance("Mill.", "Miller", "M.M.", "Michael");
        Reference nomRef = ReferenceFactory.newBook();
        nomRef.setTitle("My book");
        TaxonName accName = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Genus", null, "species", null, author, nomRef, "55", null);
        Reference secRef = ReferenceFactory.newBook();
        secRef.setTitle("My secbook");
        Taxon taxon = Taxon.NewInstance(accName, secRef);
        taxon.setUuid(taxonUuid1);
        taxonService.save(taxon);

        //facts
        TaxonDescription td = TaxonDescription.NewInstance(taxon);
        Distribution germany = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.PRESENT());
        td.addElement(germany);
        Distribution france = Distribution.NewInstance(Country.FRANCE(), PresenceAbsenceTerm.INTRODUCED());
        td.addElement(france);

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
    }

}
