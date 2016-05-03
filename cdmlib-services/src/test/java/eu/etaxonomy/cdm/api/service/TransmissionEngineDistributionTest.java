// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution;
import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution.AggregationMode;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.kohlbecker
 * @date Feb 26, 2013
 *
 */
public class TransmissionEngineDistributionTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(TransmissionEngineDistributionTest.class);

    private static final UUID T_LAPSANA_UUID = UUID.fromString("f65d47bd-4f49-4ab1-bc4a-bc4551eaa1a8");

    private static final UUID T_LAPSANA_COMMUNIS_UUID = UUID.fromString("2a5ceebb-4830-4524-b330-78461bf8cb6b");

    private static final UUID T_LAPSANA_COMMUNIS_COMMUNIS_UUID = UUID.fromString("441a3c40-0c84-11de-8c30-0800200c9a66");

    private static final UUID T_LAPSANA_COMMUNIS_ADENOPHORA_UUID = UUID.fromString("e4acf200-63b6-11dd-ad8b-0800200c9a66");

    private static final UUID T_LAPSANA_COMMUNIS_ALPINA_UUID = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");

    private static final UUID CLASSIFICATION_UUID = UUID.fromString("4b266053-a841-4980-b548-3f21d8d7d712");


    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private TransmissionEngineDistribution engine;

    // --- Distributions --- //
    // tdwg3 level YUG :  Yugoslavia
    // contains tdwg4 level areas :
    //   YUG-BH	Bosnia-Herzegovina
    //   YUG-CR	Croatia
    //   YUG-KO	Kosovo
    //   YUG-MA	Macedonia
    //   YUG-MN	Montenegro
    private NamedArea yug = null;
    private NamedArea yug_bh = null;
    private NamedArea yug_cr = null;
    private NamedArea yug_ko = null;
    private NamedArea yug_ma = null;
    private NamedArea yug_mn = null;

    List<NamedArea> superAreas = null;
    Rank upperRank = null;
    Rank lowerRank = null;

    private Classification classification;


    @Before
    public void setUp() {

        superAreas = Arrays.asList(new NamedArea[]{
        		termService.getAreaByTdwgAbbreviation("YUG")
        });
        lowerRank = Rank.SPECIES();
        upperRank = Rank.GENUS();

        classification = classificationService.load(CLASSIFICATION_UUID);

        yug = termService.getAreaByTdwgAbbreviation("YUG");
        yug_bh = termService.getAreaByTdwgAbbreviation("YUG-BH");
        yug_cr = termService.getAreaByTdwgAbbreviation("YUG-CR");
        yug_ko = termService.getAreaByTdwgAbbreviation("YUG-KO");
        yug_ma = termService.getAreaByTdwgAbbreviation("YUG-MA");
        yug_mn = termService.getAreaByTdwgAbbreviation("YUG-MN");

        engine.updatePriorities();
    }

    @Test
    @DataSet
    public void testPriorities(){

        Set<Extension> extensions = termService.load(PresenceAbsenceTerm.CULTIVATED().getUuid()).getExtensions();
        assertEquals(TransmissionEngineDistribution.EXTENSION_VALUE_PREFIX + "45", extensions.iterator().next().getValue());
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="TransmissionEngineDistributionTest.xml"),
    })
