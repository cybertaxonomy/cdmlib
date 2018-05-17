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
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author k.luther
 * @since 17.01.2018
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
            ExportDataWrapper<?> exportData = result.getExportData();
            @SuppressWarnings("unchecked")
            Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();


            byte[] taxon = data.get(CdmLightExportTable.TAXON.getTableName());
            Assert.assertNotNull("Taxon table must not be null", taxon);
            String taxonStr = new String(taxon);
            String notExpected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\"";
            Assert.assertFalse("Result must not contain root taxon", taxonStr.startsWith(notExpected));
            String expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"4096df99-7274-421e-8843-211b603d832e\",\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
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

        }

        @Test
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
        public void testFullTreeWithUnpublished(){

            CdmLightExportConfigurator config = new CdmLightExportConfigurator(null);
            config.setTarget(TARGET.EXPORT_DATA);
            config.getTaxonNodeFilter().setIncludeUnpublished(true);
            ExportResult result = defaultExport.invoke(config);
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
                Assert.assertTrue("There should be 5 taxa", count == 6);// 6 because of the header line

                stream = new ByteArrayInputStream(data.get(CdmLightExportTable.REFERENCE.getTableName()));
                reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
                count = 0;
                while ((line = reader.readLine()) != null) {
                    count ++;
                }
                Assert.assertTrue("There should be 6 references", count == 7);
                stream = new ByteArrayInputStream(data.get(CdmLightExportTable.SYNONYM.getTableName()));
                reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
                count = 0;
                while ((line = reader.readLine()) != null) {
                    count ++;
                }
                Assert.assertTrue("There should be 1 synonym", count == 2);
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail("IO Exception thrown during test.");
            }
            byte[] taxon = data.get(CdmLightExportTable.TAXON.getTableName());
            Assert.assertNotNull("Taxon table must not be null", taxon);
            String taxonStr = new String(taxon);
            String notExpected =  "\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\"";
            Assert.assertFalse("Result must not contain root taxon", taxonStr.startsWith(notExpected));
            String expected = "\"b2c86698-500e-4efb-b9ae-6bb6e701d4bc\",\"4096df99-7274-421e-8843-211b603d832e\",\"CdmLightExportTest Classification\",\"3483cc5e-4c77-4c80-8cb0-73d43df31ee3\",\"9182e136-f2e2-4f9a-9010-3f35908fb5e0\",\"4b6acca1-959b-4790-b76e-e474a0882990\",\"My sec ref\"";
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

        }

        @Test
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
        public void testFullData(){

            CdmLightExportConfigurator config = new CdmLightExportConfigurator(null);
            config.setTarget(TARGET.EXPORT_DATA);

            ExportResult result = defaultExport.invoke(config);
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
                Assert.assertTrue("There should be 4 references", count == 5);
                try{
                    stream = new ByteArrayInputStream(data.get(CdmLightExportTable.SYNONYM.getTableName()));
                    Assert.fail("There should not be a synonym table, because the only synonym is not public.");
                }catch(NullPointerException e){
                    //OK, should be thrown
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void createFullTestDataSet() {
            Set<TaxonNode> nodesToSave = new HashSet<>();

            Reference sec1 = ReferenceFactory.newGeneric();
            setUuid(sec1, "4b6acca1-959b-4790-b76e-e474a0882990");
            sec1.setTitle("My sec ref");

            Classification classification = Classification.NewInstance("CdmLightExportTest Classification");
            setUuid(classification, "4096df99-7274-421e-8843-211b603d832e");

            NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
            TaxonName familyName = parser.parseReferencedName("Family L., Sp. Pl. 3: 22. 1752",
                    NomenclaturalCode.ICNAFP, Rank.FAMILY());
            setUuid(familyName,"e983cc5e-4c77-4c80-8cb0-73d43df31ef7");
            setUuid(familyName.getNomenclaturalReference(), "b0dd7f4a-0c7f-4372-bc5d-3b676363bc63");
            Taxon family = Taxon.NewInstance(familyName, sec1);
            setUuid(family,"3162e136-f2e2-4f9a-9010-3f35908fbae1");
            TaxonNode node1 = classification.addChildTaxon(family, sec1, "22");
            setUuid(node1, "0fae5ad5-ffa2-4100-bcd7-8aa9dda0aebc");
            nodesToSave.add(node1);

            TaxonName genusName = parser.parseReferencedName("Genus Humb., The book of botany 3: 22. 1804",
                    NomenclaturalCode.ICNAFP, Rank.GENUS());
            setUuid(genusName,"5e83cc5e-4c77-4d80-8cb0-73d63df35ee3");
            setUuid(genusName.getNomenclaturalReference(), "5ed27f4a-6c7f-4372-bc5d-3b67636abc52");
            Taxon genus = Taxon.NewInstance(genusName, sec1);
            setUuid(genus,"3f52e136-f2e1-4f9a-9010-2f35908fbd39");

            TaxonNode node2 = node1.addChildTaxon(genus, sec1, "33");
            setUuid(node2, "43ca733b-fe3a-42ce-8a92-000e27badf44");
            nodesToSave.add(node2);


            TaxonName speciesName = parser.parseReferencedName("Genus species Mill., The book of botany 3: 22. 1804",
                    NomenclaturalCode.ICNAFP, Rank.SPECIES());
            setUuid(speciesName,"f983cc5e-4c77-4c80-8cb0-73d43df31ee9");
            setUuid(speciesName.getNomenclaturalReference(), "a0dd7f4a-0c7f-4372-bc5d-3b676363bc0e");
            Taxon species = Taxon.NewInstance(speciesName, sec1);
            setUuid(species,"9182e136-f2e2-4f9a-9010-3f35908fb5e0");
            TaxonName synonymName = parser.parseReferencedName("Genus synonym Mill., The book of botany 3: 22. 1804", NomenclaturalCode.ICNAFP, Rank.SPECIES());

            setUuid(synonymName, "1584157b-5c43-4150-b271-95b2c99377b2");
            Synonym  synonymUnpublished = Synonym.NewInstance(synonymName, sec1);
            setUuid(synonymName, "a87c16b7-8299-4d56-a682-ce20973428ea");
            synonymUnpublished.setPublish(false);
            species.addHomotypicSynonym(synonymUnpublished);
            TaxonNode node3 = node2.addChildTaxon(species, sec1, "33");
            setUuid(node3, "a0c9733a-fe3a-42ce-8a92-000e27bfdfa3");
            nodesToSave.add(node3);

            TaxonName subspeciesName = parser.parseReferencedName("Genus species subsp. subspec Mill., The book of botany 3: 22. 1804",
                    NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
            setUuid(subspeciesName,"3483cc5e-4c77-4c80-8cb0-73d43df31ee3");
            setUuid(subspeciesName.getNomenclaturalReference(), "b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f");

            Taxon subspecies = Taxon.NewInstance(subspeciesName, sec1);
            setUuid(subspecies, "b2c86698-500e-4efb-b9ae-6bb6e701d4bc");
            TaxonNode node4 = node3.addChildTaxon(subspecies, sec1, "33");
            setUuid(node4, "f8c9933a-fe3a-42ce-8a92-000e27bfdfac");
            nodesToSave.add(node4);

            TaxonName subspeciesNameUnpublished = parser.parseReferencedName("Genus species subsp. unpublished Mill., The book of botany 3: 22. 1804",
                    NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
            setUuid(subspeciesNameUnpublished,"b6da7ab2-6c67-44b7-9719-2557542f5a23");

            Taxon subspeciesUnpublished = Taxon.NewInstance(subspeciesNameUnpublished, sec1);
            setUuid(subspeciesUnpublished, "290e295a-9089-4616-a30c-15ded79e064f");
            subspeciesUnpublished.setPublish(false);
            TaxonNode node5 = node3.addChildTaxon(subspeciesUnpublished, sec1, "33");
            setUuid(node5, "81d9c9b2-c8fd-4d4f-a0b4-e7e656dcdc20");
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
