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
        System.out.println(result.createReport());
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();
        for (String key : data.keySet()){
            byte[] byt = data.get(key);
            System.out.println(key + ": " + new String(byt) );
        }

        //metadata
        byte[] metadata = data.get(DwcaTaxOutputFile.METADATA.getTableName());
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
        byte[] core = data.get(DwcaTaxOutputFile.TAXON.getTableName());
        Assert.assertNull("Core must not exist", core);

        //reference
        byte[] ref = data.get(DwcaTaxOutputFile.REFERENCE.getTableName());
        Assert.assertNull("Reference must not exist", ref);

        //distribution
        byte[] distribution = data.get(DwcaTaxOutputFile.DISTRIBUTION.getTableName());
        Assert.assertNull("Distribution must not exist", distribution);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testSubTree(){
        createFullTestDataSet();
        File destinationFolder = null;
        DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(null, destinationFolder, null);
        Set<UUID> subtreeUuids = new HashSet<>();
        subtreeUuids.add(UUID.fromString("f8c9933a-fe3a-42ce-8a92-000e27bfdfac"));
        config.setSubtreeUuids(subtreeUuids);
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
        System.out.println(result.createReport());
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();
        for (String key : data.keySet()){
            byte[] byt = data.get(key);
            System.out.println(key + ": " + new String(byt) );
        }
        //metadata
        byte[] metadata = data.get(DwcaTaxOutputFile.METADATA.getTableName());
        Assert.assertNotNull("Metadata must not be null", metadata);
        //further tests in fullData

        //core
        byte[] core = data.get(DwcaTaxOutputFile.TAXON.getTableName());
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


        String expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"Genus species subsp. subspec Mill.\",\"Subspecies\",\"accepted\",,\"4b6acca1-959b-4790-b76e-e474a0882990\",\"a0dd7f4a-0c7f-4372-bc5d-3b676363bc0e\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"Genus species subsp. subspec Mill.\",\"Genus species subsp. subspec Mill. sec. My sec ref\",";
        Assert.assertTrue(coreStr.contains(expected));


        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"Genus species subsp. subspec Mill.\",\"Subspecies\",\"accepted\",,\"4b6acca1-959b-4790-b76e-e474a0882990\",\"a0dd7f4a-0c7f-4372-bc5d-3b676363bc0e\",\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"Genus species subsp. subspec Mill.\",\"Genus species subsp. subspec Mill. sec. My sec ref\",,\"My sec ref\",\"Mill., The book of botany 3. 1804\",,\"Genus\",,\"species\",\"subspec\",\"subsp.\",,\"ICNAFP\",,,,,,,,,,\"DwcaExportTest Classificaiton\",";
        Assert.assertTrue(coreStr.contains(expected));

        //distribution
        byte[] distribution = data.get(DwcaTaxOutputFile.DISTRIBUTION.getTableName());
        Assert.assertNull("Distribution must not exist", distribution);

        //reference
        byte[] ref = data.get(DwcaTaxOutputFile.REFERENCE.getTableName());
        Assert.assertNotNull("Reference must not be null", ref);
        String refStr = new String(ref);
        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",,,,,\"My sec ref\",\"My sec ref\",,,,,,,,,";
        Assert.assertTrue(refStr.contains(expected));
        expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",,,,,\"Mill., The book of botany 3. 1804\",,\"Mill.\",\"1804\",,,,,,,";
        Assert.assertTrue(refStr.contains(expected));
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testFullData(){
        createFullTestDataSet();
        File destinationFolder = null;
        DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(null, destinationFolder, null);
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
        System.out.println(result.createReport());
        ExportDataWrapper<?> exportData = result.getExportData();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();
        for (String key : data.keySet()){
            byte[] byt = data.get(key);
            System.out.println(key + ": " + new String(byt) );
        }
        System.out.println();
        //metadata
        byte[] metadata = data.get(DwcaTaxOutputFile.METADATA.getTableName());
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
                + "<field index=\"16\" term=\"http://rs.tdwg.org/ontology/voc/TaxonName#uninomial\"></field>"
                + "<field index=\"17\" term=\"http://rs.tdwg.org/ontology/voc/TaxonName#genusPart\"></field><field index=\"18\" term=\"http://rs.tdwg.org/ontology/voc/TaxonName#infragenericEpithet\"></field><field index=\"19\" term=\"http://rs.tdwg.org/dwc/terms/specificEpithet\"></field><field index=\"20\" term=\"http://rs.tdwg.org/dwc/terms/infraspecificEpithet\"></field><field index=\"21\" term=\"http://rs.tdwg.org/dwc/terms/verbatimTaxonRank\"></field><field index=\"22\" term=\"http://rs.tdwg.org/dwc/terms/vernacularName\"></field>"
                + "<field index=\"23\" term=\"http://rs.tdwg.org/dwc/terms/nomenclaturalCode\"></field><field index=\"24\" term=\"http://rs.tdwg.org/dwc/terms/nomenclaturalStatus\"></field><field index=\"25\" term=\"http://rs.tdwg.org/dwc/terms/taxonRemarks\"></field><field index=\"26\" term=\"http://purl.org/dc/terms/modified\"></field>"
                + "<field index=\"27\" term=\"http://purl.org/dc/terms/language\"></field><field index=\"28\" term=\"http://purl.org/dc/terms/rights\"></field><field index=\"29\" term=\"http://purl.org/dc/terms/rightsHolder\"></field><field index=\"30\" term=\"http://purl.org/dc/terms/accessRights\"></field>"
                + "<field index=\"31\" term=\"http://purl.org/dc/terms/bibliographicCitation\"></field><field index=\"32\" term=\"http://rs.tdwg.org/dwc/terms/informationWithheld\"></field><field index=\"33\" term=\"http://rs.tdwg.org/dwc/terms/datasetName\"></field><field index=\"34\" term=\"http://purl.org/dc/terms/source\"></field></core>";
        //TODO continue
        System.out.println(metaDataStr);
        Assert.assertTrue(metaDataStr.contains(metaCore));

        //core
        byte[] core = data.get(DwcaTaxOutputFile.TAXON.getTableName());
        Assert.assertNotNull("Core must not be null", core);
        String coreStr = new String(core);
        String expected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",,\"Genus species subsp. subspec Mill.\",\"Subspecies\",\"accepted\"";
        Assert.assertTrue(coreStr.contains(expected));


        //reference
        byte[] ref = data.get(DwcaTaxOutputFile.REFERENCE.getTableName());
        Assert.assertNotNull("Reference must not be null", ref);
        String refStr = new String(ref);
        expected = "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",,,,,\"My sec ref\",\"My sec ref\",,,,,,,,,";
        Assert.assertTrue(refStr.contains(expected));
        expected = "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",,,,,\"Mill., The book of botany 3. 1804\",,\"Mill.\",\"1804\",,,,,,,";
        Assert.assertTrue(refStr.contains(expected));

        //distribution
        byte[] distribution = data.get(DwcaTaxOutputFile.DISTRIBUTION.getTableName());
        Assert.assertNotNull("Distribution must not be null", distribution);
        String distributionStr = new String(distribution);
        expected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"c204c529-d8d2-458f-b939-96f0ebd2cbe8\",\"Africa\",,,\"present\",,\"uncertain\",,,,,,";
        Assert.assertTrue(distributionStr.contains(expected));



    }

    /**
     * {@inheritDoc}
     */
    public void createFullTestDataSet() {
        Set<TaxonNode> nodesToSave = new HashSet<>();
        Reference sec1 = ReferenceFactory.newGeneric();
        setUuid(sec1, "4b6acca1-959b-4790-b76e-e474a0882990");
        sec1.setTitle("My sec ref");

        Classification classification = Classification.NewInstance("DwcaExportTest Classificaiton");
        setUuid(classification, "4096df99-7274-421e-8843-211b603d832e");

        NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
        TaxonName name1 = parser.parseReferencedName("Genus species subsp. subspec Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(name1,"f983cc5e-4c77-4c80-8cb0-73d43df31ee9");
        setUuid((Reference)name1.getNomenclaturalReference(), "a0dd7f4a-0c7f-4372-bc5d-3b676363bc0e");
        Taxon root = Taxon.NewInstance(name1, sec1);
        setUuid(root,"9182e136-f2e2-4f9a-9010-3f35908fb5e0");

        TaxonNode node1 = classification.addChildTaxon(root, sec1, "22");
        setUuid(node1, "0fae5ad5-ffa2-4100-bcd7-8aa9dda0aebc");
        nodesToSave.add(node1);

        Taxon child = Taxon.NewInstance(name1, sec1);
        setUuid(child, "b2c86698-500e-4efb-b9ae-6bb6e701d4bc");
        TaxonNode node2 = node1.addChildTaxon(child, sec1, "33");
        setUuid(node2, "f8c9933a-fe3a-42ce-8a92-000e27bfdfac");
        nodesToSave.add(node2);

        classificationService.save(classification);
        taxonNodeService.save(nodesToSave);

        TaxonDescription description = TaxonDescription.NewInstance(root);

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
