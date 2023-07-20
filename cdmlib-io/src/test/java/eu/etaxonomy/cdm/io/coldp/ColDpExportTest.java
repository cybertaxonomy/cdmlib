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
import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.io.common.ExportResult;
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

    //FIXME basionymID, nom. status
    private String basionymID = NONE;
    private String expectedSubspeciesNameLine = uuid(subspeciesNameUuid) + NONE2 + basionymID + "\"Genus species subsp. subspec\",\"Mill.\",\"subspecies\","
            + NONE + "\"Genus\"," + NONE + "\"species\",\"subspec\"," + NONE + "\"ICN\"," + NONE + uuid(subspeciesNomRefUuid) + "\"1804\",\"22\"," + NONE2 + NONE_END;


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
    public void testSubTree(){

        //config+invoke
        ColDpExportConfigurator config = ColDpExportConfigurator.NewInstance();
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(node4Uuid));
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();

        //test exceptions
        testExceptionsErrorsWarnings(result);

        //counts and result lists
        List<String> taxonResult = getStringList(data, ColDpExportTable.TAXON);
        Assert.assertEquals("There should be 1 taxon", 1, taxonResult.size()-1);// 1 header line

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        Assert.assertEquals("There should be no synonym", 0, synonymResult.size()-1);// 1 header line

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        Assert.assertEquals("There should be 2 references (1 nomenclatural references and 1 sec reference)", 2, referenceResult.size()-1);// 1 header line

        List<String> distributionResult = getStringList(data, ColDpExportTable.DISTRIBUTION);
        Assert.assertEquals("There should be 1 distribution", 1, distributionResult.size()-1);// 1 header line

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME);
        Assert.assertEquals("There should be 1 name", 1, nameResult.size()-1);// 1 header line

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
        Assert.assertEquals(expectedSubspeciesNameLine, nameStr);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFullTreeWithUnpublished(){

        //config+invoke
        ColDpExportConfigurator config = ColDpExportConfigurator.NewInstance();
        config.setTarget(TARGET.EXPORT_DATA);
        config.getTaxonNodeFilter().setIncludeUnpublished(true);
        ExportResult result = defaultExport.invoke(config);
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();

        //test exceptions
        testExceptionsErrorsWarnings(result);

        //test counts
        List<String> taxonResult = getStringList(data, ColDpExportTable.TAXON);
        Assert.assertEquals("There should be 5 taxa", 5, taxonResult.size()-1);// 1 header line

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        Assert.assertEquals("There should be 1 synonym", 1, synonymResult.size()-1);// 1 header line

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME);
        Assert.assertEquals("There should be 6 names", 6, nameResult.size()-1);// 1 header line

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        Assert.assertEquals("There should be 7 references (6 nomenclatural references and 1 sec reference)", 7, referenceResult.size()-1);// 1 header line

        List<String> distributionResult = getStringList(data, ColDpExportTable.DISTRIBUTION);
        Assert.assertEquals("There should be 1 distribution", 1, distributionResult.size()-1);// 1 header line

        List<String> vernacularNameResult = getStringList(data, ColDpExportTable.VERNACULAR_NAME);
        Assert.assertEquals("There should be 0 vernacular names", 0, vernacularNameResult.size()-1);// 1 header line


        //TODO name, relName, treatment, typeMaterial, speciesInteraction, taxRel, distribution,
        //     vernacularName, media, speciesEstimate ...

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
        String nameStr = getLine(nameResult, subspeciesNameUuid);
        String scientificNameString = new String(data.get(ColDpExportTable.NAME.getTableName()));
        System.out.println(scientificNameString);
        Assert.assertEquals(expectedSubspeciesNameLine, nameStr);

        //TODO NameRelation, TypeMaterial, Synonym, SpeciesInteraction, TaxonConceptRelation
        //     SpeciesEstimate, Media, Treatment, VernacularName
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFullData(){

        //config+invoke
        ColDpExportConfigurator config = ColDpExportConfigurator.NewInstance();
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();

        //test exceptions
        testExceptionsErrorsWarnings(result);

        //test counts
        List<String> taxonResult = getStringList(data, ColDpExportTable.TAXON);
        Assert.assertEquals("There should be 4 taxa", 4, taxonResult.size()-1);// 1 header line

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME);
        Assert.assertEquals("There should be 4 names", 4, nameResult.size()-1);// 1 header line

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        Assert.assertEquals("There should be 5 references", 5, referenceResult.size()-1);// 1 header line

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        Assert.assertEquals("There should be no synonym", 0, synonymResult.size()-1);// 1 header line
    }


    @Override
    protected ColDpExportConfigurator newConfigurator() {
        ColDpExportConfigurator config = ColDpExportConfigurator.NewInstance();
        return config;
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}