/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.coldp;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;

import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportTestBase;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

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
            "\"ARM\",\"Armenia\",\"iso\",\"uncertain\"," + NONE + NONE_END;

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
            NONE + NONE + "\"B\",\"A555\"," + NONE + "\"Somewhere in the forest\",\"Armenia\",\"55.55611111111111\",\"-15.22\"," +
            NONE + NONE3 + "\"Collector team\"," + NONE + NONE + NONE_END;

    //FIXME vernacular name line
    //TODO vern name: area + refID
    private String expectedVernacularNameLine = uuid(speciesTaxonUuid) + NONE +
            "\"Tanne\"," + NONE + "\"ger\"," + NONE3 + NONE_END;

    @Before
    public void setUp()  {
        createFullTestDataSet();
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
//    @Ignore
    public void testSubTree_andWithFullName(){

        //config+invoke
        ColDpExportConfigurator config = newConfigurator();
        config.setIncludeFullName(true);
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(node4Uuid));
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);

        //counts and result lists
        List<String> taxonResult = getStringList(data, ColDpExportTable.TAXON);
        Assert.assertEquals("There should be 1 taxon", 1, taxonResult.size() - COUNT_HEADER);

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        Assert.assertEquals("There should be no synonym", 0, synonymResult.size() - COUNT_HEADER);

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        Assert.assertEquals("There should be 2 references (1 nomenclatural references and 1 sec reference)", 2, referenceResult.size() - COUNT_HEADER);

        List<String> distributionResult = getStringList(data, ColDpExportTable.DISTRIBUTION);
        Assert.assertEquals("There should be 1 distribution", 1, distributionResult.size() - COUNT_HEADER);

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME_WITH_FULLNAME);
        Assert.assertEquals("There should be 1 name", 1, nameResult.size() - COUNT_HEADER);

        //taxon
        //... species
        String taxonStr = getTableString(data, ColDpExportTable.TAXON);
        String notExpected = speciesTaxonUuid.toString();
        Assert.assertFalse("Result must not contain root of subtree taxon (species taxon)", taxonStr.startsWith(notExpected));
        //... subspecies
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        Assert.assertEquals(expectedSubspeciesTaxonLine, subspeciesLine);

        //reference
        String nomRefLine = getLine(referenceResult, subspeciesNomRefUuid);
        Assert.assertEquals(expectedNomRefLine, nomRefLine);

        //distribution
        String distributionStr = getLine(distributionResult, subspeciesTaxonUuid); // new String(distribution);
        Assert.assertEquals(expectedArmenianDistributionLine, distributionStr);

        //name
        //TODO duplicated check withUnpublished
        String nameStr = getLine(nameResult, subspeciesNameUuid);
        Assert.assertEquals(expectedSubspeciesNameLineWithFullName, nameStr);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })// data itself is created in base class
    public void testFullTreeWithUnpublished(){

        //config+invoke
        ColDpExportConfigurator config = newConfigurator();
        config.getTaxonNodeFilter().setIncludeUnpublished(true);
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);

        //test counts
        List<String> taxonResult = getStringList(data, ColDpExportTable.TAXON);
        Assert.assertEquals("There should be 5 taxa", 5, taxonResult.size() - COUNT_HEADER);

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        Assert.assertEquals("There should be 3 synonym", 3, synonymResult.size() - COUNT_HEADER);

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME);
        Assert.assertEquals("There should be 8 names", 8, nameResult.size() - COUNT_HEADER);

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        Assert.assertEquals("There should be 11 references (8 nomenclatural references and 3 sec reference)", 11, referenceResult.size() - COUNT_HEADER);

        List<String> distributionResult = getStringList(data, ColDpExportTable.DISTRIBUTION);
        Assert.assertEquals("There should be 1 distribution", 1, distributionResult.size() - COUNT_HEADER);

        List<String> vernacularNameResult = getStringList(data, ColDpExportTable.VERNACULAR_NAME);
        Assert.assertEquals("There should be 1 vernacular names", 1, vernacularNameResult.size() - COUNT_HEADER);

        //FIXME should be 2 at least, include name type designations
        List<String> nameRelationResult = getStringList(data, ColDpExportTable.NAME_RELATION);
        Assert.assertEquals("There should be 1 name relations", 1, nameRelationResult.size() - COUNT_HEADER);

        List<String> mediaResult = getStringList(data, ColDpExportTable.MEDIA);
        Assert.assertEquals("There should be 1 media", 1, mediaResult.size() - COUNT_HEADER);

        List<String> typeMaterialResult = getStringList(data, ColDpExportTable.TYPE_MATERIAL);
        Assert.assertEquals("There should be 1 type materials", 1, typeMaterialResult.size() - COUNT_HEADER);

        //not yet necessary

        List<String> taxonConceptRelResult = getStringList(data, ColDpExportTable.TAXON_CONCEPT_RELATION);
        Assert.assertEquals("There should be 0 taxon concept relations", 0, taxonConceptRelResult.size() - COUNT_HEADER);

        List<String> speciesInteractionResult = getStringList(data, ColDpExportTable.SPECIES_INTERACTION);
        Assert.assertEquals("There should be 0 species interaction", 0, speciesInteractionResult.size() - COUNT_HEADER);

        //not yet in model

        List<String> speciesEstimateResult = getStringList(data, ColDpExportTable.SPECIES_ESTIMATE);
        Assert.assertEquals("There should be 0 species estimates", 0, speciesEstimateResult.size() - COUNT_HEADER);

        List<String> treatmentResult = getStringList(data, ColDpExportTable.TREATMENT);
        Assert.assertEquals("There should be 0 taxon concept relations", 0, treatmentResult.size() - COUNT_HEADER);

        //test single data

        //... invisible root node
        Assert.assertEquals("Result must not contain root taxon",
                0, taxonResult.stream().filter(line->line.contains(rootNodeUuid.toString())).count());

        //subspecies taxon
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        Assert.assertEquals(expectedSubspeciesTaxonLine, subspeciesLine);

        //unpublished/excluded/note
        //TODO evaluate unpublished flag and discuss how to handle excluded
        String unpublishedLine = getLine(taxonResult, subspeciesUnpublishedTaxonUuid);
        String expectedExcluded = uuid(subspeciesUnpublishedTaxonUuid)+ NONE2 + uuid(speciesTaxonUuid) + NONE2 +
                uuid(subspeciesUnpublishedNameUUID) + NONE + uuid(ref1UUID)+ NONE3 + FALSE + NONE + BOOL_NULL +
                NONE20 + NONE_END;
        Assert.assertEquals(expectedExcluded, unpublishedLine);

        //references
        String nomRefLine = getLine(referenceResult, subspeciesNomRefUuid);
        Assert.assertEquals(expectedNomRefLine, nomRefLine);

        //distribution
        String distributionStr = getLine(distributionResult, subspeciesTaxonUuid); // new String(distribution);
        Assert.assertEquals(expectedArmenianDistributionLine, distributionStr);

        //name
        //... family => test nom. status 'conserved'
        String nameStr = getLine(nameResult, familyNameUuid);
        Assert.assertEquals(expectedFamilyNameLine, nameStr);
        //... subspecies
        nameStr = getLine(nameResult, subspeciesNameUuid);
        Assert.assertEquals(expectedSubspeciesNameLine, nameStr);

        //media
