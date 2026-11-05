/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.IoResultBase.IoInfo;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.coldp.ColDpExportTable;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportTestBase;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;

/**
 * @author k.luther
 * @since 17.01.2018
 */
public class CdmLightExportTest
        extends TaxonTreeExportTestBase<CdmLightExportConfigurator,CdmLightExportState> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @BeforeEach
    public void setUp()  {
        createFullTestDataSet();
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testSubTree() {

        //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(node4Uuid));
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);

        //taxon table
        byte[] taxonByte = data.get(CdmLightExportTable.TAXON.getTableName());
        assertNotNull(taxonByte, "Taxon table must not be null");
        String taxonStr = new String(taxonByte);
        String notExpected = speciesTaxonUuid.toString();
        assertFalse(taxonStr.startsWith(notExpected), "Result must not contain root taxon");
        String expected = uuid(subspeciesTaxonUuid) + uuid(classificationUuid) + "\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\"," + uuid(speciesTaxonUuid) + "\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
        assertTrue(taxonStr.contains(expected));

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
        assertEquals(1, countHeader, "There should be 1 synonym header");
        assertEquals(2, countDummyLine, "There should be 2 dummy entries");
        assertEquals(0, count, "There should be 0 real synomyms");

        //reference table
        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        String subspeciesNomRefLine = getLine(referenceResult, subspeciesNomRefUuid);
        expected = uuid(subspeciesNomRefUuid) +"\"Mill. (1804)\",\"\",\"The book of botany\",\"1804\"," + NONE + "\"0\"," + NONE4 + NONE4 + "\"3\",\"1804\",\"Mill.\"";
        assertTrue(subspeciesNomRefLine.contains(expected));

        //geographic fact
        byte[] geographicAreaFact = data.get(CdmLightExportTable.GEOGRAPHIC_AREA_FACT.getTableName());
        String geographicAreaFactString = new String(geographicAreaFact);
        assertNotNull(geographicAreaFact, "Geographical fact table must not be null");
        expected ="\"674e9e27-9102-4166-8626-8cb871a9a89b\"," + uuid(subspeciesTaxonUuid) + "\"Armenia\",\"present\"";
        assertTrue(geographicAreaFactString.contains(expected));

        //nom. author
        byte[] nomenclaturalAuthor = data.get(CdmLightExportTable.NOMENCLATURAL_AUTHOR.getTableName());
        String nomenclaturalAuthorString = new String(nomenclaturalAuthor);
        assertNotNull(nomenclaturalAuthor, "Nomenclatural Author table must not be null");
        expected ="\"Mill.\",\"Mill.\",\"\",\"\",\"\",\"\"";
        assertTrue(nomenclaturalAuthorString.contains(expected));

        //scientific name
        byte[] scientificName = data.get(CdmLightExportTable.SCIENTIFIC_NAME.getTableName());
        String scientificNameString = new String(scientificName);
        assertNotNull(scientificName, "Scientific Name table must not be null");
        expected ="\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"\",\"Subspecies\",\"43\",\"Genus species subsp. subspec Mill.\",\"Genus species subsp. subspec\",\"Genus\",\"\",\"\",\"species\",\"subsp.\",\"subspec\",\"\",\"\",\"\",";
        assertTrue(scientificNameString.contains(expected));
        expected = "\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\"";
        assertTrue(scientificNameString.contains(expected));

        expected ="\"Book\",\"The book of botany\",\"The book of botany\",\"Mill.\",\"Mill.\",\"3:22\",\"3\",\"22\",\"1804\",\"1804\",\"\",\"\",\"\",\"\"";
        assertTrue(scientificNameString.contains(expected));

        //homotypic group
        List<String> hgList = getStringList(data, CdmLightExportTable.HOMOTYPIC_GROUP);
        assertNotNull(hgList, "HomotypicGroup table must not be null");
        assertTrue(hgList.size() > 1, "HomotypicGroup table must not be empty or only have header line");
        String line = getLine(hgList, subspeciesNameHgUuid);
        expected ="\"c60c0ce1-0fa0-468a-9908-8e9afed05714\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\",\"\",\"\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804 sec. My sec ref\",\"\",\"\",\"0\",\"\"";

        assertEquals(expected, line);
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testFullTreeWithUnpublished(){

        //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        config.getTaxonNodeFilter().setIncludeUnpublished(true);
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);

        //test counts
        List<String> taxonResult = getStringList(data, CdmLightExportTable.TAXON);
        assertEquals(5, taxonResult.size() - COUNT_HEADER, "There should be 5 taxa");

        List<String> referenceResult = getStringList(data, CdmLightExportTable.REFERENCE);
        assertEquals(19, referenceResult.size() - COUNT_HEADER, "There should be 19 references");

        List<String> synonymResult = getStringList(data, CdmLightExportTable.SYNONYM);
        assertEquals(5, synonymResult.size() - COUNT_HEADER, "There should be 5 synonym");

        //test single data
        assertEquals(0, taxonResult.stream().filter(line->line.contains(rootNodeUuid.toString())).count(), "Result must not contain root taxon");

        //subspecies
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        String expected = uuid(subspeciesTaxonUuid) + uuid(classificationUuid) + "\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\","+uuid(speciesTaxonUuid)+"\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
        assertEquals(expected, subspeciesLine.substring(0, expected.length()));

        String expectedSecNameUsedInSource = "\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i>\",\"Mill.\",";
        assertTrue(subspeciesLine.contains(expectedSecNameUsedInSource));

        //unpublished/excluded/note
        String unpublishedLine = getLine(taxonResult, subspeciesUnpublishedTaxonUuid);
        String expectedExcluded = "\"\",\"0\",\"0\",\"0\",\"1\",\"1\",\"0\",\"0\",\"0\",\"0\",\"0\",\""+TaxonNodeStatus.EXCLUDED.getLabel()+"\",\"My status note\",\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref: 27\",\"0\"";
        assertTrue(unpublishedLine.contains(expectedExcluded));

        //references
        String nomRefLine = getLine(referenceResult, UUID.fromString("b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f"));
        expected ="\"b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f\",\"Mill. (1804)\",\"\",\"The book of botany\",\"1804\",\"\",\"0\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"3\",\"1804\",\"Mill.\"";
        assertEquals(expected, nomRefLine.substring(0, expected.length()));
        assertTrue(nomRefLine.contains(",\"BK\","));
        String expectedAfterRefType = nomRefLine.substring(nomRefLine.indexOf(",\"BK\","));
        //#10488
        assertEquals(",\"BK\",\"\",\"Mill. 1804: The book of botany 3\",\"Mill. (1804b)\",\"Mill. 1804b: The book of botany 3\"", expectedAfterRefType, "Test unique year 1804b ");

        //geo area
        byte[] geographicAreaFact = data.get(CdmLightExportTable.GEOGRAPHIC_AREA_FACT.getTableName());
        String geographicAreaFactString = new String(geographicAreaFact);
        assertNotNull(geographicAreaFact, "Geographical fact table must not be null");
        expected ="\"674e9e27-9102-4166-8626-8cb871a9a89b\"," + uuid(subspeciesTaxonUuid) + "\"Armenia\",\"present\"";
        assertTrue(geographicAreaFactString.contains(expected));

        //nom author
        byte[] nomenclaturalAuthor = data.get(CdmLightExportTable.NOMENCLATURAL_AUTHOR.getTableName());
        String nomenclaturalAuthorString = new String(nomenclaturalAuthor);
        assertNotNull(nomenclaturalAuthor, "Nomenclatural Author table must not be null");
        expected ="\"Mill.\",\"Mill.\",\"\",\"\",\"\",\"\"";
        assertTrue(nomenclaturalAuthorString.contains(expected));

        //names
        List<String> nameList = getStringList(data, CdmLightExportTable.SCIENTIFIC_NAME);
        assertNotNull(nameList, "Scientific Name table must not be null");
        String line = getLine(nameList, subspeciesNameUuid);
        expected ="\""+subspeciesNameUuid+"\",\"\",\"Subspecies\",\"43\",\"Genus species subsp. subspec Mill.\",\"Genus species subsp. subspec\",\"Genus\",\"\",\"\",\"species\",\"subsp.\",\"subspec\",\"\",\"\",\"\",";
        assertTrue(line.contains(expected));
        expected ="\"Book\",\"The book of botany\",\"The book of botany\",\"Mill.\",\"Mill.\",\"3:22\",\"3\",\"22\",\"1804\",\"1804\",\"\",\"\",\"\",\"\"";
        assertTrue(line.contains(expected));
        assertNotNull(getLine(nameList, earlierHomonymUuid), "The earlier homonym should be included");
        //#10562
        assertNull(getLine(nameList, earlierHomonymBasionymUuid), "The basionym of the earlier homonym should not be included");

        //homotypic group
        List<String> hgList = getStringList(data, CdmLightExportTable.HOMOTYPIC_GROUP);
        assertNotNull(hgList, "HomotypicGroup table must not be null");
        assertTrue(hgList.size() > 1, "HomotypicGroup table must not be empty or only have header line");
        line = getLine(hgList, subspeciesNameHgUuid);
        assertNotNull(line, "Subspecies homotypic group record does not exist for predefined uuid");
        expected ="\"c60c0ce1-0fa0-468a-9908-8e9afed05714\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\",\"\",\"\",\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804 sec. My sec ref\",\"\",\"\",\"0\",\"\"";

        assertEquals(expected, line);
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testFullDataPublished(){

        //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);
        assertTrue(result.getExportType().equals(ExportType.CDM_LIGHT)); //test export type

        //test ...
        //taxon
        List<String> taxonResult = getStringList(data, CdmLightExportTable.TAXON);
        assertEquals(4, taxonResult.size() - COUNT_HEADER, "There should be 4 taxa");

        //reference
        List<String> referenceResult = getStringList(data, CdmLightExportTable.REFERENCE);
        assertEquals(17, referenceResult.size() - COUNT_HEADER, "There should be 17 references (11 nomenclatural references including 1 in-reference and 4 sec reference, 2 fact sources)");

        //Test for unique citation
        String line = getLine(referenceResult, ref2UUID);
        String expected = "Author (1980)";
        assertTrue(line.contains(expected));
        line = getLine(referenceResult, ref3UUID);
        expected = "Author (1980a)";
        assertTrue(line.contains(expected));

        //synonyms
        List<String> synonymResult = getStringList(data, CdmLightExportTable.SYNONYM);
        assertEquals(4, synonymResult.size() - COUNT_HEADER, "There should be 4 synonyms (three synonyms, 1 MAN)");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testUniqueCitation(){

        //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);
        assertTrue(result.getExportType().equals(ExportType.CDM_LIGHT)); //test export type

        //test ...
        //taxon
        List<String> taxonResult = getStringList(data, CdmLightExportTable.TAXON);
        assertEquals(4, taxonResult.size() - COUNT_HEADER, "There should be 4 taxa");
        String line = getLine(taxonResult, speciesTaxonUuid);
        assertTrue(line.contains("Author (1980a)"));
        line = getLine(taxonResult, genusTaxonUuid);
        assertTrue(line.contains("Author (1980)"));

        //reference
        List<String> referenceResult = getStringList(data, CdmLightExportTable.REFERENCE);
        assertEquals(17, referenceResult.size() - COUNT_HEADER, "There should be 17 references (10 nomenclatural references and 4 sec reference, 2 fact sources, 1 in-reference)");

        //Test for unique citation
        line = getLine(referenceResult, ref2UUID);
        String expected = "Author (1980)";
        assertTrue(line.contains(expected));
        line = getLine(referenceResult, ref3UUID);
        expected = "Author (1980a)";
        assertTrue(line.contains(expected));

        //synonyms
        List<String> synonymResult = getStringList(data, CdmLightExportTable.SYNONYM);
        assertEquals(4, synonymResult.size() - COUNT_HEADER, "There should be 4 synonyms (3 synonyms, 1 MAN)");
        line = getLine(synonymResult, basionymSynonymUuid);
        //unique citation with detail
        assertTrue(line.contains("Author (1980a: 67)"));

        line = getLine(synonymResult, misappliedTaxonUuid);
        //unique citation for MAN sec
        assertTrue(line.contains("Author (1980b)"));

        List<String> factsResult = getStringList(data, CdmLightExportTable.FACT_SOURCES);
        line = getLine(factsResult, "81c7b7db-e12b-45fe-96ed-dab940043232");//UUID of Common name
        line.contains("c68e0a88-7b96-465d-b4ce-d14b13daf94f");//UUID of fact citation
        line = getLine(referenceResult, "c68e0a88-7b96-465d-b4ce-d14b13daf94f"); // line of fact citation in references
        assertTrue(line.contains("Testauthor (1981)"));

        line = getLine(factsResult, "674e9e27-9102-4166-8626-8cb871a9a89b");//UUID of distribution
        line.contains("e2edf07d-552a-46cd-85da-86a491847aeb");//UUID of fact citation
        line = getLine(referenceResult, "e2edf07d-552a-46cd-85da-86a491847aeb"); // line of fact citation in references
        assertTrue(line.contains("Testauthor (1981a)"));
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testTypeDesignationOutput(){

      //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);
        assertTrue(result.getExportType().equals(ExportType.CDM_LIGHT)); //test export type
        List<String> hgList = getStringList(data, CdmLightExportTable.HOMOTYPIC_GROUP);
        assertNotNull(hgList, "HomotypicGroup table must not be null");
        assertTrue(hgList.size() > 1, "HomotypicGroup table must not be empty or only have header line");
        String line = getLine(hgList, speciesNameHgUuid);

        String expectedSpecimen = "Holotype: Armenia, Somewhere in the forest, 55°33'22\"\"N, 15°13'12\"\"W (WGS84), Collector team CT222 (B A555).";

        assertTrue(line.contains(expectedSpecimen));
        String expectedText = "Textual typedesignation test.";
        assertTrue(line.contains(expectedText));
        //textual type designation should be contained only once
        assertTrue(line.lastIndexOf(expectedText)== line.indexOf(expectedText));
        //TODO: NameTypedesignation

    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testSynSecOutput(){

      //config + invoke
        CdmLightExportConfigurator config = newConfigurator();
        config.setShowSynSecForHomotypicGroup(true);

        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);
        assertTrue(result.getExportType().equals(ExportType.CDM_LIGHT)); //test export type
        List<String> hgList = getStringList(data, CdmLightExportTable.HOMOTYPIC_GROUP);
        assertNotNull(hgList, "HomotypicGroup table must not be null");
        assertTrue(hgList.size() > 1, "HomotypicGroup table must not be empty or only have header line");
        String line = getLine(hgList, speciesNameHgUuid);

        String expectedSynSec = "syn. sec. Author (1980: 67), My sec ref";

        assertTrue(line.contains(expectedSynSec));

        //show syn sec for every synonym
        config.setShowSynSecForHomotypicGroup(false);
        result = defaultExport.invoke(config);
        data = (Map<String, byte[]>)checkAndGetData(result);
        assertTrue(result.getExportType().equals(ExportType.CDM_LIGHT)); //test export type
        hgList = getStringList(data, CdmLightExportTable.HOMOTYPIC_GROUP);
        assertNotNull(hgList, "HomotypicGroup table must not be null");
        assertTrue(hgList.size() > 1, "HomotypicGroup table must not be empty or only have header line");
        line = getLine(hgList, speciesNameHgUuid);
        expectedSynSec = "syn. sec. My sec ref";

        assertTrue(line.contains(expectedSynSec));

    }

    @SuppressWarnings("deprecation")
    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testWithErrors() {

        boolean printReport = false;

        //create data that creates an error
        Taxon speciesTaxon = (Taxon)this.taxonService.find(speciesTaxonUuid);
        Set<CommonTaxonName> commonNames = speciesTaxon.getDescriptionItems(Feature.COMMON_NAME(), CommonTaxonName.class);
        commonNames.iterator().next().setFeature(Feature.DISTRIBUTION());
        //end create

        CdmLightExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        checkAndGetData(result, 0, 1, 0);
        IoInfo error = result.getErrors().get(0);
        String message = error.getMessage();
        assertEquals("The distribution description for the taxon 9182e136-f2e2-4f9a-9010-3f35908fb5e0 is not of type distribution. Could not be exported. "
                + "UUID of the description element",
                message.substring(0, 163));
        String report = result.createReport().toString();
        if (printReport) {
            System.out.println(report);
        }

        //TODO needs discussion if exports with errors should be "successful"
        assertEquals("Export was successfull.", report.substring(1, 24));  //first row is linebreak
        assertTrue(report.contains("Errors:"));
        assertFalse(report.contains("Exceptions:"));

        //create data that creates an exception
        Taxon subspeciesTaxon = (Taxon)this.taxonService.find(subspeciesTaxonUuid);
        Set<Distribution> distributions = subspeciesTaxon.getDescriptionItems(Feature.DISTRIBUTION(), Distribution.class);
        distributions.iterator().next().setFeature(null);
        //end create

        result = defaultExport.invoke(config);
        checkAndGetData(result, 1, 1, 0);
        IoInfo exception = result.getExceptions().get(0);
        assertEquals("An unexpected error occurred when handling description of Taxon: b2c866",
                exception.getMessage().substring(0, 71));
        report = result.createReport().toString();
        assertTrue(report.contains("Errors:"));
        assertTrue(report.contains("Exceptions:"));

        if (printReport) {
            System.out.println(report);
        }
    }

    @Override
    protected CdmLightExportConfigurator newConfigurator() {
        return CdmLightExportConfigurator.NewInstance();
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}