//  @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class) //, value="./BlankDataSet.xml")
    public void test_ignore() {

        addDistributions(
                T_LAPSANA_COMMUNIS_ALPINA_UUID,
                new Distribution[] {
                        // should succeed during area aggregation be ignored by rank aggregation
                        // => yug will get status ENDEMIC_FOR_THE_RELEVANT_AREA
                        //    but only for LAPSANA_COMMUNIS_ALPINA
                        Distribution.NewInstance(yug_mn, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()),
                        // should be ignored by area aggregation
                        // => LAPSANA_COMMUNIS will wave distribution with yug_ko and INTRODUCED_FORMERLY_INTRODUCED
                        Distribution.NewInstance(yug_ko, PresenceAbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED()),
               }
            );

        engine.accumulate(AggregationMode.byAreasAndRanks, superAreas, lowerRank, upperRank, null, null);

        Taxon lapsana_communis_alpina  = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        assertEquals(2, lapsana_communis_alpina.getDescriptions().size());
        // TODO test for yug => ENDEMIC_FOR_THE_RELEVANT_AREA in computed description

        Taxon lapsana_communis  = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_UUID);
        assertEquals(1, lapsana_communis.getDescriptions().size());
        TaxonDescription description = lapsana_communis.getDescriptions().iterator().next();
        assertEquals(1, description.getElements().size());
        int numExpectedFound = 0;
        for (DescriptionElementBase element : description.getElements()){
            Distribution distribution = (Distribution)element;
            if(distribution.getArea().equals(yug_ko)){
                numExpectedFound++;
                assertEquals("aggregated status of area YUG-KO wrong", PresenceAbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED().getLabel(), distribution.getStatus().getLabel());
            }
        }
        assertEquals("All three expected areas should have been found before", numExpectedFound, 1);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="TransmissionEngineDistributionTest.xml"),
    })
    public void testArea_area() {

        addDistributions(
                T_LAPSANA_COMMUNIS_ALPINA_UUID,
                new Distribution[] {
                        Distribution.NewInstance(yug_mn, PresenceAbsenceTerm.CULTIVATED()),
                        Distribution.NewInstance(yug_ko, PresenceAbsenceTerm.NATIVE()), // should succeed
                        Distribution.NewInstance(yug_bh, PresenceAbsenceTerm.INTRODUCED())
               }
            );

        Taxon lapsana_communis_alpina  = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        assertEquals(1, lapsana_communis_alpina.getDescriptions().size());

        engine.accumulate(AggregationMode.byAreas, superAreas, lowerRank, upperRank, classification, null);

        lapsana_communis_alpina  = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        assertEquals(2, lapsana_communis_alpina.getDescriptions().size());

        boolean expectedAreaFound = false;
        for (TaxonDescription description : lapsana_communis_alpina.getDescriptions()) {
            Distribution distribution = (Distribution) description.getElements().iterator().next(); // only one aggregated area expected
            if(distribution.getArea().equals(yug)) {
                expectedAreaFound = true;
                assertEquals("aggregated status of area YUG is wrong", PresenceAbsenceTerm.NATIVE().getLabel(), distribution.getStatus().getLabel());
            }
        }
        assertTrue("The areae YUG should have been found", expectedAreaFound);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testArea_rank_and_area() {

        addDistributions(
                T_LAPSANA_COMMUNIS_ALPINA_UUID,
                new Distribution[] {
                        Distribution.NewInstance(yug_mn, PresenceAbsenceTerm.CULTIVATED()),
                        Distribution.NewInstance(yug_ko, PresenceAbsenceTerm.NATIVE()), // should succeed
               }
            );
        addDistributions(
                T_LAPSANA_COMMUNIS_UUID,
                new Distribution[] {
                        Distribution.NewInstance(yug_mn, PresenceAbsenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION()),
                        Distribution.NewInstance(yug_ko, PresenceAbsenceTerm.CULTIVATED()),
               }
            );

        engine.accumulate(AggregationMode.byAreasAndRanks, superAreas, lowerRank, upperRank, null, null);

        Taxon lapsana_communis  = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_UUID);
        assertEquals(2, lapsana_communis.getDescriptions().size());

        Taxon lapsana = (Taxon) taxonService.load(T_LAPSANA_UUID);
        assertEquals(1, lapsana.getDescriptions().size());
        TaxonDescription description = lapsana.getDescriptions().iterator().next();
        assertEquals(3, description.getElements().size());
        int numExpectedFound = 0;
        for (DescriptionElementBase element : description.getElements()){
            Distribution distribution = (Distribution)element;
            if(distribution.getArea().equals(yug)){
                numExpectedFound++;
                assertEquals("aggregated status of area YUG is wrong", PresenceAbsenceTerm.NATIVE().getLabel(), distribution.getStatus().getLabel());
            }
            if(distribution.getArea().equals(yug_mn)){
                numExpectedFound++;
                assertEquals("aggregated status of area YUG-MN is wrong", PresenceAbsenceTerm.CULTIVATED().getLabel(), distribution.getStatus().getLabel());
            }
            if(distribution.getArea().equals(yug_ko)){
                numExpectedFound++;
                assertEquals("aggregated status of area YUG-KO wrong", PresenceAbsenceTerm.NATIVE().getLabel(), distribution.getStatus().getLabel());
            }
        }
        assertEquals("All three expected areas should have been found before", numExpectedFound, 3);
    }

    /**
     * creates a new description for the taxon identified by the UUIDs
     * @param taxonUuid
     * @param distributions
     */
    private void addDistributions(UUID taxonUuid, Distribution[] distributions) {
        Taxon taxon = (Taxon) taxonService.load(taxonUuid);
        if(taxon == null) {
            throw new NullPointerException("No taxon found for " + taxonUuid);
        }
        TaxonDescription description = TaxonDescription.NewInstance(taxon);

         for (Distribution distribution : distributions) {
             description.addElement(distribution);
        }
        taxonService.saveOrUpdate(taxon);
        // need to write to database for transmission engine
        commitAndStartNewTransaction(null);
    }


