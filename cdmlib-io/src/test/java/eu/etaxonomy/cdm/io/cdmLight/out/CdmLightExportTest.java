/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight.out;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.cdmLight.CdmLightExportConfigurator;
import eu.etaxonomy.cdm.io.cdmLight.CdmLightExportTable;
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
 * @author k.luther
 * @date 17.01.2018
 *
 */
public class CdmLightExportTest extends CdmTransactionalIntegrationTest{

        @SuppressWarnings("unused")
        private static final Logger logger = Logger.getLogger(CdmLightExportTest.class);

        @SpringBeanByName
        private CdmApplicationAwareDefaultExport<CdmLightExportConfigurator> defaultExport;

        @SpringBeanByType
        private IClassificationService classificationService;

        @SpringBeanByType
        private ITermService termService;

        @SpringBeanByType
        private ITaxonNodeService taxonNodeService;

        @Before
        public void setUp()  {
//            DefinedTerm ipniIdentifierTerm = DefinedTerm.NewIdentifierTypeInstance("IPNI Identifier", "IPNI Identifier", "IPNI Identifier");
//            ipniIdentifierTerm.setUuid(DefinedTerm.uuidIpniNameIdentifier);
//
//            DefinedTerm tropicosIdentifierTerm = DefinedTerm.NewIdentifierTypeInstance("Tropicos Identifier", "Tropicos Identifier", "Tropicos Identifier");
//            tropicosIdentifierTerm.setUuid(DefinedTerm.uuidTropicosNameIdentifier);
//
//            DefinedTerm wfoIdentifierTerm = DefinedTerm.NewIdentifierTypeInstance("WFO Identifier", "WFO Identifier", "WFO Identifier");
//            wfoIdentifierTerm.setUuid(DefinedTerm.uuidWfoNameIdentifier);
//            List<DefinedTermBase> terms = new ArrayList();
//            terms.add(wfoIdentifierTerm);
//            terms.add(tropicosIdentifierTerm);
//            terms.add(ipniIdentifierTerm);
//            termService.saveOrUpdate(terms);
            createFullTestDataSet();
        }




        @Test
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
        public void testSubTree(){


            CdmLightExportConfigurator config = new CdmLightExportConfigurator(null);
            config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(UUID.fromString("f8c9933a-fe3a-42ce-8a92-000e27bfdfac")));

            config.setTarget(TARGET.EXPORT_DATA);
            ExportResult result = defaultExport.invoke(config);
            System.out.println(result.createReport());
            ExportDataWrapper<?> exportData = result.getExportData();
            @SuppressWarnings("unchecked")
            Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();
            for (String key : data.keySet()){

                byte[] byt = data.get(key);
                System.out.print(key + ": " + new String(byt) );

            }


            byte[] taxon = data.get(CdmLightExportTable.TAXON.getTableName());
            Assert.assertNotNull("Taxon table must not be null", taxon);
            String taxonStr = new String(taxon);
            String notExpected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\"";
            Assert.assertFalse("Result must not contain root taxon", taxonStr.startsWith(notExpected));
            String expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"4096df99-7274-421e-8843-211b603d832e\",\"DwcaExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
            Assert.assertTrue(taxonStr.contains(expected));

            byte[] reference = data.get(CdmLightExportTable.REFERENCE.getTableName());
            String referenceString = new String(reference);
            Assert.assertNotNull("Reference table must not be null", reference);
            expected ="\"b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f\",\"null (1804)\",\"\",\"The book of botany\",\"1804\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"3\",\"1804\",\"Mill.\"";
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
            expected ="\"Book\",\"The book of botany\",\"Mill., The book of botany 3. 1804\",\"Mill.\",\"Mill.\",\"3:22\",\"3\",\"22\",\"1804\",\"1804\",\"\",\"\",\"\",\"\"";
            Assert.assertTrue(scientificNameString.contains(expected));

