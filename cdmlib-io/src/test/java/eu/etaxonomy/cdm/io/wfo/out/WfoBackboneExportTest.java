/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;

import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportTestBase;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

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
            + str("subspecies") + str("Valid") + str("The book of botany 3: 22. 1804") + str("Accepted")
            + NONE2 + uuid(ref1UUID) + NONE3 + str("https://www.abc.de/mytaxon/cdm_dataportal/taxon/" + subspeciesTaxonUuid)
            + NONE_END;

    private String expectedSecRefLine = uuid(ref1UUID) + str("My sec ref") + NONE_END;

    private String expectedFamilyNameLine = str(familyWfoId) + NONE + uuid(familyNameUuid) +
            str("Familyname") + str("family") + str("") + str("L.") +
            str("Familyname") + NONE4 + NONE3 +
            str("family") + str("Conserved") + str("Sp. Pl. 3: 22. 1752") + str("Accepted")
            + NONE2 + uuid(ref1UUID) + NONE3 + str("http://www.abc.de/mytaxon/cdm_dataportal/taxon/" + familyTaxonUuid)
            + NONE_END;

    private String expectedSpeciesNameLine = str(speciesWfoId) + NONE + uuid(speciesNameUuid) +
            str("Genus species") + str("species") + str("WFO-12347g") + str("(Mill.) Hook") +
            str("Familyname") + NONE3 + str("Genus") + NONE + str("species") + NONE +
            str("species") + str("Valid") + str("in J. Appl. Synon. 5: 33. 1824") + str("Accepted")
            + NONE + str(speciesBasionymWfoId) + uuid(ref1UUID) + NONE3 + str("http://www.abc.de/mytaxon/cdm_dataportal/taxon/" + speciesTaxonUuid)
            + NONE_END;

    private String expectedSpeciesBasionymNameLine = str(speciesBasionymWfoId) + NONE + uuid(basionymNameUuid) +
            str("Sus basionus") + str("species") + NONE + str("Mill.") +
            str("Familyname") + NONE3 + str("Sus") + NONE + str("basionus") + NONE +
            str("species") + str("Valid") + str("The book of botany 3: 22. 1804") + str("homotypicSynonym") +
            str(speciesWfoId) + NONE + uuid(ref1UUID) + NONE3 +
            //TODO 2 highlite => highlight once changed in portal code
            str("http://www.abc.de/mytaxon/cdm_dataportal/taxon/" + speciesTaxonUuid + "/synonymy?highlite=" + basionymSynonymUuid)
            + NONE_END;

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

        //config+invoke
        WfoBackboneExportConfigurator config = newConfigurator();
        config.setFamilyStr("Myfamily");
        config.setSourceLinkBaseUrl("https://www.abc.de/mytaxon");
        config.setTaxonNodeFilter(TaxonNodeFilter.NewSubtreeInstance(node4Uuid));
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);

        //counts and result lists
        List<String> taxonResult = getStringList(data, WfoBackboneExportTable.CLASSIFICATION);
        Assert.assertEquals("There should be 1 taxon (1 accepted)", 1, taxonResult.size() - COUNT_HEADER);

//        List<String> synonymResult = getStringList(data, WfoBackboneExportTable.CLASSIFICATION);
//        Assert.assertEquals("There should be no synonym", 0, synonymResult.size() - COUNT_HEADER);

        List<String> referenceResult = getStringList(data, WfoBackboneExportTable.REFERENCE);
        Assert.assertEquals("There should be 1 references (1 sec reference)", 1, referenceResult.size() - COUNT_HEADER);

        //taxon
        //... species
        String taxonStr = getTableString(data, WfoBackboneExportTable.CLASSIFICATION);
        String notExpected = speciesTaxonUuid.toString();
        Assert.assertFalse("Result must not contain root of subtree taxon (species taxon)", taxonStr.startsWith(notExpected));
        //... subspecies
        String subspeciesLine = getLine(taxonResult, subspeciesWfoId);
        Assert.assertEquals(expectedSubspeciesTaxonLine, subspeciesLine);

        //reference
        String secRefLine = getLine(referenceResult, ref1UUID);
        Assert.assertEquals(expectedSecRefLine, secRefLine);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })// data itself is created in base class
    public void testFullTreeWithUnpublished(){

        //config+invoke
        WfoBackboneExportConfigurator config = newConfigurator();
        config.getTaxonNodeFilter().setIncludeUnpublished(true);
        //Note: on purpose we do not define a familyStr here as it is to be taken from the persisted family
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);

        //test counts
        List<String> taxonResult = getStringList(data, WfoBackboneExportTable.CLASSIFICATION);
