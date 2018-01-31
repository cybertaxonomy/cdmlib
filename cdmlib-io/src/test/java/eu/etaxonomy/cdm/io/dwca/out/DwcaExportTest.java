/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.model.common.CdmBase;
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
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @date 25.06.2017
 *
 */
public class DwcaExportTest  extends CdmTransactionalIntegrationTest{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DwcaExportTest.class);

    private static final UUID UUID_SUBSPEC_NODE = UUID.fromString("f8c9933a-fe3a-42ce-8a92-000e27bfdfac");
    private static final String UUID_UNPUBLISHED_TAXON = "e5cdc392-4e0b-49ad-84e9-8c4b22d1827c";


    @SpringBeanByName
    private CdmApplicationAwareDefaultExport<DwcaTaxExportConfigurator> defaultExport;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private ITaxonNodeService taxonNodeService;


    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testEmptyData(){
        File destinationFolder = null;
        DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(null, destinationFolder, null);
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
//        System.out.println(result.createReport());
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();
        for (String key : data.keySet()){
            byte[] byt = data.get(key);
//            System.out.println(key + ": " + new String(byt) );
        }

        //metadata
        byte[] metadata = data.get(DwcaTaxExportFile.METADATA.getTableName());
        Assert.assertNotNull("Metadata must not be null", metadata);
        String metaDataStr = new String(metadata);
        String metaHeader = "<?xml version=\"1.0\" ?><archive xmlns=\"http://rs.tdwg.org/dwc/text/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://rs.tdwg.org/dwc/text/ http://rs.tdwg.org/dwc/text/tdwg_dwc_text.xsd\"></archive>";
        Assert.assertTrue(metaDataStr.contains(metaHeader));
        String metaCore = "<core";
        Assert.assertFalse(metaDataStr.contains(metaCore));
        metaCore = "<field";
        Assert.assertFalse(metaDataStr.contains(metaCore));
        metaCore = "<files";
        Assert.assertFalse(metaDataStr.contains(metaCore));

        //core
        byte[] core = data.get(DwcaTaxExportFile.TAXON.getTableName());
        Assert.assertNull("Core must not exist", core);

        //reference
        byte[] ref = data.get(DwcaTaxExportFile.REFERENCE.getTableName());
        Assert.assertNull("Reference must not exist", ref);

        //distribution
        byte[] distribution = data.get(DwcaTaxExportFile.DISTRIBUTION.getTableName());
        Assert.assertNull("Distribution must not exist", distribution);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testSubTree(){
        createFullTestDataSet();
        File destinationFolder = null;
        DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(null, destinationFolder, null);
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(UUID_SUBSPEC_NODE));
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
//        System.out.println(result.createReport());
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();
        for (String key : data.keySet()){
            byte[] byt = data.get(key);
            System.out.println(key + ": " + new String(byt) );
        }
        //metadata
        byte[] metadata = data.get(DwcaTaxExportFile.METADATA.getTableName());
        Assert.assertNotNull("Metadata must not be null", metadata);
        //further tests in fullData

        //core
        byte[] core = data.get(DwcaTaxExportFile.TAXON.getTableName());
        Assert.assertNotNull("Core must not be null", core);
        String coreStr = new String(core);
//        String notExpected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\"";
//        Assert.assertFalse("Result must not contain root taxon", coreStr.contains(notExpected));
//
//        String expected;
//        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"Genus species subsp. subspec Mill.\",\"Subspecies\",\"accepted\",,\"4b6acca1-959b-4790-b76e-e474a0882990\",";
//        Assert.assertTrue(coreStr.contains(expected));
//
//        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"Genus species subsp. subspec Mill.\",\"Subspecies\",\"accepted\",,\"4b6acca1-959b-4790-b76e-e474a0882990\",\"4aa0824a-1197-4143-bc63-702ebfada9d2\"";
//        Assert.assertTrue(coreStr.contains(expected));
//        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"Genus species subsp. subspec Mill.\",\"Subspecies\",\"accepted\",,\"4b6acca1-959b-4790-b76e-e474a0882990\",\"4aa0824a-1197-4143-bc63-702ebfada9d2\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\"";
//        Assert.assertTrue(coreStr.contains(expected));
//        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"Genus species subsp. subspec Mill.\",\"Subspecies\",\"accepted\",,\"4b6acca1-959b-4790-b76e-e474a0882990\",\"4aa0824a-1197-4143-bc63-702ebfada9d2\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"Genus species subsp. subspec Mill.\"";
//        Assert.assertTrue(coreStr.contains(expected));


        String expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"Genus species subsp. subspec Mill.\",\"Subspecies\",\"accepted\",,\"4b6acca1-959b-4790-b76e-e474a0882990\",\"b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"Genus species subsp. subspec Mill.\",\"Genus species Mill. sec. My sec ref\",";
//        System.out.println(coreStr);
        Assert.assertTrue(coreStr.contains(expected));

        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"Genus species subsp. subspec Mill.\",\"Subspecies\",\"accepted\",,\"4b6acca1-959b-4790-b76e-e474a0882990\",\"b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"Genus species subsp. subspec Mill.\",\"Genus species Mill. sec. My sec ref\",,\"My sec ref\",\"Mill., The book of botany 3. 1804\",,\"Genus\",,\"species\",\"subspec\",\"subsp.\",,\"ICNAFP\",,,,,,,,,,\"DwcaExportTest Classification\",";
        Assert.assertTrue(coreStr.contains(expected));

        //distribution
        byte[] distribution = data.get(DwcaTaxExportFile.DISTRIBUTION.getTableName());
        Assert.assertNull("Distribution must not exist", distribution);

        //reference
        byte[] ref = data.get(DwcaTaxExportFile.REFERENCE.getTableName());
        Assert.assertNotNull("Reference must not be null", ref);
        String refStr = new String(ref);
//        System.out.println(refStr);
        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",,,,,\"My sec ref\",\"My sec ref\",,,,,,,,,";
        Assert.assertTrue(refStr.contains(expected));
        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",,,,,\"Mill., The book of botany 3. 1804\",,\"Mill.\",\"1804\",,,,,,,";
        Assert.assertTrue(refStr.contains(expected));
        //header
        expected = "coreid,identifier,identifier,identifier,identifier,bibliographicCitation,title,creator,date,source,description,subject,language,rights,taxonRemarks,type";
        Assert.assertTrue(refStr.contains(expected));

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testFullData(){
        createFullTestDataSet();
        File destinationFolder = null;
        DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(null, destinationFolder, null);
        config.setTarget(TARGET.EXPORT_DATA);
        config.setWithHigherClassification(true);
        ExportResult result = defaultExport.invoke(config);
//        System.out.println(result.createReport());
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();
        for (String key : data.keySet()){
            byte[] byt = data.get(key);
//            System.out.println(key + ": " + new String(byt) );
        }
//      System.out.println();
        //metadata
        byte[] metadata = data.get(DwcaTaxExportFile.METADATA.getTableName());
        Assert.assertNotNull("Metadata must not be null", metadata);
        String metaDataStr = new String(metadata);
        String metaHeader = "<?xml version=\"1.0\" ?><archive xmlns=\"http://rs.tdwg.org/dwc/text/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://rs.tdwg.org/dwc/text/ http://rs.tdwg.org/dwc/text/tdwg_dwc_text.xsd\">";
        Assert.assertTrue(metaDataStr.contains(metaHeader));

        String metaCore = "<core encoding=\"UTF-8\" linesTerminatedBy=\"\r\n\" fieldsEnclosedBy=\"&quot;\""
                + " fieldsTerminatedBy=\",\" ignoreHeaderLines=\"1\" rowType=\"http://rs.tdwg.org/dwc/terms/Taxon\">"
                + "<files><location>coreTax.txt</location></files><id index=\"0\"></id><field index=\"1\" term=\"http://rs.tdwg.org/dwc/terms/scientificNameID\"></field><field index=\"2\" term=\"http://rs.tdwg.org/dwc/terms/acceptedNameUsageID\"></field><field index=\"3\" term=\"http://rs.tdwg.org/dwc/terms/parentNameUsageID\"></field><field index=\"4\" term=\"http://rs.tdwg.org/dwc/terms/scientificName\"></field><field index=\"5\" term=\"http://rs.tdwg.org/dwc/terms/taxonRank\"></field>"
                + "<field index=\"6\" term=\"http://rs.tdwg.org/dwc/terms/taxonomicStatus\"></field><field index=\"7\" term=\"http://rs.tdwg.org/dwc/terms/originalNameUsageID\"></field>"
                + "<field index=\"8\" term=\"http://rs.tdwg.org/dwc/terms/nameAccordingToID\"></field>"
                + "<field index=\"9\" term=\"http://rs.tdwg.org/dwc/terms/namePublishedInID\"></field><field index=\"10\" term=\"http://rs.tdwg.org/dwc/terms/taxonConceptID\"></field><field index=\"11\" term=\"http://rs.tdwg.org/dwc/terms/acceptedNameUsage\"></field><field index=\"12\" term=\"http://rs.tdwg.org/dwc/terms/parentNameUsage\"></field><field index=\"13\" term=\"http://rs.tdwg.org/dwc/terms/originalNameUsage\"></field><field index=\"14\" term=\"http://rs.tdwg.org/dwc/terms/nameAccordingTo\"></field>"
                + "<field index=\"15\" term=\"http://rs.tdwg.org/dwc/terms/namePublishedIn\"></field>"

                + "<field index=\"16\" term=\"http://rs.tdwg.org/dwc/terms/higherClassification\"></field><field index=\"17\" term=\"http://rs.tdwg.org/dwc/terms/kingdom\"></field><field index=\"18\" term=\"http://rs.tdwg.org/dwc/terms/phylum\"></field><field index=\"19\" term=\"http://rs.tdwg.org/dwc/terms/class\"></field><field index=\"20\" term=\"http://rs.tdwg.org/dwc/terms/order\"></field><field index=\"21\" term=\"http://rs.tdwg.org/dwc/terms/family\"></field><field index=\"22\" term=\"http://rs.tdwg.org/dwc/terms/genus\"></field><field index=\"23\" term=\"http://rs.tdwg.org/dwc/terms/subgenus\"></field>"
                + "<field index=\"24\" term=\"http://rs.tdwg.org/ontology/voc/TaxonName#uninomial\"></field>"
                + "<field index=\"25\" term=\"http://rs.tdwg.org/ontology/voc/TaxonName#genusPart\"></field><field index=\"26\" term=\"http://rs.tdwg.org/ontology/voc/TaxonName#infragenericEpithet\"></field><field index=\"27\" term=\"http://rs.tdwg.org/dwc/terms/specificEpithet\"></field><field index=\"28\" term=\"http://rs.tdwg.org/dwc/terms/infraspecificEpithet\"></field><field index=\"29\" term=\"http://rs.tdwg.org/dwc/terms/verbatimTaxonRank\"></field><field index=\"30\" term=\"http://rs.tdwg.org/dwc/terms/vernacularName\"></field>"

                + "<field index=\"31\" term=\"http://rs.tdwg.org/dwc/terms/nomenclaturalCode\"></field><field index=\"32\" term=\"http://rs.tdwg.org/dwc/terms/nomenclaturalStatus\"></field><field index=\"33\" term=\"http://rs.tdwg.org/dwc/terms/taxonRemarks\"></field><field index=\"34\" term=\"http://purl.org/dc/terms/modified\"></field>"
                + "<field index=\"35\" term=\"http://purl.org/dc/terms/language\"></field><field index=\"36\" term=\"http://purl.org/dc/terms/rights\"></field><field index=\"37\" term=\"http://purl.org/dc/terms/rightsHolder\"></field><field index=\"38\" term=\"http://purl.org/dc/terms/accessRights\"></field>"
                + "<field index=\"39\" term=\"http://purl.org/dc/terms/bibliographicCitation\"></field><field index=\"40\" term=\"http://rs.tdwg.org/dwc/terms/informationWithheld\"></field><field index=\"41\" term=\"http://rs.tdwg.org/dwc/terms/datasetName\"></field><field index=\"42\" term=\"http://purl.org/dc/terms/source\"></field></core>" +
                "";
        //TODO continue
//        System.out.println(metaDataStr);
        Assert.assertTrue(metaDataStr.contains(metaCore));

        //core
        byte[] core = data.get(DwcaTaxExportFile.TAXON.getTableName());
        Assert.assertNotNull("Core must not be null", core);
        String coreStr = new String(core);
        String expected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"3f52e136-f2e1-4f9a-9010-2f35908fbd39\",\"Genus species Mill.\",\"Species\",\"accepted\"";
//        System.out.println(coreStr);
        Assert.assertTrue(coreStr.contains(expected));
        String expectedClassification = "\"Family|Genus|Genus species\"";
        Assert.assertTrue(coreStr.contains(expectedClassification));
        Assert.assertFalse(coreStr.contains(UUID_UNPUBLISHED_TAXON));


        //reference
        byte[] ref = data.get(DwcaTaxExportFile.REFERENCE.getTableName());
        Assert.assertNotNull("Reference must not be null", ref);
        String refStr = new String(ref);

        expected = "\"3162e136-f2e2-4f9a-9010-3f35908fbae1\",,,,,\"My sec ref\",\"My sec ref\",,,,,,,,,";
//        System.out.println(refStr);
        Assert.assertTrue(refStr.contains(expected));
        expected = "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",,,,,\"Mill., The book of botany 3. 1804\",,\"Mill.\",\"1804\",,,,,,,";
        Assert.assertTrue(refStr.contains(expected));

        //distribution
        byte[] distribution = data.get(DwcaTaxExportFile.DISTRIBUTION.getTableName());
        Assert.assertNotNull("Distribution must not be null", distribution);
        String distributionStr = new String(distribution);
        expected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"c204c529-d8d2-458f-b939-96f0ebd2cbe8\",\"Africa\",,,\"present\",,\"uncertain\",,,,,,";
        Assert.assertTrue(distributionStr.contains(expected));

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testUnpublished(){
        createFullTestDataSet();
        File destinationFolder = null;
        DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(null, destinationFolder, null);
        config.setTarget(TARGET.EXPORT_DATA);
        config.getTaxonNodeFilter().setIncludeUnpublished(true);
        ExportResult result = defaultExport.invoke(config);

        //core
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();
        byte[] core = data.get(DwcaTaxExportFile.TAXON.getTableName());
        Assert.assertNotNull("Core must not be null", core);
        String coreStr = new String(core);
        String expected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\"";
        Assert.assertTrue(coreStr.contains(expected));

        Assert.assertTrue(coreStr.contains(UUID_UNPUBLISHED_TAXON));
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testNoHeader(){
        createFullTestDataSet();
        File destinationFolder = null;
        DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(null, destinationFolder, null);
        config.setTarget(TARGET.EXPORT_DATA);
        config.setHasHeaderLines(false);
        ExportResult result = defaultExport.invoke(config);

        //core
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();
        byte[] core = data.get(DwcaTaxExportFile.TAXON.getTableName());
        Assert.assertNotNull("Core must not be null", core);
        String coreStr = new String(core);
        String expected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\"";
        Assert.assertTrue(coreStr.contains(expected));
//        System.out.println(coreStr);
        expected = "coreid,identifier,identifier,identifier,identifier,bibliographicCitation,title,creator,date,source,description,subject,language,rights,taxonRemarks,type";
        Assert.assertFalse(coreStr.contains(expected));
        expected = "coreid";
        Assert.assertFalse(coreStr.startsWith(expected));

        //reference
        byte[] ref = data.get(DwcaTaxExportFile.REFERENCE.getTableName());
        Assert.assertNotNull("Reference must not be null", ref);
        String refStr = new String(ref);
        expected = "\"3162e136-f2e2-4f9a-9010-3f35908fbae1\",,,,,\"My sec ref\",\"My sec ref\",,,,,,,,,";
//        System.out.println(refStr);
        Assert.assertTrue(refStr.contains(expected));
        expected = "coreid,identifier,identifier,identifier,identifier,bibliographicCitation,title,creator,date,source,description,subject,language,rights,taxonRemarks,type";
        Assert.assertFalse(refStr.contains(expected));
        expected = "coreid";
        Assert.assertFalse(refStr.startsWith(expected));

    }

    /**
     * {@inheritDoc}
     */
    public void createFullTestDataSet() {
        Set<TaxonNode> nodesToSave = new HashSet<>();
        Reference sec1 = ReferenceFactory.newGeneric();
        setUuid(sec1, "4b6acca1-959b-4790-b76e-e474a0882990");
        sec1.setTitle("My sec ref");

        Classification classification = Classification.NewInstance("DwcaExportTest Classification");
        setUuid(classification, "4096df99-7274-421e-8843-211b603d832e");

        NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();

        //family
        TaxonName familyName = parser.parseReferencedName("Family L., Sp. Pl. 3: 22. 1752",
                NomenclaturalCode.ICNAFP, Rank.FAMILY());
        setUuid(familyName,"e983cc5e-4c77-4c80-8cb0-73d43df31ef7");
        setUuid((Reference)familyName.getNomenclaturalReference(), "b0dd7f4a-0c7f-4372-bc5d-3b676363bc63");
        Taxon family = Taxon.NewInstance(familyName, sec1);
        setUuid(family,"3162e136-f2e2-4f9a-9010-3f35908fbae1");
        TaxonNode node1 = classification.addChildTaxon(family, sec1, "22");
        setUuid(node1, "0fae5ad5-ffa2-4100-bcd7-8aa9dda0aebc");
        nodesToSave.add(node1);

        //genus
        TaxonName genusName = parser.parseReferencedName("Genus Humb., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.GENUS());
        setUuid(genusName,"5e83cc5e-4c77-4d80-8cb0-73d63df35ee3");
        setUuid((Reference)genusName.getNomenclaturalReference(), "5ed27f4a-6c7f-4372-bc5d-3b67636abc52");
        Taxon genus = Taxon.NewInstance(genusName, sec1);
        setUuid(genus,"3f52e136-f2e1-4f9a-9010-2f35908fbd39");

        TaxonNode node2 = node1.addChildTaxon(genus, sec1, "33");
        setUuid(node2, "43ca733b-fe3a-42ce-8a92-000e27badf44");
        nodesToSave.add(node2);

        //species
        TaxonName speciesName = parser.parseReferencedName("Genus species Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SPECIES());
        setUuid(speciesName,"f983cc5e-4c77-4c80-8cb0-73d43df31ee9");
        setUuid((Reference)speciesName.getNomenclaturalReference(), "a0dd7f4a-0c7f-4372-bc5d-3b676363bc0e");
        Taxon species = Taxon.NewInstance(speciesName, sec1);
        setUuid(species,"9182e136-f2e2-4f9a-9010-3f35908fb5e0");

        TaxonNode node3 = node2.addChildTaxon(species, sec1, "33");
        setUuid(node3, "a0c9733a-fe3a-42ce-8a92-000e27bfdfa3");
        nodesToSave.add(node3);

        //subspecies
        TaxonName subspeciesName = parser.parseReferencedName("Genus species subsp. subspec Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(subspeciesName,"3483cc5e-4c77-4c80-8cb0-73d43df31ee3");
        setUuid((Reference)subspeciesName.getNomenclaturalReference(), "b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f");

        Taxon subspecies = Taxon.NewInstance(subspeciesName, sec1);
        setUuid(subspecies, "b2c86698-500e-4efb-b9ae-6bb6e701d4bc");
        TaxonNode node4 = node3.addChildTaxon(subspecies, sec1, "33");
        node4.setUuid(UUID_SUBSPEC_NODE);
        nodesToSave.add(node4);

        //unpublished
        TaxonName unpublishedName = parser.parseReferencedName("Genus species subsp. unpublish Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(unpublishedName,"ebcf6bb6-6da8-46fe-9c22-127aa4cb9549");
        setUuid((Reference)unpublishedName.getNomenclaturalReference(), "51725dbd-e4a1-43ea-8363-fe8eb1152a49");

        Taxon unpublishedSpecies = Taxon.NewInstance(unpublishedName, sec1);
        unpublishedSpecies.setPublish(false);
        setUuid(unpublishedSpecies, UUID_UNPUBLISHED_TAXON);
        TaxonNode nodeUnpublished = node3.addChildTaxon(unpublishedSpecies, sec1, "34");
        setUuid(nodeUnpublished, "01368584-b626-4255-9dc4-ff011d44f493");
        nodesToSave.add(nodeUnpublished);

        classificationService.save(classification);
        taxonNodeService.save(nodesToSave);

        TaxonDescription description = TaxonDescription.NewInstance(species);

        Distribution distribution = Distribution.NewInstance(NamedArea.AFRICA(), PresenceAbsenceTerm.PRESENT());
        setUuid(distribution,"674e9e27-9102-4166-8626-8cb871a9a89b");
        description.addElement(distribution);
        commitAndStartNewTransaction(null);


    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        //      try {
        //      writeDbUnitDataSetFile(new String[] {
        //              "Classification",
        //      }, "testAttachDnaSampleToDerivedUnit");
        //  } catch (FileNotFoundException e) {
        //      e.printStackTrace();
        //  }
    }


    private void setUuid(CdmBase cdmBase, String uuidStr) {
        cdmBase.setUuid(UUID.fromString(uuidStr));
    }

}
