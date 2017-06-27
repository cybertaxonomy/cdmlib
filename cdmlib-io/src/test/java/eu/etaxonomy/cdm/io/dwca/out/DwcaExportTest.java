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
import java.util.Map;
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
import eu.etaxonomy.cdm.io.dwca.in.DwcaImportIntegrationTest;
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
    private static final Logger logger = Logger.getLogger(DwcaImportIntegrationTest.class);

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
        //metadata
        byte[] metadata = data.get(DwcaTaxOutputFile.METADATA.getTableName());
        Assert.assertNotNull("Metadata must not be null", metadata);
        String metaDataStr = new String(metadata);
        String metaHeader = "<?xml version=\"1.0\" ?><archive xmlns=\"http://rs.tdwg.org/dwc/text/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://rs.tdwg.org/dwc/text/ http://rs.tdwg.org/dwc/text/tdwg_dwc_text.xsd\">";
        Assert.assertTrue(metaDataStr.contains(metaHeader));
        //unclear if fields terminated should be really: fieldsTerminatedBy=\"\r\n\"
        String metaCore = "<core encoding=\"UTF-8\" linesTerminatedBy=\"\r\n\" fieldsEnclosedBy=\"&quot;\""
                + " fieldsTerminatedBy=\"\r\n\" ignoreHeaderLines=\"1\" rowType=\"http://rs.tdwg.org/dwc/terms/Taxon\">"
                + "<files><location>coreTax.txt</location></files><id index=\"0\"></id>"
                + "<field index=\"1\" term=\"http://rs.tdwg.org/dwc/terms/scientificNameID\"></field>"
                + "<field index=\"2\" term=\"http://rs.tdwg.org/dwc/terms/acceptedNameUsageID\"></field>"
                + "<field index=\"3\" term=\"http://rs.tdwg.org/dwc/terms/parentNameUsageID\"></field>"
                + "<field index=\"4\" term=\"http://rs.tdwg.org/dwc/terms/scientificName\"></field>"
                + "<field index=\"5\" term=\"http://rs.tdwg.org/dwc/terms/taxonRank\"></field>"
                + "<field index=\"6\" term=\"http://rs.tdwg.org/dwc/terms/taxonomicStatus\"></field>"
                + "<field index=\"7\" term=\"http://purl.org/dc/terms/bibliographicCitation\">";
        //TODO continue
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
        Reference sec1 = ReferenceFactory.newGeneric();
        setUuid(sec1, "4b6acca1-959b-4790-b76e-e474a0882990");
        sec1.setTitle("My sec ref");

        Classification classification = Classification.NewInstance("DwcaExportTest Classificaiton");
        setUuid(classification, "4096df99-7274-421e-8843-211b603d832e");

        NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
        TaxonName name1 = parser.parseReferencedName("Genus species subsp. subspec Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(name1,"f983cc5e-4c77-4c80-8cb0-73d43df31ee9");
        Taxon taxon = Taxon.NewInstance(name1, sec1);
        setUuid(taxon,"9182e136-f2e2-4f9a-9010-3f35908fb5e0");

        TaxonNode node1 = classification.addChildTaxon(taxon, sec1, "22");

        classificationService.save(classification);
        taxonNodeService.save(node1);

        TaxonDescription description = TaxonDescription.NewInstance(taxon);

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
