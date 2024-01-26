/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
 * @date 26.01.2024
 */
@Ignore
public class WfoClassificationExportTest
        extends TaxonTreeExportTestBase<WfoExportConfigurator,WfoExportState> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    //still missing: alternativeID, sourceID, sequenceIndex, branchLength, scrutinizerXXX, referenceID, environment, extinct, ...
    //FIXME link
    private String expectedSubspeciesTaxonLine = uuid(subspeciesTaxonUuid)+ NONE2 + uuid(speciesTaxonUuid) + NONE2 +
            uuid(subspeciesNameUuid) + NONE + uuid(ref1UUID)+ NONE3 + FALSE + NONE + BOOL_NULL + NONE20 + NONE_END;

    //  TODO check other NONE entries:
    private String expectedNomRefLine = uuid(subspeciesNomRefUuid) + NONE2 +
            "\"Mill. 1804: The book of botany 3\",\"book\",\"Mill.\"," + NONE4 + "\"1804\"," + NONE3 + "\"3\"," + NONE10 + NONE_END;

    private String expectedFamilyNameLine = uuid(familyNameUuid) + NONE3 + "\"Family\",\"L.\",\"family\",\"Family\"," +
            NONE4 + NONE + "\"L.\"," + NONE + "\"1752\"," + NONE3 + "\"ICN\",\"conserved\"," +
            uuid(familyNomRefUuid) + "\"1752\",\"22\"," + NONE2 + NONE_END;

    //FIXME basionymID, nom. status
    private String basionymID = NONE;
    private String expectedSubspeciesNameLine = uuid(subspeciesNameUuid) + NONE2 + basionymID + "\"Genus species subsp. subspec\",\"Mill.\",\"subspecies\","
            + NONE + "\"Genus\"," + NONE + "\"species\",\"subspec\"," + NONE + "\"Mill.\"," + NONE + "\"1804\"," + NONE3 +
            "\"ICN\"," + VALID + uuid(subspeciesNomRefUuid) + "\"1804\",\"22\"," + NONE2 + NONE_END;
    private String expectedSubspeciesNameLineWithFullName = uuid(subspeciesNameUuid) + NONE2 + basionymID + "\"Genus species subsp. subspec\","
            + "\"Genus species subsp. subspec Mill.\",\"Mill.\",\"subspecies\","
            + NONE + "\"Genus\"," + NONE + "\"species\",\"subspec\"," + NONE + "\"Mill.\"," + NONE + "\"1804\"," + NONE3 +
            "\"ICN\"," + VALID + uuid(subspeciesNomRefUuid) + "\"1804\",\"22\"," + NONE2 + NONE_END;


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
        WfoExportConfigurator config = newConfigurator();
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(node4Uuid));
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);

        //counts and result lists
        List<String> taxonResult = getStringList(data, WfoExportTable.CLASSIFICATION);
        Assert.assertEquals("There should be 1 taxon", 1, taxonResult.size() - COUNT_HEADER);

        List<String> synonymResult = getStringList(data, WfoExportTable.CLASSIFICATION);
        Assert.assertEquals("There should be no synonym", 0, synonymResult.size() - COUNT_HEADER);

        List<String> referenceResult = getStringList(data, WfoExportTable.REFERENCE);
        Assert.assertEquals("There should be 2 references (1 nomenclatural references and 1 sec reference)", 2, referenceResult.size() - COUNT_HEADER);

        //taxon
        //... species
        String taxonStr = getTableString(data, WfoExportTable.CLASSIFICATION);
        String notExpected = speciesTaxonUuid.toString();
        Assert.assertFalse("Result must not contain root of subtree taxon (species taxon)", taxonStr.startsWith(notExpected));
        //... subspecies
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        Assert.assertEquals(expectedSubspeciesTaxonLine, subspeciesLine);

        //reference
        String nomRefLine = getLine(referenceResult, subspeciesNomRefUuid);
        Assert.assertEquals(expectedNomRefLine, nomRefLine);

//        //name
//        //TODO duplicated check withUnpublished
//        String nameStr = getLine(nameResult, subspeciesNameUuid);
//        Assert.assertEquals(expectedSubspeciesNameLineWithFullName, nameStr);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })// data itself is created in base class
    public void testFullTreeWithUnpublished(){

        //config+invoke
        WfoExportConfigurator config = newConfigurator();
        config.getTaxonNodeFilter().setIncludeUnpublished(true);
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);

        //test counts
        List<String> taxonResult = getStringList(data, WfoExportTable.CLASSIFICATION);
        Assert.assertEquals("There should be 5 taxa", 5, taxonResult.size() - COUNT_HEADER);

        List<String> synonymResult = getStringList(data, WfoExportTable.CLASSIFICATION);
        Assert.assertEquals("There should be 2 synonym", 2, synonymResult.size() - COUNT_HEADER);

        List<String> nameResult = getStringList(data, WfoExportTable.CLASSIFICATION);
        Assert.assertEquals("There should be 7 names", 7, nameResult.size() - COUNT_HEADER);

        List<String> referenceResult = getStringList(data, WfoExportTable.REFERENCE);
        Assert.assertEquals("There should be 8 references (7 nomenclatural references and 1 sec reference)", 8, referenceResult.size() - COUNT_HEADER);

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

        //name
        //... family => test nom. status 'conserved'
        String nameStr = getLine(nameResult, familyNameUuid);
        Assert.assertEquals(expectedFamilyNameLine, nameStr);
        //... subspecies
        nameStr = getLine(nameResult, subspeciesNameUuid);
        Assert.assertEquals(expectedSubspeciesNameLine, nameStr);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFullData(){

        //config+invoke
        WfoExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);
        Assert.assertTrue(result.getExportType().equals(ExportType.COLDP)); //test export type

        //test counts
        List<String> taxonResult = getStringList(data, WfoExportTable.CLASSIFICATION);
        Assert.assertEquals("There should be 4 taxa", 4, taxonResult.size() - COUNT_HEADER);

        List<String> nameResult = getStringList(data, WfoExportTable.CLASSIFICATION);
        for ( Entry<String, byte[]> b : data.entrySet()) {
            System.out.println("Key:" + b.getKey() + "; Value: " + b.getValue());
        }
        System.out.println(data);
        Assert.assertEquals("There should be 5 names", 5, nameResult.size() - COUNT_HEADER);

        List<String> referenceResult = getStringList(data, WfoExportTable.REFERENCE);
        Assert.assertEquals("There should be 6 references", 6, referenceResult.size() - COUNT_HEADER);

        List<String> synonymResult = getStringList(data, WfoExportTable.CLASSIFICATION);
        Assert.assertEquals("There should be no synonym", 1, synonymResult.size() - COUNT_HEADER);

        //tbc
    }

    @Override
    protected WfoExportConfigurator newConfigurator() {
        WfoExportConfigurator config = WfoExportConfigurator.NewInstance();
        config.setTarget(TARGET.EXPORT_DATA);
        return config;
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}