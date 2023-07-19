/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.coldp;

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
import org.junit.Ignore;
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
import eu.etaxonomy.cdm.model.location.Country;
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
 * Test class for COL-DP export.
 * Note: this test was originally copied from CDM-light export test.
 *
 * @author a.mueller
 * @date 19.07.2023
 */
public class ColDpExportTest extends CdmTransactionalIntegrationTest{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private static final String NONE_END  = "\"\""; //empty entry without separator to next entry
    private static final String NONE  = NONE_END + ","; //empty entry
    private static final String NONE2  = NONE + NONE; //2 empty entries
    private static final String NONE3  = NONE2 + NONE; //3 empty entries
    private static final String NONE4  = NONE2 + NONE2; //4 empty entries
    private static final String NONE9  = NONE4 + NONE4 + NONE; //7 empty entries
    private static final String NONE10  = NONE9 + NONE; //10 empty entries
    private static final String NONE20  = NONE10 + NONE10; //20 empty entries
    private static final String FALSE  = "\"0\","; //false
    private static final String TRUE  = "\"1\","; //true
    private static final String BOOL_NULL = NONE;  //boolean null



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

    private static final UUID subspeciesNameUuid = UUID.fromString("3483cc5e-4c77-4c80-8cb0-73d43df31ee3");
    private static final UUID subspeciesUnpublishedNameUUID = UUID.fromString("b6da7ab2-6c67-44b7-9719-2557542f5a23");

    private static final UUID subspeciesNomRefUuid = UUID.fromString("b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f");

    private static final UUID ref1UUID = UUID.fromString("4b6acca1-959b-4790-b76e-e474a0882990");

    @SpringBeanByName
    private CdmApplicationAwareDefaultExport<ColDpExportConfigurator> defaultExport;

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
    @Ignore
    public void testSubTree(){

        ColDpExportConfigurator config = ColDpExportConfigurator.NewInstance();
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(UUID.fromString("f8c9933a-fe3a-42ce-8a92-000e27bfdfac")));

        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
        ExportDataWrapper<?> exportData = result.getExportData();

        //test exceptions
        testExceptionsErrorsWarnings(result);

        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();

        byte[] taxonByte = data.get(ColDpExportTable.TAXON.getTableName());
        Assert.assertNotNull("Taxon table must not be null", taxonByte);
        String taxonStr = new String(taxonByte);
        String notExpected = "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\"";
        Assert.assertFalse("Result must not contain root taxon", taxonStr.startsWith(notExpected));
        String expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"4096df99-7274-421e-8843-211b603d832e\",\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
        Assert.assertTrue(taxonStr.contains(expected));

        byte[] reference = data.get(ColDpExportTable.REFERENCE.getTableName());
        String referenceString = new String(reference);
        Assert.assertNotNull("Reference table must not be null", reference);
        expected ="\"b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f\",\"Mill. (1804)\",\"\",\"The book of botany\",\"1804\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"3\",\"1804\",\"Mill.\"";
        Assert.assertTrue(referenceString.contains(expected));

        byte[] geographicAreaFact = data.get(ColDpExportTable.DISTRIBUTION.getTableName());
        String geographicAreaFactString = new String(geographicAreaFact);
        Assert.assertNotNull("Geographical fact table must not be null", geographicAreaFact);
        expected ="\"674e9e27-9102-4166-8626-8cb871a9a89b\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"Africa\",\"present\"";
        Assert.assertTrue(geographicAreaFactString.contains(expected));

        byte[] scientificName = data.get(ColDpExportTable.NAME.getTableName());
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

        List<String> nameResult = getStringList(data, ColDpExportTable.NAME);
        Assert.assertEquals("There should be 6 names", 6, nameResult.size()-1);// 1 header line

        List<String> referenceResult = getStringList(data, ColDpExportTable.REFERENCE);
        Assert.assertEquals("There should be 7 references (6 nomenclatural references and 1 sec reference)", 7, referenceResult.size()-1);// 1 header line

        List<String> synonymResult = getStringList(data, ColDpExportTable.SYNONYM);
        Assert.assertEquals("There should be 1 synonym", 1, synonymResult.size()-1);// 1 header line

        List<String> distributionResult = getStringList(data, ColDpExportTable.DISTRIBUTION);
        Assert.assertEquals("There should be 1 distribution", 1, distributionResult.size()-1);// 1 header line


        //TODO name, relName, treatment, typeMaterial, speciesInteraction, taxRel, distribution,
        //     vernacularName, media, speciesEstimate ...

        //test single data
        //... invisible root node
        Assert.assertEquals("Result must not contain root taxon",
                0, taxonResult.stream().filter(line->line.contains(rootNodeUuid.toString())).count());

