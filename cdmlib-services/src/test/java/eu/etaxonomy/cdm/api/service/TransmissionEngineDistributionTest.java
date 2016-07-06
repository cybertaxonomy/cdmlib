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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution;
import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution.AggregationMode;
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
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

    private Reference book_a = null;
    private Reference book_b = null;


    @Before
    public void setUp() {

        superAreas = Arrays.asList(new NamedArea[]{
        		termService.getAreaByTdwgAbbreviation("YUG")
        });
        lowerRank = Rank.SUBSPECIES();
        upperRank = Rank.GENUS();

        classification = classificationService.load(CLASSIFICATION_UUID);

        yug = termService.getAreaByTdwgAbbreviation("YUG");
        yug_bh = termService.getAreaByTdwgAbbreviation("YUG-BH");
        yug_cr = termService.getAreaByTdwgAbbreviation("YUG-CR");
        yug_ko = termService.getAreaByTdwgAbbreviation("YUG-KO");
        yug_ma = termService.getAreaByTdwgAbbreviation("YUG-MA");
        yug_mn = termService.getAreaByTdwgAbbreviation("YUG-MN");

        book_a = ReferenceFactory.newBook();
        book_a.setTitle("book_a");
        book_b = ReferenceFactory.newBook();
        book_b.setTitle("book_a");

        engine.setBatchMinFreeHeap(100 * 1024 * 1024);
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
    public void test_ignore() throws JvmLimitsException {

        addDistributions(
                T_LAPSANA_COMMUNIS_ALPINA_UUID,
                Arrays.asList(new Distribution[] {
                        // should succeed during area aggregation be ignored by rank aggregation
                        // => yug will get status ENDEMIC_FOR_THE_RELEVANT_AREA
                        //    but only for LAPSANA_COMMUNIS_ALPINA
                        Distribution.NewInstance(yug_mn, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()),
                        // should be ignored by area aggregation
                        // => LAPSANA_COMMUNIS will wave distribution with yug_ko and INTRODUCED_FORMERLY_INTRODUCED
                        Distribution.NewInstance(yug_ko, PresenceAbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED()),
               })
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
    public void testArea_area() throws JvmLimitsException {

        Set<Distribution> distributions_LCA = new HashSet<>();

        distributions_LCA.add(newDistribution(book_a, yug_mn, PresenceAbsenceTerm.CULTIVATED(), "1"));
        distributions_LCA.add(newDistribution(book_a, yug_ko, PresenceAbsenceTerm.NATIVE(), "2")); // NATIVE should succeed
        distributions_LCA.add(newDistribution(book_a, yug_bh, PresenceAbsenceTerm.INTRODUCED(), "3"));
        distributions_LCA.add(newDistribution(book_a, yug_ma, PresenceAbsenceTerm.NATIVE(), "4")); // NATIVE should succeed

        addDistributions(
                T_LAPSANA_COMMUNIS_ALPINA_UUID,
                distributions_LCA
            );

        Taxon lapsana_communis_alpina  = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        assertEquals(1, lapsana_communis_alpina.getDescriptions().size());

        engine.accumulate(AggregationMode.byAreas, superAreas, lowerRank, upperRank, classification, null);

        lapsana_communis_alpina  = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        assertEquals(2, lapsana_communis_alpina.getDescriptions().size());

        Distribution accumulatedDistribution = null;
        for (TaxonDescription description : lapsana_communis_alpina.getDescriptions()) {
            if(description.hasMarker(MarkerType.COMPUTED(), true)) {
                assertNull("only one computed Distribution should exists", accumulatedDistribution);
                assertEquals("the computed Decsription should have only one element", 1, description.getElements().size());
                accumulatedDistribution = (Distribution) description.getElements().iterator().next();
                assertEquals("Expecting area to be YUG", yug, accumulatedDistribution.getArea());
                assertEquals("Expecting status to be NATIVE", PresenceAbsenceTerm.NATIVE().getLabel(), accumulatedDistribution.getStatus().getLabel());
            }
        }
        assertNotNull("The area YUG should have been found", accumulatedDistribution);
        assertEquals("Expecting two source references", 2, accumulatedDistribution.getSources().size());
        Iterator<DescriptionElementSource> sourceIt = accumulatedDistribution.getSources().iterator();
        // should contain source_LCA_yug_ma and source_LCA_yug_ko, testing the microreference which is unique in the tests
        assertTrue(" 2  4 ".contains(" " + sourceIt.next().getCitationMicroReference() + " "));
        assertTrue(" 2  4 ".contains(" " + sourceIt.next().getCitationMicroReference() + " "));
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="TransmissionEngineDistributionTest.xml"),
    })
    public void testArea_rank_and_area_1() throws JvmLimitsException {

        Set<Distribution> distributions_LCA = new HashSet<>();
        distributions_LCA.add(newDistribution(book_a, yug_mn, PresenceAbsenceTerm.CULTIVATED(), "1"));
        distributions_LCA.add(newDistribution(book_a, yug_ko, PresenceAbsenceTerm.NATIVE(), "2")); // NATIVE should succeed

        addDistributions(
                T_LAPSANA_COMMUNIS_ALPINA_UUID,
                distributions_LCA
            );

        Set<Distribution> distributions_LC = new HashSet<>();
        distributions_LC.add(newDistribution(book_a, yug_mn, PresenceAbsenceTerm.CULTIVATED(), "3"));
        distributions_LC.add(newDistribution(book_a, yug_ko, PresenceAbsenceTerm.NATIVE(), "4")); // NATIVE should succeed

        commitAndStartNewTransaction(null);

        addDistributions(
                T_LAPSANA_COMMUNIS_UUID,
                distributions_LC
            );

        engine.accumulate(AggregationMode.byAreasAndRanks, superAreas, lowerRank, upperRank, null, null);

        Taxon lapsana_communis  = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_UUID);
        assertEquals("Lapsana communis alpina must only have 2 Descriptions", 2, lapsana_communis.getDescriptions().size());

        Taxon lapsana = (Taxon) taxonService.load(T_LAPSANA_UUID);
        assertEquals("Lapsana communis must only have 1 Description", 1, lapsana.getDescriptions().size());
        TaxonDescription description = lapsana.getDescriptions().iterator().next();
        assertTrue(description.hasMarker(MarkerType.COMPUTED(), true));
        assertEquals(3, description.getElements().size());
        int numExpectedFound = 0;
        for (DescriptionElementBase element : description.getElements()){
            Distribution distribution = (Distribution)element;
            if(distribution.getArea().equals(yug)){
                numExpectedFound++;
                assertEquals("aggregated status of area YUG is wrong", PresenceAbsenceTerm.NATIVE().getLabel(), distribution.getStatus().getLabel());
                assertEquals(2, distribution.getSources().size());
                Iterator<DescriptionElementSource> sourceIt = distribution.getSources().iterator();
                // should contain source_LCA_yug_ma and source_LCA_yug_ko, testing the microreference which is unique in the tests
                assertTrue(" 2  4 ".contains(" " + sourceIt.next().getCitationMicroReference() + " "));
                assertTrue(" 2  4 ".contains(" " + sourceIt.next().getCitationMicroReference() + " "));
            }
            if(distribution.getArea().equals(yug_mn)){
                numExpectedFound++;
                assertEquals("aggregated status of area YUG-MN is wrong", PresenceAbsenceTerm.CULTIVATED().getLabel(), distribution.getStatus().getLabel());
                assertEquals(2, distribution.getSources().size());
                Iterator<DescriptionElementSource> sourceIt = distribution.getSources().iterator();
                // should contain source_LCA_yug_ma and source_LCA_yug_ko, testing the microreference which is unique in the tests
                assertTrue(" 1  3 ".contains(" " + sourceIt.next().getCitationMicroReference() + " "));
                assertTrue(" 1  3 ".contains(" " + sourceIt.next().getCitationMicroReference() + " "));
            }
            if(distribution.getArea().equals(yug_ko)){
                numExpectedFound++;
                assertEquals("aggregated status of area YUG-KO wrong", PresenceAbsenceTerm.NATIVE().getLabel(), distribution.getStatus().getLabel());
                assertEquals(2, distribution.getSources().size());
                Iterator<DescriptionElementSource> sourceIt = distribution.getSources().iterator();
                // should contain source_LCA_yug_ma and source_LCA_yug_ko, testing the microreference which is unique in the tests
                assertTrue(" 2  4 ".contains(" " + sourceIt.next().getCitationMicroReference() + " "));
                assertTrue(" 2  4 ".contains(" " + sourceIt.next().getCitationMicroReference() + " "));
            }
        }
        assertEquals("All three expected areas should have been found before", numExpectedFound, 3);
    }

    /**
     * Variant of {@link #testArea_rank_and_area_1()} with alternate source references to check the
     * suppression of duplicates.
     *
     * This test relies on {@link #testArea_rank_and_area_1()}
     * an makes assertions only on the alternative source references
     * @throws JvmLimitsException
     */
    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="TransmissionEngineDistributionTest.xml"),
    })
    public void testArea_rank_and_area_2() throws JvmLimitsException {

        Set<Distribution> distributions_LCA = new HashSet<Distribution>();
        distributions_LCA.add(newDistribution(book_a, yug_ko, PresenceAbsenceTerm.NATIVE(), "1"));
        distributions_LCA.add(newDistribution(book_b, yug_ko, PresenceAbsenceTerm.NATIVE(), "2"));

        addDistributions(
                T_LAPSANA_COMMUNIS_ALPINA_UUID,
                distributions_LCA
            );

        engine.accumulate(AggregationMode.byAreasAndRanks, superAreas, lowerRank, upperRank, null, null);

        Taxon lapsana_communis = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_UUID);
        int computedDescriptionsCnt = 0;
        for(TaxonDescription description : lapsana_communis.getDescriptions()) {
            if(description.hasMarker(MarkerType.COMPUTED(), true)) {
                computedDescriptionsCnt++;
                assertEquals(2, description.getElements().size()); // yug, yug_ko
                for(DescriptionElementBase distribution : description.getElements()) {
                    logger.debug(((Distribution)distribution).getArea() + " " + sourcesToString(distribution));
                    if(((Distribution)distribution).getArea().equals(yug_ko)){
                        assertEquals(2, distribution.getSources().size());
                    }
                    if(((Distribution)distribution).getArea().equals(yug)){
                        assertEquals(2, distribution.getSources().size());
                    }
                }
            }
        }
        assertEquals(1, computedDescriptionsCnt);
    }


    /**
     * Variant of {@link #testArea_rank_and_area_1()} with alternate source references to check the
     * suppression of duplicates.
     *
     * This test relies on {@link #testArea_rank_and_area_1()}
     * an makes assertions only on the alternative source references
     * @throws JvmLimitsException
     */
    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="TransmissionEngineDistributionTest.xml"),
    })
    public void testArea_rank_and_area_3() throws JvmLimitsException {

        Set<Distribution> distributions_LCA = new HashSet<Distribution>();
        distributions_LCA.add(newDistribution(book_a, yug_ko, PresenceAbsenceTerm.NATIVE(), "1"));
        distributions_LCA.add(newDistribution(book_a, yug_ko, PresenceAbsenceTerm.NATIVE(), "3"));

        addDistributions(
                T_LAPSANA_COMMUNIS_ALPINA_UUID,
                distributions_LCA
            );

        Set<Distribution> distributions_LC = new HashSet<>();
        distributions_LC.add(newDistribution(book_a, yug_ko, PresenceAbsenceTerm.NATIVE(), "1"));
        distributions_LC.add(newDistribution(book_b, yug_ko, PresenceAbsenceTerm.NATIVE(), "2"));

        addDistributions(
                T_LAPSANA_COMMUNIS_UUID,
                distributions_LC
            );

        engine.accumulate(AggregationMode.byAreasAndRanks, superAreas, lowerRank, upperRank, null, null);

        Taxon lapsana_communis = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_UUID);
        int computedDescriptionsCnt = 0;
        for(TaxonDescription description : lapsana_communis.getDescriptions()) {
            if(description.hasMarker(MarkerType.COMPUTED(), true)) {
                computedDescriptionsCnt++;
                assertEquals(2, description.getElements().size());
                for(DescriptionElementBase distribution : description.getElements()) {
                    logger.debug(((Distribution)distribution).getArea() + " " + sourcesToString(distribution));
                    if(((Distribution)distribution).getArea().equals(yug_ko)){
                        assertEquals(2, distribution.getSources().size());
                    }
                    if(((Distribution)distribution).getArea().equals(yug)){
                        assertEquals(3, distribution.getSources().size());
                    }
                }
            }
        }
        assertEquals(1, computedDescriptionsCnt);
    }

    /**
     * Variant of {@link #testArea_rank_and_area_1()} with alternate source references to
     * check the handling of the case where the target taxon already has the distribution which is the
     * result of the aggregation (see http://dev.e-taxonomy.eu/trac/ticket/4366#comment:12)
     *
     * This test relies on {@link #testArea_rank_and_area_1()}
     * an makes assertions only on the alternative source references
     * @throws JvmLimitsException
     */
    @Test
    @Ignore
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="TransmissionEngineDistributionTest.xml"),
    })
    public void testArea_rank_and_area_4() throws JvmLimitsException {

        Set<Distribution> distributions_LCA = new HashSet<>();
        distributions_LCA.add(newDistribution(book_a, yug_ko, PresenceAbsenceTerm.NATIVE(), "1"));

        addDistributions(
                T_LAPSANA_COMMUNIS_ALPINA_UUID,
                distributions_LCA
            );

        Set<Distribution> distributions_LC = new HashSet<>();
        distributions_LC.add(newDistribution(book_a, yug, PresenceAbsenceTerm.NATIVE(), "2")); //  should succeed

        addDistributions(
                T_LAPSANA_COMMUNIS_UUID,
                distributions_LC
            );

        engine.accumulate(AggregationMode.byAreasAndRanks, superAreas, lowerRank, upperRank, null, null);

        Taxon lapsana_communis = (Taxon) taxonService.load(T_LAPSANA_COMMUNIS_UUID);
        int computedDescriptionsCnt = 0;
        for(TaxonDescription description : lapsana_communis.getDescriptions()) {
            if(description.hasMarker(MarkerType.COMPUTED(), true)) {
                computedDescriptionsCnt++;
                assertEquals(2, description.getElements().size());
                Distribution distribution = (Distribution)description.getElements().iterator().next();
                if(distribution.getArea().equals(yug_ko)){
                    assertEquals(2, distribution.getSources().size());
                    DescriptionElementSource source = distribution.getSources().iterator().next();
                    assertEquals("2", source.getCitationMicroReference());
                }
            }
        }
        assertEquals(1, computedDescriptionsCnt);
    }

    /**
     * @param referenceTitle
     * @param area
     * @param status
     * @param microCitation
     * @return
     */
    private Distribution newDistribution(Reference reference, NamedArea area, PresenceAbsenceTerm status,
            String microCitation) {
        DescriptionElementSource source = DescriptionElementSource.NewPrimarySourceInstance(reference, microCitation);
        Distribution distribution = Distribution.NewInstance(area, status);
        distribution.getSources().add(source);
        return distribution;
    }

    /**
     * creates a new description for the taxon identified by the UUIDs
     * @param taxonUuid
     * @param distributions
     */
    private void addDistributions(UUID taxonUuid, Collection<Distribution> distributions) {
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

    private String sourcesToString(DescriptionElementBase deb) {
        StringBuffer out = new StringBuffer();
        for ( DescriptionElementSource source : deb.getSources()) {
            out.append(source.getCitation().getTitle() + " : " + source.getCitationMicroReference() + ", ");
        }
        return out.toString();
    }


    //@Test //  uncomment to create test data file//
    @Override
    public void createTestDataSet() throws FileNotFoundException {

        // --- References --- //
        Reference sec = ReferenceFactory.newDatabase();
        sec.setTitleCache("Test", true);
        Reference nomRef = ReferenceFactory.newBook();
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
                "HIBERNATE_SEQUENCES"
         });

    }

}