//      taxonResult.stream().forEach(tr->System.out.println(tr));
        Assert.assertEquals("There should be 8 taxa (5 accepted + 4 synonyms)", 9, taxonResult.size() - COUNT_HEADER);
        Assert.assertEquals("There should be 5 accepted taxa", 5, filterAccepted(taxonResult).size());
        Assert.assertEquals("There should be 3 synonyms", 4, filterSynonyms(taxonResult).size());

        //reference counts
        List<String> referenceResult = getStringList(data, WfoBackboneExportTable.REFERENCE);
        Assert.assertEquals("There should be 1 reference (1 sec reference)", 1, referenceResult.size() - COUNT_HEADER);

        //test single data

        //... invisible root node
        Assert.assertEquals("Result must not contain root taxon",
                0, taxonResult.stream().filter(line->line.contains(rootNodeUuid.toString())).count());

        //subspecies taxon
        String subspeciesLine = getLine(taxonResult, subspeciesWfoId);
        String expectedSubspecies = expectedSubspeciesTaxonLine.replace("Myfamily", "Familyname").replace("https://", "http://");
        Assert.assertEquals(expectedSubspecies , subspeciesLine);

        //unpublished/excluded/note
        String unpublishedLine = getLine(taxonResult, subspeciesUnpublishedWfoId);
        String expectedExcluded = str(subspeciesUnpublishedWfoId)+ NONE + uuid(subspeciesUnpublishedNameUUID) +
                str("Genus species subsp. unpublished") + str("subspecies") +
                str(speciesWfoId) + str("Mill.") + str("Familyname") + NONE3 + str("Genus")
                + NONE + str("species") + str("unpublished") + str("subspecies") +
                str ("Valid") + str("The book of botany 3: 22. 1804") +
                str("Accepted") + NONE2 + uuid(ref1UUID) + NONE3 + str("http://www.abc.de/mytaxon/cdm_dataportal/taxon/" + subspeciesUnpublishedTaxonUuid)
                + strEnd("Excluded: My status note");
        Assert.assertEquals(expectedExcluded, unpublishedLine);

        //original spelling
        String originalSpellingLine = getLine(taxonResult, speciesOrigSpellingWfoId);
        String expectedOrigSpellingLine = str(speciesOrigSpellingWfoId) + NONE + uuid(origSpellingNameUuid) +
                str("Sus basyonus") + str("species") + NONE + str("Mill.") +
                str("Familyname") + NONE3 + str("Sus") + NONE + str("basyonus") + NONE +
                str("species") + str("orthografia") + str("The book of botany 3: 22. 1804") +
                str("Synonym") +
                str(speciesWfoId) + NONE + NONE + NONE3 +
                //TODO 2 link for orig spelling
                NONE //str("" + speciesTaxonUuid + "/synonymy?highlite=" + basionymSynonymUuid)
                + NONE_END;
        Assert.assertEquals(expectedOrigSpellingLine, originalSpellingLine);

        //... family => test nom. status 'conserved'
        String nameStr = getLine(taxonResult, familyWfoId);
        Assert.assertEquals(expectedFamilyNameLine, nameStr);

        //... species => has basionym
        String speciesStr = getLine(taxonResult, speciesWfoId);
        Assert.assertEquals(expectedSpeciesNameLine, speciesStr);

        //... species basionym/synonym
        String speciesBasionymStr = getLine(taxonResult, speciesBasionymWfoId);
        Assert.assertEquals(expectedSpeciesBasionymNameLine, speciesBasionymStr);

        //references
        String secRefLine = getLine(referenceResult, ref1UUID);
        Assert.assertEquals(expectedSecRefLine, secRefLine);

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
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFullData(){

        //config+invoke
        WfoBackboneExportConfigurator config = newConfigurator();
        ExportResult result = defaultExport.invoke(config);
        Map<String, byte[]> data = checkAndGetData(result);
        Assert.assertTrue(result.getExportType().equals(ExportType.WFO_BACKBONE)); //test export type

        //test counts
        List<String> taxonResult = getStringList(data, WfoBackboneExportTable.CLASSIFICATION);
//      taxonResult.stream().forEach(tr->System.out.println(tr));
        Assert.assertEquals("There should be 7 taxa (4 acceptd + 3 synonym)", 7, taxonResult.size() - COUNT_HEADER);
        Assert.assertEquals("There should be 4 accepted taxa", 4, filterAccepted(taxonResult).size());
        Assert.assertEquals("There should be 3 synonyms", 3, filterSynonyms(taxonResult).size());
        List<String> referenceResult = getStringList(data, WfoBackboneExportTable.REFERENCE);
        Assert.assertEquals("There should be 1 reference", 1, referenceResult.size() - COUNT_HEADER);

        //tbc
    }

    @Override
    public void testFullSampleData() {
        super.testFullSampleData();
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