            byte[] homotypicGroup = data.get(CdmLightExportTable.HOMOTYPIC_GROUP.getTableName());
            String homotypicGroupString = new String(homotypicGroup);
            Assert.assertNotNull("Reference table must not be null", homotypicGroup);
            expected ="\"Genus species subsp. subspec Mill.\",\"\"";
            Assert.assertTrue(homotypicGroupString.contains(expected));


            config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(UUID.fromString("5ed27f4a-6c7f-4372-bc5d-3b67636abc52")));

            config.setTarget(TARGET.EXPORT_DATA);
            result = defaultExport.invoke(config);
            System.out.println(result.createReport());
            exportData = result.getExportData();

            data = (Map<String, byte[]>) exportData.getExportData();
            for (String key : data.keySet()){

                byte[] byt = data.get(key);
                System.out.print(key + ": " + new String(byt) );

            }

        }

        @Test
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
        public void testFullData(){

            File destinationFolder = null;
            CdmLightExportConfigurator config = new CdmLightExportConfigurator(null);
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
                Assert.assertTrue("There should be 4 references", count == 5);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
//            System.out.println();
            //metadata
//            byte[] metadata = data.get(DwcaTaxExportFile.METADATA.getTableName());
//            Assert.assertNotNull("Metadata must not be null", metadata);
//            String metaDataStr = new String(metadata);
//            String metaHeader = "<?xml version=\"1.0\" ?><archive xmlns=\"http://rs.tdwg.org/dwc/text/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://rs.tdwg.org/dwc/text/ http://rs.tdwg.org/dwc/text/tdwg_dwc_text.xsd\">";
//            Assert.assertTrue(metaDataStr.contains(metaHeader));
//
//            String metaCore = "<core encoding=\"UTF-8\" linesTerminatedBy=\"\r\n\" fieldsEnclosedBy=\"&quot;\""
//                    + " fieldsTerminatedBy=\",\" ignoreHeaderLines=\"1\" rowType=\"http://rs.tdwg.org/dwc/terms/Taxon\">"
//                    + "<files><location>coreTax.txt</location></files><id index=\"0\"></id><field index=\"1\" term=\"http://rs.tdwg.org/dwc/terms/scientificNameID\"></field><field index=\"2\" term=\"http://rs.tdwg.org/dwc/terms/acceptedNameUsageID\"></field><field index=\"3\" term=\"http://rs.tdwg.org/dwc/terms/parentNameUsageID\"></field><field index=\"4\" term=\"http://rs.tdwg.org/dwc/terms/scientificName\"></field><field index=\"5\" term=\"http://rs.tdwg.org/dwc/terms/taxonRank\"></field>"
//                    + "<field index=\"6\" term=\"http://rs.tdwg.org/dwc/terms/taxonomicStatus\"></field><field index=\"7\" term=\"http://rs.tdwg.org/dwc/terms/originalNameUsageID\"></field>"
//                    + "<field index=\"8\" term=\"http://rs.tdwg.org/dwc/terms/nameAccordingToID\"></field>"
//                    + "<field index=\"9\" term=\"http://rs.tdwg.org/dwc/terms/namePublishedInID\"></field><field index=\"10\" term=\"http://rs.tdwg.org/dwc/terms/taxonConceptID\"></field><field index=\"11\" term=\"http://rs.tdwg.org/dwc/terms/acceptedNameUsage\"></field><field index=\"12\" term=\"http://rs.tdwg.org/dwc/terms/parentNameUsage\"></field><field index=\"13\" term=\"http://rs.tdwg.org/dwc/terms/originalNameUsage\"></field><field index=\"14\" term=\"http://rs.tdwg.org/dwc/terms/nameAccordingTo\"></field>"
//                    + "<field index=\"15\" term=\"http://rs.tdwg.org/dwc/terms/namePublishedIn\"></field>"
//
//                    + "<field index=\"16\" term=\"http://rs.tdwg.org/dwc/terms/higherClassification\"></field><field index=\"17\" term=\"http://rs.tdwg.org/dwc/terms/kingdom\"></field><field index=\"18\" term=\"http://rs.tdwg.org/dwc/terms/phylum\"></field><field index=\"19\" term=\"http://rs.tdwg.org/dwc/terms/class\"></field><field index=\"20\" term=\"http://rs.tdwg.org/dwc/terms/order\"></field><field index=\"21\" term=\"http://rs.tdwg.org/dwc/terms/family\"></field><field index=\"22\" term=\"http://rs.tdwg.org/dwc/terms/genus\"></field><field index=\"23\" term=\"http://rs.tdwg.org/dwc/terms/subgenus\"></field>"
//                    + "<field index=\"24\" term=\"http://rs.tdwg.org/ontology/voc/TaxonName#uninomial\"></field>"
//                    + "<field index=\"25\" term=\"http://rs.tdwg.org/ontology/voc/TaxonName#genusPart\"></field><field index=\"26\" term=\"http://rs.tdwg.org/ontology/voc/TaxonName#infragenericEpithet\"></field><field index=\"27\" term=\"http://rs.tdwg.org/dwc/terms/specificEpithet\"></field><field index=\"28\" term=\"http://rs.tdwg.org/dwc/terms/infraspecificEpithet\"></field><field index=\"29\" term=\"http://rs.tdwg.org/dwc/terms/verbatimTaxonRank\"></field><field index=\"30\" term=\"http://rs.tdwg.org/dwc/terms/vernacularName\"></field>"
//
//                    + "<field index=\"31\" term=\"http://rs.tdwg.org/dwc/terms/nomenclaturalCode\"></field><field index=\"32\" term=\"http://rs.tdwg.org/dwc/terms/nomenclaturalStatus\"></field><field index=\"33\" term=\"http://rs.tdwg.org/dwc/terms/taxonRemarks\"></field><field index=\"34\" term=\"http://purl.org/dc/terms/modified\"></field>"
//                    + "<field index=\"35\" term=\"http://purl.org/dc/terms/language\"></field><field index=\"36\" term=\"http://purl.org/dc/terms/rights\"></field><field index=\"37\" term=\"http://purl.org/dc/terms/rightsHolder\"></field><field index=\"38\" term=\"http://purl.org/dc/terms/accessRights\"></field>"
//                    + "<field index=\"39\" term=\"http://purl.org/dc/terms/bibliographicCitation\"></field><field index=\"40\" term=\"http://rs.tdwg.org/dwc/terms/informationWithheld\"></field><field index=\"41\" term=\"http://rs.tdwg.org/dwc/terms/datasetName\"></field><field index=\"42\" term=\"http://purl.org/dc/terms/source\"></field></core>" +
//                    "";
//            //TODO continue
////            System.out.println(metaDataStr);
//            Assert.assertTrue(metaDataStr.contains(metaCore));
//
//            //core
//            byte[] core = data.get(DwcaTaxExportFile.TAXON.getTableName());
//            Assert.assertNotNull("Core must not be null", core);
//            String coreStr = new String(core);
//            String expected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"f983cc5e-4c77-4c80-8cb0-73d43df31ee9\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"3f52e136-f2e1-4f9a-9010-2f35908fbd39\",\"Genus species Mill.\",\"Species\",\"accepted\"";
////            System.out.println(coreStr);
//            Assert.assertTrue(coreStr.contains(expected));
//            String expectedClassification = "\"Family|Genus|Genus species\"";
//            Assert.assertTrue(coreStr.contains(expectedClassification));


            //reference
//            byte[] ref = data.get(DwcaTaxExportFile.REFERENCE.getTableName());
//            Assert.assertNotNull("Reference must not be null", ref);
//            String refStr = new String(ref);
//            expected = "\"3162e136-f2e2-4f9a-9010-3f35908fbae1\",,,,,\"My sec ref\",\"My sec ref\",,,,,,,,,";
////            System.out.println(refStr);
//            Assert.assertTrue(refStr.contains(expected));
//            expected = "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",,,,,\"Mill., The book of botany 3. 1804\",,\"Mill.\",\"1804\",,,,,,,";
//            Assert.assertTrue(refStr.contains(expected));
//
//            //distribution
//            byte[] distribution = data.get(DwcaTaxExportFile.DISTRIBUTION.getTableName());
//            Assert.assertNotNull("Distribution must not be null", distribution);
//            String distributionStr = new String(distribution);
//            expected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"c204c529-d8d2-458f-b939-96f0ebd2cbe8\",\"Africa\",,,\"present\",,\"uncertain\",,,,,,";
//            Assert.assertTrue(distributionStr.contains(expected));

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
            TaxonName familyName = parser.parseReferencedName("Family L., Sp. Pl. 3: 22. 1752",
                    NomenclaturalCode.ICNAFP, Rank.FAMILY());
            setUuid(familyName,"e983cc5e-4c77-4c80-8cb0-73d43df31ef7");
            setUuid((Reference)familyName.getNomenclaturalReference(), "b0dd7f4a-0c7f-4372-bc5d-3b676363bc63");
            Taxon family = Taxon.NewInstance(familyName, sec1);
            setUuid(family,"3162e136-f2e2-4f9a-9010-3f35908fbae1");
            TaxonNode node1 = classification.addChildTaxon(family, sec1, "22");
            setUuid(node1, "0fae5ad5-ffa2-4100-bcd7-8aa9dda0aebc");
            nodesToSave.add(node1);

            TaxonName genusName = parser.parseReferencedName("Genus Humb., The book of botany 3: 22. 1804",
                    NomenclaturalCode.ICNAFP, Rank.GENUS());
            setUuid(genusName,"5e83cc5e-4c77-4d80-8cb0-73d63df35ee3");
            setUuid((Reference)genusName.getNomenclaturalReference(), "5ed27f4a-6c7f-4372-bc5d-3b67636abc52");
            Taxon genus = Taxon.NewInstance(genusName, sec1);
            setUuid(genus,"3f52e136-f2e1-4f9a-9010-2f35908fbd39");

            TaxonNode node2 = node1.addChildTaxon(genus, sec1, "33");
            setUuid(node2, "43ca733b-fe3a-42ce-8a92-000e27badf44");
            nodesToSave.add(node2);


            TaxonName speciesName = parser.parseReferencedName("Genus species Mill., The book of botany 3: 22. 1804",
                    NomenclaturalCode.ICNAFP, Rank.SPECIES());
            setUuid(speciesName,"f983cc5e-4c77-4c80-8cb0-73d43df31ee9");
            setUuid((Reference)speciesName.getNomenclaturalReference(), "a0dd7f4a-0c7f-4372-bc5d-3b676363bc0e");
            Taxon species = Taxon.NewInstance(speciesName, sec1);
            setUuid(species,"9182e136-f2e2-4f9a-9010-3f35908fb5e0");

            TaxonNode node3 = node2.addChildTaxon(species, sec1, "33");
            setUuid(node3, "a0c9733a-fe3a-42ce-8a92-000e27bfdfa3");
            nodesToSave.add(node3);

            TaxonName subspeciesName = parser.parseReferencedName("Genus species subsp. subspec Mill., The book of botany 3: 22. 1804",
                    NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
            setUuid(subspeciesName,"3483cc5e-4c77-4c80-8cb0-73d43df31ee3");
            setUuid((Reference)subspeciesName.getNomenclaturalReference(), "b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f");

            Taxon subspecies = Taxon.NewInstance(subspeciesName, sec1);
            setUuid(subspecies, "b2c86698-500e-4efb-b9ae-6bb6e701d4bc");
            TaxonNode node4 = node3.addChildTaxon(subspecies, sec1, "33");
            setUuid(node4, "f8c9933a-fe3a-42ce-8a92-000e27bfdfac");
            nodesToSave.add(node4);

            classificationService.save(classification);
            taxonNodeService.save(nodesToSave);

            TaxonDescription description = TaxonDescription.NewInstance(species);

            Distribution distribution = Distribution.NewInstance(NamedArea.AFRICA(), PresenceAbsenceTerm.PRESENT());
            setUuid(distribution,"674e9e27-9102-4166-8626-8cb871a9a89b");
            description.addElement(distribution);

            subspecies.addDescription(description);
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
