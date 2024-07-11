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
        expected = uuid(subspeciesNomRefUuid) +"\"Mill. (1804)\",\"\",\"The book of botany\",\"1804\"," + NONE10 + "\"3\",\"1804\",\"Mill.\"";
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
        if (config.isAddHTML()){
            expected = "\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\"";
            Assert.assertTrue(scientificNameString.contains(expected));
        }

        expected ="\"Book\",\"The book of botany\",\"The book of botany\",\"Mill.\",\"Mill.\",\"3:22\",\"3\",\"22\",\"1804\",\"1804\",\"\",\"\",\"\",\"\"";
        Assert.assertTrue(scientificNameString.contains(expected));

        //homotypic group
        byte[] homotypicGroup = data.get(CdmLightExportTable.HOMOTYPIC_GROUP.getTableName());
        String homotypicGroupString = new String(homotypicGroup);
        Assert.assertNotNull("Homotypic Group table must not be null", homotypicGroup);
        if (config.isAddHTML()){
            expected ="\"= <i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\",\"\"";
        }else{
            expected ="\"= Genus species subsp. subspec Mill., The book of botany 3: 22. 1804\",\"\"";
        }
        Assert.assertTrue(homotypicGroupString.contains(expected));
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
        Assert.assertEquals("There should be 9 references (8 nomenclatural references including an in-reference"
                + " and 1 sec reference)", 9, referenceResult.size() - COUNT_HEADER);

        List<String> synonymResult = getStringList(data, CdmLightExportTable.SYNONYM);
        Assert.assertEquals("There should be 2 synonym", 2, synonymResult.size() - COUNT_HEADER);

        //test single data
        Assert.assertEquals("Result must not contain root taxon",
                0, taxonResult.stream().filter(line->line.contains(rootNodeUuid.toString())).count());

        //subspecies
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        String expected = uuid(subspeciesTaxonUuid) + uuid(classificationUuid) + "\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\","+uuid(speciesTaxonUuid)+"\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
        Assert.assertEquals(expected, subspeciesLine.substring(0, expected.length()));
        String expectedSecNameUsedInSource = "\"My sec ref\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"Genus species subsp. subspec\",\"Mill.\",";
        Assert.assertTrue(subspeciesLine.contains(expectedSecNameUsedInSource));

        //unpublished/excluded/note
        String unpublishedLine = getLine(taxonResult, subspeciesUnpublishedTaxonUuid);
        String expectedExcluded = "\"\",\"0\",\"0\",\"0\",\"1\",\"1\",\"0\",\"0\",\"0\",\"0\",\"0\",\""+TaxonNodeStatus.EXCLUDED.getLabel()+"\",\"My status note\",\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref: 27\",\"0\"";
        Assert.assertTrue(unpublishedLine.contains(expectedExcluded));

        //references
        String nomRefLine = getLine(referenceResult, UUID.fromString("b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f"));
        expected ="\"b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f\",\"Mill. (1804)\",\"\",\"The book of botany\",\"1804\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"3\",\"1804\",\"Mill.\"";
        Assert.assertEquals(expected, nomRefLine.substring(0, expected.length()));
        Assert.assertTrue(nomRefLine.contains(",\"BK\","));
        String expectedAfterRefType = nomRefLine.substring(nomRefLine.indexOf(",\"BK\","));
        //#10488
        Assert.assertEquals("Test unique year 1804b ", ",\"BK\",\"\",\"Mill. 1804: The book of botany 3\",\"Mill. (1804b)\",\"Mill. 1804b: The book of botany 3\"", expectedAfterRefType);

        byte[] geographicAreaFact = data.get(CdmLightExportTable.GEOGRAPHIC_AREA_FACT.getTableName());
        String geographicAreaFactString = new String(geographicAreaFact);
        Assert.assertNotNull("Geographical fact table must not be null", geographicAreaFact);
        expected ="\"674e9e27-9102-4166-8626-8cb871a9a89b\"," + uuid(subspeciesTaxonUuid) + "\"Armenia\",\"present\"";
        Assert.assertTrue(geographicAreaFactString.contains(expected));

        byte[] nomenclaturalAuthor = data.get(CdmLightExportTable.NOMENCLATURAL_AUTHOR.getTableName());
        String nomenclaturalAuthorString = new String(nomenclaturalAuthor);
        Assert.assertNotNull("Nomenclatural Author table must not be null", nomenclaturalAuthor);
        expected ="\"Mill.\",\"Mill.\",\"\",\"\",\"\",\"\"";
        Assert.assertTrue(nomenclaturalAuthorString.contains(expected));

        byte[] scientificName = data.get(CdmLightExportTable.SCIENTIFIC_NAME.getTableName());
        String scientificNameString = new String(scientificName);
        Assert.assertNotNull("Scientific Name table must not be null", scientificName);
        expected ="\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"\",\"Subspecies\",\"43\",\"Genus species subsp. subspec Mill.\",\"Genus species subsp. subspec\",\"Genus\",\"\",\"\",\"species\",\"subsp.\",\"subspec\",\"\",\"\",\"\",";
        Assert.assertTrue(scientificNameString.contains(expected));
        expected ="\"Book\",\"The book of botany\",\"The book of botany\",\"Mill.\",\"Mill.\",\"3:22\",\"3\",\"22\",\"1804\",\"1804\",\"\",\"\",\"\",\"\"";
        Assert.assertTrue(scientificNameString.contains(expected));

        byte[] homotypicGroup = data.get(CdmLightExportTable.HOMOTYPIC_GROUP.getTableName());
        String homotypicGroupString = new String(homotypicGroup);
        Assert.assertNotNull("HomotypicGroup table must not be null", homotypicGroup);
        if (config.isAddHTML()){
            expected ="\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\",\"\",\"\",\"= <i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804 My sec ref\",\"\",\"\"";
        }else{
            expected ="\"Genus species subsp. subspec Mill., The book of botany 3: 22. (1804)\",\"\",\"\",\"= Genus species subsp. subspec Mill., The book of botany 3: 22. (1804) My sec ref\",\"\",\"\"";
        }
        Assert.assertTrue(homotypicGroupString.contains(expected));
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
        Assert.assertEquals("There should be 7 references (6 nomenclatural references and 1 sec reference)", 7, referenceResult.size() - COUNT_HEADER);

        //synonyms
        List<String> synonymResult = getStringList(data, CdmLightExportTable.SYNONYM);
        Assert.assertEquals("There should be 1 synonym", 1, synonymResult.size() - COUNT_HEADER);
    }
//    @Test
//    @DataSets({
//        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
//        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
//    })
//    public void testUniqueCitationsDataPublished(){
//        // add additional reference
//        Reference ref1 = ReferenceFactory.newArticle();
//        ref1.setAuthorship(null);
//        ref1.setDatePublished(null);
//
//      //config + invoke
//        CdmLightExportConfigurator config = newConfigurator();
//        ExportResult result = defaultExport.invoke(config);
//        Map<String, byte[]> data = checkAndGetData(result);
//        Assert.assertTrue(result.getExportType().equals(ExportType.CDM_LIGHT)); //test export type
//
//
//
//    }

    @Override
    protected CdmLightExportConfigurator newConfigurator() {
        return CdmLightExportConfigurator.NewInstance();
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}