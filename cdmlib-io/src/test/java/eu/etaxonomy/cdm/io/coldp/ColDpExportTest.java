/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.coldp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportTestBase;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;

/**
 * Test class for COL-DP export.
 * Note: this test was originally copied from CDM-light export test.
 *
 * @author a.mueller
 * @date 19.07.2023
 */
public class ColDpExportTest
        extends TaxonTreeExportTestBase<ColDpExportConfigurator,ColDpExportState> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    //still missing: alternativeID, sourceID, sequenceIndex, branchLength, scrutinizerXXX, referenceID, environment, extinct, ...
    //FIXME link
    private String expectedSubspeciesTaxonLine = uuid(subspeciesTaxonUuid)+ NONE2 + uuid(speciesTaxonUuid) + NONE2 +
            uuid(subspeciesNameUuid) + NONE + uuid(ref1UUID)+ NONE3 + FALSE + NONE + BOOL_NULL + NONE20 + NONE_END;

    //  TODO check other NONE entries:
    private String expectedNomRefLine = uuid(subspeciesNomRefUuid) + NONE2 +
            "\"Mill. 1804: The book of botany 3\",\"book\",\"Mill.\"," + NONE4 + "\"1804\"," + NONE3 + "\"3\"," + NONE10 + NONE_END;

    //TODO check referenceID and remarks
    private String expectedArmenianDistributionLine = uuid(subspeciesTaxonUuid) + NONE +
            "\"ARM\",\"Armenia\",\"iso\",\"uncertain\",\"e2edf07d-552a-46cd-85da-86a491847aeb\"," + NONE_END;

    private String expectedFamilyNameLine = uuid(familyNameUuid) + "\"wfo:WFO-12347f\"," + NONE2 + "\"Familyname\",\"L.\",\"family\",\"Familyname\"," +
            NONE4 + NONE + "\"L.\"," + NONE + "\"1752\"," + NONE3 + "\"ICN\",\"conserved\"," +
            uuid(familyNomRefUuid) + "\"1752\",\"22\"," + NONE2 + NONE_END;

    //FIXME basionymID, nom. status
    private String basionymID = NONE;
    private String expectedSubspeciesNameLine = uuid(subspeciesNameUuid) + "\"wfo:WFO-12347ss\"," + NONE + basionymID + "\"Genus species subsp. subspec\",\"Mill.\",\"subspecies\","
            + NONE + "\"Genus\"," + NONE + "\"species\",\"subspec\"," + NONE + "\"Mill.\"," + NONE + "\"1804\"," + NONE3 +
            "\"ICN\"," + VALID + uuid(subspeciesNomRefUuid) + "\"1804\",\"22\"," + NONE2 + NONE_END;
    private String expectedSubspeciesNameLineWithFullName = uuid(subspeciesNameUuid) + "\"wfo:WFO-12347ss\"," + NONE + basionymID + "\"Genus species subsp. subspec\","
            + "\"Genus species subsp. subspec Mill.\",\"Mill.\",\"subspecies\","
            + NONE + "\"Genus\"," + NONE + "\"species\",\"subspec\"," + NONE + "\"Mill.\"," + NONE + "\"1804\"," + NONE3 +
            "\"ICN\"," + VALID + uuid(subspeciesNomRefUuid) + "\"1804\",\"22\"," + NONE2 + NONE_END;

    //FIXME media line
    private String expectedMediaLine = uuid(subspeciesTaxonUuid)+ NONE + "\"https://www.abc.de/fghi.jpg\",\"image/jpg\","
            + NONE + "\"My nice image\",\"2023-07-20\"," + NONE2 + NONE_END;

    //FIXME name relation line 1
    private String expectedNameRelationLine1 = uuid(speciesNameUuid) + uuid(basionymNameUuid)+
            NONE + "\"basionym\"," + NONE + NONE_END;

    //FIXME name relation line 2
    private String expectedNameRelationLine2 = NONE_END;

    //FIXME type material line
    private String expectedTypeMaterialLine = uuid(specimenUuid) + NONE + uuid(speciesNameUuid) +
            "\"Textual typedesignation test.\"," + NONE + "\"B\",\"A555\"," + NONE + "\"Somewhere in the forest\",\"Armenia\",\"55.55611111111111\",\"-15.22\"," +
            NONE + NONE3 + "\"Collector team\"," + NONE + NONE + NONE_END;

    //FIXME vernacular name line
    //TODO vern name: area + refID
    private String expectedVernacularNameLine = uuid(speciesTaxonUuid) + NONE +
            "\"Tanne\"," + NONE + "\"ger\"," + NONE3 + "\"c68e0a88-7b96-465d-b4ce-d14b13daf94f\"" ;

    @BeforeEach
    public void setUp()  {
        createFullTestDataSet();
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
//    @Disabled
    public void testSubTree_andWithFullName(){

        //config+invoke
        ColDpExportConfigurator config = newConfigurator();
        config.setIncludeFullName(true);
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(node4Uuid));
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);

        //counts and result lists
        List<String> taxonResult = getStringList(data, ColDpExportTable.TAXON);
        assertEquals(1, taxonResult.size() - COUNT_HEADER, "There should be 1 taxon");

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        assertEquals(0, synonymResult.size() - COUNT_HEADER, "There should be no synonym");

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        assertEquals(3, referenceResult.size() - COUNT_HEADER, "There should be 3 references (1 nomenclatural references and 1 sec reference, 1 fact reference)");

        List<String> distributionResult = getStringList(data, ColDpExportTable.DISTRIBUTION);
        assertEquals(1, distributionResult.size() - COUNT_HEADER, "There should be 1 distribution");

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME_WITH_FULLNAME);
        assertEquals(1, nameResult.size() - COUNT_HEADER, "There should be 1 name");

        //taxon
        //... species
        String taxonStr = getTableString(data, ColDpExportTable.TAXON);
        String notExpected = speciesTaxonUuid.toString();
        assertFalse(taxonStr.startsWith(notExpected), "Result must not contain root of subtree taxon (species taxon)");
        //... subspecies
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        assertEquals(expectedSubspeciesTaxonLine, subspeciesLine);

        //reference
        String nomRefLine = getLine(referenceResult, subspeciesNomRefUuid);
        assertEquals(expectedNomRefLine, nomRefLine);

        //distribution
        String distributionStr = getLine(distributionResult, subspeciesTaxonUuid); // new String(distribution);
        assertEquals(expectedArmenianDistributionLine, distributionStr);

        //name
        //TODO duplicated check withUnpublished
        String nameStr = getLine(nameResult, subspeciesNameUuid);
        assertEquals(expectedSubspeciesNameLineWithFullName, nameStr);
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })// data itself is created in base class
    public void testFullTreeWithUnpublished(){

        //config+invoke
        ColDpExportConfigurator config = newConfigurator();
        config.getTaxonNodeFilter().setIncludeUnpublished(true);
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);

        //test counts
        List<String> taxonResult = getStringList(data, ColDpExportTable.TAXON);
        assertEquals(5, taxonResult.size() - COUNT_HEADER, "There should be 5 taxa");

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        assertEquals(4, synonymResult.size() - COUNT_HEADER, "There should be 4 synonym");

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME);
        assertEquals(9, nameResult.size() - COUNT_HEADER, "There should be 9 names");

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        assertEquals(14, referenceResult.size() - COUNT_HEADER, "There should be 14 references (9 nomenclatural references and 3 sec reference, 2 fact references)");

        List<String> distributionResult = getStringList(data, ColDpExportTable.DISTRIBUTION);
        assertEquals(1, distributionResult.size() - COUNT_HEADER, "There should be 1 distribution");

        List<String> vernacularNameResult = getStringList(data, ColDpExportTable.VERNACULAR_NAME);
        assertEquals(1, vernacularNameResult.size() - COUNT_HEADER, "There should be 1 vernacular names");

        //FIXME should be 2 at least, include name type designations
        List<String> nameRelationResult = getStringList(data, ColDpExportTable.NAME_RELATION);
        assertEquals(1, nameRelationResult.size() - COUNT_HEADER, "There should be 1 name relations");

        List<String> mediaResult = getStringList(data, ColDpExportTable.MEDIA);
        assertEquals(1, mediaResult.size() - COUNT_HEADER, "There should be 1 media");

        List<String> typeMaterialResult = getStringList(data, ColDpExportTable.TYPE_MATERIAL);
        assertEquals(1, typeMaterialResult.size() - COUNT_HEADER, "There should be 1 type materials");

        //not yet necessary

        List<String> taxonConceptRelResult = getStringList(data, ColDpExportTable.TAXON_CONCEPT_RELATION);
        assertEquals(0, taxonConceptRelResult.size() - COUNT_HEADER, "There should be 0 taxon concept relations");

        List<String> speciesInteractionResult = getStringList(data, ColDpExportTable.SPECIES_INTERACTION);
        assertEquals(0, speciesInteractionResult.size() - COUNT_HEADER, "There should be 0 species interaction");

        //not yet in model

        List<String> speciesEstimateResult = getStringList(data, ColDpExportTable.SPECIES_ESTIMATE);
        assertEquals(0, speciesEstimateResult.size() - COUNT_HEADER, "There should be 0 species estimates");

        List<String> treatmentResult = getStringList(data, ColDpExportTable.TREATMENT);
        assertEquals(0, treatmentResult.size() - COUNT_HEADER, "There should be 0 taxon concept relations");

        //test single data

        //... invisible root node
        assertEquals(0, taxonResult.stream().filter(line->line.contains(rootNodeUuid.toString())).count(), "Result must not contain root taxon");

        //subspecies taxon
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        assertEquals(expectedSubspeciesTaxonLine, subspeciesLine);

        //unpublished/excluded/note
        //TODO evaluate unpublished flag and discuss how to handle excluded
        String unpublishedLine = getLine(taxonResult, subspeciesUnpublishedTaxonUuid);
        String expectedExcluded = uuid(subspeciesUnpublishedTaxonUuid)+ NONE2 + uuid(speciesTaxonUuid) + NONE2 +
                uuid(subspeciesUnpublishedNameUUID) + NONE + uuid(ref1UUID)+ NONE3 + FALSE + NONE + BOOL_NULL +
                NONE20 + NONE_END;
        assertEquals(expectedExcluded, unpublishedLine);

        //references
        String nomRefLine = getLine(referenceResult, subspeciesNomRefUuid);
        assertEquals(expectedNomRefLine, nomRefLine);

        //distribution
        String distributionStr = getLine(distributionResult, subspeciesTaxonUuid); // new String(distribution);
        assertEquals(expectedArmenianDistributionLine, distributionStr);

        //name
        //... family => test nom. status 'conserved'
        String nameStr = getLine(nameResult, familyNameUuid);
        assertEquals(expectedFamilyNameLine, nameStr);
        //... subspecies
        nameStr = getLine(nameResult, subspeciesNameUuid);
        assertEquals(expectedSubspeciesNameLine, nameStr);

        //media
