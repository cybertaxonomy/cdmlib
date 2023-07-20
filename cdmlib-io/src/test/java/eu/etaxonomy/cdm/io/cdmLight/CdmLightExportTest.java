/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author k.luther
 * @since 17.01.2018
 */
public class CdmLightExportTest extends CdmTransactionalIntegrationTest{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private static final UUID rootNodeUuid = UUID.fromString("a67b4efd-6148-46a9-a377-1efd14768cfa");
    private static final UUID node1Uuid = UUID.fromString("0fae5ad5-ffa2-4100-bcd7-8aa9dda0aebc");
    private static final UUID node2Uuid = UUID.fromString("43ca733b-fe3a-42ce-8a92-000e27badf44");
    private static final UUID node3Uuid = UUID.fromString("a0c9733a-fe3a-42ce-8a92-000e27bfdfa3");
    private static final UUID node4Uuid = UUID.fromString("f8c9933a-fe3a-42ce-8a92-000e27bfdfac");
    private static final UUID node5Uuid = UUID.fromString("81d9c9b2-c8fd-4d4f-a0b4-e7e656dcdc20");

    private static final UUID familyTaxonUuid = UUID.fromString("3162e136-f2e2-4f9a-9010-3f35908fbae1");
    private static final UUID genusTaxonUuid = UUID.fromString("3f52e136-f2e1-4f9a-9010-2f35908fbd39");
    private static final UUID speciesTaxonUuid = UUID.fromString("9182e136-f2e2-4f9a-9010-3f35908fb5e0");
    private static final UUID subspeciesTaxonUuid = UUID.fromString("b2c86698-500e-4efb-b9ae-6bb6e701d4bc");
    private static final UUID subspeciesUnpublishedTaxonUuid = UUID.fromString("290e295a-9089-4616-a30c-15ded79e064f");

    @SpringBeanByName
    private CdmApplicationAwareDefaultExport<CdmLightExportConfigurator> defaultExport;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private ITaxonNodeService taxonNodeService;

    @SpringBeanByType
    private ICommonService commonService;

    @Before
    public void setUp()  {
        createFullTestDataSet();
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testSubTree(){

        CdmLightExportConfigurator config = CdmLightExportConfigurator.NewInstance();
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(UUID.fromString("f8c9933a-fe3a-42ce-8a92-000e27bfdfac")));

        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
        ExportDataWrapper<?> exportData = result.getExportData();
        testExceptionsErrorsWarnings(result);

        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();

        byte[] taxonByte = data.get(CdmLightExportTable.TAXON.getTableName());
        Assert.assertNotNull("Taxon table must not be null", taxonByte);
        String taxonStr = new String(taxonByte);
        String notExpected = "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\"";
        Assert.assertFalse("Result must not contain root taxon", taxonStr.startsWith(notExpected));
        String expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"4096df99-7274-421e-8843-211b603d832e\",\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
        Assert.assertTrue(taxonStr.contains(expected));

        byte[] reference = data.get(CdmLightExportTable.REFERENCE.getTableName());
        String referenceString = new String(reference);
        Assert.assertNotNull("Reference table must not be null", reference);
        expected ="\"b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f\",\"Mill. (1804)\",\"\",\"The book of botany\",\"1804\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"3\",\"1804\",\"Mill.\"";
        Assert.assertTrue(referenceString.contains(expected));

        byte[] geographicAreaFact = data.get(CdmLightExportTable.GEOGRAPHIC_AREA_FACT.getTableName());
        String geographicAreaFactString = new String(geographicAreaFact);
        Assert.assertNotNull("Geographical fact table must not be null", geographicAreaFact);
        expected ="\"674e9e27-9102-4166-8626-8cb871a9a89b\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"Africa\",\"present\"";
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
        if (config.isAddHTML()){
            expected = "\"<i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\"";
            Assert.assertTrue(scientificNameString.contains(expected));
        }

        expected ="\"Book\",\"The book of botany\",\"The book of botany\",\"Mill.\",\"Mill.\",\"3:22\",\"3\",\"22\",\"1804\",\"1804\",\"\",\"\",\"\",\"\"";
        Assert.assertTrue(scientificNameString.contains(expected));

        byte[] homotypicGroup = data.get(CdmLightExportTable.HOMOTYPIC_GROUP.getTableName());
        String homotypicGroupString = new String(homotypicGroup);
        Assert.assertNotNull("Reference table must not be null", homotypicGroup);
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

        CdmLightExportConfigurator config = CdmLightExportConfigurator.NewInstance();
        config.setTarget(TARGET.EXPORT_DATA);
        config.getTaxonNodeFilter().setIncludeUnpublished(true);

        ExportResult result = defaultExport.invoke(config);
        testExceptionsErrorsWarnings(result);
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();

        //test counts
        List<String> taxonResult = getStringList(data, CdmLightExportTable.TAXON);
        Assert.assertEquals("There should be 5 taxa", 5, taxonResult.size()-1);// 1 header line

        List<String> referenceResult = getStringList(data, CdmLightExportTable.REFERENCE);
        Assert.assertEquals("There should be 7 references (6 nomenclatural references and 1 sec reference)", 7, referenceResult.size()-1);// 1 header line

        List<String> synonymResult = getStringList(data, CdmLightExportTable.SYNONYM);
        Assert.assertEquals("There should be 1 synonym", 1, synonymResult.size()-1);// 1 header line

        //test single data
        Assert.assertEquals("Result must not contain root taxon",
                0, taxonResult.stream().filter(line->line.contains(rootNodeUuid.toString())).count());

        //subspecies
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        String expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"4096df99-7274-421e-8843-211b603d832e\",\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
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

        byte[] geographicAreaFact = data.get(CdmLightExportTable.GEOGRAPHIC_AREA_FACT.getTableName());
        String geographicAreaFactString = new String(geographicAreaFact);
        Assert.assertNotNull("Geographical fact table must not be null", geographicAreaFact);
        expected ="\"674e9e27-9102-4166-8626-8cb871a9a89b\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"Africa\",\"present\"";
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
        Assert.assertNotNull("Reference table must not be null", homotypicGroup);
        if (config.isAddHTML()){
            expected ="\"= <i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804\",\"\",\"\",\"= <i>Genus</i> <i>species</i> subsp. <i>subspec</i> Mill., The book of botany 3: 22. 1804 My sec ref\",\"\",\"\"";
        }else{
            expected ="\"= Genus species subsp. subspec Mill., The book of botany 3: 22. (1804)\",\"\",\"\",\"= Genus species subsp. subspec Mill., The book of botany 3: 22. (1804) My sec ref\",\"\",\"\"";
        }
        Assert.assertTrue(homotypicGroupString.contains(expected));
    }