        //subspecies taxon
        String subspeciesLine = getLine(taxonResult, subspeciesTaxonUuid);
        //still missing: alternativeID, sourceID, sequenceIndex, branchLength, scrutinizerXXX, referenceID, environment, extinct, ...
        //FIXME link
        String expected = uuid(subspeciesTaxonUuid)+ NONE2 + uuid(speciesTaxonUuid) + NONE2 +
                uuid(subspeciesNameUuid) + NONE + uuid(ref1UUID)+ NONE3 + FALSE + NONE + BOOL_NULL + NONE20 + NONE_END;
        Assert.assertEquals(expected, subspeciesLine);

        //unpublished/excluded/note
        //TODO evaluate unpublished flag and discuss how to handle excluded
        String unpublishedLine = getLine(taxonResult, subspeciesUnpublishedTaxonUuid);
        String expectedExcluded = uuid(subspeciesUnpublishedTaxonUuid)+ NONE2 + uuid(speciesTaxonUuid) + NONE2 +
                uuid(subspeciesUnpublishedNameUUID) + NONE + uuid(ref1UUID)+ NONE3 + FALSE + NONE + BOOL_NULL +
                NONE20 + NONE_END;
        Assert.assertEquals(expectedExcluded, unpublishedLine);

        //references
        String nomRefLine = getLine(referenceResult, subspeciesNomRefUuid);
        expected = uuid(subspeciesNomRefUuid) + NONE2 + "\"Mill. 1804: The book of botany 3\",\"book\",\"Mill.\"," + NONE4 + "\"1804\"," + NONE3 + "\"3\"," + NONE10 + NONE_END;
//       TODO check other NONE entries:
        Assert.assertEquals(expected, nomRefLine);

        //distribution
        String distributionStr = getLine(distributionResult, subspeciesTaxonUuid); // new String(distribution);
        expected = uuid(subspeciesTaxonUuid) + NONE + "\"ARM\",\"Armenia\",\"iso\",\"uncertain\"," + NONE + NONE_END;
        //TODO enable anf fix areaID, gazetter and status. Also check referenceID and remarks
        Assert.assertEquals(expected, distributionStr);

        //name
        String nameStr = getLine(nameResult, subspeciesNameUuid);
        String scientificNameString = new String(data.get(ColDpExportTable.NAME.getTableName()));
        System.out.println(scientificNameString);
        //FIXME basionymID, nom. status
        String basionymID = NONE;
        expected = uuid(subspeciesNameUuid) + NONE2 + basionymID + "\"Genus species subsp. subspec\",\"Mill.\",\"subspecies\","
                + NONE + "\"Genus\"," + NONE + "\"species\",\"subspec\"," + NONE + "\"ICN\"," + NONE + uuid(subspeciesNomRefUuid) + "\"1804\",\"22\"," + NONE2 + NONE_END;
        Assert.assertEquals(expected, nameStr);

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

