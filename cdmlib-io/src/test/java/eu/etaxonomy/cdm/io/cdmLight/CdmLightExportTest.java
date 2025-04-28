/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;

import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.coldp.ColDpExportTable;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportTestBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author k.luther
 * @since 17.01.2018
 */
public class CdmLightExportTest
        extends TaxonTreeExportTestBase<CdmLightExportConfigurator,CdmLightExportState> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Before
    public void setUp()  {
        createFullTestDataSet();
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testSubTree() {

        //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(node4Uuid));
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);

        //taxon table
        byte[] taxonByte = data.get(CdmLightExportTable.TAXON.getTableName());
        Assert.assertNotNull("Taxon table must not be null", taxonByte);
        String taxonStr = new String(taxonByte);
        String notExpected = speciesTaxonUuid.toString();
        Assert.assertFalse("Result must not contain root taxon", taxonStr.startsWith(notExpected));
        String expected = uuid(subspeciesTaxonUuid) + uuid(classificationUuid) + "\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\"," + uuid(speciesTaxonUuid) + "\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
        Assert.assertTrue(taxonStr.contains(expected));

        //synonyms
        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        int countDummyLine = 0;
        int countHeader = 0;
        int count = 0;
        for (String line : synonymResult) {
            if (line.startsWith("\"DUMMY")){   // || line.startsWith("\"Synonym_ID")
                countDummyLine++;
            }else if (line.startsWith("\"Synonym_ID")) {
                countHeader++;
            }else {
                count++;
            }
        }
        Assert.assertEquals("There should be 1 synonym header", 1, countHeader);
        Assert.assertEquals("There should be 2 dummy entries", 2, countDummyLine);
        Assert.assertEquals("There should be 0 real synomyms", 0, count);

        //reference table
        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        String subspeciesNomRefLine = getLine(referenceResult, subspeciesNomRefUuid);
        expected = uuid(subspeciesNomRefUuid) +"\"Mill. (1804)\",\"\",\"The book of botany\",\"1804\"," + NONE + "\"0\"," + NONE4 + NONE4 + "\"3\",\"1804\",\"Mill.\"";
        Assert.assertTrue(subspeciesNomRefLine.contains(expected));

        //geographic fact
        byte[] geographicAreaFact = data.get(CdmLightExportTable.GEOGRAPHIC_AREA_FACT.getTableName());
        String geographicAreaFactString = new String(geographicAreaFact);
        Assert.assertNotNull("Geographical fact table must not be null", geographicAreaFact);
        expected ="\"674e9e27-9102-4166-8626-8cb871a9a89b\"," + uuid(subspeciesTaxonUuid) + "\"Armenia\",\"present\"";
        Assert.assertTrue(geographicAreaFactString.contains(expected));

        //nom. author
        byte[] nomenclaturalAuthor = data.get(CdmLightExportTable.NOMENCLATURAL_AUTHOR.getTableName());
        String nomenclaturalAuthorString = new String(nomenclaturalAuthor);
        Assert.assertNotNull("Nomenclatural Author table must not be null", nomenclaturalAuthor);
        expected ="\"Mill.\",\"Mill.\",\"\",\"\",\"\",\"\"";
        Assert.assertTrue(nomenclaturalAuthorString.contains(expected));

        //scientific name
        byte[] scientificName = data.get(CdmLightExportTable.SCIENTIFIC_NAME.getTableName());
        String scientificNameString = new String(scientificName);
        Assert.assertNotNull("Scientific Name table must not be null", scientificName);
        expected ="\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"\",\"Subspecies\",\"43\",\"Genus species subsp. subspec Mill.\",\"Genus species subsp. subspec\",\"Genus\",\"\",\"\",\"species\",\"subsp.\",\"subspec\",\"\",\"\",\"\",";
        Assert.assertTrue(scientificNameString.contains(expected));
        expected = "\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\"";
        Assert.assertTrue(scientificNameString.contains(expected));

        expected ="\"Book\",\"The book of botany\",\"The book of botany\",\"Mill.\",\"Mill.\",\"3:22\",\"3\",\"22\",\"1804\",\"1804\",\"\",\"\",\"\",\"\"";
        Assert.assertTrue(scientificNameString.contains(expected));

        //homotypic group
        List<String> hgList = getStringList(data, CdmLightExportTable.HOMOTYPIC_GROUP);
        Assert.assertNotNull("HomotypicGroup table must not be null", hgList);
        Assert.assertTrue("HomotypicGroup table must not be empty or only have header line", hgList.size() > 1);
        String line = getLine(hgList, subspeciesNameHgUuid);
        expected ="\"c60c0ce1-0fa0-468a-9908-8e9afed05714\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\",\"\",\"\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804 sec. My sec ref\",\"\",\"\",\"0\",\"\"";

        Assert.assertEquals(expected, line);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFullTreeWithUnpublished(){

        //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        config.getTaxonNodeFilter().setIncludeUnpublished(true);
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);

        //test counts
        List<String> taxonResult = getStringList(data, CdmLightExportTable.TAXON);
        Assert.assertEquals("There should be 5 taxa", 5, taxonResult.size() - COUNT_HEADER);

        List<String> referenceResult = getStringList(data, CdmLightExportTable.REFERENCE);
        Assert.assertEquals("There should be 11 references (10 nomenclatural references including an in-reference"
                + " and 3 sec reference)", 13, referenceResult.size() - COUNT_HEADER);

        List<String> synonymResult = getStringList(data, CdmLightExportTable.SYNONYM);
        Assert.assertEquals("There should be 3 synonym", 3, synonymResult.size() - COUNT_HEADER);

        //test single data
        Assert.assertEquals("Result must not contain root taxon",
                0, taxonResult.stream().filter(line->line.contains(rootNodeUuid.toString())).count());

        //subspecies
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        String expected = uuid(subspeciesTaxonUuid) + uuid(classificationUuid) + "\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\","+uuid(speciesTaxonUuid)+"\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
        Assert.assertEquals(expected, subspeciesLine.substring(0, expected.length()));

        String expectedSecNameUsedInSource = "\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i>\",\"Mill.\",";
        Assert.assertTrue(subspeciesLine.contains(expectedSecNameUsedInSource));

        //unpublished/excluded/note
        String unpublishedLine = getLine(taxonResult, subspeciesUnpublishedTaxonUuid);
        String expectedExcluded = "\"\",\"0\",\"0\",\"0\",\"1\",\"1\",\"0\",\"0\",\"0\",\"0\",\"0\",\""+TaxonNodeStatus.EXCLUDED.getLabel()+"\",\"My status note\",\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref: 27\",\"0\"";
        Assert.assertTrue(unpublishedLine.contains(expectedExcluded));

        //references
        String nomRefLine = getLine(referenceResult, UUID.fromString("b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f"));
        expected ="\"b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f\",\"Mill. (1804)\",\"\",\"The book of botany\",\"1804\",\"\",\"0\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"3\",\"1804\",\"Mill.\"";
        Assert.assertEquals(expected, nomRefLine.substring(0, expected.length()));
        Assert.assertTrue(nomRefLine.contains(",\"BK\","));
        String expectedAfterRefType = nomRefLine.substring(nomRefLine.indexOf(",\"BK\","));
        //#10488
        Assert.assertEquals("Test unique year 1804b ", ",\"BK\",\"\",\"Mill. 1804: The book of botany 3\",\"Mill. (1804b)\",\"Mill. 1804b: The book of botany 3\"", expectedAfterRefType);

        //geo area
        byte[] geographicAreaFact = data.get(CdmLightExportTable.GEOGRAPHIC_AREA_FACT.getTableName());
        String geographicAreaFactString = new String(geographicAreaFact);
        Assert.assertNotNull("Geographical fact table must not be null", geographicAreaFact);
        expected ="\"674e9e27-9102-4166-8626-8cb871a9a89b\"," + uuid(subspeciesTaxonUuid) + "\"Armenia\",\"present\"";
        Assert.assertTrue(geographicAreaFactString.contains(expected));

        //nom author
        byte[] nomenclaturalAuthor = data.get(CdmLightExportTable.NOMENCLATURAL_AUTHOR.getTableName());
        String nomenclaturalAuthorString = new String(nomenclaturalAuthor);
        Assert.assertNotNull("Nomenclatural Author table must not be null", nomenclaturalAuthor);
        expected ="\"Mill.\",\"Mill.\",\"\",\"\",\"\",\"\"";
        Assert.assertTrue(nomenclaturalAuthorString.contains(expected));

        //names
        List<String> nameList = getStringList(data, CdmLightExportTable.SCIENTIFIC_NAME);
        Assert.assertNotNull("Scientific Name table must not be null", nameList);
        String line = getLine(nameList, subspeciesNameUuid);
        expected ="\""+subspeciesNameUuid+"\",\"\",\"Subspecies\",\"43\",\"Genus species subsp. subspec Mill.\",\"Genus species subsp. subspec\",\"Genus\",\"\",\"\",\"species\",\"subsp.\",\"subspec\",\"\",\"\",\"\",";
        Assert.assertTrue(line.contains(expected));
        expected ="\"Book\",\"The book of botany\",\"The book of botany\",\"Mill.\",\"Mill.\",\"3:22\",\"3\",\"22\",\"1804\",\"1804\",\"\",\"\",\"\",\"\"";
        Assert.assertTrue(line.contains(expected));
        Assert.assertNotNull("The earlier homonym should be included", getLine(nameList, earlierHomonymUuid));
        //#10562
        Assert.assertNull("The basionym of the earlier homonym should not be included", getLine(nameList, earlierHomonymBasionymUuid));

        //homotypic group
        List<String> hgList = getStringList(data, CdmLightExportTable.HOMOTYPIC_GROUP);
        Assert.assertNotNull("HomotypicGroup table must not be null", hgList);
        Assert.assertTrue("HomotypicGroup table must not be empty or only have header line", hgList.size() > 1);
        line = getLine(hgList, subspeciesNameHgUuid);
        Assert.assertNotNull("Subspecies homotypic group record does not exist for predefined uuid", line);
        expected ="\"c60c0ce1-0fa0-468a-9908-8e9afed05714\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\",\"\",\"\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804 sec. My sec ref\",\"\",\"\",\"0\",\"\"";

        Assert.assertEquals(expected, line);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFullDataPublished(){

        //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);
        Assert.assertTrue(result.getExportType().equals(ExportType.CDM_LIGHT)); //test export type

        //test ...
        //taxon
        List<String> taxonResult = getStringList(data, CdmLightExportTable.TAXON);
        Assert.assertEquals("There should be 4 taxa", 4, taxonResult.size() - COUNT_HEADER);

        //reference
        List<String> referenceResult = getStringList(data, CdmLightExportTable.REFERENCE);
        Assert.assertEquals("There should be 11 references (8 nomenclatural references and 3 sec reference)", 11, referenceResult.size() - COUNT_HEADER);

        //Test for unique citation
        String line = getLine(referenceResult, ref2UUID);
        String expected = "Author (1980)";
        Assert.assertTrue(line.contains(expected));
        line = getLine(referenceResult, ref3UUID);
        expected = "Author (1980a)";
        Assert.assertTrue(line.contains(expected));


        //synonyms
        List<String> synonymResult = getStringList(data, CdmLightExportTable.SYNONYM);
        Assert.assertEquals("There should be 2 synonyms", 2, synonymResult.size() - COUNT_HEADER);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testUniqueCitation(){

        //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);
        Assert.assertTrue(result.getExportType().equals(ExportType.CDM_LIGHT)); //test export type

        //test ...
        //taxon
        List<String> taxonResult = getStringList(data, CdmLightExportTable.TAXON);
        Assert.assertEquals("There should be 4 taxa", 4, taxonResult.size() - COUNT_HEADER);
        String line = getLine(taxonResult, speciesTaxonUuid);
        Assert.assertTrue(line.contains("Author (1980a)"));
        line = getLine(taxonResult, genusTaxonUuid);
        Assert.assertTrue(line.contains("Author (1980)"));

        //reference
        List<String> referenceResult = getStringList(data, CdmLightExportTable.REFERENCE);
        Assert.assertEquals("There should be 16 references (9 nomenclatural references and 4 sec reference, 2 fact sources, 1 in-reference)", 16, referenceResult.size() - COUNT_HEADER);

        //Test for unique citation
        line = getLine(referenceResult, ref2UUID);
        String expected = "Author (1980)";
        Assert.assertTrue(line.contains(expected));
        line = getLine(referenceResult, ref3UUID);
        expected = "Author (1980a)";
        Assert.assertTrue(line.contains(expected));


        //synonyms
        List<String> synonymResult = getStringList(data, CdmLightExportTable.SYNONYM);
        Assert.assertEquals("There should be 3 synonyms (2 synonyms, 1 MAN)", 3, synonymResult.size() - COUNT_HEADER);
        line = getLine(synonymResult, basionymSynonymUuid);
        //unique citation with detail
        Assert.assertTrue(line.contains("Author (1980a: 67)"));

        line = getLine(synonymResult, misappliedTaxonUuid);
        //unique citation for MAN sec
        Assert.assertTrue(line.contains("Author (1980b)"));

        List<String> factsResult = getStringList(data, CdmLightExportTable.FACT_SOURCES);
        line = getLine(factsResult, "81c7b7db-e12b-45fe-96ed-dab940043232");//UUID of Common name
        line.contains("c68e0a88-7b96-465d-b4ce-d14b13daf94f");//UUID of fact citation
        line = getLine(referenceResult, "c68e0a88-7b96-465d-b4ce-d14b13daf94f"); // line of fact citation in references
        Assert.assertTrue(line.contains("Testauthor (1981)"));

        line = getLine(factsResult, "674e9e27-9102-4166-8626-8cb871a9a89b");//UUID of distribution
        line.contains("e2edf07d-552a-46cd-85da-86a491847aeb");//UUID of fact citation
        line = getLine(referenceResult, "e2edf07d-552a-46cd-85da-86a491847aeb"); // line of fact citation in references
        Assert.assertTrue(line.contains("Testauthor (1981a)"));
    }


    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testTypeDesignationOutput(){


      //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);
        Assert.assertTrue(result.getExportType().equals(ExportType.CDM_LIGHT)); //test export type
        List<String> hgList = getStringList(data, CdmLightExportTable.HOMOTYPIC_GROUP);
        Assert.assertNotNull("HomotypicGroup table must not be null", hgList);
        Assert.assertTrue("HomotypicGroup table must not be empty or only have header line", hgList.size() > 1);
        String line = getLine(hgList, speciesNameHgUuid);

        String expectedSpecimen = "Holotype: Armenia, Somewhere in the forest, 55°33'22\"\"N, 15°13'12\"\"W (WGS84), Collector team CT222 (B A555).";

        Assert.assertTrue(line.contains(expectedSpecimen));
        String expectedText = "Textual typedesignation test.";
        Assert.assertTrue(line.contains(expectedText));
        //textual type designation should be contained only once
        Assert.assertTrue(line.lastIndexOf(expectedText)== line.indexOf(expectedText));
        //TODO: NameTypedesignation


    }

    @Override
    protected CdmLightExportConfigurator newConfigurator() {
        return CdmLightExportConfigurator.NewInstance();
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}