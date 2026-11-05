/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportTestBase;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;

/**
 * Test class for COL-DP export.
 * Note: this test was originally copied from CDM-light export test.
 *
 * @author a.mueller
 * @date 26.01.2024
 */
public class WfoBackboneExportTest
        extends TaxonTreeExportTestBase<WfoBackboneExportConfigurator,WfoBackboneExportState> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    //still missing: TODO
    private String expectedSubspeciesTaxonLine = str(subspeciesWfoId)+ NONE + uuid(subspeciesNameUuid) +
            str("Genus species subsp. subspec") + str("subspecies") + str(speciesWfoId) + str("Mill.") +
            str("Myfamily")+ NONE3 + str("Genus") + NONE + str("species") + str("subspec")
            + NONE + str("Valid") + str("The book of botany 3: 22. 1804") + str("Accepted")
            + NONE2 + uuid(ref1UUID) + NONE3 + str("https://www.abc.de/mytaxon/cdm_dataportal/taxon/" + subspeciesTaxonUuid)
            + NONE_END;

    private String expectedSecRefLine = uuid(ref1UUID) + str("My sec ref") + NONE_END;

    private String expectedFamilyNameLine = str(familyWfoId) + NONE + uuid(familyNameUuid) +
            str("Familyname") + str("family") + str("") + str("L.") +
            str("Familyname") + NONE4 + NONE3 +
            NONE + str("Conserved") + str("Sp. Pl. 3: 22. 1752") + str("Accepted")
            + NONE2 + uuid(ref1UUID) + NONE3 + str("http://www.abc.de/mytaxon/cdm_dataportal/taxon/" + familyTaxonUuid)
            + NONE_END;

    private String expectedSpeciesNameLine = str(speciesWfoId) + str("urn:lsid:ipni.org:names:100000-3") + uuid(speciesNameUuid) +
            str("Genus species") + str("species") + str("WFO-12347g") + str("(Mill.) Hook") +
            str("Familyname") + NONE3 + str("Genus") + NONE + str("species") + NONE +
            NONE + str("Valid") + str("in J. Appl. Synon. 5: 33. 1824") + str("Accepted")
            + NONE + str(speciesBasionymWfoId) + uuid(ref3UUID) + NONE3 + str("http://www.abc.de/mytaxon/cdm_dataportal/taxon/" + speciesTaxonUuid)
            + NONE_END;

    private String expectedSpeciesBasionymNameLine = str(speciesBasionymWfoId) + NONE + uuid(basionymNameUuid) +
            str("Sus basionus") + str("species") + NONE + str("Mill.") +
            str("Familyname") + NONE3 + str("Sus") + NONE + str("basionus") + NONE +
            NONE + str("Valid") + str("The book of botany 3: 22. 1804") + str("homotypicSynonym") +
            str(speciesWfoId) + NONE + uuid(ref3UUID) + NONE3 +
            str("http://www.abc.de/mytaxon/cdm_dataportal/taxon/" + speciesTaxonUuid + "/synonymy?highlight=" + basionymSynonymUuid)
            + NONE_END;

    @BeforeEach
    public void setUp()  {
        createFullTestDataSet();
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testSubTree(){

        //config+invoke
        WfoBackboneExportConfigurator config = newConfigurator();
        config.setFamilyStr("Myfamily");
        config.setSourceLinkBaseUrl("https://www.abc.de/mytaxon");
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(node4Uuid));
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result);

        //counts and result lists
        List<String> taxonResult = getStringList(data, WfoBackboneExportTable.CLASSIFICATION);
        assertEquals(1, taxonResult.size() - COUNT_HEADER, "There should be 1 taxon (1 accepted)");

//        List<String> synonymResult = getStringList(data, WfoBackboneExportTable.CLASSIFICATION);
//        assertEquals(0, synonymResult.size() - COUNT_HEADER, "There should be no synonym");

        List<String> referenceResult = getStringList(data, WfoBackboneExportTable.REFERENCE);
        assertEquals(1, referenceResult.size() - COUNT_HEADER, "There should be 1 references (1 sec reference)");

        //taxon
        //... species
        String taxonStr = getTableString(data, WfoBackboneExportTable.CLASSIFICATION);
        String notExpected = speciesTaxonUuid.toString();
        assertFalse(taxonStr.startsWith(notExpected), "Result must not contain root of subtree taxon (species taxon)");
        //... subspecies
        String subspeciesLine = getLine(taxonResult, subspeciesWfoId);
        assertEquals(expectedSubspeciesTaxonLine, subspeciesLine);

        //reference
        String secRefLine = getLine(referenceResult, ref1UUID);
        assertEquals(expectedSecRefLine, secRefLine);
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })// data itself is created in base class
    public void testFullTreeWithUnpublished(){

        //config+invoke
        WfoBackboneExportConfigurator config = newConfigurator();
        config.getTaxonNodeFilter().setIncludeUnpublished(true);
        //Note: on purpose we do not define a familyStr here as it is to be taken from the persisted family
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result, 0, 1, 1);  //1 warning expected for missing basionym author, 1 error because of missing wfo identifier for a synonym

        //test counts
        List<String> taxonResult = getStringList(data, WfoBackboneExportTable.CLASSIFICATION);
