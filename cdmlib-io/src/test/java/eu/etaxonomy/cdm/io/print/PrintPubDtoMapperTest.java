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

/**
 * @author muellera
 * @since 09.09.2026
 */
public class PrintPubDtoMapperTest {

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
    }

    @Test
    public void testMapNodeToDto() {
        PrintPubExportConfigurator config = PrintPubExportConfigurator.NewInstance();
        PrintPubExportState state = new PrintPubExportState(config);
        PrintPubTaxonSummaryDTO taxonDto = mapper.mapNodeToDto(taxonNode, 1, state);
        Assert.assertTrue(taxonDto.homotypicSynonymGroup != null);
        Assert.assertTrue(taxonDto.homotypicSynonymGroup.typeSpecimenString != null);

    }

}