//        String mediaString = new String(data.get(ColDpExportTable.MEDIA.getTableName()));
//        System.out.println(mediaString);
        String mediaStr = getLine(mediaResult, subspeciesTaxonUuid);
        assertEquals(expectedMediaLine, mediaStr);

        //name relation
        //TODO is speciesNameUuid specific enough?
        String nameRelStr = getLine(nameRelationResult, speciesNameUuid);
        assertEquals(expectedNameRelationLine1, nameRelStr);

        //type material
        print(typeMaterialResult);
        String typeMaterialStr = getLine(typeMaterialResult, specimenUuid);
        assertEquals(expectedTypeMaterialLine, typeMaterialStr);

        //vernacular name
        String vernacularNameStr = getLine(vernacularNameResult, speciesTaxonUuid);
        assertEquals(expectedVernacularNameLine, vernacularNameStr);

        //TODO   Synonym,
        //     TaxonConceptRelation, SpeciesInteraction, SpeciesEstimate, Treatment
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testFullData(){

        //config+invoke
        ColDpExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);
        assertTrue(result.getExportType().equals(ExportType.COLDP)); //test export type

        //test counts
        List<String> taxonResult = getStringList(data, ColDpExportTable.TAXON);
        assertEquals(4, taxonResult.size() - COUNT_HEADER, "There should be 4 taxa");

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME);
        assertEquals(7, nameResult.size() - COUNT_HEADER, "There should be 7 names");

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        assertEquals(12, referenceResult.size() - COUNT_HEADER, "There should be 12 references");

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        assertEquals(3, synonymResult.size() - COUNT_HEADER, "There should be three synonyms");

        //tbc
    }

    @Override
    protected ColDpExportConfigurator newConfigurator() {
        ColDpExportConfigurator config = ColDpExportConfigurator.NewInstance();
        config.setTarget(TARGET.EXPORT_DATA);
        return config;
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}