//      taxonResult.stream().forEach(tr->System.out.println(tr));
        assertEquals(9, taxonResult.size() - COUNT_HEADER, "There should be 8 taxa (5 accepted + 4 synonyms)");
        assertEquals(5, filterAccepted(taxonResult).size(), "There should be 5 accepted taxa");
        assertEquals(4, filterSynonyms(taxonResult).size(), "There should be 3 synonyms");

        //reference counts
        List<String> referenceResult = getStringList(data, WfoBackboneExportTable.REFERENCE);
        assertEquals(3, referenceResult.size() - COUNT_HEADER, "There should be 3 reference (3 sec reference)");

        //test single data

        //... invisible root node
        assertEquals(0, taxonResult.stream().filter(line->line.contains(rootNodeUuid.toString())).count(), "Result must not contain root taxon");

        //subspecies taxon
        String subspeciesLine = getLine(taxonResult, subspeciesWfoId);
        String expectedSubspecies = expectedSubspeciesTaxonLine.replace("Myfamily", "Familyname").replace("https://", "http://");
        assertEquals(expectedSubspecies , subspeciesLine);

        //unpublished/excluded/note
        String unpublishedLine = getLine(taxonResult, subspeciesUnpublishedWfoId);
        String expectedExcluded = str(subspeciesUnpublishedWfoId)+ NONE + uuid(subspeciesUnpublishedNameUUID) +
                str("Genus species subsp. unpublished") + str("subspecies") +
                str(speciesWfoId) + str("Mill.") + str("Familyname") + NONE3 + str("Genus")
                + NONE + str("species") + str("unpublished") + NONE +
                str ("Valid") + str("The book of botany 3: 22. 1804") +
                str("Accepted") + NONE2 + uuid(ref1UUID) + NONE3 + str("http://www.abc.de/mytaxon/cdm_dataportal/taxon/" + subspeciesUnpublishedTaxonUuid)
                + strEnd("Excluded: My status note");
        assertEquals(expectedExcluded, unpublishedLine);

        //original spelling
        String originalSpellingLine = getLine(taxonResult, speciesOrigSpellingWfoId);
        String expectedOrigSpellingLine = str(speciesOrigSpellingWfoId) + NONE + uuid(origSpellingNameUuid) +
                str("Sus basyonus") + str("species") + NONE + str("Mill.") +
                str("Familyname") + NONE3 + str("Sus") + NONE + str("basyonus") + NONE +
                NONE + str("orthografia") + str("The book of botany 3: 22. 1804") +
                str("homotypicSynonym") +
                str(speciesWfoId) + NONE + NONE + NONE3 +
                str("http://www.abc.de/mytaxon/cdm_dataportal/name/bde06b48-bbda-428c-af4e-50c39db55821")
                + NONE_END;
        assertEquals(expectedOrigSpellingLine, originalSpellingLine);

        //... family => test nom. status 'conserved'
        String nameStr = getLine(taxonResult, familyWfoId);
        assertEquals(expectedFamilyNameLine, nameStr);

        //... species => has basionym
        String speciesStr = getLine(taxonResult, speciesWfoId);
        assertEquals(expectedSpeciesNameLine, speciesStr);

        //... species basionym/synonym
        String speciesBasionymStr = getLine(taxonResult, speciesBasionymWfoId);
        assertEquals(expectedSpeciesBasionymNameLine, speciesBasionymStr);

        //references
        String secRefLine = getLine(referenceResult, ref1UUID);
        assertEquals(expectedSecRefLine, secRefLine);

    }

    private List<String> filterSynonyms(List<String> taxonResult) {
        return taxonResult.stream()
                .filter(s->s.contains("typicSynonym\"") || s.contains("\"Synonym\""))
                .collect(Collectors.toList());
    }

    private List<String> filterAccepted(List<String> taxonResult) {
        return taxonResult.stream()
                .filter(s->s.contains("Accepted"))
                .collect(Collectors.toList());
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testFullData(){

        //config+invoke
        WfoBackboneExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = (Map<String, byte[]>)checkAndGetData(result, 0, 1, 1);  //1 warning expected for missing basionym author,one error because of a missing wfo identifier
        assertTrue(result.getExportType().equals(ExportType.WFO_BACKBONE)); //test export type

        //test counts
        List<String> taxonResult = getStringList(data, WfoBackboneExportTable.CLASSIFICATION);
//      taxonResult.stream().forEach(tr->System.out.println(tr));
        assertEquals(7, taxonResult.size() - COUNT_HEADER, "There should be 7 taxa (4 acceptd + 3 synonym)");
        assertEquals(4, filterAccepted(taxonResult).size(), "There should be 4 accepted taxa");
        assertEquals(3, filterSynonyms(taxonResult).size(), "There should be 3 synonyms");
        List<String> referenceResult = getStringList(data, WfoBackboneExportTable.REFERENCE);
        assertEquals(3, referenceResult.size() - COUNT_HEADER, "There should be 3 reference");

        //tbc
    }

    @Override
    public void testFullSampleData() {
        super.testFullSampleData(0, 1, 1);  //1 warning expected for missing basionym author); 1 error because of a missing wfo identifier
    }

    @Override
    protected WfoBackboneExportConfigurator newConfigurator() {
        WfoBackboneExportConfigurator config = WfoBackboneExportConfigurator.NewInstance();
        config.setTarget(TARGET.EXPORT_DATA);
        config.setSourceLinkBaseUrl("http://www.abc.de/mytaxon/");
        return config;
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}