    private String getLine(List<String> list, UUID uuid) {
        return list.stream().filter(line->line.startsWith("\""+ uuid.toString())).findFirst().get();
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFullData(){

        CdmLightExportConfigurator config = CdmLightExportConfigurator.NewInstance();
        config.setTarget(TARGET.EXPORT_DATA);

        ExportResult result = defaultExport.invoke(config);
        testExceptionsErrorsWarnings(result);

        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();

        ByteArrayInputStream stream = new ByteArrayInputStream( data.get(CdmLightExportTable.TAXON.getTableName()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        String line;
        int count = 0;
        try {
            while ((line = reader.readLine()) != null) {
                count ++;
            }
            Assert.assertTrue("There should be 4 taxa", count == 5);// 5 because of the header line

            stream = new ByteArrayInputStream(data.get(CdmLightExportTable.REFERENCE.getTableName()));
            reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
            count = 0;
            while ((line = reader.readLine()) != null) {
                count ++;
            }
            Assert.assertTrue("There should be 5 references", count == 6);
            try{
                stream = new ByteArrayInputStream(data.get(CdmLightExportTable.SYNONYM.getTableName()));
                // now there are always all tables also if empty
                reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

                boolean dummyLine = true;
                count = 0;
                while ((line = reader.readLine()) != null) {
                    if (!(line.startsWith("\"DUMMY") || line.startsWith("\"Synonym_ID"))){
                        dummyLine = dummyLine && false;
                    }
                    count++;
                }
                Assert.assertTrue("There should be 0 synomyms", dummyLine && count == 3);
//                    Assert.fail("There should not be a synonym table, because the only synonym is not public.");
            }catch(NullPointerException e){
                //OK, should be thrown
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //this test only test the CDM-light export runs without throwing exception
    //on the full sample data
    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFullSampleData(){

        //create data
        commonService.createFullSampleData();
        commitAndStartNewTransaction();

        //config+invoke
        CdmLightExportConfigurator config = CdmLightExportConfigurator.NewInstance();
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);

        //test exceptions
        testExceptionsErrorsWarnings(result);
    }

    private void testExceptionsErrorsWarnings(ExportResult result) {
        Assert.assertTrue(result.getExceptions().size() == 0);
        Assert.assertTrue(result.getErrors().size() == 0);
        Assert.assertTrue(result.getWarnings().size() == 0);
    }

    private List<String> getStringList(Map<String, byte[]> data, CdmLightExportTable table) {
        List<String> result = new ArrayList<>();
        ByteArrayInputStream stream = new ByteArrayInputStream( data.get(table.getTableName()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            Assert.fail("IOException during result read");
        }
        return result;
    }

    public void createFullTestDataSet() {

        Set<TaxonNode> nodesToSave = new HashSet<>();

        Reference sec1 = ReferenceFactory.newGeneric();
        setUuid(sec1, "4b6acca1-959b-4790-b76e-e474a0882990");
        sec1.setTitle("My sec ref");

        Classification classification = Classification.NewInstance("CdmLightExportTest Classification");
        setUuid(classification, "4096df99-7274-421e-8843-211b603d832e");
        setUuid(classification.getRootNode(), rootNodeUuid);

        NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
        TaxonName familyName = parser.parseReferencedName("Family L., Sp. Pl. 3: 22. 1752",
                NomenclaturalCode.ICNAFP, Rank.FAMILY());
        setUuid(familyName,"e983cc5e-4c77-4c80-8cb0-73d43df31ef7");
        setUuid(familyName.getNomenclaturalReference(), "b0dd7f4a-0c7f-4372-bc5d-3b676363bc63");
        Taxon family = Taxon.NewInstance(familyName, sec1);
        setUuid(family, familyTaxonUuid);
        TaxonNode node1 = classification.addChildTaxon(family, sec1, "22");
        setUuid(node1, node1Uuid.toString());
        nodesToSave.add(node1);

        TaxonName genusName = parser.parseReferencedName("Genus Humb., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.GENUS());
        setUuid(genusName,"5e83cc5e-4c77-4d80-8cb0-73d63df35ee3");
        setUuid(genusName.getNomenclaturalReference(), "5ed27f4a-6c7f-4372-bc5d-3b67636abc52");
        Taxon genus = Taxon.NewInstance(genusName, sec1);
        setUuid(genus, genusTaxonUuid);

        TaxonNode node2 = node1.addChildTaxon(genus, sec1, "33");
        setUuid(node2, node2Uuid);
        nodesToSave.add(node2);

        TaxonName speciesName = parser.parseReferencedName("Genus species Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SPECIES());
        setUuid(speciesName,"f983cc5e-4c77-4c80-8cb0-73d43df31ee9");
        setUuid(speciesName.getNomenclaturalReference(), "a0dd7f4a-0c7f-4372-bc5d-3b676363bc0e");
        Taxon species = Taxon.NewInstance(speciesName, sec1);
        setUuid(species, speciesTaxonUuid);
        TaxonName synonymName = parser.parseReferencedName("Genus synonym Mill., The book of botany 3: 22. 1804", NomenclaturalCode.ICNAFP, Rank.SPECIES());

        setUuid(synonymName, "1584157b-5c43-4150-b271-95b2c99377b2");
        Synonym  synonymUnpublished = Synonym.NewInstance(synonymName, sec1);
        setUuid(synonymName, "a87c16b7-8299-4d56-a682-ce20973428ea");
        synonymUnpublished.setPublish(false);
        species.addHomotypicSynonym(synonymUnpublished);
        TaxonNode node3 = node2.addChildTaxon(species, sec1, "33");
        setUuid(node3, node3Uuid.toString());
        nodesToSave.add(node3);

        TaxonName subspeciesName = parser.parseReferencedName("Genus species subsp. subspec Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(subspeciesName,"3483cc5e-4c77-4c80-8cb0-73d43df31ee3");
        setUuid(subspeciesName.getNomenclaturalReference(), "b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f");

        Taxon subspecies = Taxon.NewInstance(subspeciesName, sec1);
        subspecies.getSecSource().setNameUsedInSource(subspeciesName);
        setUuid(subspecies, subspeciesTaxonUuid);
        TaxonNode node4 = node3.addChildTaxon(subspecies, sec1, "33");
        setUuid(node4, node4Uuid);
        nodesToSave.add(node4);

        TaxonName subspeciesNameUnpublished = parser.parseReferencedName("Genus species subsp. unpublished Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(subspeciesNameUnpublished,"b6da7ab2-6c67-44b7-9719-2557542f5a23");

        Taxon subspeciesUnpublished = Taxon.NewInstance(subspeciesNameUnpublished, sec1);
        setUuid(subspeciesUnpublished, subspeciesUnpublishedTaxonUuid);
        subspeciesUnpublished.setPublish(false);
        TaxonNode node5 = node3.addChildTaxon(subspeciesUnpublished, sec1, "33");
        //excluded node
        setUuid(node5, node5Uuid.toString());
        node5.setStatus(TaxonNodeStatus.EXCLUDED);
        node5.setCitation(sec1);
        node5.setCitationMicroReference("27");
        node5.putStatusNote(Language.ENGLISH(), "My status note");
        nodesToSave.add(node5);

        classificationService.save(classification);
        taxonNodeService.save(nodesToSave);

        TaxonDescription description = TaxonDescription.NewInstance(species);

        Distribution distribution = Distribution.NewInstance(NamedArea.AFRICA(), PresenceAbsenceTerm.PRESENT());
        setUuid(distribution,"674e9e27-9102-4166-8626-8cb871a9a89b");
        description.addElement(distribution);

        subspecies.addDescription(description);
        commitAndStartNewTransaction(null);
    }

    private void setUuid(CdmBase cdmBase, String uuidStr) {
        cdmBase.setUuid(UUID.fromString(uuidStr));
    }

    private void setUuid(CdmBase cdmBase, UUID uuid) {
        cdmBase.setUuid(uuid);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}