//        String mediaString = new String(data.get(ColDpExportTable.MEDIA.getTableName()));
//        System.out.println(mediaString);
        String mediaStr = getLine(mediaResult, subspeciesTaxonUuid);
        Assert.assertEquals(expectedMediaLine, mediaStr);

        //name relation
        //TODO is speciesNameUuid specific enough?
        String nameRelStr = getLine(nameRelationResult, speciesNameUuid);
        Assert.assertEquals(expectedNameRelationLine1, nameRelStr);

        //type material
        print(typeMaterialResult);
        String typeMaterialStr = getLine(typeMaterialResult, specimenUuid);
        Assert.assertEquals(expectedTypeMaterialLine, typeMaterialStr);

        //vernacular name
        String vernacularNameStr = getLine(vernacularNameResult, speciesTaxonUuid);
        Assert.assertEquals(expectedVernacularNameLine, vernacularNameStr);

        //TODO   Synonym,
        //     TaxonConceptRelation, SpeciesInteraction, SpeciesEstimate, Treatment
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFullData(){

        //config+invoke
        ColDpExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);
        Assert.assertTrue(result.getExportType().equals(ExportType.COLDP)); //test export type

        //test counts
        List<String> taxonResult = getStringList(data, ColDpExportTable.TAXON);
        Assert.assertEquals("There should be 4 taxa", 4, taxonResult.size() - COUNT_HEADER);

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME);
        Assert.assertEquals("There should be 6 names", 6, nameResult.size() - COUNT_HEADER);

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        Assert.assertEquals("There should be 9 references", 9, referenceResult.size() - COUNT_HEADER);

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        Assert.assertEquals("There should be two synonyms", 2, synonymResult.size() - COUNT_HEADER);

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