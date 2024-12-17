/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.out;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * Base class for taxon tree table based exports such as CDM light or COL-DP.
 *
 * @author a.mueller
 * @date 20.07.2023
 */
public abstract class TaxonTreeExportTestBase
            <CONFIG extends TaxonTreeExportConfiguratorBase<STATE,CONFIG>,STATE extends TaxonTreeExportStateBase<CONFIG,STATE>>
        extends CdmTransactionalIntegrationTest{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    protected static final String NONE_END  = "\"\""; //empty entry without separator to next entry
    protected static final String NONE  = NONE_END + ","; //empty entry
    protected static final String NONE2  = NONE + NONE; //2 empty entries
    protected static final String NONE3  = NONE2 + NONE; //3 empty entries
    protected static final String NONE4  = NONE2 + NONE2; //4 empty entries
    protected static final String NONE9  = NONE4 + NONE4 + NONE; //7 empty entries
    protected static final String NONE10  = NONE9 + NONE; //10 empty entries
    protected static final String NONE20  = NONE10 + NONE10; //20 empty entries
    protected static final String FALSE  = "\"0\","; //false
    protected static final String TRUE  = "\"1\","; //true
    protected static final String BOOL_NULL = NONE;  //boolean null
    protected static final String VALID = "\"acceptable\",";  //name status valid

    protected static final int COUNT_HEADER = 1;

    protected static final UUID classificationUuid = UUID.fromString("4096df99-7274-421e-8843-211b603d832e");

    //taxon node uuid
    protected static final UUID rootNodeUuid = UUID.fromString("a67b4efd-6148-46a9-a377-1efd14768cfa");
    protected static final UUID node1Uuid = UUID.fromString("0fae5ad5-ffa2-4100-bcd7-8aa9dda0aebc");
    protected static final UUID node2Uuid = UUID.fromString("43ca733b-fe3a-42ce-8a92-000e27badf44");
    protected static final UUID node3Uuid = UUID.fromString("a0c9733a-fe3a-42ce-8a92-000e27bfdfa3");
    protected static final UUID node4Uuid = UUID.fromString("f8c9933a-fe3a-42ce-8a92-000e27bfdfac");
    protected static final UUID node5Uuid = UUID.fromString("81d9c9b2-c8fd-4d4f-a0b4-e7e656dcdc20");

    //taxon uuid
    protected static final UUID familyTaxonUuid = UUID.fromString("3162e136-f2e2-4f9a-9010-3f35908fbae1");
    protected static final UUID genusTaxonUuid = UUID.fromString("3f52e136-f2e1-4f9a-9010-2f35908fbd39");
    protected static final UUID speciesTaxonUuid = UUID.fromString("9182e136-f2e2-4f9a-9010-3f35908fb5e0");
    protected static final UUID basionymSynonymUuid = UUID.fromString("08dfb25d-2283-42d6-9711-45656d988f4c");
    protected static final UUID subspeciesTaxonUuid = UUID.fromString("b2c86698-500e-4efb-b9ae-6bb6e701d4bc");
    protected static final UUID subspeciesUnpublishedTaxonUuid = UUID.fromString("290e295a-9089-4616-a30c-15ded79e064f");

    //name uuid
    protected static final UUID familyNameUuid = UUID.fromString("e983cc5e-4c77-4c80-8cb0-73d43df31ef7");
    protected static final UUID genusNameUuid = UUID.fromString("5e83cc5e-4c77-4d80-8cb0-73d63df35ee3");
    protected static final UUID speciesNameUuid = UUID.fromString("f983cc5e-4c77-4c80-8cb0-73d43df31ee9");
    protected static final UUID subspeciesNameUuid = UUID.fromString("3483cc5e-4c77-4c80-8cb0-73d43df31ee3");
    protected static final UUID subspeciesUnpublishedNameUUID = UUID.fromString("b6da7ab2-6c67-44b7-9719-2557542f5a23");
    protected static final UUID basionymNameUuid = UUID.fromString("c7962b1f-950e-4e28-a3d1-aa0583dfdc92");
    protected static final UUID origSpellingNameUuid = UUID.fromString("bde06b48-bbda-428c-af4e-50c39db55821");
    protected static final UUID earlierHomonymUuid = UUID.fromString("ad8aebe1-3980-4d0b-83c1-2f4e9c00284a");
    protected static final UUID earlierHomonymBasionymUuid = UUID.fromString("c098e6d3-fbd2-4412-94e0-30bccaf74059");

    //HG uuid
    protected static final UUID subspeciesNameHgUuid = UUID.fromString("c60c0ce1-0fa0-468a-9908-8e9afed05714");

    //WFO IDs
    protected static final String familyWfoId = "WFO-12347f";
    protected static final String speciesWfoId = "WFO-123477";
    protected static final String speciesBasionymWfoId = "WFO-123457b";
    protected static final String subspeciesWfoId = "WFO-12347ss";
    protected static final String subspeciesUnpublishedWfoId = "WFO-12347uss";
    protected static final String speciesOrigSpellingWfoId = "WFO-123477os";

    //reference uuid
    protected static final UUID familyNomRefUuid = UUID.fromString("b0dd7f4a-0c7f-4372-bc5d-3b676363bc63");
    protected static final UUID genusNomRefUuid = UUID.fromString("5ed27f4a-6c7f-4372-bc5d-3b67636abc52");
    protected static final UUID speciesNomRefUuid = UUID.fromString("a0dd7f4a-0c7f-4372-bc5d-3b676363bc0e");
    protected static final UUID subspeciesNomRefUuid = UUID.fromString("b8dd7f4a-0c7f-4372-bc5d-3b676363bc0f");
    protected static final UUID ref1UUID = UUID.fromString("4b6acca1-959b-4790-b76e-e474a0882990");
    protected static final UUID ref2UUID = UUID.fromString("b70e6ff1-c559-4b4c-860d-f035fce936b1");
    protected static final UUID ref3UUID = UUID.fromString("fa2a13e2-140b-44ef-8eec-3cffdda36e44");

    //facts uuid
    protected static final UUID distributionArmeniaUuid = UUID.fromString("674e9e27-9102-4166-8626-8cb871a9a89b");
    protected static final UUID commonNameTanneUuid = UUID.fromString("81c7b7db-e12b-45fe-96ed-dab940043232");

    //specimen uuid
    protected static final UUID specimenUuid = UUID.fromString("cafca31a-e0ab-489c-8add-602fd26a408f");

    @SpringBeanByName
    protected CdmApplicationAwareDefaultExport<CONFIG> defaultExport;

    @SpringBeanByType
    protected IClassificationService classificationService;

    @SpringBeanByType
    protected ITaxonNodeService taxonNodeService;

    @SpringBeanByType
    protected ICommonService commonService;

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
        CONFIG config = newConfigurator();
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);

        //test exceptions
        testExceptionsErrorsWarnings(result);
    }

    protected abstract CONFIG newConfigurator();

    protected void testExceptionsErrorsWarnings(ExportResult result) {
        testExceptionsErrorsWarnings(result, 0, 0, 0);
    }

    protected void testExceptionsErrorsWarnings(ExportResult result, int exceptions, int errors,int warnings) {
        if (result.getExceptions().size() > 0 && exceptions == 0) {
            result.getExceptions().iterator().next().getException().printStackTrace();
        }
        Assert.assertEquals("The number of exceptions differs from the expected number of exceptions",
                exceptions, result.getExceptions().size());
        Assert.assertEquals("The number of errors differs from the expected number of errors",
                errors, result.getErrors().size());
        Assert.assertEquals("The number of warnings differs from the expected number of warnings",
                warnings, result.getWarnings().size());
    }

    protected void setUuid(CdmBase cdmBase, String uuidStr) {
        cdmBase.setUuid(UUID.fromString(uuidStr));
    }

    protected void setUuid(CdmBase cdmBase, UUID uuid) {
        cdmBase.setUuid(uuid);
    }

    protected String uuid(UUID uuid) {
        return "\"" + uuid + "\",";
    }

    protected String str(String str) {
        return strEnd(str)+",";
    }

    protected String strEnd(String str) {
        return "\"" + str + "\"";
    }

    protected String getLine(List<String> list, UUID uuid) {
        for (String line : list) {
            if (line.startsWith("\""+ uuid.toString())) {
                return line;
            }
        }
        return null;
    }

    protected String getLine(List<String> list, String str) {
        for (String line : list) {
            if (line.startsWith("\""+ str +"\"")) {
                return line;
            }
        }
        return null;
    }

    protected List<String> getStringList(Map<String, byte[]> data, ITaxonTreeExportTable table) {
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

    protected String getTableString(Map<String, byte[]> data, ITaxonTreeExportTable table) {
        byte[] tableByte = data.get(table.getTableName());
        Assert.assertNotNull(table.getTableName() + " table must not be null", tableByte);
        return new String(tableByte);
    }

    protected Map<String, byte[]> checkAndGetData(ExportResult result) {

        //test exceptions
        testExceptionsErrorsWarnings(result);

        //transform to data map
        ExportDataWrapper<?> exportData = result.getExportData();
        Assert.assertNotNull("Export data must not be null", exportData);

        @SuppressWarnings("unchecked")
        Map<String, byte[]> data = (Map<String, byte[]>) exportData.getExportData();

        return data;
    }

    protected void print(List<String> resultsToPrint) {
        System.out.println(resultsToPrint);
    }

    protected void createFullTestDataSet() {

        Set<TaxonNode> nodesToSave = new HashSet<>();
        NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();

        //sec ref
        Reference sec1 = ReferenceFactory.newGeneric();
        setUuid(sec1, ref1UUID);
        sec1.setTitle("My sec ref");


        Reference ref1 = ReferenceFactory.newGeneric();
        setUuid(ref1, ref2UUID);
        ref1.setTitle("My first ref");
        ref1.setAuthorship(Person.NewTitledInstance("Author"));
        ref1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1980));

        Reference ref2 = ReferenceFactory.newGeneric();
        setUuid(ref2, ref3UUID);
        ref2.setTitle("My second ref");
        ref2.setAuthorship(Person.NewTitledInstance("Author"));
        ref2.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1980));

        //classification
        Classification classification = Classification.NewInstance("CdmLightExportTest Classification");
        setUuid(classification, classificationUuid);
        setUuid(classification.getRootNode(), rootNodeUuid);

        //family
        TaxonName familyName = parser.parseReferencedName("Familyname L., Sp. Pl. 3: 22. 1752",
                NomenclaturalCode.ICNAFP, Rank.FAMILY());
        familyName.addStatus(NomenclaturalStatusType.CONSERVED(), null, null);
        addWfoIdentifier(familyName, familyWfoId);
        setUuid(familyName, familyNameUuid);
        setUuid(familyName.getNomenclaturalReference(), familyNomRefUuid);
        Taxon family = Taxon.NewInstance(familyName, sec1);
        setUuid(family, familyTaxonUuid);
        TaxonNode node1 = classification.addChildTaxon(family, sec1, "22");
        setUuid(node1, node1Uuid.toString());
        nodesToSave.add(node1);

        //genus
        TaxonName genusName = parser.parseReferencedName("Genus Humb., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.GENUS());
        addWfoIdentifier(genusName, "WFO-12347g");
        setUuid(genusName, genusNameUuid);
        setUuid(genusName.getNomenclaturalReference(), genusNomRefUuid);
        Taxon genus = Taxon.NewInstance(genusName, ref1);
        setUuid(genus, genusTaxonUuid);

        TaxonNode node2 = node1.addChildTaxon(genus, sec1, "33");
        setUuid(node2, node2Uuid);
        nodesToSave.add(node2);

        //species
        TaxonName speciesName = parser.parseReferencedName("Genus species (Mill.) Hook in J. Appl. Synon. 5: 33. 1824",
                NomenclaturalCode.ICNAFP, Rank.SPECIES());
        addWfoIdentifier(speciesName, speciesWfoId);
        setUuid(speciesName, speciesNameUuid);
        setUuid(speciesName.getNomenclaturalReference(), speciesNomRefUuid);
        Taxon species = Taxon.NewInstance(speciesName, ref2);
        setUuid(species, speciesTaxonUuid);
        TaxonNode node3 = node2.addChildTaxon(species, sec1, "33");
        setUuid(node3, node3Uuid.toString());
        nodesToSave.add(node3);

        //species basionym
        TaxonName basionymName = parser.parseReferencedName("Sus basionus Mill., The book of botany 3: 22. 1804", NomenclaturalCode.ICNAFP, Rank.SPECIES());
        basionymName.setUuid(basionymNameUuid);
        addWfoIdentifier(basionymName, speciesBasionymWfoId);
        Synonym basionymSynonym = species.addBasionymSynonym(basionymName, species.getSec(), "67");
        basionymSynonym.setUuid(basionymSynonymUuid);

        //heterotypic, illegal synonym
        TaxonName laterHomonymName = parser.parseReferencedName("Pus illegitimus Late, The later book: 15. 1908", NomenclaturalCode.ICNAFP, Rank.SPECIES());
        addWfoIdentifier(laterHomonymName, "wfo-333888");
        @SuppressWarnings("unused")
        Synonym laterHomonymSynonym = species.addHeterotypicSynonymName(laterHomonymName);

        TaxonName earlierHomonymName = parser.parseReferencedName("Pus illegitimus (Mus) Earl., The earlier book: 1. 1858", NomenclaturalCode.ICNAFP, Rank.SPECIES());
        earlierHomonymName.setUuid(earlierHomonymUuid);
        addWfoIdentifier(earlierHomonymName, "wfo-111222");
        laterHomonymName.addRelationshipToName(earlierHomonymName, NameRelationshipType.LATER_HOMONYM());

        TaxonName earlierHomonymBasionymName = parser.parseReferencedName("Basio illegitimus Mus, The earliest book: 2. 1854", NomenclaturalCode.ICNAFP, Rank.SPECIES());
        earlierHomonymName.addBasionym(earlierHomonymBasionymName);
        earlierHomonymBasionymName.setUuid(earlierHomonymBasionymUuid);
        commonService.save(earlierHomonymName);
        commonService.save(earlierHomonymBasionymName);

        //original spelling
        TaxonName speciesOrigSpelling = parser.parseReferencedName("Sus basyonus", NomenclaturalCode.ICNAFP, Rank.SPECIES());
        speciesOrigSpelling.setUuid(origSpellingNameUuid);
        addWfoIdentifier(speciesOrigSpelling, speciesOrigSpellingWfoId);
        basionymName.getNomenclaturalSource().setNameUsedInSource(speciesOrigSpelling);

        //unpublished species synonym
        TaxonName synonymName = parser.parseReferencedName("Genus synonym Mill., The book of botany 4: 23. 1805", NomenclaturalCode.ICNAFP, Rank.SPECIES());
        setUuid(synonymName, "1584157b-5c43-4150-b271-95b2c99377b2");
        addWfoIdentifier(synonymName, "WFO-12347us");

        Synonym synonymUnpublished = Synonym.NewInstance(synonymName, sec1);
        setUuid(synonymUnpublished, "a87c16b7-8299-4d56-a682-ce20973428ea");
        synonymUnpublished.setPublish(false);
        species.addHomotypicSynonym(synonymUnpublished);

        //subspecies
        TaxonName subspeciesName = parser.parseReferencedName("Genus species subsp. subspec Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(subspeciesName, subspeciesNameUuid);
        setUuid(subspeciesName.getNomenclaturalReference(), subspeciesNomRefUuid);
        setUuid(subspeciesName.getHomotypicalGroup(), subspeciesNameHgUuid);
        addWfoIdentifier(subspeciesName, subspeciesWfoId);

        Taxon subspecies = Taxon.NewInstance(subspeciesName, sec1);
        subspecies.getSecSource().setNameUsedInSource(subspeciesName);
        setUuid(subspecies, subspeciesTaxonUuid);
        TaxonNode node4 = node3.addChildTaxon(subspecies, sec1, "33");
        setUuid(node4, node4Uuid);
        nodesToSave.add(node4);

        //unpublished subspecies
        TaxonName subspeciesNameUnpublished = parser.parseReferencedName("Genus species subsp. unpublished Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        setUuid(subspeciesNameUnpublished, subspeciesUnpublishedNameUUID);
        addWfoIdentifier(subspeciesNameUnpublished, subspeciesUnpublishedWfoId);

        Taxon subspeciesUnpublished = Taxon.NewInstance(subspeciesNameUnpublished, sec1);
        setUuid(subspeciesUnpublished, subspeciesUnpublishedTaxonUuid);
        subspeciesUnpublished.setPublish(false);
        TaxonNode node5 = node3.addChildTaxon(subspeciesUnpublished, sec1, "33");
        //... excluded node
        setUuid(node5, node5Uuid.toString());
        node5.setStatus(TaxonNodeStatus.EXCLUDED);
        node5.setCitation(sec1);
        node5.setCitationMicroReference("27");
        node5.putPlacementNote(Language.ENGLISH(), "My status note");
        nodesToSave.add(node5);

        //save
        classificationService.save(classification);
        taxonNodeService.save(nodesToSave);

        //add Armenia distribution to subspecies //TODO why after save?
        TaxonDescription description = TaxonDescription.NewInstance(subspecies);
        Distribution distribution = Distribution.NewInstance(Country.ARMENIA(), PresenceAbsenceTerm.PRESENT());
        setUuid(distribution, distributionArmeniaUuid);
        description.addElement(distribution);

        //add common name to species
        TaxonDescription description2 = TaxonDescription.NewInstance(species);
        CommonTaxonName commonName = CommonTaxonName.NewInstance("Tanne", Language.GERMAN());
        setUuid(commonName, commonNameTanneUuid);
        description2.addElement(commonName);

        //add media
        TaxonDescription subspeciesImageGallery = TaxonDescription.NewInstance(subspecies, true);
        TextData mediaHolder = TextData.NewInstance(Feature.IMAGE());
        subspeciesImageGallery.addElement(mediaHolder);
        Media media = Media.NewInstance(URI.create("https://www.abc.de/fghi.jpg"), 10034, "image/jpg", "jpg");
        media.setMediaCreated(TimePeriodParser.parseString("2023-07-20"));  //TODO period or freetext
        media.setTitleCache("My nice image", true);
        mediaHolder.addMedia(media);

        //add specimen type to species name
        DerivedUnitFacade facade = DerivedUnitFacade.NewPreservedSpecimenInstance();
        facade.setCountry(Country.ARMENIA());
        facade.setLocality("Somewhere in the forest");
        try {
            facade.setExactLocationByParsing("15°13'12''W", "55°33'22''N", ReferenceSystem.WGS84(), 5);
        } catch (ParseException e) {
            Assert.fail();
        }
        Team collector = Team.NewTitledInstance("Collector team", "Coll. team");
        facade.setCollector(collector);
        facade.setFieldNumber("CT222");
        facade.setAccessionNumber("A555");
        Collection berlinCollection = Collection.NewInstance("B", "Berlin Collection");
        facade.setCollection(berlinCollection);
        DerivedUnit specimen = facade.innerDerivedUnit();
        specimen.setUuid(specimenUuid);
        speciesName.addSpecimenTypeDesignation(specimen, SpecimenTypeDesignationStatus.HOLOTYPE(),
                null, null, null, false, false);

        //add name type
        genusName.addNameTypeDesignation(basionymName, null, null, null, NameTypeDesignationStatus.ORIGINAL_DESIGNATION(), false);

        //TODO textual type


        //commit
        commitAndStartNewTransaction(null);
    }

    private void addWfoIdentifier(TaxonName synonymName, String identifier) {
        synonymName.addIdentifier(identifier, IdentifierType.IDENTIFIER_NAME_WFO());
    }
}