        try {
            ByteArrayInputStream stream = new ByteArrayInputStream( data.get(ColDpExportTable.TAXON.getTableName()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                count ++;
            }
            Assert.assertTrue("There should be 4 taxa", count == 5);// 5 because of the header line

            //references
            stream = new ByteArrayInputStream(data.get(ColDpExportTable.REFERENCE.getTableName()));
            reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
            count = 0;
            while ((line = reader.readLine()) != null) {
                count ++;
            }
            Assert.assertTrue("There should be 5 references", count == 6);

            //synonyms
            try{
                stream = new ByteArrayInputStream(data.get(ColDpExportTable.SYNONYM.getTableName()));
                //there are always all tables also if empty
                reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
                count = 0;
                while ((line = reader.readLine()) != null) {
                    count++;
                }
                Assert.assertTrue("There should be 0 synomyms", count == 1);
//                Assert.fail("There should not be a synonym table, because the only synonym is not public.");
            }catch(NullPointerException e){
                Assert.fail();
                //OK, should be thrown
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //this test only test the COL-DB export runs without throwing exception
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
        ColDpExportConfigurator config = ColDpExportConfigurator.NewInstance();
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

    private List<String> getStringList(Map<String, byte[]> data, ColDpExportTable table) {

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

    private String uuid(UUID uuid) {
        return "\"" + uuid + "\",";
    }

    private String getLine(List<String> list, UUID uuid) {
        return list.stream().filter(line->line.startsWith("\""+ uuid.toString())).findFirst().get();
    }

    public void createFullTestDataSet() {

        Set<TaxonNode> nodesToSave = new HashSet<>();
        NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();

        //sec ref
        Reference sec1 = ReferenceFactory.newGeneric();
        setUuid(sec1, ref1UUID);
        sec1.setTitle("My sec ref");

        //classification
        Classification classification = Classification.NewInstance("CdmLightExportTest Classification");
        setUuid(classification, "4096df99-7274-421e-8843-211b603d832e");
        setUuid(classification.getRootNode(), rootNodeUuid);

        //family
        TaxonName familyName = parser.parseReferencedName("Family L., Sp. Pl. 3: 22. 1752",
                NomenclaturalCode.ICNAFP, Rank.FAMILY());
        setUuid(familyName,"e983cc5e-4c77-4c80-8cb0-73d43df31ef7");
        setUuid(familyName.getNomenclaturalReference(), "b0dd7f4a-0c7f-4372-bc5d-3b676363bc63");
        Taxon family = Taxon.NewInstance(familyName, sec1);
        setUuid(family, familyTaxonUuid);
        TaxonNode node1 = classification.addChildTaxon(family, sec1, "22");
        setUuid(node1, node1Uuid.toString());
        nodesToSave.add(node1);

        //genus
        TaxonName genusName = parser.parseReferencedName("Genus Humb., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.GENUS());
        setUuid(genusName,"5e83cc5e-4c77-4d80-8cb0-73d63df35ee3");
        setUuid(genusName.getNomenclaturalReference(), "5ed27f4a-6c7f-4372-bc5d-3b67636abc52");
        Taxon genus = Taxon.NewInstance(genusName, sec1);
        setUuid(genus, genusTaxonUuid);

        TaxonNode node2 = node1.addChildTaxon(genus, sec1, "33");
        setUuid(node2, node2Uuid);
        nodesToSave.add(node2);

        //species
        TaxonName speciesName = parser.parseReferencedName("Genus species Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SPECIES());
        setUuid(speciesName,"f983cc5e-4c77-4c80-8cb0-73d43df31ee9");
        setUuid(speciesName.getNomenclaturalReference(), "a0dd7f4a-0c7f-4372-bc5d-3b676363bc0e");
        Taxon species = Taxon.NewInstance(speciesName, sec1);
        setUuid(species, speciesTaxonUuid);
        TaxonName synonymName = parser.parseReferencedName("Genus synonym Mill., The book of botany 3: 22. 1804", NomenclaturalCode.ICNAFP, Rank.SPECIES());

        //species synonym
        setUuid(synonymName, "1584157b-5c43-4150-b271-95b2c99377b2");
        Synonym  synonymUnpublished = Synonym.NewInstance(synonymName, sec1);
        setUuid(synonymName, "a87c16b7-8299-4d56-a682-ce20973428ea");
        synonymUnpublished.setPublish(false);
        species.addHomotypicSynonym(synonymUnpublished);
        TaxonNode node3 = node2.addChildTaxon(species, sec1, "33");
        setUuid(node3, node3Uuid.toString());
        nodesToSave.add(node3);

        //subspecies
        TaxonName subspeciesName = parser.parseReferencedName("Genus species subsp. subspec Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(subspeciesName, subspeciesNameUuid);
        setUuid(subspeciesName.getNomenclaturalReference(), subspeciesNomRefUuid);

        Taxon subspecies = Taxon.NewInstance(subspeciesName, sec1);
        subspecies.getSecSource().setNameUsedInSource(subspeciesName);
        setUuid(subspecies, subspeciesTaxonUuid);
        TaxonNode node4 = node3.addChildTaxon(subspecies, sec1, "33");
        setUuid(node4, node4Uuid);
        nodesToSave.add(node4);

        //subspecies unpublished
        TaxonName subspeciesNameUnpublished = parser.parseReferencedName("Genus species subsp. unpublished Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(subspeciesNameUnpublished, subspeciesUnpublishedNameUUID);

        Taxon subspeciesUnpublished = Taxon.NewInstance(subspeciesNameUnpublished, sec1);
        setUuid(subspeciesUnpublished, subspeciesUnpublishedTaxonUuid);
        subspeciesUnpublished.setPublish(false);
        TaxonNode node5 = node3.addChildTaxon(subspeciesUnpublished, sec1, "33");
        //... excluded node
        setUuid(node5, node5Uuid.toString());
        node5.setStatus(TaxonNodeStatus.EXCLUDED);
        node5.setCitation(sec1);
        node5.setCitationMicroReference("27");
        node5.putStatusNote(Language.ENGLISH(), "My status note");
        nodesToSave.add(node5);

        classificationService.save(classification);
        taxonNodeService.save(nodesToSave);

        TaxonDescription description = TaxonDescription.NewInstance(species);

        Distribution distribution = Distribution.NewInstance(Country.ARMENIA(), PresenceAbsenceTerm.PRESENT());
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
