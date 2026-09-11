/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.print;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.io.print.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.io.print.mapper.PrintPubBibliographyCollector;
import eu.etaxonomy.cdm.io.print.mapper.PrintPubDtoMapper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author muellera
 * @since 09.09.2026
 */
public class PrintPubDtoMapperTest extends TermTestBase {

    private TaxonNode taxonNode;
    private PrintPubDtoMapper mapper;

    @Before
    public void setUp() throws Exception {

        PrintPubBibliographyCollector bibCollector = new PrintPubBibliographyCollector();
        mapper = new PrintPubDtoMapper(bibCollector);

        //name
        TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setGenusOrUninomial("Genus");
        name.setSpecificEpithet("species");
        //ref
        Reference sec = ReferenceFactory.newBook();
        sec.setTitle("My sec");
        sec.setDatePublished(TimePeriodParser.parseStringVerbatim("2026"));
        //taxon
        Taxon taxon = Taxon.NewInstance(name, sec);
        Classification classification = Classification.NewInstance("Test Classification", sec);
        taxonNode = classification.addChildTaxon(taxon, sec, "22");
        //type specimen
        DerivedUnit typeSpecimen = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        typeSpecimen.setTitleCache("My type specimen", true);
        taxonNode.getTaxon().getName()
            .addSpecimenTypeDesignation(typeSpecimen, SpecimenTypeDesignationStatus.HOLOTYPE(),
                    sec, "33", null, false, false);

        //common name
        TaxonDescription factSet = TaxonDescription.NewInstance(taxon);
        CommonTaxonName commonName = CommonTaxonName.NewInstance("My flower", Language.ENGLISH(), NamedArea.EUROPE());
        factSet.addElement(commonName);
        CommonTaxonName commonName2 = CommonTaxonName.NewInstance("First flower name", Language.IRISH(), NamedArea.AFRICA());
        factSet.addElement(commonName2);

        //distribution
        Distribution distribution = Distribution.NewInstance(NamedArea.EUROPE(), PresenceAbsenceTerm.INTRODUCED());
        factSet.addElement(distribution);
        Distribution distribution2 = Distribution.NewInstance(NamedArea.AFRICA(), PresenceAbsenceTerm.PRESENT());
        factSet.addElement(distribution2);

    }

    @Test
    public void testMapNodeToDto() {
        PrintPubExportConfigurator config = PrintPubExportConfigurator.NewInstance();
        PrintPubExportState state = new PrintPubExportState(config);

        //map
        PrintPubTaxonSummaryDTO taxonDto = mapper.mapNodeToDto(taxonNode, 1, state);

        //types
        Assert.assertTrue(taxonDto.homotypicSynonymGroup != null);
        Assert.assertTrue(taxonDto.homotypicSynonymGroup.typeSpecimenString != null);

        //facts
        Assert.assertEquals("First flower name [Irish], My flower [English]", taxonDto.commonNameString);

        //TODO preliminary
        Assert.assertEquals("Africa, Europe", taxonDto.distributionString);

    }

}