//    @Test //  uncomment to create test data file//
    @Override
    public void createTestDataSet() throws FileNotFoundException {

        // --- References --- //
        Reference<?> sec = ReferenceFactory.newDatabase();
        sec.setTitleCache("Test", true);
        Reference<?> nomRef = ReferenceFactory.newBook();
        sec.setTitleCache("Sp.Pl.", true);

        referenceService.save(sec);
        referenceService.save(nomRef);


        // --- Taxa --- //
        //  Lapsana
        //        L. communis
        //            L. communis subsp. communis
        //            L. communis subsp. adenophora
        //            L. communis subsp. alpina
        //  Sonchella
        //        S. dentata
        //        S. stenoma
        BotanicalName n_lapsana = BotanicalName.NewInstance(Rank.GENUS());
        n_lapsana.setTitleCache("Lapsana", true);
        Taxon t_lapsana = Taxon.NewInstance(n_lapsana, sec);
        t_lapsana.setUuid(T_LAPSANA_UUID);
        taxonService.saveOrUpdate(t_lapsana);

        BotanicalName n_lapsana_communis = BotanicalName.NewInstance(Rank.SPECIES());
        n_lapsana_communis.setTitleCache("L. communis", true);
        Taxon t_lapsana_communis = Taxon.NewInstance(n_lapsana_communis, sec);
        t_lapsana_communis.setUuid(T_LAPSANA_COMMUNIS_UUID);
        taxonService.saveOrUpdate(t_lapsana_communis);

        BotanicalName n_lapsana_communis_communis = BotanicalName.NewInstance(Rank.SUBSPECIES());
        n_lapsana_communis_communis.setTitleCache("L. communis subsp. communis", true);
        Taxon t_lapsana_communis_communis = Taxon.NewInstance(n_lapsana_communis_communis, sec);
        t_lapsana_communis_communis.setUuid(T_LAPSANA_COMMUNIS_COMMUNIS_UUID);
        taxonService.saveOrUpdate(t_lapsana_communis_communis);

        BotanicalName n_lapsana_communis_adenophora = BotanicalName.NewInstance(Rank.SUBSPECIES());
        n_lapsana_communis_adenophora.setTitleCache("L. communis subsp. adenophora", true);
        Taxon t_lapsana_communis_adenophora = Taxon.NewInstance(n_lapsana_communis_adenophora, sec);
        t_lapsana_communis_adenophora.setUuid(T_LAPSANA_COMMUNIS_ADENOPHORA_UUID);
        taxonService.saveOrUpdate(t_lapsana_communis_adenophora);

        BotanicalName n_lapsana_communis_alpina = BotanicalName.NewInstance(Rank.SUBSPECIES());
        n_lapsana_communis_alpina.setTitleCache("L. communis subsp. alpina", true);
        Taxon t_lapsana_communis_alpina = Taxon.NewInstance(n_lapsana_communis_alpina, sec);
        t_lapsana_communis_alpina.setUuid(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        taxonService.saveOrUpdate(t_lapsana_communis_alpina);

        // --- Classification --- //
        Classification classification = Classification.NewInstance("TestClassification");
        classification.setUuid(CLASSIFICATION_UUID);
        classificationService.save(classification);
        TaxonNode node_lapsana = classification.addChildTaxon(t_lapsana, sec, null);
        TaxonNode node_lapsana_communis = node_lapsana.addChildTaxon(t_lapsana_communis, sec, null);
        node_lapsana_communis.addChildTaxon(t_lapsana_communis_communis, sec, null);
        node_lapsana_communis.addChildTaxon(t_lapsana_communis_adenophora, sec, null);
        node_lapsana_communis.addChildTaxon(t_lapsana_communis_alpina, sec, null);
        classificationService.saveOrUpdate(classification);

        // --- Distributions --- //
        // tdwg3 level YUG :  Yugoslavia
        // contains tdwg4 level areas :
        //   YUG-BH	Bosnia-Herzegovina
        //   YUG-CR	Croatia
        //   YUG-KO	Kosovo
        //   YUG-MA	Macedonia
        //   YUG-MN	Montenegro

        // assigning distribution information to taxa
        // expectations regarding the aggregation can be found in the comments below
//        TaxonDescription d_lapsana_communis_communis = TaxonDescription.NewInstance(t_lapsana_communis_communis);
//        d_lapsana_communis_communis.addElement(Distribution.NewInstance(
//                    TdwgArea.getAreaByTdwgAbbreviation("YUG-MN"),
//                    PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA() // should be ignored
//                    );

        commitAndStartNewTransaction(null);

        writeDbUnitDataSetFile(new String[] {
                "TAXONBASE", "TAXONNAMEBASE",
                "REFERENCE", "DESCRIPTIONELEMENTBASE", "DESCRIPTIONBASE",
                "AGENTBASE", "CLASSIFICATION",  "TAXONNODE",
                "HOMOTYPICALGROUP", "LANGUAGESTRING",
         });

    }

}
